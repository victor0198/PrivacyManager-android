package privacymanager.android.UI.credentials;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import privacymanager.android.R;
import privacymanager.android.models.CredentialModel;
import privacymanager.android.utils.database.DataBaseHelper;

public class AddCredentialsUI extends AppCompatActivity {
    public EditText serviceET;
    public EditText loginET;
    public EditText passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tmp_credentials_add_activity);

        serviceET = findViewById(R.id.cService);
        loginET = findViewById(R.id.cLogin);
        passwordET = findViewById(R.id.cPassword);

        createListeners();
    }

    private void createListeners() {
        findViewById(R.id.cAdd).setOnClickListener(view -> {
            String service = serviceET.getText().toString();
            String login = loginET.getText().toString();
            String password = passwordET.getText().toString();
            if (service.equals("")){
                Toast.makeText(getApplicationContext(), "Input service name", Toast.LENGTH_LONG).show();
                return;
            }
            if (login.equals("")){
                Toast.makeText(getApplicationContext(), "Input login name", Toast.LENGTH_LONG).show();
                return;
            }
            if (password.equals("")){
                Toast.makeText(getApplicationContext(), "Input password name", Toast.LENGTH_LONG).show();
                return;
            }
            CredentialModel newCredential = new CredentialModel(
                    0,
                    service,
                    login,
                    password);

            DataBaseHelper dataBaseHelper = new DataBaseHelper(AddCredentialsUI.this);

            boolean additionResult = dataBaseHelper.addCredential(getApplicationContext(), newCredential);

            if (additionResult) {
                Toast.makeText(getApplicationContext(), "Credential added.", Toast.LENGTH_LONG).show();
                Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: New credential added to database.");
            }else{
                Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: Could not add credential to DB.");
            }
        });
    }
}
