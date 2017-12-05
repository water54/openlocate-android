package com.openlocate.example;

import android.app.Application;
import com.openlocate.android.core.OpenLocate;

import java.util.HashMap;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + BuildConfig.TOKEN);

        OpenLocate.Configuration configuration = new OpenLocate.Configuration.Builder(this, BuildConfig.URL)
                .setHeaders(headers)
                .withoutDeviceManufacturer()
                .withoutDeviceModel()
                .build();

        OpenLocate.initialize(configuration);
    }
}
