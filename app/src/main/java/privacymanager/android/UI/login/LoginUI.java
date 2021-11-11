package privacymanager.android.UI.login;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Properties;

import privacymanager.android.R;
import privacymanager.android.UI.register.RegisterUI;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.Crypto;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.internet.InternetConnection;

public class LoginUI extends AppCompatActivity {
    private final String USERNAME_SP = "username";
    private final String PASSWORD_SP = "password";
    private final String IDENTIFIER_SP = "identifier";
    private final String ID_SP = "id";
    private final String JWT_SP = "JWT";
    private String HOST_ADDRESS;
    private Intent intent;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = getIntent();
        ctx = getApplicationContext();
        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");

        checkAccount();
    }

    protected ActivityResultLauncher<Intent> launchRegistration = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(ctx,
                            "Registered successfully",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

    /**
     * Check if the user is registered. If not, start the register activity.
     */
    private void checkAccount(){
        String identifier = SharedPreferencesEditor.getFromSharedPreferences(this, IDENTIFIER_SP);

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

        boolean result = authLogic(
                this,
                name.getText().toString(),
                pass.getText().toString()
        );

        if (result){
            setResult(RESULT_OK, this.intent);
            finish();
        }
    }

    /**
     * Save the @id in SP and close the current activity.
     *
     * @param ctx      application context
     * @param username username
     * @param password main password
     * @return authentication status
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean authLogic(Context ctx, String username, String password){

        // check android key storage
        String encryptedPassword = SharedPreferencesEditor.getFromSharedPreferences(ctx, "identifier");
        String decryptedText;
        boolean isRegisterd = true;
        try{
            encryptedPassword.equals("");
            // if previous statement is executed, an account does exist
        }catch (Exception e){
            isRegisterd = false;
        }

        // Make online authentication to obtain user data
        if (!isRegisterd){
            if (!InternetConnection.checkConnection(ctx)){
                Toast.makeText(ctx,
                        "Connect to the internet for authentication.",
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }

            // authentication url
            String url = HOST_ADDRESS.concat(Props.getAppProperty(ctx,"AUTHENTICATION"));

            // authentication payload
            JSONObject bodyParameters = new JSONObject();
            try {
                bodyParameters.put(USERNAME_SP, username);
                bodyParameters.put(PASSWORD_SP, password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // build the request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                    response -> {
                        Log.d(LoginUI.class.toString(), "AuthLogin() :: ++++++++++++++++++++++++.");
                        Log.d(LoginUI.class.toString(), "AuthLogin() :: " + response);

                        String id, jwt;
                        try {
                            id = response.get("ownerId").toString();
                            Log.d(LoginUI.class.toString(), "AuthLogin() :: storeAccount id: " + id);
                        } catch (JSONException e) {
                            Log.d(LoginUI.class.toString(), "AuthLogin() :: was not able to get id ");
                            e.printStackTrace();
                            return;
                        }

                        try {
                            jwt = response.get("token").toString();
                            Log.d(LoginUI.class.toString(), "AuthLogin() :: Got JWT: " + jwt);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }

                        String encryptedIdentifier;
                        // make account identifier
                        try {
                            encryptedIdentifier = Crypto.encrypt(
                                    username.getBytes(UTF_8),
                                    password);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        SharedPreferencesEditor.saveInSharedPreferences(ctx, USERNAME_SP, username);
                        SharedPreferencesEditor.saveInSharedPreferences(ctx, IDENTIFIER_SP, encryptedIdentifier);
                        SharedPreferencesEditor.saveInSharedPreferences(ctx, PASSWORD_SP, password);
                        SharedPreferencesEditor.saveInSharedPreferences(ctx, ID_SP, id);
                        SharedPreferencesEditor.saveInSharedPreferences(ctx, JWT_SP, jwt);

                        Log.d(LoginUI.class.toString(), "AuthLogin() :: Account stored.");

                        setResult(RESULT_OK, this.intent);
                        finish();

                    },
                    error -> {
                        if (error.networkResponse == null){
                            Toast.makeText(ctx,
                                    "Server not available.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            return;
                        }
                        if (error.networkResponse.statusCode == 401){
                            Toast.makeText(ctx,
                                    "This account is not registered.",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
            );

            // make request
            RequestQueue requestQueue = Volley.newRequestQueue(ctx);
            requestQueue.add(jsonObjectRequest);

            Log.d(LoginUI.class.toString(), "AuthLogin() :: Reached the end of online authentication.");

        }

        if (isRegisterd){
            try{
                decryptedText = Crypto.decrypt(encryptedPassword, password);
            }catch (Exception e){
                Toast.makeText(ctx,
                        "Wrong credentials",
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }

            SharedPreferencesEditor.saveInSharedPreferences(ctx, PASSWORD_SP, password);

            // locally authenticate
            if (decryptedText.equals(username)){
                return true;
            }else{
                Toast.makeText(ctx,
                        "Wrong credentials",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
        Log.d(LoginUI.class.toString(), "Not supposed to reach this point.");
        return false;
    }
}
