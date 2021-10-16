package passwordmanager.android.data.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import passwordmanager.android.R;
import passwordmanager.android.UI.login.LoginUI;

public class Login {

    public static int authLogic(String nickname, String password){
        // -1 -> not registered
        // 0  -> local account
        // >0  -> userId
        int id = -1;

        // check android key storage
        id = 0;


        return id;
    }

}