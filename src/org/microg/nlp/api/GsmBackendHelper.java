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

import android.content.Context;

import java.util.Set;

/**
 * Utility class to support backends that use GSM for geolocation.
 * <p/>
 * This class is incomplete. Do not use in production grade code.
 */
public class GsmBackendHelper extends AbstractBackendHelper {
    private final Listener listener;

    /**
     * Create a new instance of {@link GsmBackendHelper}. Call this in
     * {@link LocationBackendService#onCreate()}.
     *
     * @throws IllegalArgumentException if either context or listener is null.
     */
    public GsmBackendHelper(Context context, Listener listener) {
        super(context);
        if (listener == null)
            throw new IllegalArgumentException("listener must not be null");
        this.listener = listener;
    }

    /**
     * Call this in {@link org.microg.nlp.api.LocationBackendService#onOpen()}.
     */
    @Override
    public synchronized void onOpen() {
        super.onOpen();
    }

    /**
     * Call this in {@link org.microg.nlp.api.LocationBackendService#onClose()}.
     */
    @Override
    public synchronized void onClose() {
        super.onClose();
    }

    public interface Listener {
        public void onGsmChanged(Set<GsmCell> gsmCells);
    }

    public static class GsmCell {

    }
}
