package io.josemmo.gaenmonitor.scanner;

import java.util.List;

@FunctionalInterface
public interface GaenScannerListener {
    void notify(List<GaenBeacon> result);
}