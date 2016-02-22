/*
 * Copyright 2013-2016 microG Project Team
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

/**
 * Use {@link Constants} instead.
 */
@Deprecated
public class NlpApiConstants {
    public static final String ACTION_LOCATION_BACKEND = "org.microg.nlp.LOCATION_BACKEND";
    public static final String ACTION_GEOCODER_BACKEND = "org.microg.nlp.GEOCODER_BACKEND";
    public static final String ACTION_RELOAD_SETTINGS = "org.microg.nlp.RELOAD_SETTINGS";
    public static final String ACTION_FORCE_LOCATION = "org.microg.nlp.FORCE_LOCATION";
    public static final String PERMISSION_FORCE_LOCATION = "org.microg.permission.FORCE_COARSE_LOCATION";
    public static final String INTENT_EXTRA_LOCATION = "location";
    public static final String LOCATION_EXTRA_BACKEND_PROVIDER = "SERVICE_BACKEND_PROVIDER";
    public static final String LOCATION_EXTRA_BACKEND_COMPONENT = "SERVICE_BACKEND_COMPONENT";
    public static final String LOCATION_EXTRA_OTHER_BACKENDS = "OTHER_BACKEND_RESULTS";
    public static final String METADATA_BACKEND_SETTINGS_ACTIVITY = "org.microg.nlp.BACKEND_SETTINGS_ACTIVITY";
    public static final String METADATA_BACKEND_ABOUT_ACTIVITY = "org.microg.nlp.BACKEND_ABOUT_ACTIVITY";
    public static final String METADATA_BACKEND_SUMMARY = "org.microg.nlp.BACKEND_SUMMARY";
}
