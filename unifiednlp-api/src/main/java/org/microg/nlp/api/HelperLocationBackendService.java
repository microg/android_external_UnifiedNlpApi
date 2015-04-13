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

import android.location.Location;

import java.util.HashSet;
import java.util.Set;

public abstract class HelperLocationBackendService extends LocationBackendService {

    private boolean opened;
    private Set<AbstractBackendHelper> helpers = new HashSet<AbstractBackendHelper>();

    public synchronized void addHelper(AbstractBackendHelper helper) {
        helpers.add(helper);
        if (opened) {
            helper.onOpen();
        }
    }

    public synchronized void removeHelpers() {
        if (opened) {
            for (AbstractBackendHelper helper : helpers) {
                helper.onClose();
            }
        }
        helpers.clear();
    }

    @Override
    protected synchronized void onOpen() {
        for (AbstractBackendHelper helper : helpers) {
            helper.onOpen();
        }
        opened = true;
    }

    @Override
    protected synchronized void onClose() {
        for (AbstractBackendHelper helper : helpers) {
            helper.onClose();
        }
        opened = false;
    }

    @Override
    protected synchronized Location update() {
        for (AbstractBackendHelper helper : helpers) {
            helper.onUpdate();
        }
        return null;
    }
}
