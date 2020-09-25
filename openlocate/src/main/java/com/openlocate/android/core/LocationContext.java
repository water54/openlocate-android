package com.openlocate.android.core;

import android.app.ActivityManager;

import java.util.HashMap;
import java.util.Map;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public enum LocationContext {

    FGROUND("fground"),
    BGROUND("bground"),
    UNKNOWN("unknown");

    private final String value;

    private static final Map<String, LocationContext> lookup  = new HashMap<>();

    static {
        for (LocationContext context: LocationContext.values()) {
            lookup.put(context.getValue(), context);
        }
    }

    LocationContext(final String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    public static LocationContext get(String value) {
        return lookup.get(value);
    }

    static boolean isForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    static LocationContext getLocationContext() {
        return isForeground() ? LocationContext.FGROUND : LocationContext.BGROUND;
    }
}
