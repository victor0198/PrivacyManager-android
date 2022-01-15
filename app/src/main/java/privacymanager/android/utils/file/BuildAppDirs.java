package privacymanager.android.utils.file;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class BuildAppDirs {
    private final static String TAG = BuildAppDirs.class.toString();

    public static void build(){

        File file = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager");

        if(!file.exists()) {
            Log.d(TAG, "Directory does not exist, create it");
        }
        if (!file.mkdirs()) {
            file.mkdirs();
            Log.d(TAG, "Directory already created");
        }

        file = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/encrypted");
        if(!file.exists()) {
            Log.d(TAG, "Directory 'encrypted' does not exist, create it");
        }
        if (!file.mkdirs()) {
            file.mkdirs();
            Log.d(TAG, "Directory 'encrypted' already created");
        }

        file = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/decrypted");
        if(!file.exists()) {
            Log.d(TAG, "Directory 'decrypted' does not exist, create it");
        }
        if (!file.mkdirs()) {
            file.mkdirs();
            Log.d(TAG, "Directory 'decrypted' already created");
        }

        file = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/database");
        if(!file.exists()) {
            Log.d(TAG, "Directory 'database' does not exist, create it");
        }
        if (!file.mkdirs()) {
            file.mkdirs();
            Log.d(TAG, "Directory 'database' already created");
        }
    }
}
