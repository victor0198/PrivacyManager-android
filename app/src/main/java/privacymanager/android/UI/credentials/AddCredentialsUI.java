package privacymanager.android.UI.credentials;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import privacymanager.android.R;
import privacymanager.android.UI.login.LoginUI;
import privacymanager.android.models.CredentialModel;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.database.DataBaseHelper;

public class AddCredentialsUI extends AppCompatActivity {
    private final String USER_ID_PARAM = "userId";
    private final String CREDENTIAL_ID_PARAM = "credentialId";
    private final String SERVICE_PARAM = "service";
    private final String LOGIN_PARAM = "login";
    private final String PASSWORD_PARAM = "password";
    private EditText serviceET;
    private EditText loginET;
    private EditText passwordET;
    private CheckBox checkUpload;
    private Context ctx;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials_add);
        ctx = getApplicationContext();
        intent = getIntent();

        checkUpload = findViewById(R.id.cCheckUpload);
        checkUpload.setChecked(true);

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

            // save credential on local DB
            if (service.equals("")){
                Toast.makeText(ctx, "Input service name", Toast.LENGTH_LONG).show();
                return;
            }
            if (login.equals("")){
                Toast.makeText(ctx, "Input login name", Toast.LENGTH_LONG).show();
                return;
            }
            if (password.equals("")){
                Toast.makeText(ctx, "Input password name", Toast.LENGTH_LONG).show();
                return;
            }
            CredentialModel newCredential = new CredentialModel(
                    0,
                    service,
                    login,
                    password);

            DataBaseHelper dataBaseHelper = new DataBaseHelper(AddCredentialsUI.this);

            boolean additionResult = dataBaseHelper.addCredential(ctx, newCredential);

            if (additionResult) {
                Toast.makeText(ctx, "Credential added.", Toast.LENGTH_LONG).show();
                Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: New credential added to database.");
            }else{
                Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: Could not add credential to DB.");
                return;
            }

            // upload credential on server
            if (additionResult && checkUpload.isChecked()) {
                Integer credentialsId = dataBaseHelper.getCredentialsId(service, login, password);
                if (credentialsId == 0){
                    Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: Could not find credentials in DB.");
                    return;
                }

                String url = "http://10.0.2.2:8080/api/new_credential";

                JSONObject bodyParameters = new JSONObject();
                try {
                    bodyParameters.put(USER_ID_PARAM, SharedPreferencesEditor.getFromSharedPreferences(ctx, "id"));
                    bodyParameters.put(CREDENTIAL_ID_PARAM, credentialsId);
                    bodyParameters.put(SERVICE_PARAM, service);
                    bodyParameters.put(LOGIN_PARAM, login);
                    bodyParameters.put(PASSWORD_PARAM, password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                        response -> {
                            Log.d(LoginUI.class.toString(), "Auth online: :: was not able to get id ");
                            setResult(RESULT_OK, this.intent);
                            finish();
                        },
                        error -> {
                            Toast.makeText(ctx,
                                    "Could not upload to server.",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                ){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headerMap = new HashMap<String, String>();
                        headerMap.put("Content-Type", "application/json");
                        String JWT_SP = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ2aWN0aG9yIiwiaWF0IjoxNjM1NzA3NDE2LCJleHAiOjE2MzU3OTM4MTZ9.NXMBXoNR-h8ZGbqZkz8hKDNbLF5tVqnmX3_J02J5nNFwOM7PlVCoTLWtSyOuLBfMBdpZckMo2avv3hN5gpEUog";
                        String authorisationValue = "Bearer " + JWT_SP;
                        Log.d(LoginUI.class.toString(), "Auth value: " + authorisationValue);
                        headerMap.put("Authorization", authorisationValue);
                        return headerMap;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(ctx);
                requestQueue.add(jsonObjectRequest);

            }
        });
    }
}
