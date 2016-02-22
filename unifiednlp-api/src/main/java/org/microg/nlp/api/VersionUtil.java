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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class VersionUtil {

    public static String getPackageApiVersion(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return applicationInfo.metaData == null ? null
                : applicationInfo.metaData.getString(Constants.METADATA_API_VERSION);
    }

    public static String getServiceApiVersion(Context context) {
        String apiVersion = getPackageApiVersion(context, "com.google.android.gms");
        return apiVersion != null ? apiVersion
                : getPackageApiVersion(context, "com.google.android.location");
    }

    public static String getSelfApiVersion(Context context) {
        String apiVersion = getPackageApiVersion(context, context.getPackageName());
        if (!Constants.API_VERSION.equals(apiVersion)) {
            Log.w("VersionUtil", "You did not specify the currently used api version in your manifest.\n" +
                    "When using gradle + aar, this should be done automatically, if not, add the\n" +
                    "following to your <application> tag\n" +
                    "<meta-data android:name=\"" + Constants.METADATA_API_VERSION +
                    "\" android:value=\"" + Constants.API_VERSION + "\" />");
            apiVersion = Constants.API_VERSION;
        }
        return apiVersion;
    }
}
