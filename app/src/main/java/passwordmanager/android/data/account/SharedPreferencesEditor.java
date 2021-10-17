package passwordmanager.android.data.account;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesEditor {

    public static void saveInSharedPreferences(Context ctx, String key, String value){
        SharedPreferences.Editor editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getFromSharedPreferences(Context ctx, String key){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void clear(Context ctx) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit();
        editor.clear().apply();
    }
}
