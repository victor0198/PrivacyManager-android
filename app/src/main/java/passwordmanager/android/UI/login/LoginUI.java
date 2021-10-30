package passwordmanager.android.UI.login;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import passwordmanager.android.R;
import passwordmanager.android.UI.register.RegisterUI;
import passwordmanager.android.data.account.Crypto;
import passwordmanager.android.data.account.SharedPreferencesEditor;
import passwordmanager.android.data.internet.InternetConnection;

public class LoginUI extends AppCompatActivity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = getIntent();

        checkAccount();
    }

    protected ActivityResultLauncher<Intent> launchRegistration = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(),
                            "Registered successfully",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

    /**
     * Check if the user is registered. If not, start the register activity.
     */
    private void checkAccount(){
        String identifier = SharedPreferencesEditor.getFromSharedPreferences(this, "identifier");

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

            // registration url
            String url = "http://10.0.2.2:8080/api/auth/signin";

            // registration payload
            JSONObject bodyParameters = new JSONObject();
            try {
                bodyParameters.put("username", username);
                bodyParameters.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // build the request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                    response -> {
                        Log.d(LoginUI.class.toString(), "Auth online:" + response);

                        String id;
                        try {
                            id = response.get("ownerId").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            id = "0";
                        }

                        if (!id.equals("0")){
                            String encryptedIdentifier = "";
                            // make account identifier
                            try {
                                encryptedIdentifier = Crypto.encrypt(
                                        username.getBytes(UTF_8),
                                        password);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // store account identifier
                            if (!encryptedIdentifier.equals("")){
                                SharedPreferencesEditor.saveInSharedPreferences(ctx, "username", username);
                                SharedPreferencesEditor.saveInSharedPreferences(ctx, "identifier", encryptedIdentifier);
                                SharedPreferencesEditor.saveInSharedPreferences(ctx, "id", id);
                                Log.d(LoginUI.class.toString(), "Auth online: :: storeAccount id: " + id);
                            }

                            setResult(RESULT_OK, this.intent);
                            finish();

                        }else {

                            Log.d(LoginUI.class.toString(), "Auth online: :: was not able to get id ");
                        }
                    },
                    error -> {
                        Toast.makeText(ctx,
                                "Server error.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
            );

            // make request
            RequestQueue requestQueue = Volley.newRequestQueue(ctx);
            requestQueue.add(jsonObjectRequest);

            Log.d(LoginUI.class.toString(), "Reached the end of online authentication.");

        }

        if (isRegisterd){
            try{
                decryptedText = Crypto.decrypt(encryptedPassword, password);
            }catch (Exception e){
                Toast.makeText(ctx,
                        "Wrong credentials 1",
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }

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
