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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to support backends that use Cells for geolocation.
 * <p/>
 * Due to consistent changes in APIs for cell retrieval, this class will only work on Android 4.2+
 * <p/>
 * This class is incomplete. Do not use in production grade code.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class CellBackendHelper extends AbstractBackendHelper {
    private static final String TAG = "NlpCellBackendHelper";
    private final Listener listener;
    private final TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private Set<Cell> cells = new HashSet<>();

    /**
     * Create a new instance of {@link CellBackendHelper}. Call this in
     * {@link LocationBackendService#onCreate()}.
     *
     * @throws IllegalArgumentException if either context or listener is null.
     * @throws IllegalStateException    if android version is below 4.2
     */
    public CellBackendHelper(Context context, Listener listener) {
        super(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
            throw new IllegalStateException("Requires Android 4.2+");
        if (listener == null)
            throw new IllegalArgumentException("listener must not be null");
        this.listener = listener;
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // For some reason the Constructor of PhoneStateListener accepting a Looper is hidden,
        // so we have to change the looper manually...
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                phoneStateListener = new PhoneStateListener() {
                    private boolean supportsCellInfoChanged = true;

                    @Override
                    public void onCellInfoChanged(List<CellInfo> cellInfo) {
                        if (cellInfo != null) {
                            onCellsChanged(cellInfo);
                        } else {
                            Log.d(TAG, "Device seems not to support onCellInfoChanged()");
                            supportsCellInfoChanged = false;
                        }
                    }

                    @Override
                    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                        if (!supportsCellInfoChanged) {
                            onCellInfoChanged(telephonyManager.getAllCellInfo());
                        }
                    }
                };
                registerPhoneStateListener();
            }
        });
    }

    private int getMcc() {
        try {
            return Integer.parseInt(telephonyManager.getNetworkOperator().substring(0, 3));
        } catch (Exception e) {
            return -1;
        }
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private Cell parseCellInfo(CellInfo info) {
        try {
            if (info instanceof CellInfoGsm) {
                CellIdentityGsm identity = ((CellInfoGsm) info).getCellIdentity();
                CellSignalStrengthGsm strength = ((CellInfoGsm) info).getCellSignalStrength();
                return new Cell(Cell.CellType.GSM, identity.getMcc(), identity.getMnc(),
                        identity.getLac(), identity.getCid(), -1, strength.getDbm());
            } else if (info instanceof CellInfoCdma) {
                CellIdentityCdma identity = ((CellInfoCdma) info).getCellIdentity();
                CellSignalStrengthCdma strength = ((CellInfoCdma) info).getCellSignalStrength();
                return new Cell(Cell.CellType.CDMA, getMcc(), identity.getSystemId(),
                        identity.getNetworkId(), identity.getBasestationId(), -1, strength.getDbm());
            } else {
                return parceCellInfo18(info);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private Cell parceCellInfo18(CellInfo info) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) return null;
        if (info instanceof CellInfoWcdma) {
            CellIdentityWcdma identity = ((CellInfoWcdma) info).getCellIdentity();
            CellSignalStrengthWcdma strength = ((CellInfoWcdma) info).getCellSignalStrength();
            return new Cell(Cell.CellType.UMTS, identity.getMcc(), identity.getMnc(),
                    identity.getLac(), identity.getCid(), identity.getPsc(), strength.getDbm());
        } else if (info instanceof CellInfoLte) {
            CellIdentityLte identity = ((CellInfoLte) info).getCellIdentity();
            CellSignalStrengthLte strength = ((CellInfoLte) info).getCellSignalStrength();
            return new Cell(Cell.CellType.LTE, identity.getMcc(), identity.getMnc(),
                    identity.getTac(), identity.getCi(), identity.getPci(), strength.getDbm());
        }
        return null;
    }

    private void onCellsChanged(List<CellInfo> cellInfo) {
        if (loadCells(cellInfo)) {
            listener.onCellsChanged(getCells());
        }
    }

    private synchronized boolean loadCells(List<CellInfo> cellInfo) {
        cells.clear();
        currentDataUsed = false;
        for (CellInfo info : cellInfo) {
            Cell cell = parseCellInfo(info);
            if (cell == null) continue;
            Log.d(TAG, "onCellsChanged.allCellInfo: " + cell);
        }
        for (NeighboringCellInfo info : telephonyManager.getNeighboringCellInfo()) {
            Log.d(TAG, "onCellsChange.neighboringCellInfo: " + info);
        }
        Log.d(TAG, "onCellsChange.operator: " + telephonyManager.getNetworkOperator());
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

    public synchronized Set<Cell> getCells() {
        currentDataUsed = true;
        return new HashSet<>(cells);
    }

    /**
     * Call this in {@link org.microg.nlp.api.LocationBackendService#onOpen()}.
     */
    @Override
    public synchronized void onOpen() {
        super.onOpen();
        if (phoneStateListener != null) registerPhoneStateListener();
    }

    private synchronized void registerPhoneStateListener() {
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CELL_INFO
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * Call this in {@link org.microg.nlp.api.LocationBackendService#onClose()}.
     */
    @Override
    public synchronized void onClose() {
        super.onClose();
        if (phoneStateListener != null)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    public interface Listener {
        public void onCellsChanged(Set<Cell> cells);
    }

    public static class Cell {
        private CellType type;
        private int mcc;
        private int mnc;
        private int lac;
        private long cid;
        private int psc;
        private int signal;

        public Cell(CellType type, int mcc, int mnc, int lac, long cid, int psc, int signal) {
            if (type == null)
                throw new IllegalArgumentException("Each cell has an type!");
            this.type = type;
            boolean cdma = type == CellType.CDMA;
            if (mcc < 0 || mcc > 999)
                throw new IllegalArgumentException("Invalid MCC");
            this.mcc = mcc;
            if (cdma ? (mnc < 1 || mnc > 32767) : (mnc < 0 || mnc > 999))
                throw new IllegalArgumentException("Invalid MNC");
            this.mnc = mnc;
            if (lac < 1 || lac > (cdma ? 65534 : 65533))
                throw new IllegalArgumentException("Invalid LAC");
            this.lac = lac;
            if (cid < 0)
                throw new IllegalArgumentException("Invalid CID");
            this.cid = cid;
            this.psc = psc;
            this.signal = signal;
        }

        /**
         * @return RSCP for UMTS, RSRP for LTE, RSSI for GSM and CDMA
         */
        public int getSignal() {
            return signal;
        }

        public CellType getType() {
            return type;
        }

        public int getMcc() {
            return mcc;
        }

        public int getMnc() {
            return mnc;
        }

        public int getLac() {
            return lac;
        }

        public long getCid() {
            return cid;
        }

        public int getPsc() {
            return psc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cell cell = (Cell) o;

            if (cid != cell.cid) return false;
            if (lac != cell.lac) return false;
            if (mcc != cell.mcc) return false;
            if (mnc != cell.mnc) return false;
            if (psc != cell.psc) return false;
            if (signal != cell.signal) return false;
            if (type != cell.type) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + mcc;
            result = 31 * result + mnc;
            result = 31 * result + lac;
            result = 31 * result + (int) (cid ^ (cid >>> 32));
            result = 31 * result + psc;
            result = 31 * result + signal;
            return result;
        }

        @Override
        public String toString() {
            return "Cell{" +
                    "type=" + type +
                    ", mcc=" + mcc +
                    ", mnc=" + mnc +
                    ", lac=" + lac +
                    ", cid=" + cid +
                    (psc != -1 ? (", psc=" + psc) : "") +
                    ", signal=" + signal +
                    '}';
        }

        public enum CellType {GSM, UMTS, LTE, CDMA}
    }
}
