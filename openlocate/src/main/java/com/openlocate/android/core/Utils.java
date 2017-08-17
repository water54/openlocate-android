package com.openlocate.android.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

final class Utils {

    static HashMap<String, String> hashMapFromString(String mapString) {
        if (mapString == null || mapString.isEmpty()) {
            return null;
        }

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(
                    mapString.substring(1, mapString.length() - 1).replace(", ", "\n"))
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry: properties.entrySet()) {
            map.put((String)entry.getKey(), (String)entry.getValue());
        }

        return map;
    }
}
