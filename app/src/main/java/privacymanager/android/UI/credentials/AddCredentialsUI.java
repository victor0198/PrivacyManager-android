package privacymanager.android.UI.credentials;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import privacymanager.android.R;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.Crypto;
import privacymanager.android.utils.security.GeneratePassword;

public class AddCredentialsUI extends AppCompatActivity {
    private static final String USER_ID_PARAM = "userId";
    private static final String CREDENTIALS_ID_PARAM = "credentialId";
    private static final String SERVICE_PARAM = "service";
    private static final String LOGIN_PARAM = "login";
    private static final String PASSWORD_PARAM = "password";
    private static final String USERNAME_SP = "username";
    private String HOST_ADDRESS;
    private EditText serviceET;
    private EditText loginET;
    private EditText passwordET;
    private CheckBox checkUpload;
    private Context ctx;
    private Intent intent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials_add);
        ctx = getApplicationContext();
        intent = getIntent();
        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");

        checkUpload = findViewById(R.id.cCheckUpload);
        checkUpload.setChecked(false);

        serviceET = findViewById(R.id.cService);
        loginET = findViewById(R.id.cLogin);
        passwordET = findViewById(R.id.cPassword);

        createListeners();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createListeners() {
        findViewById(R.id.cAdd).setOnClickListener(view -> {
            CredentialsModel credentialsModel = saveCredentials();
            if (credentialsModel != null && checkUpload.isChecked()){
                if (checkJWT()){
                    uploadCredentials();
                }else{
//                    getJWTAndUpload();
                    Toast.makeText(ctx, "Not connected to server. Credentials saved only locally.", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, this.intent);
                    finish();
                }
            }else if(credentialsModel != null){
                setResult(RESULT_OK, this.intent);
                finish();
            }
        });

        findViewById(R.id.backNewCredintials).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });

        findViewById(R.id.generatePasswordBtn).setOnClickListener(view -> {
            TextView passText = findViewById(R.id.cPassword);
            GeneratePassword generatePassword = new GeneratePassword();
            String generatedPassword = generatePassword.generateSecurePassword(12, 3, 3, 3, 3);
            passText.setText(generatedPassword);
        });
    }

    private boolean checkJWT() {
        String my_JWT;
        if (checkUpload.isChecked()){
            my_JWT = this.intent.getStringExtra("JWT");
            if (my_JWT.equals("")){
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private CredentialsModel saveCredentials() {
        CredentialsModel newCredentials = null;

        String service = serviceET.getText().toString();
        String login = loginET.getText().toString();
        String password = passwordET.getText().toString();

        // save credential on local DB
        if (service.equals("")){
            Toast.makeText(ctx, "Input service name", Toast.LENGTH_LONG).show();
            return newCredentials;
        }
        if (login.equals("")){
            Toast.makeText(ctx, "Input login name", Toast.LENGTH_LONG).show();
            return newCredentials;
        }
        if (password.equals("")){
            Toast.makeText(ctx, "Input password name", Toast.LENGTH_LONG).show();
            return newCredentials;
        }

        newCredentials = new CredentialsModel(
                0,
                service,
                login,
                password);

        DataBaseHelper dataBaseHelper = new DataBaseHelper(AddCredentialsUI.this);

        boolean additionResult = dataBaseHelper.addCredential(ctx, newCredentials);

        if (additionResult) {
            Toast.makeText(ctx, "Credentials added.", Toast.LENGTH_LONG).show();
            Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: New credentials added to database.");
        }else{
            Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: Could not add credentials to DB.");
            return null;
        }

        return newCredentials;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadCredentials() {
        String service = serviceET.getText().toString();
        String login = loginET.getText().toString();
        String password = passwordET.getText().toString();

        DataBaseHelper dataBaseHelper = new DataBaseHelper(AddCredentialsUI.this);
        Integer credentialsId = dataBaseHelper.getCredentialsId(service, login, password);
        if (credentialsId == 0){
            Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: Could not find credentials in DB.");
            return;
        }

        String url = HOST_ADDRESS.concat(Props.getAppProperty(ctx,"ADD_CREDENTIALS"));

        String encryptedService, encryptedLogin, encryptedPassword;
        try{
            encryptedService = Crypto.encrypt(service.getBytes(StandardCharsets.UTF_8),
                    this.intent.getStringExtra("password"));
            encryptedLogin = Crypto.encrypt(login.getBytes(StandardCharsets.UTF_8),
                    this.intent.getStringExtra("password"));
            encryptedPassword = Crypto.encrypt(password.getBytes(StandardCharsets.UTF_8),
                    this.intent.getStringExtra("password"));
        }catch (Exception e){
            Toast.makeText(ctx,
                    "Could not prepare credentials for uploading.",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        JSONObject bodyParameters = new JSONObject();
        try {
            bodyParameters.put(USER_ID_PARAM, SharedPreferencesEditor.getFromSharedPreferences(ctx, "id"));
            bodyParameters.put(CREDENTIALS_ID_PARAM, credentialsId);
            bodyParameters.put(SERVICE_PARAM, encryptedService);
            bodyParameters.put(LOGIN_PARAM, encryptedLogin);
            bodyParameters.put(PASSWORD_PARAM, encryptedPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String JWT = this.intent.getStringExtra("JWT");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                response -> {
                    Log.d(AddCredentialsUI.class.toString(), "Credentials uploaded.");
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
                String authorisationValue = "Bearer " + JWT;
                headerMap.put("Authorization", authorisationValue);
                return headerMap;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(jsonObjectRequest);
    }
}
