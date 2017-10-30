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

        OpenLocate.Configuration configuration = new OpenLocate.Configuration.Builder(this, "https://api.safegraph.com/v1/provider/17d375ec-a2ea-11e7-8078-02ae47b9ff6b/devicelocation")
                .setHeaders(headers)
                .withoutDeviceManufacturer()
                .withoutDeviceModel()
                .build();
        OpenLocate.initialize(configuration);
    }
}
