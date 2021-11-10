package privacymanager.android.UI.credentials;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import privacymanager.android.R;
import privacymanager.android.UI.dialogs.ConfirmCredentialsDelete;
import privacymanager.android.UI.dialogs.ConfirmExit;
import privacymanager.android.models.CredentialsModel;

public class CredentialsDetails extends AppCompatActivity {
    private Intent intent;
    private String servicePassword;
    private Context ctx;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials_details);
        intent = getIntent();
        ctx = getApplicationContext();

        servicePassword = intent.getStringExtra("passwordToShow");
        EditText passwordText = findViewById(R.id.passwordText);
        passwordText.setText(servicePassword);

        setListeners();
    }

    private void setListeners() {
        findViewById(R.id.backCredintialsDetails).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });

        findViewById(R.id.copyPasswordBtn).setOnClickListener(view -> {
            setClipboard(this.ctx, servicePassword);
            Toast.makeText(this.ctx,
                    "Copied",
                    Toast.LENGTH_LONG)
                    .show();
        });

        findViewById(R.id.delete_credentials).setOnClickListener(view -> {
            CredentialsModel credentialsToDelete = new CredentialsModel(
                    intent.getIntExtra("credentialsId", -1),
                    intent.getStringExtra("service"),
                    intent.getStringExtra("login"),
                    intent.getStringExtra("password")
            );
            ConfirmCredentialsDelete confirmCredentialsDelete = new ConfirmCredentialsDelete(this, intent, getApplicationContext(), credentialsToDelete);
            FragmentManager fragmentManager = getSupportFragmentManager();
            confirmCredentialsDelete.show(fragmentManager, "deleteDialog");
        });
    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }
}