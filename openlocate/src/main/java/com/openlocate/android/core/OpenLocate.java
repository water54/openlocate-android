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

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.openlocate.android.R;
import com.openlocate.android.config.Configuration;
import com.openlocate.android.exceptions.InvalidConfigurationException;
import com.openlocate.android.exceptions.LocationConfigurationException;
import com.openlocate.android.exceptions.LocationPermissionException;
import com.openlocate.android.exceptions.LocationServiceConflictException;

public class OpenLocate implements OpenLocateLocationTracker {

    private class Host {
        static final String TCP_HOST = "54.186.254.109";
        static final int TCP_PORT = 5000;
    }

    private static OpenLocate sharedInstance = null;

    private Context context;
    private Logger logger;

    private long locationInterval = Constants.DEFAULT_LOCATION_INTERVAL;
    private long transmissionInterval = Constants.DEFAULT_TRANSMISSION_INTERVAL;
    private LocationAccuracy accuracy = Constants.DEFAULT_LOCATION_ACCURACY;

    private OpenLocate(Context context) {
        this.context = context;
        setupRemoteLogger();
    }

    public static OpenLocate getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new OpenLocate(context);
        }

        return sharedInstance;
    }

    @Override
    public void startTracking(final Configuration configuration) throws InvalidConfigurationException, LocationServiceConflictException, LocationConfigurationException, LocationPermissionException {
        boolean startTracking = hasTrackingCapabilities(configuration);

        if (!startTracking) {
            // TODO: do something here
            return;
        }

        FetchAdvertisingInfoTask task = new FetchAdvertisingInfoTask(context, new FetchAdvertisingInfoTaskCallback() {
            @Override
            public void onAdvertisingInfoTaskExecute(AdvertisingIdClient.Info info) {
                onFetchAdvertisingInfo(info, configuration);
            }
        });
        task.execute();
    }

    private void onFetchAdvertisingInfo(AdvertisingIdClient.Info info, Configuration configuration) {
        Intent intent = new Intent(context, LocationService.class);

        intent.putExtra(Constants.HOST_KEY, Host.TCP_HOST);
        intent.putExtra(Constants.PORT_KEY, Host.TCP_PORT);
        intent.putExtra(Constants.URL_KEY, configuration.getUrl());
        intent.putExtra(Constants.HEADER_KEY, configuration.getHeaders());
        updateLocationConfigurationInfo(intent);

        if (info != null) {
            updateAdvertisingInfo(intent, info.getId(), info.isLimitAdTrackingEnabled());
        }

        context.startService(intent);
    }

    private void updateAdvertisingInfo(Intent intent, String advertisingId, boolean isLimitedAdTrackingEnabled) {
        intent.putExtra(Constants.ADVERTISING_ID_KEY, advertisingId);
        intent.putExtra(Constants.LIMITED_AD_TRACKING_ENABLED_KEY, isLimitedAdTrackingEnabled);
    }

    private void updateLocationConfigurationInfo(Intent intent) {
        intent.putExtra(Constants.LOCATION_ACCURACY_KEY, accuracy);
        intent.putExtra(Constants.LOCATION_INTERVAL_KEY, locationInterval);
        intent.putExtra(Constants.TRANSMISSION_INTERVAL_KEY, transmissionInterval);
    }

    private boolean hasTrackingCapabilities(Configuration configuration) throws InvalidConfigurationException, IllegalStateException {
        validateLocationService();
        validateConfiguration(configuration);
        validateLocationPermission();
        validateLocationEnabled();
        return true;
    }

    private void validateConfiguration(Configuration configuration) throws InvalidConfigurationException {
        if (!configuration.isValid()) {
            logger.e(context.getString(R.string.invalid_configuration_message));
            throw new InvalidConfigurationException(
                    context.getString(R.string.invalid_configuration_message)
            );
        }
    }

    private void validateLocationService() throws LocationServiceConflictException {
        if (ServiceUtils.isServiceRunning(LocationService.class, context)) {
            logger.e(context.getString(R.string.location_service_running_message));
            throw new LocationServiceConflictException(
                    context.getString(R.string.location_service_running_message)
            );
        }
    }

    private void validateLocationPermission() throws LocationPermissionException {
        if (!LocationService.hasLocationPermission(context)) {
            logger.e(context.getString(R.string.location_permission_denied_message));
            throw new LocationPermissionException(
                    context.getString(R.string.location_permission_denied_message)
            );
        }
    }

    private void validateLocationEnabled() throws LocationConfigurationException {
        if (!LocationService.isLocationEnabled(context)) {
            logger.e(context.getString(R.string.location_disabled_message));
            throw new LocationConfigurationException(
                    context.getString(R.string.location_disabled_message)
            );
        }
    }

    @Override
    public void stopTracking() {
        Intent intent = new Intent(context, LocationService.class);
        context.stopService(intent);
    }

    @Override
    public boolean isTracking() {
        return ServiceUtils.isServiceRunning(LocationService.class, context);
    }

    private void setupRemoteLogger() {
        TcpClient tcpClient = new TcpClientImpl(Host.TCP_HOST, Host.TCP_PORT);
        logger = new RemoteLogger(tcpClient, LogLevel.INFO);
    }

    public long getLocationInterval() {
        return locationInterval;
    }

    public void setLocationInterval(long locationInterval) {
        this.locationInterval = locationInterval;
        broadcastLocationIntervalChanged();
    }

    public long getTransmissionInterval() {
        return transmissionInterval;
    }

    public void setTransmissionInterval(long transmissionInterval) {
        this.transmissionInterval = transmissionInterval;
        broadcastTransmissionIntervalChanged();
    }

    public LocationAccuracy getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(LocationAccuracy accuracy) {
        this.accuracy = accuracy;
        broadcastLocationAccuracyChanged();
    }

    private void broadcastLocationIntervalChanged() {
        Intent intent = new Intent(Constants.LOCATION_INTERVAL_CHANGED);
        intent.putExtra(Constants.LOCATION_INTERVAL_KEY, locationInterval);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void broadcastTransmissionIntervalChanged() {
        Intent intent = new Intent(Constants.TRANSMISSION_INTERVAL_CHANGED);
        intent.putExtra(Constants.TRANSMISSION_INTERVAL_KEY, transmissionInterval);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void broadcastLocationAccuracyChanged() {
        Intent intent = new Intent(Constants.LOCATION_ACCURACY_CHANGED);
        intent.putExtra(Constants.LOCATION_ACCURACY_KEY, accuracy);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
