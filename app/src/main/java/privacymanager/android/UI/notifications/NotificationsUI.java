package privacymanager.android.UI.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import privacymanager.android.R;
import privacymanager.android.UI.credentials.AddCredentialsUI;
import privacymanager.android.UI.credentials.CredentialsUI;
import privacymanager.android.UI.friendship.SearchUI;
import privacymanager.android.models.NotificationsModel;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.AsymmetricCryptography;
import privacymanager.android.utils.security.Crypto;
import privacymanager.android.utils.security.CryptoUtils;

public class NotificationsUI extends AppCompatActivity {
    private static final String TAG = NotificationsUI.class.getSimpleName();
    private static final String JWT_SP = "JWT";
    private String HOST_ADDRESS;
    private Context ctx;
    private Intent intent;
    private List<NotificationsModel> notificationsModelList = new ArrayList<>();
    private ListView listView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ctx = getApplicationContext();
        intent = getIntent();
        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");

        listView = (ListView) findViewById(R.id.notificationsList);
//        listView.setItemsCanFocus(false);
        getMyNotifications();

        findViewById(R.id.backNotificationsBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });
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
                            NotificationsModel notification = new NotificationsModel();
                            JSONObject obj = notificationsList.getJSONObject(i);
                            if (obj.getString("status").equals("PENDING")) {
                                notification.setCreatedRequestId(obj.getLong("createdRequestId"));
                                notification.setSenderId(obj.getLong("senderId"));
                                notification.setSenderUsername(obj.getString("senderUsername"));
                                notification.setReceiverId(obj.getLong("receiverId"));
                                notification.setPublicKey(obj.getString("publicKey"));
                                notification.setStatus(obj.getString("status"));
                                notificationsModelList.add(notification);
                            }
                        }
                    }catch (JSONException e) {
                        Log.d(TAG,"ERROR: One of object fields is missing.");
                    }
                    TextView notificationsCounter = findViewById(R.id.notNmb);
                    if (notificationsModelList.size()>0) {
                        notificationsCounter.setText(notificationsModelList.size() + " notification" + (notificationsModelList.size() > 1 ? "s" : ""));
                    }

                    Log.d(TAG,"ERROR:" + notificationsModelList.toString());
                    dispatchPopulateAccessibilityEvent();
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
                String JWT = SharedPreferencesEditor.getFromSharedPreferences(ctx, JWT_SP);
                String authorisationValue = "Bearer " + JWT;
                headerMap.put("Authorization", authorisationValue);
                return headerMap;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(jsonObjectRequest);

    }


    public void dispatchPopulateAccessibilityEvent() {

        List<Long> notificationsIds = new ArrayList<Long>();
        List<String> notificationsKeys = new ArrayList<String>();
        List<String> notificationsTexts = new ArrayList<String>();
        List<String> notificationsUsername = new ArrayList<String>();

        for (int i=0; i<notificationsModelList.size(); i++){
            notificationsTexts.add("New friendship request from:");
            notificationsIds.add(this.notificationsModelList.get(i).getSenderId());
            notificationsKeys.add(this.notificationsModelList.get(i).getPublicKey());
            notificationsUsername.add(this.notificationsModelList.get(i).getSenderUsername());
        }

        NotificationsUI.CustomNotificationsList customCountryList = new NotificationsUI.CustomNotificationsList(this, intent, notificationsIds, notificationsKeys, notificationsTexts, notificationsUsername);
        this.listView.setAdapter(customCountryList);
//        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View v,
//                                    final int position, long id) {
//                Log.i("List View Clicked", "**********");
//                Toast.makeText(NotificationsUI.this,
//                        "List View Clicked:" + position, Toast.LENGTH_LONG)
//                        .show();
//            }
//        });
    }

    public static class CustomNotificationsList extends ArrayAdapter {
        private List<Long> notificationsIds;
        private List<String> notificationsKeys;
        private List<String> notificationsTexts;
        private List<String> notificationsUsername;
        private Activity context;
        private Intent intent;

        public CustomNotificationsList(Activity context, Intent intent, List<Long> notificationsIds, List<String> notificationsKeys, List<String> notificationsTexts, List<String> notificationsUsername) {
            super(context, R.layout.row_credintials, notificationsUsername);
            this.context = context;
            this.notificationsIds = notificationsIds;
            this.notificationsKeys = notificationsKeys;
            this.notificationsTexts = notificationsTexts;
            this.notificationsUsername = notificationsUsername;

            Log.i("notificationsIds", notificationsIds.toString());
            Log.i("notificationsKeys", notificationsKeys.toString());
            Log.i("notificationsTexts", notificationsTexts.toString());
            Log.i("notificationsUsername", notificationsUsername.toString());
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NotificationsHolder holder = null;

            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if (convertView == null){
                row = inflater.inflate(R.layout.row_notifications, null, true); // false??
                holder = new NotificationsHolder();
                holder.senderId = notificationsIds.get(position);
                holder.senderUsername = row.findViewById(R.id.textViewUsername);
                holder.publicKey = notificationsKeys.get(position);
                holder.notificationText = row.findViewById(R.id.textViewText);
                holder.accept = (Button) row.findViewById(R.id.acceptBtn);
                holder.decline = (Button) row.findViewById(R.id.declineBtn);
                row.setTag(holder);
            }else {
                holder = (NotificationsHolder) row.getTag();
            }

            holder.senderUsername.setText(notificationsUsername.get(position));
            holder.notificationText.setText(notificationsTexts.get(position));

            holder.accept.setOnClickListener(view -> {
                // TODO Auto-generated method stub
                Log.i("Accept Button Clicked", notificationsIds.get(position).toString() + notificationsUsername.get(position));
                sendFriendshipResponse(notificationsIds.get(position), notificationsKeys.get(position), "ACCEPT");
            });

            holder.decline.setOnClickListener(view -> {
                // TODO Auto-generated method stub
                Log.i("Decline Button Clicked", notificationsIds.get(position).toString() + notificationsUsername.get(position));
                sendFriendshipResponse(notificationsIds.get(position), "", "REJECT");
            });

            return  row;
        }

        static class NotificationsHolder {
            long senderId;
            TextView notificationText;
            TextView senderUsername;
            String publicKey;
            Button accept;
            Button decline;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void sendFriendshipResponse(long frInitiatorId, String publicKey, String status){

            String url = Props.getAppProperty(context,"HOST_ADDRESS").concat(Props.getAppProperty(context,"ANSWER_FRIENDSHIP_REQUEST"));


            JSONObject bodyParameters = new JSONObject();
            if (status.equals("ACCEPT")) {
                SecretKey secretKey = null;
                try{
                    secretKey = CryptoUtils.getAESKeyFromPassword(CryptoUtils.getRandomNonce(128).toString().toCharArray(), CryptoUtils.getRandomNonce(64));
                }catch (Exception e){
                    Log.d(AddCredentialsUI.class.toString(), "Could not create symmetric key.");
                    return;
                }

                byte[] byte_secretKey = secretKey.getEncoded();
                Log.d(TAG, "\nBYTE FR KEY::: " + Arrays.toString(byte_secretKey));
                String secretKeyString = Base64.getEncoder().encodeToString(byte_secretKey);
                Log.d(TAG, "\nSTRING FR KEY::" + secretKeyString);

                Log.d(TAG, "PUBLIC KEY STRING::" + publicKey);
                byte[] byte_pubkey  = Base64.getDecoder().decode(publicKey);
                System.out.println("PUBLIC BYTE KEY::" + Arrays.toString(byte_pubkey));

                KeyFactory factory = null;
                PublicKey public_key = null;
                try{
                    factory = KeyFactory.getInstance("RSA", "BC");
                    public_key = (PublicKey) factory.generatePublic(new X509EncodedKeySpec(byte_pubkey));
                }catch (Exception e){
                    Log.d(AddCredentialsUI.class.toString(), "Could not recreate public key from string.");
                    return;
                }

                byte[] Byte_keyResponse = null;
                try {
                    Byte_keyResponse = AsymmetricCryptography.do_RSAEncryption( secretKeyString, public_key);
                } catch (Exception e) {
                    Log.d(AddCredentialsUI.class.toString(), "Could not encrypt symmetric key with public key.");
                    return;
                }

                String String_keyResponse = Base64.getEncoder().encodeToString(Byte_keyResponse);
                Log.d(TAG, "\nString_keyResponse KEY::" + String_keyResponse);

                try {
                    bodyParameters.put("frInitiatorId", frInitiatorId);
                    bodyParameters.put("status", status);
                    bodyParameters.put("symmetricKey", String_keyResponse);
                } catch (JSONException e) {
                    Log.d(AddCredentialsUI.class.toString(), "Could not prepare response payload.");
                    return;
                }
            }else{
                try {
                    bodyParameters.put("frInitiatorId", frInitiatorId);
                    bodyParameters.put("status", status);
                    bodyParameters.put("symmetricKey", "");
                } catch (JSONException e) {
                    Log.d(AddCredentialsUI.class.toString(), "Could not prepare response payload.");
                    return;
                }
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                    response -> {
                        Log.d(AddCredentialsUI.class.toString(), "Credentials uploaded.");
                        context.setResult(RESULT_OK, intent);
                        context.finish();
                    },
                    error -> {
                        Toast.makeText(context,
                                "Could send friendship response.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headerMap = new HashMap<String, String>();
                    headerMap.put("Content-Type", "application/json");
                    String JWT = SharedPreferencesEditor.getFromSharedPreferences(context, JWT_SP);
                    String authorisationValue = "Bearer " + JWT;
                    headerMap.put("Authorization", authorisationValue);
                    return headerMap;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(jsonObjectRequest);
        }
    }

}
