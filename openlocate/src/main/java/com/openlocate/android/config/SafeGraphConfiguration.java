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
package com.openlocate.android.config;

import java.util.HashMap;
import java.util.UUID;

public final class SafeGraphConfiguration extends Configuration {

    private static final String BASE_URL = "https://api.safegraph.com/v1";
    private static final String HEADER_KEY = "Authorization";

    private UUID uuid;
    private String token;

    public SafeGraphConfiguration(UUID uuid, String token) {
        this.uuid = uuid;
        this.token = token;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String getUrl() {
        if (uuid == null) {
            return null;
        }

        return BASE_URL + "/provider/" + uuid.toString().toLowerCase() + "/devicelocation";
    }

    @Override
    public HashMap<String, String> getHeaders() {
        if (token == null) {
            return null;
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put(HEADER_KEY, "Bearer " + token);

        return headers;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && getHeaders() != null;
    }
}
