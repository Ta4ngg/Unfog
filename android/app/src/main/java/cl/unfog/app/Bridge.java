package cl.unfog.app;

import android.webkit.JavascriptInterface;

/** Puente que la web usa como window.UnfogNative. */
public class Bridge {
    private final MainActivity act;

    Bridge(MainActivity act) {
        this.act = act;
    }

    @JavascriptInterface
    public void start() {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                act.ensureAndStart();
            }
        });
    }

    @JavascriptInterface
    public void stop() {
        act.stopTracking();
    }

    /** Devuelve y vacía las ubicaciones guardadas: [[lat,lon,precision,hora],...] */
    @JavascriptInterface
    public String drain() {
        return Store.drain(act);
    }

    /** "ok" si el servicio está activo, "denied" si faltan permisos, "off" si está apagado. */
    @JavascriptInterface
    public String status() {
        if (LocationService.running) return "ok";
        if (act.isDenied()) return "denied";
        return "off";
    }
}
