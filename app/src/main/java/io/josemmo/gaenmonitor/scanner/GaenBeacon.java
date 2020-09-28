package io.josemmo.gaenmonitor.scanner;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import io.josemmo.gaenmonitor.Utils;

public class GaenBeacon {
    private long timestamp;
    private String macAddress;
    private int rssi;
    private byte[] randomId;

    /**
     * Get instance from scan result
     * @param  result Scan result
     * @return        Beacon or NULL in case of not a beacon
     */
    @Nullable
    public static GaenBeacon fromScanResult(ScanResult result) {
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord == null) {
            return null;
        }

        // Get service data
        byte[] serviceData = scanRecord.getServiceData(GaenScanner.SERVICE_UUID);
        if (serviceData == null) {
            return null;
        }

        // Build instance
        GaenBeacon beacon = new GaenBeacon();
        beacon.timestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + result.getTimestampNanos() / 1000000;
        beacon.macAddress = result.getDevice().getAddress();
        beacon.rssi = result.getRssi();
        beacon.randomId = serviceData;
        return beacon;
    }


    /**
     * Get timestamp
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }


    /**
     * Get MAC address
     * @return MAC address
     */
    public String getMacAddress() {
        return macAddress;
    }


    /**
     * Get RSSI
     * @return RSSI
     */
    public int getRssi() {
        return rssi;
    }


    /**
     * Get random ID
     * @return Random ID
     */
    public byte[] getRandomId() {
        return randomId;
    }


    /**
     * Get distance to beacon
     * @return Distance in meters
     */
    public double getDistance() {
        return Math.pow(10, (-69 - rssi) / 100f);
    }


    /**
     * To string
     * @return String representation
     */
    @Override
    public String toString() {
        return "GaenBeacon(" + timestamp + ", " + macAddress + ", " + rssi + ", " + Utils.toHex(randomId) + ")";
    }
}
