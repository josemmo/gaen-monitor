package io.josemmo.gaenmonitor;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.josemmo.gaenmonitor.scanner.GaenBeacon;
import io.josemmo.gaenmonitor.scanner.GaenScanner;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private List<GaenBeacon> beacons = new ArrayList<>();

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WebView engine
        webView = findViewById(R.id.WebView);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.parseColor("#222222"));

        // Enable debug mode in case of debug release
        if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // Load UI document
        webView.loadUrl("file:///android_asset/index.html");

        // Start GAEN scanner
        GaenScanner scanner = new GaenScanner();
        scanner.setOnBeaconListChangeListener(beacons -> this.beacons = beacons);
        scanner.start();

        // Schedule UI update
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
           @Override
           public void run() {
               webView.post(() -> updateUi());
           }
       }, 0, 2000);
    }


    /**
     * Update UI
     */
    private void updateUi() {
        final StringBuilder script = new StringBuilder();
        script.append("javascript:window.dispatchEvent(new CustomEvent('beacons', {detail: '");
        for (GaenBeacon beacon : beacons) {
            script.append(beacon.getTimestamp()).append("|");
            script.append(beacon.getDistance()).append("|");
            script.append(Utils.toHex(beacon.getRandomId()));
            script.append(";");
        }
        script.append("'}));");
        webView.loadUrl(script.toString());
    }
}