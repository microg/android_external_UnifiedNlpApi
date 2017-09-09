package org.microg.nlp.api;

import java.util.Map;
import org.microg.nlp.api.LocationCallback;
import android.content.Intent;
import android.location.Location;

interface LocationBackend {
    void open(LocationCallback callback);
    Location update(in List<String> options);
    void close();
    Intent getInitIntent();
    Intent getSettingsIntent();
    Intent getAboutIntent();
}
