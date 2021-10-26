package passwordmanager.android.UI.menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import passwordmanager.android.R;
import passwordmanager.android.UI.credentials.CredentialsUI;
import passwordmanager.android.UI.dialogs.ConfirmExit;
import passwordmanager.android.UI.login.LoginUI;
import passwordmanager.android.UI.register.RegisterUI;
import passwordmanager.android.data.account.SharedPreferencesEditor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = RegisterUI.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authenticate();

        setListeners();
    }

    @Override
    public void onBackPressed() {
        ConfirmExit confirmExit = new ConfirmExit();
        FragmentManager fragmentManager = getSupportFragmentManager();
        confirmExit.show(fragmentManager, "exitDialog");
    }

    private void authenticate() {
        Intent intent = new Intent(this, LoginUI.class);
        launchAuthentication.launch(intent);
    }

    private void setListeners() {
        findViewById(R.id.deleteAccount).setOnClickListener(view->{
            SharedPreferencesEditor.clear(this);
        });

        findViewById(R.id.credentialsBtn).setOnClickListener(view->{
            Intent intent = new Intent(this, CredentialsUI.class);
            launchFunctionality.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> launchAuthentication = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Context ctx = getApplicationContext();
                    String username = SharedPreferencesEditor.getFromSharedPreferences(ctx, "username");

                    TextView welcomingText = (TextView) findViewById(R.id.textViewWelcoming);
                    welcomingText.setText("Hi ".
                            concat(username)
                    );
                }else{
                    Log.d(TAG, "StartAuthForResult() :: result -> skipped");
                    this.finish();
                    System.exit(0);
                }
            });

    private final ActivityResultLauncher<Intent> launchFunctionality = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to menu");
                }
            });

}
