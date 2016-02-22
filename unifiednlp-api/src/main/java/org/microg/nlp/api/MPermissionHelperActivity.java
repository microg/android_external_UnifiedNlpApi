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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

@TargetApi(Build.VERSION_CODES.M)
public class MPermissionHelperActivity extends Activity {
    public static final String EXTRA_PERMISSIONS = "org.microg.nlp.api.mperms";
    private static final int REQUEST_CODE_PERMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] mperms = getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
        if (mperms == null || mperms.length == 0) {
            setResult(RESULT_OK);
            finish();
        } else {
            requestPermissions(mperms, REQUEST_CODE_PERMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean ok = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) ok = false;
        }
        setResult(ok ? RESULT_OK : RESULT_CANCELED);
        finish();
    }
}
