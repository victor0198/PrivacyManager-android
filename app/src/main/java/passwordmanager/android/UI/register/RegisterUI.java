package passwordmanager.android.UI.register;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import passwordmanager.android.R;
import passwordmanager.android.data.account.Crypto;
import passwordmanager.android.data.account.SharedPreferencesEditor;

public class RegisterUI extends AppCompatActivity {
    private static final String TAG = RegisterUI.class.getSimpleName();
    private Intent intent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        intent = getIntent();

        checkSecureLock();
        // TODO: check internet access

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "http://10.0.2.2:8080/api/v1/registration";
                EditText username = (EditText) findViewById(R.id.rPersonName);
                EditText password = (EditText) findViewById(R.id.rPassword);

                JSONObject bodyParameters = new JSONObject();
                try {
                    bodyParameters.put("username", username.getText().toString());
                    bodyParameters.put("password", password.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                        response -> {
                            Log.d(TAG, "createRegisterRequest() :: onResponse() ::" + response);

                            try {
                                closeActivity(response.get("id").toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                closeActivity("0");
                            }

                            boolean stored = storeAccount();
                            Log.d(TAG, "createRegisterRequest() :: storeAccount : " + (stored?"stored":"not sored"));

                        },
                        error -> {
                            Log.d(TAG, "createBookingRequest() :: onErrorResponse() ::" + error);
                        }
                );

                requestQueue.add(jsonObjectRequest);


            }
        });
    }

    public void checkSecureLock(){
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if(!keyguardManager.isKeyguardSecure()){
            Toast.makeText(this,
                    "Secure lock screen was not set up.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean storeAccount(){
        try{
            EditText usernameET = (EditText) findViewById(R.id.rPersonName);
            EditText passwordET = (EditText) findViewById(R.id.rPassword);
            EditText passwordConfirmET = (EditText) findViewById(R.id.rPasswordConfirm);
            String name = usernameET.getText().toString();
            String pass = passwordET.getText().toString();

            if (!pass.equals(passwordConfirmET.getText().toString())){
                Toast.makeText(this,
                        "Passwords does not match.",
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }

            // make account identifier
            String encryptedTextBase64 = Crypto.encrypt(
                    name.getBytes(UTF_8),
                    pass);

            // store account identifier
            SharedPreferencesEditor.saveInSharedPreferences(this, "username", name);
            SharedPreferencesEditor.saveInSharedPreferences(this, "identifier", encryptedTextBase64);

        }catch (Exception e){
            throw new RuntimeException(e);
        }

        return true;
    }

    private void closeActivity(String id){
        // store id
        SharedPreferencesEditor.saveInSharedPreferences(this, "id", id);

        Log.d(TAG, "createRegisterRequest() :: onResponse() :: CloseActivity() :: user_id: " + id);

        setResult(RESULT_OK, intent);
        finish();
    }

}
