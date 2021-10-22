package passwordmanager.android.data.login;

import androidx.annotation.RequiresApi;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import passwordmanager.android.data.account.Crypto;
import passwordmanager.android.data.account.SharedPreferencesEditor;

public class Login {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean authLogic(Context ctx, String username, String password){

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
            return false;
        }

        // locally authenticate
        if (decryptedText.equals(username)){
            return true;
        }else{
            Toast.makeText(ctx,
                    "Wrong credentials",
                    Toast.LENGTH_LONG)
                    .show();
        }

        return false;
    }

}