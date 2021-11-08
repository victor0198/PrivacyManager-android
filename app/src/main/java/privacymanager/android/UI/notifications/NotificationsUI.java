package privacymanager.android.UI.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import privacymanager.android.R;
import privacymanager.android.models.NotificationsModel;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.props.Props;

public class NotificationsUI extends AppCompatActivity {
    private static final String TAG = NotificationsUI.class.getSimpleName();
    private static final String JWT_SP = "JWT";
    private String HOST_ADDRESS;
    private Context ctx;
    private Intent intent;
//    private List<FriendshipRequestCreated> requestsList = ;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ctx = getApplicationContext();
        intent = getIntent();
        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");

        getMyNotifications();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getMyNotifications() {
        JSONObject bodyParameters = new JSONObject();

        String url = HOST_ADDRESS.concat(Props.getAppProperty(ctx,"MY_NOTIFICATIONS"));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, bodyParameters,
                response -> {

                    JSONArray notificationsList = null;
                    try {
                        notificationsList = response.getJSONArray("notificationsList");
                    } catch (JSONException e) {
                        Log.d(TAG,"ERROR: There is no notificationsList field in response.");
                    }

                    String notifications = "";

                    try {
                        for (int i = 0 ; i < notificationsList.length(); i++) {
                            JSONObject obj = notificationsList.getJSONObject(i);
                            String createdRequestId = obj.getString("createdRequestId");
                            String senderId = obj.getString("senderId");
                            String receiverId = obj.getString("receiverId");
                            String publicKey = obj.getString("publicKey");
                            String status = obj.getString("status");
                            notifications += "createdRequestId:" + createdRequestId
                                    + "\n" +
                                    "senderId:" + senderId
                                    + "\n" +
                                    "receiverId:" + receiverId
                                    + "\n" +
                                    "publicKey:" + publicKey.substring(0,10) + "..."
                                    + "\n" +
                                    "status:" + status
                                    + "\n\n";
                        }
                    }catch (JSONException e) {
                        Log.d(TAG,"ERROR: One of object fields is missing.");
                    }
                    TextView notificationsView = (TextView) findViewById(R.id.my_not);
                    notificationsView.setText(notifications);
                },
                error -> {
//                    Log.d(TAG,"ERROR:"+ error.toString());
//                    Toast.makeText(ctx,
//                            "Could not get your notifications.",
//                            Toast.LENGTH_LONG)
//                            .show();
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

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private boolean saveFriendshihpPrivateKey(Integer futureFriendId, PrivateKey privateKey) {
//        DataBaseHelper dataBaseHelper = new DataBaseHelper(SearchUI.this);
//
//        byte[] byte_pubkey = privateKey.getEncoded();
//        Log.d(TAG, "\nBYTE KEY::: " + Arrays.toString(byte_pubkey));
//        String privateKeyString = Base64.getEncoder().encodeToString(byte_pubkey);
//        Log.d(TAG, "\nSTRING KEY::" + privateKeyString);
//        dataBaseHelper.addFriendshipReqest(ctx, futureFriendId, privateKeyString);
//
//        return true;
//    }

}
