package com.openlocate.android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openlocate.android.config.Configuration;
import com.openlocate.android.exceptions.GooglePlayServicesNotAvailable;
import com.openlocate.android.exceptions.LocationDisabledException;
import com.openlocate.android.exceptions.LocationPermissionException;

import java.util.HashMap;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!SharedPreferenceUtils.getInstance(context).getBoolanValue(Constants.SERVICE_STATUS, false)) {
            return;
        }

        Log.d("BootCompleteReceiver", "Boot Receiver");
        String url = SharedPreferenceUtils.getInstance(context).getStringValue(Constants.URL_KEY, "");
        HashMap<String, String> headers = SharedPreferenceUtils.getInstance(context).loadMap(Constants.HEADER_KEY);

        Configuration configuration = new Configuration.Builder()
                .setUrl(url)
                .setHeaders(headers)
                .build();

        try {
            OpenLocate.getInstance(context).startTracking(configuration);
        } catch (LocationDisabledException e) {
            e.printStackTrace();
        } catch (LocationPermissionException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailable googlePlayServicesNotAvailable) {
            googlePlayServicesNotAvailable.printStackTrace();
        }
    }
}
