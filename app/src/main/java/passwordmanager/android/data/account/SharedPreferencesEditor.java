package passwordmanager.android.data.account;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesEditor {

    /**
     * Save a value in SP.
     *
     * @param ctx   application context
     * @param key   the key for future accessing the value
     * @param value the value to be stored
     */
    public static void saveInSharedPreferences(Context ctx, String key, String value){
        SharedPreferences.Editor editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get a value from SP.
     *
     * @param ctx application context
     * @param key the key to access the value
     * @return obtained value
     */
    public static String getFromSharedPreferences(Context ctx, String key){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    /**
     * Delete all values stored in SP.
     *
     * @param ctx application context
     */
    public static void clear(Context ctx) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit();
        editor.clear().apply();
    }
}
