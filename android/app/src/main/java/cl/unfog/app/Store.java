package cl.unfog.app;

import android.content.Context;
import android.location.Location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Cola en un archivo: el servicio agrega ubicaciones y la web las recoge cuando está visible. */
final class Store {
    private static final long MAX_BYTES = 3L * 1024 * 1024;

    private Store() {
    }

    private static File file(Context c) {
        return new File(c.getFilesDir(), "queue.txt");
    }

    static synchronized void add(Context c, Location l) {
        File f = file(c);
        if (f.exists() && f.length() > MAX_BYTES) return;
        String line = l.getLatitude() + "," + l.getLongitude() + "," + l.getAccuracy() + "," + l.getTime() + "\n";
        try (FileOutputStream out = new FileOutputStream(f, true)) {
            out.write(line.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    static synchronized String drain(Context c) {
        File f = file(c);
        if (!f.exists()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (!first) sb.append(',');
                sb.append('[').append(line).append(']');
                first = false;
            }
        } catch (IOException e) {
            return "[]";
        }
        f.delete();
        return sb.append(']').toString();
    }
}
