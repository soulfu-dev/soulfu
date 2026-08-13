package com.game.soulfu;

import android.util.Log;

import org.libsdl.app.SDLActivity;
import android.os.Bundle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SoulFuActivity extends SDLActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Extract assets BEFORE SDL initializes C native code
        copyAssetToInternalStorage("datafile.sdf");
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
    private void copyAssetToInternalStorage(String filename) {
        File outFile = new File(getFilesDir(), filename);

        // Extract if the file doesn't exist yet
        if (!outFile.exists()) {
            try (InputStream in = getAssets().open(filename);
                 OutputStream out = new FileOutputStream(outFile)) {

                byte[] buffer = new byte[64 * 1024]; // 64KB buffer
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                Log.i("SoulFu", "Successfully extracted " + filename);
            } catch (Exception e) {
                Log.e("SoulFu", "Failed to extract " + filename, e);
            }
        }
    }
    @Override
    protected String[] getLibraries() {
        return new String[] {
                "soulfu"
        };
    }
}