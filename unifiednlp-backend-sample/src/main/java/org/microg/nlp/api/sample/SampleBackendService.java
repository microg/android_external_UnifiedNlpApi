package org.microg.nlp.api.sample;

import android.location.Location;
import android.util.Log;
import java.util.List;
import org.microg.nlp.api.LocationBackendService;
import org.microg.nlp.api.LocationHelper;

public class SampleBackendService extends LocationBackendService {
	private static final String TAG = SampleBackendService.class.getName();

	@Override
	protected Location update(List<String> options) {
		if (System.currentTimeMillis() % 60000 > 2000) {
			Log.d(TAG, "I decided not to answer now...");
			return null;
		}
		Location location = LocationHelper.create("sample", 42, 42, 42);
		Log.d(TAG, "I was asked for location and I answer: " + location);
		return location;
	}
}
