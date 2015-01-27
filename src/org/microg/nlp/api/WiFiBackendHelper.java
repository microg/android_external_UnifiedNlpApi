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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to support backends that use WiFis for location.
 */
public class WiFiBackendHelper {
    private final Context context;
    private final WifiManager wifiManager;
    private final static IntentFilter wifiBroadcastFilter = new IntentFilter(WifiManager
            .SCAN_RESULTS_AVAILABLE_ACTION);
    private final BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onWiFisChanged();
        }
    };

    private final Set<WiFi> wiFis = new HashSet<>();
    private final Listener listener;
    private State state = State.DISABLED;
    private boolean ignoreNomap = true;
    private boolean currentWiFisUsed = true;

    public WiFiBackendHelper(Context context, Listener listener) {
        if (context == null || listener == null)
            throw new IllegalArgumentException("context and listener must not be null");
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.listener = listener;
    }

    public void setIgnoreNomap(boolean ignoreNomap) {
        this.ignoreNomap = ignoreNomap;
    }

    public void onOpen() {
        if (state == State.WAITING || state == State.SCANNING)
            throw new IllegalStateException("Do not call onOpen if not closed before");
        currentWiFisUsed = true;
        context.registerReceiver(wifiBroadcastReceiver, wifiBroadcastFilter);
        state = State.WAITING;
    }

    public void onClose() {
        if (state == State.DISABLED || state == State.DISABLING)
            throw new IllegalStateException("Do not call onClose if not opened before");
        if (state == State.WAITING) {
            state = State.DISABLED;
        } else {
            state = State.DISABLING;
        }
        context.unregisterReceiver(wifiBroadcastReceiver);
    }

    public void onUpdate() {
        if (!currentWiFisUsed) {
            currentWiFisUsed = true;
            listener.onWiFisChanged(wiFis);
        } else {
            scanWiFis();
        }
    }

    private void onWiFisChanged() {
        if (loadWiFis()) {
            currentWiFisUsed = true;
            listener.onWiFisChanged(wiFis);
        }
    }

    public synchronized boolean scanWiFis() {
        if (state == State.DISABLED)
            throw new IllegalStateException("can't scan on disabled WiFiBackendHelper");
        if (wifiManager.isWifiEnabled() || isScanAlawaysAvailable()) {
            state = State.SCANNING;
            wifiManager.startScan();
            return true;
        }
        return false;
    }

    private boolean isScanAlawaysAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && wifiManager.isScanAlwaysAvailable();
    }

    public synchronized boolean loadWiFis() {
        wiFis.clear();
        currentWiFisUsed = false;
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            if (ignoreNomap && scanResult.SSID.toLowerCase().endsWith("_nomap")) continue;
            wiFis.add(new WiFi(scanResult.BSSID, scanResult.level));
        }
        if (state == State.DISABLING)
            state = State.DISABLED;
        switch (state) {
            default:
            case DISABLED:
                return false;
            case SCANNING:
                state = State.WAITING;
                return true;
        }
    }

    public synchronized Set<WiFi> getWiFis() {
        currentWiFisUsed = true;
        return new HashSet<>(wiFis);
    }

    private enum State {DISABLED, WAITING, SCANNING, DISABLING}

    public interface Listener {
        public void onWiFisChanged(Set<WiFi> wiFis);
    }

    public static class WiFi {
        private final String bssid;
        private final int rssi;

        public String getBssid() {
            return bssid;
        }

        public int getRssi() {
            return rssi;
        }

        public WiFi(String bssid, int rssi) {
            this.bssid = wellFormedMac(bssid);
            this.rssi = rssi;
        }
    }

    /**
     * Bring a mac address to the form FF:FF:FF:FF:FF:FF
     *
     * @param mac mac to be cleaned
     * @return cleaned up mac
     */
    public static String wellFormedMac(String mac) {
        int HEX_RADIX = 16;
        int[] bytes = new int[6];
        String[] splitAtColon = mac.split(":");
        if (splitAtColon.length == 6) {
            for (int i = 0; i < 6; ++i) {
                bytes[i] = Integer.parseInt(splitAtColon[i], HEX_RADIX);
            }
        } else {
            String[] splitAtLine = mac.split("-");
            if (splitAtLine.length == 6) {
                for (int i = 0; i < 6; ++i) {
                    bytes[i] = Integer.parseInt(splitAtLine[i], HEX_RADIX);
                }
            } else if (mac.length() == 12) {
                for (int i = 0; i < 6; ++i) {
                    bytes[i] = Integer.parseInt(mac.substring(i * 2, (i + 1) * 2), HEX_RADIX);
                }
            } else if (mac.length() == 17) {
                for (int i = 0; i < 6; ++i) {
                    bytes[i] = Integer.parseInt(mac.substring(i * 3, (i * 3) + 2), HEX_RADIX);
                }
            } else {
                throw new IllegalArgumentException("Can't read this string as mac address");

            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            String hex = Integer.toHexString(bytes[i]);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            if (sb.length() != 0)
                sb.append(":");
            sb.append(hex);
        }
        return sb.toString();
    }
}
