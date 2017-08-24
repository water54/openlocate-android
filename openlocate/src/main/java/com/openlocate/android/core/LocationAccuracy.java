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

import com.google.android.gms.location.LocationRequest;

public enum LocationAccuracy {
    LOW,
    MEDIUM,
    HIGH;

    @Override
    public String toString() {
        String stringValue;

        switch (this) {
            case LOW:
                stringValue = "low";
                break;
            case MEDIUM:
                stringValue = "medium";
                break;
            default:
                stringValue = "high";
                break;
        }

        return stringValue;
    }

    int getLocationRequestAccuracy() {
        int locationAccuracy;

        switch (this) {
            case LOW:
                locationAccuracy = LocationRequest.PRIORITY_LOW_POWER;
                break;
            case MEDIUM:
                locationAccuracy = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                break;
            default:
                locationAccuracy = LocationRequest.PRIORITY_HIGH_ACCURACY;
        }

        return locationAccuracy;
    }
}
