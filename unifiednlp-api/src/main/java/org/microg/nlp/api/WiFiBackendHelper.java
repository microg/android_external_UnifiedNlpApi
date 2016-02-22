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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

/**
 * Utility class to support backends that use Wi-Fis for geolocation.
 */
@SuppressWarnings("MissingPermission")
public class WiFiBackendHelper extends AbstractBackendHelper {
    private final static IntentFilter wifiBroadcastFilter =
            new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    private final Listener listener;
    private final WifiManager wifiManager;
    private final Set<WiFi> wiFis = new HashSet<WiFi>();
    private final BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onWiFisChanged();
        }
    };

    private boolean ignoreNomap = true;

    /**
     * Create a new instance of {@link WiFiBackendHelper}. Call this in
     * {@link LocationBackendService#onCreate()}.
     *
     * @throws IllegalArgumentException if either context or listener is null.
     */
    public WiFiBackendHelper(Context context, Listener listener) {
        super(context);
        if (listener == null)
            throw new IllegalArgumentException("listener must not be null");
        this.listener = listener;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Sets whether to ignore the "_nomap" flag on Wi-Fi SSIDs or not.
     * <p/>
     * Usually, Wi-Fis whose SSID end with "_nomap" are ignored for geolocation. This behaviour can
     * be suppressed by {@code setIgnoreNomap(false)}.
     * <p/>
     * Default is {@code true}.
     */
    public void setIgnoreNomap(boolean ignoreNomap) {
        this.ignoreNomap = ignoreNomap;
    }

    /**
     * Call this in {@link LocationBackendService#onOpen()}.
     */
    public synchronized void onOpen() {
        super.onOpen();
        context.registerReceiver(wifiBroadcastReceiver, wifiBroadcastFilter);
    }

    /**
     * Call this in {@link LocationBackendService#onClose()}.
     */
    public synchronized void onClose() {
        super.onClose();
        context.unregisterReceiver(wifiBroadcastReceiver);
    }

    /**
     * Call this in {@link LocationBackendService#update()}.
     */
    public synchronized void onUpdate() {
        if (!currentDataUsed) {
            listener.onWiFisChanged(getWiFis());
        } else {
            scanWiFis();
        }
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{CHANGE_WIFI_STATE, ACCESS_WIFI_STATE, ACCESS_COARSE_LOCATION};
    }

    private void onWiFisChanged() {
        if (loadWiFis()) {
            listener.onWiFisChanged(getWiFis());
        }
    }

    private synchronized boolean scanWiFis() {
        if (state == State.DISABLED)
            return false;
        if (wifiManager.isWifiEnabled() || isScanAlwaysAvailable()) {
            state = State.SCANNING;
            wifiManager.startScan();
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean isScanAlwaysAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && wifiManager.isScanAlwaysAvailable();
    }

    private synchronized boolean loadWiFis() {
        wiFis.clear();
        currentDataUsed = false;
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            if (ignoreNomap && scanResult.SSID.toLowerCase(Locale.US).endsWith("_nomap")) continue;
            wiFis.add(new WiFi(scanResult.BSSID, scanResult.level, frequencyToChannel(scanResult.frequency), scanResult.frequency));
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

    @SuppressWarnings("MagicNumber")
    private static int frequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }

    /**
     * @return the latest scan result.
     */
    public synchronized Set<WiFi> getWiFis() {
        currentDataUsed = true;
        return new HashSet<WiFi>(wiFis);
    }

    /**
     * Interface to listen for Wi-Fi scan results.
     */
    public interface Listener {
        /**
         * Called when a new set of Wi-Fi's is discovered.
         */
        public void onWiFisChanged(Set<WiFi> wiFis);
    }

    /**
     * Represents a generic Wi-Fi scan result.
     * <p/>
     * This does contain the BSSID (mac address) and the RSSI (in dBm) of a Wi-Fi.
     * Additional data is not provided, but also not usable for geolocation.
     */
    public static class WiFi {
        private final String bssid;
        private final int rssi;
        private final int channel;
        private final int frequency;

        public String getBssid() {
            return bssid;
        }

        public int getRssi() {
            return rssi;
        }

        public int getChannel() {
            return channel;
        }

        public int getFrequency() {
            return frequency;
        }

        public WiFi(String bssid, int rssi) {
            this(bssid, rssi, -1, -1);
        }

        public WiFi(String bssid, int rssi, Integer channel, Integer frequency) {
            this.bssid = wellFormedMac(bssid);
            this.rssi = rssi;
            this.channel = channel;
            this.frequency = frequency;
        }
    }

    /**
     * Bring a mac address to the form 01:23:45:AB:CD:EF
     *
     * @param mac address to be well-formed
     * @return well-formed mac address
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
