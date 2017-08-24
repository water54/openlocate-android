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

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;

public class LocationService extends Service {

    private final static int FOREGROUND_SERVICE_TAG = 1001;

    private final static String TAG = LocationService.class.getSimpleName();
    private final static String LOCATION_DISPATCH_TAG = LocationService.class.getCanonicalName() + ".location_dispatch_task";
    private final static String LOG_DISPATCH_TAG = LocationService.class.getCanonicalName() + ".log_dispatch_task";

    private GoogleApiClient googleApiClient;
    private GcmNetworkManager networkManager;

    private long locationRequestIntervalInSecs;
    private long transmissionIntervalInSecs;
    private LocationAccuracy accuracy;

    private LocationDataSource locations;
    private LocationListener locationListener;
    private Logger logger;

    private String url;
    private HashMap<String, String> headers;

    private AdvertisingInfo advertisingInfo;

    private String tcpHost;
    private int tcpPort;

    private BroadcastReceiver locationIntervalChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setLocationRequestIntervalInSecs(intent);
            resetLocationRequest();
        }
    };

    private BroadcastReceiver transmissionIntervalChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTransmissionIntervalInSecs(intent);
            resetTransmissionInterval();
        }
    };

    private BroadcastReceiver locationAccuracyChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setLocationAccuracy(intent);
            resetLocationRequest();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SQLiteOpenHelper helper = new DatabaseHelper(this);
        locations = new LocationDatabase(helper);

        logger = new DatabaseLogger(new LoggerDatabase(helper), LogLevel.INFO);
        networkManager = GcmNetworkManager.getInstance(this);

        registerForLocalBroadcastEvents();
    }

    @Override
    public void onDestroy() {
        unschedulePeriodicTasks();
        stopLocationUpdates();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setValues(intent);
        connectGoogleClient();

        /* Starting the service as foreground service. If the service is not foreground,
        service will be killed when the app is killed.
         */
        startForeground();

        return START_STICKY;
    }

    @SuppressWarnings("unchecked")
    private void setValues(Intent intent) {
        url = intent.getStringExtra(Constants.URL_KEY);
        headers = (HashMap<String, String>) intent.getSerializableExtra(Constants.HEADER_KEY);

        tcpHost = intent.getStringExtra(Constants.HOST_KEY);
        tcpPort = intent.getIntExtra(Constants.PORT_KEY, Constants.DEFAULT_PORT);

        advertisingInfo = new AdvertisingInfo(
                intent.getStringExtra(Constants.ADVERTISING_ID_KEY),
                intent.getBooleanExtra(Constants.LIMITED_AD_TRACKING_ENABLED_KEY, false)
        );

        setLocationRequestIntervalInSecs(intent);
        setTransmissionIntervalInSecs(intent);
        setLocationAccuracy(intent);
    }

    private void setLocationRequestIntervalInSecs(Intent intent) {
        locationRequestIntervalInSecs = intent.getLongExtra(Constants.LOCATION_INTERVAL_KEY, Constants.DEFAULT_LOCATION_INTERVAL);
    }

    private void setTransmissionIntervalInSecs(Intent intent) {
        transmissionIntervalInSecs = intent.getLongExtra(Constants.TRANSMISSION_INTERVAL_KEY, Constants.DEFAULT_TRANSMISSION_INTERVAL);
    }

    private void setLocationAccuracy(Intent intent) {
        accuracy = (LocationAccuracy) intent.getSerializableExtra(Constants.LOCATION_ACCURACY_KEY);
    }

    private void connectGoogleClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new ConnectionCallbacks())
                    .addOnConnectionFailedListener(new ConnectionFailedListener())
                    .addApi(LocationServices.API)
                    .build();
        }

        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
            return;
        }

        startLocationUpdates();
    }

    private LocationRequest getLocationRequest() {
        LocationRequest request = new LocationRequest();

        request.setPriority(accuracy.getLocationRequestAccuracy());
        request.setInterval(locationRequestIntervalInSecs * 1000);
        request.setFastestInterval(locationRequestIntervalInSecs * 1000);

        return request;
    }

    private void startLocationUpdates() {
        try {
            LocationRequest request = getLocationRequest();
            locationListener = new LocationListener();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, locationListener);
            schedulePeriodicTasks();
        } catch (SecurityException e) {
            locationListener = null;
            Log.e(TAG, e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
        locationListener = null;
    }

    private void schedulePeriodicTasks() {
        scheduleDispatchLocationService();
        scheduleDispatchLogsService();
    }

    private void scheduleDispatchLocationService() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.URL_KEY, url);
        bundle.putString(Constants.HEADER_KEY, headers.toString());

        PeriodicTask task = new PeriodicTask.Builder()
                .setExtras(bundle)
                .setService(DispatchLocationService.class)
                .setPeriod(transmissionIntervalInSecs)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setTag(LOCATION_DISPATCH_TAG)
                .build();

        networkManager.schedule(task);
    }

    private void scheduleDispatchLogsService() {
        if (tcpHost == null || tcpHost.isEmpty() || tcpPort == Constants.DEFAULT_PORT) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(Constants.HOST_KEY, tcpHost);
        bundle.putInt(Constants.PORT_KEY, tcpPort);

        PeriodicTask task = new PeriodicTask.Builder()
                .setExtras(bundle)
                .setService(DispatchLogService.class)
                .setPeriod(transmissionIntervalInSecs)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setTag(LOG_DISPATCH_TAG)
                .build();

        networkManager.schedule(task);
    }

    private void unschedulePeriodicTasks() {
        networkManager.cancelAllTasks(DispatchLocationService.class);
        networkManager.cancelAllTasks(DispatchLogService.class);
    }

    // Google Api Client Connection Callback
    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            logger.e("Google Api Client Connection Suspended : " + i);
        }
    }

    // Connection Failed Listener Class
    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            logger.e("Google API Client failed to connect. Result = " + connectionResult.getErrorMessage());
        }
    }

    // OpenLocateLocation Listener class
    private class LocationListener implements com.google.android.gms.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            logger.v(location.toString());
            locations.add(new OpenLocateLocation(location, advertisingInfo));
            logger.v("COUNT - " + locations.size());
        }
    }

    static boolean isLocationEnabled(Context context) throws IllegalStateException {
        try {
            int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } catch (Settings.SettingNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    static boolean hasLocationPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void startForeground() {
        Notification notification = new Notification.Builder(this)
                .build();
        startForeground(FOREGROUND_SERVICE_TAG, notification);
    }

    private void resetLocationRequest() {
        stopLocationUpdates();
        startLocationUpdates();
    }

    private void resetTransmissionInterval() {
        unschedulePeriodicTasks();
        schedulePeriodicTasks();
    }

    private void registerForLocalBroadcastEvents() {
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(
                locationIntervalChangedReceiver,
                new IntentFilter(Constants.LOCATION_INTERVAL_CHANGED));

        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(
                transmissionIntervalChangedReceiver,
                new IntentFilter(Constants.TRANSMISSION_INTERVAL_CHANGED));

        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(
                locationAccuracyChangedReceiver,
                new IntentFilter(Constants.LOCATION_ACCURACY_CHANGED));
    }
}
