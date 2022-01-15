package privacymanager.android.UI.friendship;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
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
import privacymanager.android.UI.notifications.NotificationsUI;
import privacymanager.android.models.NotificationsModel;
import privacymanager.android.models.SearchUsersModel;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.database.DBFacade;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.AsymmetricCryptography;
import privacymanager.android.utils.security.CryptoUtils;

public class SearchUI extends AppCompatActivity {
    private static final String TAG = SearchUI.class.getSimpleName();
    private static final String RECEIVER_ID_PARAM = "receiverId";
    private static final String PUBLIC_KEY_PARAM = "publicKey";
    private String HOST_ADDRESS;
    private Context ctx;
    private Intent intent;
    private TextView searchUsername;

    private ListView listView;
    private List<SearchUsersModel> searchUsersModelList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_add);

        ctx = getApplicationContext();
        intent = getIntent();
        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");

        searchUsername = (TextView) findViewById(R.id.searchTextUsername);
        setListeners();

        listView = (ListView) findViewById(R.id.usersResultList);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setListeners() {
        findViewById(R.id.searchUsersBtn).setOnClickListener(view -> {
            if (!searchUsername.getText().toString().equals("")) {
                searchUsers(searchUsername.getText().toString());
            }else{
                Toast.makeText(ctx,
                        "Please type an username",
                        Toast.LENGTH_LONG)
                        .show();
            }
        });

        findViewById(R.id.backEncryptBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });
    }

    private void searchUsers(String usernameSubstring) {
        if (intent.getStringExtra("JWT").equals("")){
            Toast.makeText(ctx,
                    "Not connected to server",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Log.d(TAG,"Starting request building");
        JSONObject bodyParameters = new JSONObject();

        String url = HOST_ADDRESS.concat(Props.getAppProperty(ctx,"SEARCH_USER").concat(usernameSubstring));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, bodyParameters,
                response -> {
                    Log.d(TAG,"SEARCH RESULT:"+ response.toString());

                    JSONArray searchResultList = null;
                    try {
                        searchResultList = response.getJSONArray("usersFound");
                    } catch (JSONException e) {
                        Log.d(TAG,"ERROR: There is no usersFound field in response.");
                    }

                    String notifications = "";

                    try {
                        searchUsersModelList = new ArrayList<>();
                        for (int i = 0 ; i < searchResultList.length(); i++) {
                            SearchUsersModel searchUsersModel = new SearchUsersModel();
                            JSONObject obj = searchResultList.getJSONObject(i);
                            searchUsersModel.setUserId(obj.getLong("userId"));
                            searchUsersModel.setUserName(obj.getString("userName"));
                            searchUsersModelList.add(searchUsersModel);
                        }
                    }catch (JSONException e) {
                        Log.d(TAG,"ERROR: One of object fields is missing.");
                    }

                    dispatchPopulateSearchResult();
//                    setResult(RESULT_OK, this.intent);
//                    finish();
                },
                error -> {
                    Log.d(TAG,"ERROR:"+ error.toString());
                    Toast.makeText(ctx,
                            "Could not get users.",
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
        Log.d(TAG,"SEARCH request sent");
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(jsonObjectRequest);
    }

    public void dispatchPopulateSearchResult() {
        List<Long> userIds = new ArrayList<Long>();
        List<String> usernames = new ArrayList<String>();

        for (int i=0; i<searchUsersModelList.size(); i++){
            userIds.add(this.searchUsersModelList.get(i).getUserId());
            usernames.add(this.searchUsersModelList.get(i).getUserName());
        }

        SearchUI.CustomSearchList customSearchList = new SearchUI.CustomSearchList(this, intent, userIds, usernames);
        this.listView.setAdapter(customSearchList);
    }

    public static class CustomSearchList extends ArrayAdapter {
        private List<Long> userIds;
        private List<String> usernames;
        private Activity context;
        private Intent intent;

        public CustomSearchList(Activity context, Intent intent, List<Long> userIds, List<String> usernames) {
            super(context, R.layout.row_credintials, userIds);
            this.context = context;
            this.userIds = userIds;
            this.usernames = usernames;
            this.intent = intent;

            Log.i("userIds", userIds.toString());
            Log.i("usernames", usernames.toString());
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SearchUI.CustomSearchList.SearchResultsHolder holder = null;

            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if (convertView == null){
                row = inflater.inflate(R.layout.row_search, null, true); // false??
                holder = new SearchUI.CustomSearchList.SearchResultsHolder();
                holder.userId = userIds.get(position);
                holder.username = usernames.get(position);
                holder.searchResultUsername = (TextView) row.findViewById(R.id.searchResultUsernameTextView);
                holder.add = (Button) row.findViewById(R.id.addFriendBtn);
                row.setTag(holder);
            }else {
                holder = (SearchUI.CustomSearchList.SearchResultsHolder) row.getTag();
            }

            holder.searchResultUsername.setText(usernames.get(position));

            holder.add.setOnClickListener(view -> {
                Log.i("Add Button Clicked", userIds.get(position).toString() + usernames.get(position));
                sendFriendshipRequest(userIds.get(position));
            });

            return  row;
        }

        static class SearchResultsHolder {
            long userId;
            String username;
            TextView searchResultUsername;
            Button add;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        private void sendFriendshipRequest(Long futureFriendId) {
            PrivateKey privateKey;
            PublicKey publicKey;
            try{
                KeyPair kp = AsymmetricCryptography.generateRSAKeyPair();
                privateKey = kp.getPrivate();
                publicKey = kp.getPublic();
            }catch (Exception e){
                Toast.makeText(context,
                        "Could not initiate request.",
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }

            boolean privateKeySaved = saveFriendshihpPrivateKey(futureFriendId.intValue(), privateKey);

            if (privateKeySaved) {
                JSONObject bodyParameters = new JSONObject();

                byte[] byte_pubkey = publicKey.getEncoded();
                Log.d(TAG, "\nBYTE KEY::: " + Arrays.toString(byte_pubkey));
                String publicKeyString = Base64.getEncoder().encodeToString(byte_pubkey);
                Log.d(TAG, "\nSTRING KEY::" + publicKeyString);

                try {
                    bodyParameters.put(RECEIVER_ID_PARAM, futureFriendId);
                    bodyParameters.put(PUBLIC_KEY_PARAM, publicKeyString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String url = Props.getAppProperty(context,"HOST_ADDRESS").concat(Props.getAppProperty(context,"ADD_FRIEND"));
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                        response -> {
                            Toast.makeText(context,
                                    "Friendship request sent.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            context.setResult(RESULT_OK, this.intent);
                            context.finish();
                        },
                        error -> {
                            Log.d(TAG,"ERROR:"+ error.toString());
                            Toast.makeText(context,
                                    "Could not send friendship request.",
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
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                requestQueue.add(jsonObjectRequest);
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        private boolean saveFriendshihpPrivateKey(Integer futureFriendId, PrivateKey privateKey) {
            DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
            DBFacade dbFacade = new DBFacade(dataBaseHelper, this.intent.getStringExtra("password"));

            byte[] byte_pubkey = privateKey.getEncoded();
            Log.d(TAG, "\nBYTE KEY::: " + Arrays.toString(byte_pubkey));
            String privateKeyString = Base64.getEncoder().encodeToString(byte_pubkey);
            Log.d(TAG, "\nSTRING KEY::" + privateKeyString);
            dbFacade.addFriendshipReqest(context, futureFriendId, privateKeyString);

            return true;
        }
    }
}
