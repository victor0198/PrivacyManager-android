package passwordmanager.android.data.login;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import passwordmanager.android.R;
import passwordmanager.android.UI.login.LoginUI;
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
        Toast.makeText(ctx,
                username.concat(" authenticated successfully. ID ").concat(String.valueOf(id)),
                Toast.LENGTH_LONG)
                .show();

        return id;
    }

}