package cl.unfog.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;

/** Muestra la web de Unfog y le entrega la ubicación que guarda el servicio en segundo plano. */
public class MainActivity extends Activity {
    // Dirección de la web. Si cambia tu usuario o el nombre del repositorio, cámbiala aquí.
    static final String HOST = "ta4ngg.github.io";
    static final String HOME = "https://" + HOST + "/Unfog/";
    private static final int REQ_PERMS = 7;
    private WebView web;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.addJavascriptInterface(new Bridge(this), "UnfogNative");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (HOST.equals(u.getHost())) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (Exception ignored) {
                }
                return true;
            }
        });
        web.loadUrl(HOME);
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    SharedPreferences prefs() {
        return getSharedPreferences("unfog", MODE_PRIVATE);
    }

    boolean hasFineLocation() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    boolean isDenied() {
        return prefs().getBoolean("denied", false) && !hasFineLocation();
    }

    /** Pide los permisos que falten y luego enciende el servicio. */
    void ensureAndStart() {
        ArrayList<String> need = new ArrayList<>();
        if (!hasFineLocation()) {
            need.add(Manifest.permission.ACCESS_FINE_LOCATION);
            need.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (need.isEmpty()) {
            prefs().edit().putBoolean("denied", false).apply();
            startTracking();
        } else {
            requestPermissions(need.toArray(new String[0]), REQ_PERMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code != REQ_PERMS) return;
        if (hasFineLocation()) {
            prefs().edit().putBoolean("denied", false).apply();
            startTracking();
        } else {
            prefs().edit().putBoolean("denied", true).apply();
        }
    }

    void startTracking() {
        Intent i = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
        else startService(i);
    }

    void stopTracking() {
        stopService(new Intent(this, LocationService.class));
    }
}
