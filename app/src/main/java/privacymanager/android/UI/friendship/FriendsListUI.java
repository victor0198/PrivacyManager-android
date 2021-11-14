package privacymanager.android.UI.friendship;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import privacymanager.android.R;
import privacymanager.android.models.NotificationsModel;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.AsymmetricCryptography;
import privacymanager.android.utils.security.Crypto;

public class FriendsListUI extends AppCompatActivity {
    private static final String TAG = FriendsListUI.class.getSimpleName();
    private Intent intent;
    private Context ctx;
    private String HOST_ADDRESS;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        intent = getIntent();

        setListeners();

        ctx = getApplicationContext();
        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");
        getNewFriendships();
    }

    private void setListeners() {
        findViewById(R.id.addFriend).setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchUI.class);
            intent.putExtra("JWT", this.intent.getStringExtra("JWT"));
            launchAddFriend.launch(intent);
        });

        findViewById(R.id.backFriendsBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getNewFriendships() {
        JSONObject bodyParameters = new JSONObject();

        String url = HOST_ADDRESS.concat(Props.getAppProperty(ctx,"REQUEST_RESPONSES"));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, bodyParameters,
                response -> {

                    JSONArray responsesList = null;
                    try {
                        responsesList = response.getJSONArray("responses");
                    } catch (JSONException e) {
                        Log.d(TAG,"ERROR: There is no 'responses' field in response object.");
                    }


                    DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
                    try {
                        for (int i = 0 ; i < responsesList.length(); i++) {
                            JSONObject obj = responsesList.getJSONObject(i);

                            Integer requestAccepter = obj.getInt("requestAccepter");

                            String privateKeyString = dataBaseHelper.getFriendshipPrivateKey(requestAccepter);
                            PrivateKey privateFriendshipKey = loadPrivateKey(privateKeyString);
                            byte[] encryptedSecretKey = Base64.getDecoder().decode(obj.getString("symmetricKey"));

                            String symmetricKeyString = AsymmetricCryptography.do_RSADecryption(encryptedSecretKey, privateFriendshipKey);
                            Log.d(TAG,"SUCCESS :: Symmetric key: " + symmetricKeyString);

                            Integer friendshipId = obj.getInt("friendshipId");
                            boolean savedFiendship = dataBaseHelper.saveFriendshipKey(ctx, friendshipId, requestAccepter, symmetricKeyString);
                            if (!savedFiendship){
                                Toast.makeText(ctx,
                                        "A new friendship was not saved.",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        Log.d(TAG,"ERROR: Building the private key object error.");
                    } catch (JSONException e) {
                        Log.d(TAG,"ERROR: One of object fields is missing.");
                    } catch (Exception e) {
                        Log.d(TAG,"ERROR: Symmetric key decryption error.");
                    }

                },
                error -> {
                    Log.d(TAG,"ERROR:" + error.toString());

                    if(error.toString().indexOf("JSONException: End of input at character 0")>0){
                        return;
                    }
                    Toast.makeText(ctx,
                            "Could not get your notifications.",
                            Toast.LENGTH_LONG)
                            .show();
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headerMap = new HashMap<String, String>();
                headerMap.put("Content-Type", "application/json");
                String JWT = intent.getStringExtra("JWT");
                String authorisationValue = "Bearer " + JWT;
                headerMap.put("Authorization", authorisationValue);
                return headerMap;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(jsonObjectRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static PrivateKey loadPrivateKey(String key64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] clear = Base64.getDecoder().decode(key64.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }

    private final ActivityResultLauncher<Intent> launchAddFriend = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to friends list.");
                }
            });
}
