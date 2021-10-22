package passwordmanager.android.UI.login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import passwordmanager.android.R;
import passwordmanager.android.UI.register.RegisterUI;
import passwordmanager.android.data.account.SharedPreferencesEditor;
import passwordmanager.android.data.login.Login;

public class LoginUI extends AppCompatActivity {
    private Intent intent;
    private boolean registered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = getIntent();

        checkAccount();
    }

    public void checkAccount(){
        String identifier = SharedPreferencesEditor.getFromSharedPreferences(this, "identifier");

        try{
            boolean x = identifier.equals("");
            // if previous statement is executed, an account does exist
            this.registered = true;

        }catch (Exception e){
            this.registered = false;
        }

        if (!registered){
            Intent i = new Intent(this, RegisterUI.class);
            startActivityForResult(i, 0);
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
//            intent.putExtra("username", name.getText().toString());
//            intent.putExtra("password", pass.getText().toString());
//            intent.putExtra("id", result);
            setResult(RESULT_OK, this.intent);
            finish();
        }
    }
}
