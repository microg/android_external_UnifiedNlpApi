/*
 * Copyright 2014-2015 µg Project Team
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

import android.location.Location;
import android.os.Bundle;

import java.util.Collection;

public final class LocationHelper {
    private LocationHelper() {
    }

    public static Location create(String source) {
        return new Location(source);
    }

    public static Location create(String source, double latitude, double longitude,
            float accuracy) {
        Location location = create(source);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        return location;
    }

    public static Location create(String source, double latitude, double longitude, float altitude,
            Bundle extras) {
        Location location = create(source, latitude, longitude, altitude);
        location.setExtras(extras);
        return location;
    }

    public static Location create(String source, double latitude, double longitude, double altitude,
            float accuracy) {
        Location location = create(source, latitude, longitude, accuracy);
        location.setAltitude(altitude);
        return location;
    }

    public static Location create(String source, double latitude, double longitude, double altitude,
            float accuracy, Bundle extras) {
        Location location = create(source, latitude, longitude, altitude, accuracy);
        location.setExtras(extras);
        return location;
    }

    public static Location create(String source, long time) {
        Location location = create(source);
        location.setTime(time);
        return location;
    }

    public static Location create(String source, long time, Bundle extras) {
        Location location = create(source, time);
        location.setExtras(extras);
        return location;
    }

    public static Location average(String source, Collection<Location> locations) {
        if (locations == null || locations.size() == 0) {
            return null;
        }
        int num = locations.size();
        double latitude = 0;
        double longitude = 0;
        float accuracy = 0;
        int altitudes = 0;
        double altitude = 0;
        for (Location value : locations) {
            if (value != null) {
                latitude += value.getLatitude();
                longitude += value.getLongitude();
                accuracy += value.getAccuracy();
                if (value.hasAltitude()) {
                    altitude += value.getAltitude();
                    altitudes++;
                }
            }
        }
        Bundle extras = new Bundle();
        extras.putInt("AVERAGED_OF", num);
        if (altitudes > 0) {
            return create(source, latitude / num, longitude / num, altitude / altitudes,
                    accuracy / num, extras);
        } else {
            return create(source, latitude / num, longitude / num, accuracy / num, extras);
        }
    }
}
