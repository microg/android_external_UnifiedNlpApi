package org.microg.nlp.api;

import android.content.Intent;
import android.location.Location;
import android.location.Address;

interface GeocoderBackend {
    void open();
    List<Address> getFromLocation(double latitude, double longitude, int maxResults, String locale, in List<String> options);
    List<Address> getFromLocationName(String locationName, int maxResults, double lowerLeftLatitude, 
        double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, 
        String locale, in List<String> options);
    void close();
    Intent getInitIntent();
    Intent getSettingsIntent();
    Intent getAboutIntent();
}
