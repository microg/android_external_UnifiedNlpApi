package org.microg.nlp.api;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashSet;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;

/**
 * Utility class to support backend using Bluetooth for geolocation.
 */
@SuppressWarnings("MissingPermission")
public class BluetoothBackendHelper extends AbstractBackendHelper {
    private final static IntentFilter bluetoothBroadcastFilter =
            new IntentFilter();

    private final Listener listener;
    private final BluetoothAdapter bluetoothAdapter;
    private final Set<Bluetooth> bluetooths = new HashSet<Bluetooth>();
    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                bluetooths.clear();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                onBluetoothChanged();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Bluetooth bluetoothDiscovered = new Bluetooth(device.getAddress(), device.getName(), rssi);
                bluetooths.add(bluetoothDiscovered);
            }
        }
    };

    public BluetoothBackendHelper(Context context, Listener listener){
        super(context);
        if (listener == null)
            throw new IllegalArgumentException("listener must not be null");
        this.listener = listener;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothBroadcastFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothBroadcastFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothBroadcastFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    public synchronized void onOpen() {
        super.onOpen();
        bluetoothBroadcastFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothBroadcastFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothBroadcastFilter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(bluetoothBroadcastReceiver, bluetoothBroadcastFilter);
    }

    public synchronized void onClose() {
        super.onClose();
        context.unregisterReceiver(bluetoothBroadcastReceiver);
    }

    public synchronized void onUpdate() {
        if (!currentDataUsed) {
            listener.onBluetoothChanged(getBluetooths());
        } else {
            scanBluetooth();
        }
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION};
    }

    private void onBluetoothChanged() {
        if (loadBluetooths()) {
            listener.onBluetoothChanged(getBluetooths());
        }
    }

    private synchronized boolean scanBluetooth() {
        if (state == State.DISABLED)
            return false;
        if (bluetoothAdapter.isEnabled()) {
            state = State.SCANNING;
            bluetoothAdapter.startDiscovery();
            return true;
        }
        return false;
    }

    private synchronized boolean loadBluetooths() {
        currentDataUsed = false;
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

    public synchronized Set<Bluetooth> getBluetooths() {
        currentDataUsed = true;
        return new HashSet<Bluetooth>(bluetooths);
    }

    public interface Listener {
        public void onBluetoothChanged(Set<Bluetooth> bluetooth);
    }

    public static class Bluetooth {
        private final String bssid;
        private final String name;
        private final int rssi;

        public String getBssid() { return bssid; }

        public String getName() {return name; }

        public int getRssi() { return rssi; }

        public Bluetooth(String bssid, String name, int rssi) {
            this.bssid = wellFormedMac(bssid);
            this.name = name;
            this.rssi = rssi;
        }

        @Override
        public String toString() {
            return "Bluetooth{" +
                    "name=" + name +
                    ", bssid=" + bssid +
                    ", rssi=" + rssi +
                    "}";
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
