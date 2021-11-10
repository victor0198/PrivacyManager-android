package privacymanager.android.UI.friendship;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import privacymanager.android.R;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.AsymmetricCryptography;

public class SearchUI extends AppCompatActivity {
    private static final String TAG = SearchUI.class.getSimpleName();
    private static final String RECEIVER_ID_PARAM = "receiverId";
    private static final String PUBLIC_KEY_PARAM = "publicKey";
    private static final String JWT_SP = "JWT";
    private String HOST_ADDRESS;
    private Context ctx;
    private Intent intent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_add);

        ctx = getApplicationContext();
        intent = getIntent();
        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");

        setListeners();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setListeners() {
        findViewById(R.id.choosedUser).setOnClickListener(view -> {
            sendFriendshipRequest(30);
        });

        findViewById(R.id.backEncryptBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendFriendshipRequest(Integer futureFriendId) {
        PrivateKey privateKey;
        PublicKey publicKey;
        try{
            KeyPair kp = AsymmetricCryptography.generateRSAKeyPair();
            privateKey = kp.getPrivate();
            publicKey = kp.getPublic();
        }catch (Exception e){
            Toast.makeText(ctx,
                    "Could not initiate request.",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        boolean privateKeySaved = saveFriendshihpPrivateKey(futureFriendId, privateKey);

        if (privateKeySaved) {
            JSONObject bodyParameters = new JSONObject();

            byte[] byte_pubkey = publicKey.getEncoded();
            Log.d(TAG, "\nBYTE KEY::: " + Arrays.toString(byte_pubkey));
            String publicKeyString = Base64.getEncoder().encodeToString(byte_pubkey);
            Log.d(TAG, "\nSTRING KEY::" + publicKeyString);

            try {
                bodyParameters.put(RECEIVER_ID_PARAM, 27);
                //TODO: make server app to accept key of length 1600-2000 characters
                bodyParameters.put(PUBLIC_KEY_PARAM, publicKeyString);
//                bodyParameters.put(PUBLIC_KEY_PARAM, publicKeyString.substring(0, 100));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            String url = HOST_ADDRESS.concat(Props.getAppProperty(ctx,"ADD_FRIEND"));
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                    response -> {
                        Toast.makeText(ctx,
                                "Friendship request sent.",
                                Toast.LENGTH_LONG)
                                .show();

                        setResult(RESULT_OK, this.intent);
                        finish();
                    },
                    error -> {
                        Log.d(TAG,"ERROR:"+ error.toString());
                        Toast.makeText(ctx,
                                "Could not send friendship request.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headerMap = new HashMap<String, String>();
                    headerMap.put("Content-Type", "application/json");
                    String JWT = SharedPreferencesEditor.getFromSharedPreferences(ctx, JWT_SP);
                    String authorisationValue = "Bearer " + JWT;
                    headerMap.put("Authorization", authorisationValue);
                    return headerMap;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(ctx);
            requestQueue.add(jsonObjectRequest);
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean saveFriendshihpPrivateKey(Integer futureFriendId, PrivateKey privateKey) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(SearchUI.this);

        byte[] byte_pubkey = privateKey.getEncoded();
        Log.d(TAG, "\nBYTE KEY::: " + Arrays.toString(byte_pubkey));
        String privateKeyString = Base64.getEncoder().encodeToString(byte_pubkey);
        Log.d(TAG, "\nSTRING KEY::" + privateKeyString);
        dataBaseHelper.addFriendshipReqest(ctx, futureFriendId, privateKeyString);

        return true;
    }

}
