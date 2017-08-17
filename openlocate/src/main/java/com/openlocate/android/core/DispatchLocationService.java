/*
 * Copyright (c) 2017 OpenLocate
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.openlocate.android.core;

import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class DispatchLocationService extends GcmTaskService {

    private static final String TAG = DispatchLocationService.class.getSimpleName();
    private static final String LOCATIONS_KEY = "locations";

    private Logger logger;

    @Override
    @SuppressWarnings("unchecked")
    public int onRunTask(TaskParams taskParams) {
        SQLiteOpenHelper helper = new DatabaseHelper(this);
        LocationDataSource dataSource = new LocationDatabase(helper);

        LoggerDataSource loggerDataSource = new LoggerDatabase(helper);
        logger = new DatabaseLogger(loggerDataSource, LogLevel.INFO);

        String url = taskParams.getExtras().getString(Constants.URL_KEY);
        HashMap<String, String> headers = Utils.hashMapFromString(taskParams.getExtras().getString(Constants.HEADER_KEY));

        HttpClient httpClient = new HttpClientImpl();

        postLocations(httpClient, url, headers, dataSource);

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private void postLocations(HttpClient httpClient, String url, HashMap<String, String> headers, final LocationDataSource dataSource) {
        final List<OpenLocateLocation> locations = dataSource.popAll();

        if (locations == null || locations.isEmpty()) {
            return;
        }

        httpClient.post(
                url,
                getLocationsParam(locations).toString(),
                headers,
                new HttpClientCallback() {
                    @Override
                    public void onCompletion(HttpRequest request, HttpResponse response) {
                        Log.i(TAG, "Successfully posted locations");
                    }
                }, new HttpClientCallback() {
                    @Override
                    public void onCompletion(HttpRequest request, HttpResponse response) {
                        dataSource.addAll(locations);
                        logger.e("Fail to post location");
                    }
                }
        );
    }

    private JSONObject getLocationsParam(List<OpenLocateLocation> locationsToPost) {
        JSONObject jsonObject = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (OpenLocateLocation location : locationsToPost) {
            jsonArray.put(location.getJson());
        }

        try {
            jsonObject.put(LOCATIONS_KEY, jsonArray);
        } catch (JSONException e) {
            logger.e("JSON exception while posting locations " + e.getMessage());
        }

        return jsonObject;
    }
}
