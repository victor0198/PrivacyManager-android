package passwordmanager.android.UI.register;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import passwordmanager.android.R;
import passwordmanager.android.data.account.Crypto;
import passwordmanager.android.data.account.SharedPreferencesEditor;

public class RegisterUI extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        checkSecureLock();

        findViewById(R.id.register).setOnClickListener(this::storeAccount);
    }

    public void checkSecureLock(){
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if(!keyguardManager.isKeyguardSecure()){
            Toast.makeText(this,
                    "Secure lock screen was not set up.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void storeAccount(View view){
        try{

            // TODO: replace the strings with text from the user input
            String passwordString = "pass";
            String username = "nick";

            // make account identifier
            String encryptedTextBase64 = Crypto.encrypt(username.getBytes(UTF_8), passwordString);

            // store identifier
            SharedPreferencesEditor.saveInSharedPreferences(this, "identifier", encryptedTextBase64);

            setResult(RESULT_OK, getIntent());
            finish();

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

}
