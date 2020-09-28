package io.josemmo.gaenmonitor.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.josemmo.gaenmonitor.Utils;

public class GaenScanner {
    public static final String LOGTAG = "GAEN-Scanner";
    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("0000fd6f-0000-1000-8000-00805f9b34fb");
    public static final int APPLE_MANUFACTURER_ID = 76;
    public static final byte[] APPLE_MANUFACTURER_DATA = Utils.toSplitByteArray("1.0.0.0.0.0.0.0.0.0.0.8.0.0.0.0.0");
    public static final int MAX_BEACONS = 100;

    private BluetoothLeScanner bleScanner;
    private List<ScanFilter> bleFilters;
    private ScanSettings bleSettings;
    private ScanCallback bleCallback;
    private List<GaenBeacon> beacons = new ArrayList<>();
    private GaenScannerListener changeListener;

    /**
     * Class constructor
     */
    public GaenScanner() {
        bleScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

        // Define filters
        bleFilters = new ArrayList<>();
        bleFilters.add(new ScanFilter.Builder()
                .setServiceUuid(SERVICE_UUID)
                .build());
        bleFilters.add(new ScanFilter.Builder()
                .setServiceUuid(null)
                .setManufacturerData(APPLE_MANUFACTURER_ID, APPLE_MANUFACTURER_DATA)
                .build());

        // Define scan settings
        bleSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        // Define callback
        bleCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (addScanResult(result)) {
                    notifyBeaconListChange();
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                boolean hasChanged = false;
                for (ScanResult result : results) {
                    hasChanged = addScanResult(result) || hasChanged;
                }
                if (hasChanged) {
                    notifyBeaconListChange();
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(LOGTAG, "BLE scan failed with error code " + errorCode);
            }
        };
    }


    /**
     * Start scanner
     */
    public void start() {
        beacons.clear();
        bleScanner.startScan(bleFilters, bleSettings, bleCallback);
    }


    /**
     * Stop scanner
     */
    public void stop() {
        bleScanner.stopScan(bleCallback);
    }


    /**
     * Set on beacon list change listener
     * @param listener Listener
     */
    public void setOnBeaconListChangeListener(GaenScannerListener listener) {
        changeListener = listener;
    }


    /**
     * Add found scan result
     * @param  result Scan result
     * @return        TRUE for valid beacon, FALSE otherwise
     */
    private boolean addScanResult(ScanResult result) {
        GaenBeacon beacon = GaenBeacon.fromScanResult(result);
        if (beacon == null) {
            return false;
        }
        if (beacons.size() == MAX_BEACONS) {
            beacons.remove(MAX_BEACONS-1);
        }
        beacons.add(0, beacon);
        Log.d(LOGTAG, "Found new beacon: " + beacon.toString());
        return true;
    }


    /**
     * Notify beacon list change
     */
    private void notifyBeaconListChange() {
        changeListener.notify(beacons);
    }
}
