package passwordmanager.android.data.login;

import androidx.annotation.RequiresApi;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import passwordmanager.android.data.account.Crypto;
import passwordmanager.android.data.account.SharedPreferencesEditor;

public class Login {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int authLogic(Context ctx, String username, String password){
        // -1 -> not registered
        // 0  -> local account
        // >0  -> userId
        int id = -1;

        // check android key storage
        String encryptedPassword = SharedPreferencesEditor.getFromSharedPreferences(ctx, "identifier");
        String decryptedText = "";
        try{
            decryptedText = Crypto.decrypt(encryptedPassword, password);
        }catch (Exception e){
            Toast.makeText(ctx,
                    "Wrong credentials",
                    Toast.LENGTH_LONG)
                    .show();
            return -1;
        }

        // locally authenticate
        if (decryptedText.equals(username)){
            id = 0;
        }else{
            Toast.makeText(ctx,
                    "Wrong credentials",
                    Toast.LENGTH_LONG)
                    .show();
            return -1;
        }

        // authenticate on cloud
        // TODO: request server to register, receive the userId and set "id" value.

        return id;
    }

}