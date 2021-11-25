package privacymanager.android.UI.credentials;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

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
import privacymanager.android.UI.dialogs.ConfirmCredentialsDelete;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.database.DBFacade;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.Crypto;

public class CredentialsDetails extends AppCompatActivity {
    private Intent intent;
    private String servicePassword;
    private Context ctx;
    private boolean showPass = false;
    private DBFacade dbFacade;
    private static final String USER_ID_PARAM = "userId";
    private static final String CREDENTIALS_ID_PARAM = "credentialId";
    private static final String SERVICE_PARAM = "service";
    private static final String LOGIN_PARAM = "login";
    private static final String PASSWORD_PARAM = "password";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials_details);
        intent = getIntent();
        ctx = getApplicationContext();

        servicePassword = intent.getStringExtra("passwordToShow");
        EditText passwordText = findViewById(R.id.textPasswordHidden);
        passwordText.setText(servicePassword);

        ImageButton ib = findViewById(R.id.imageButtonUploadC);

        if (intent.getIntExtra("uploaded", 0) == 1) {
            ib.setVisibility(View.INVISIBLE);
        }

        DataBaseHelper dataBaseHelper = new DataBaseHelper(CredentialsDetails.this);
        dbFacade = new DBFacade(dataBaseHelper, this.intent.getStringExtra("password"));

        setListeners();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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
                    intent.getStringExtra("password"),
                    intent.getIntExtra("uploaded", 0)
            );
            ConfirmCredentialsDelete confirmCredentialsDelete = new ConfirmCredentialsDelete(this, intent, getApplicationContext(), credentialsToDelete);
            FragmentManager fragmentManager = getSupportFragmentManager();
            confirmCredentialsDelete.show(fragmentManager, "deleteDialog");
        });

        findViewById(R.id.revealOrHide).setOnClickListener(view -> {
            TextView pT = findViewById(R.id.textPasswordHidden);
            if (!showPass){
                pT.setTransformationMethod(null);
                showPass = true;
            }else{
                pT.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showPass = false;
            }
        });

        findViewById(R.id.imageButtonUploadC).setOnClickListener(view -> {
            String service = intent.getStringExtra("service");
            String login = intent.getStringExtra("login");
            String password = intent.getStringExtra("passwordToShow");

            Integer credentialsId = dbFacade.getCredentialsId(service, login, password);
            if (credentialsId == 0){
                Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: cAdd Click :: Could not find credentials in DB.");
                Log.d(AddCredentialsUI.class.toString(), " :: createListeners() :: " + service + "|" + login + "|" + password);
                return;
            }

            String url = Props.getAppProperty(ctx,"HOST_ADDRESS").concat(Props.getAppProperty(ctx,"ADD_CREDENTIALS"));

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
                        int id = dbFacade.getCredentialsId(service, login, password);
                        dbFacade.patchCredential(ctx, id, 1);
                        Toast.makeText(ctx,
                                "Credentials uploaded.",
                                Toast.LENGTH_LONG)
                                .show();
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
        });
    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }
}