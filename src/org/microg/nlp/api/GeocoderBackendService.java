/*
 * Copyright 2013-2015 µg Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microg.nlp.api;

import android.location.Address;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

public abstract class GeocoderBackendService extends AbstractBackendService {

    private final Backend backend = new Backend();
    private boolean connected = false;

    @Override
    protected IBinder getBackend() {
        return backend;
    }

    @Override
    public void disconnect() {
        if (connected) {
            onClose();
            connected = false;
        }
    }

    /**
     * @param locale The locale, formatted as a String with underscore (eg. en_US) the resulting
     *               address should be localized in
     * @see android.location.Geocoder#getFromLocation(double, double, int)
     */
    protected abstract List<Address> getFromLocation(double latitude, double longitude,
            int maxResults, String locale);

    /**
     * @param locale The locale, formatted as a String with underscore (eg. en_US) the resulting
     *               address should be localized in
     * @see android.location.Geocoder#getFromLocationName(String, int, double, double, double, double)
     */
    protected abstract List<Address> getFromLocationName(String locationName, int maxResults,
            double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude,
            double upperRightLongitude, String locale);

    private class Backend extends GeocoderBackend.Stub {

        @Override
        public void open() throws RemoteException {
            onOpen();
            connected = true;
        }

        @Override
        public List<Address> getFromLocation(double latitude, double longitude, int maxResults,
                String locale) throws RemoteException {
            return GeocoderBackendService.this
                    .getFromLocation(latitude, longitude, maxResults, locale);
        }

        @Override
        public List<Address> getFromLocationName(String locationName, int maxResults,
                double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude,
                double upperRightLongitude, String locale) throws RemoteException {
            return GeocoderBackendService.this
                    .getFromLocationName(locationName, maxResults, lowerLeftLatitude,
                            lowerLeftLongitude, upperRightLatitude, upperRightLongitude, locale);
        }

        @Override
        public void close() throws RemoteException {
            disconnect();
        }
    }
}
