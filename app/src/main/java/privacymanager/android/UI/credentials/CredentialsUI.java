package privacymanager.android.UI.credentials;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import privacymanager.android.R;

public class CredentialsUI extends AppCompatActivity {
    private static final String TAG = CredentialsUI.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);

        findViewById(R.id.addCredentials).setOnClickListener(view -> {
            Intent intent = new Intent(this, AddCredentialsUI.class);
            launchAddCredentials.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> launchAddCredentials = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to activities list.");
                }
            });
}