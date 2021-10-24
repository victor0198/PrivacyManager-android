package passwordmanager.android;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import passwordmanager.android.UI.login.LoginUI;
import passwordmanager.android.UI.register.RegisterUI;
import passwordmanager.android.data.account.SharedPreferencesEditor;

public class MainActivity extends AppCompatActivity {

    static final int AUTH_ME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoginUI.class);
        launchAuthentication.launch(intent);

        findViewById(R.id.deleteAccount).setOnClickListener((view)->{
            SharedPreferencesEditor.clear(this);
        });
    }

    protected ActivityResultLauncher<Intent> launchAuthentication = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Context ctx = getApplicationContext();
                    String username = SharedPreferencesEditor.getFromSharedPreferences(ctx, "username");
                    String id = SharedPreferencesEditor.getFromSharedPreferences(ctx, "id");

                    TextView welcomingText = (TextView) findViewById(R.id.textViewWelcoming);
                    welcomingText.setText("Hi ".
                            concat(username)
                            .concat(" ID:")
                            .concat(id)
                    );
                }
            });

}
