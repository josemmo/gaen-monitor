package io.josemmo.gaenmonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.josemmo.gaenmonitor.scanner.GaenBeacon;
import io.josemmo.gaenmonitor.scanner.GaenScanner;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;

    private WebView webView;
    private GaenScanner scanner;
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

        // Initialize GAEN scanner
        scanner = new GaenScanner();
        scanner.setOnBeaconListChangeListener(beacons -> this.beacons = beacons);

        // Schedule UI update
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
           @Override
           public void run() {
               webView.post(() -> updateUi());
           }
       }, 0, 2000);

        // Initialize app
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }


    /**
     * On request permissions result
     * @param requestCode  Request code
     * @param permissions  List of permissions
     * @param grantResults List of grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE) return;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanner.start();
        } else {
            finishAffinity();
            System.exit(1);
        }
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