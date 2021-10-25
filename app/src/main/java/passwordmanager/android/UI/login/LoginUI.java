package passwordmanager.android.UI.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import passwordmanager.android.R;
import passwordmanager.android.UI.register.RegisterUI;
import passwordmanager.android.data.account.SharedPreferencesEditor;
import passwordmanager.android.data.login.Login;

public class LoginUI extends AppCompatActivity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = getIntent();

        checkAccount();
    }

    protected ActivityResultLauncher<Intent> launchRegistration = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(),
                            "Registered successfully",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

    /**
     * Check if the user is registered. If not, start the register activity.
     */
    private void checkAccount(){
        String identifier = SharedPreferencesEditor.getFromSharedPreferences(this, "identifier");

        boolean registered = true;
        try{
            identifier.equals("");
            // if previous statement is executed, an account does exist
        }catch (Exception e){
            registered = false;
        }

        if (!registered){
            Intent intent = new Intent(this, RegisterUI.class);
            launchRegistration.launch(intent);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Registered successfully",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
    /**
     * Check the entered credentials and offer access to the app by returning to the main menu activity.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void authenticate(View v){
        EditText name = (EditText) findViewById(R.id.editTextPersonName);
        EditText pass = (EditText) findViewById(R.id.editTexPassword);

        Login loginService = new Login();
        boolean result = loginService.authLogic(
                this,
                name.getText().toString(),
                pass.getText().toString()
        );

        if (result){
            setResult(RESULT_OK, this.intent);
            finish();
        }
    }
}
