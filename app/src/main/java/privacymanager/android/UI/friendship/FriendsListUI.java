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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import privacymanager.android.R;
import privacymanager.android.UI.credentials.CredentialsUI;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.models.FriendshipModel;
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
    private DataBaseHelper dataBaseHelper;
    ListView listViewWithCheckbox;
    ListViewItemCheckboxBaseAdapter listViewDataAdapter;
    List<ListViewItemDTO> initItemList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        intent = getIntent();

        ctx = getApplicationContext();
        dataBaseHelper = new DataBaseHelper(ctx);

        setListeners();

        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");
        getNewFriendships();

        setTitle("dev2qa.com - Android ListView With CheckBox");
        // Get listview checkbox.
        listViewWithCheckbox = (ListView) findViewById(R.id.friendsListView);
        // Initiate listview data.
        initItemList = this.getInitViewItemDtoList();
        // Create a custom list view adapter with checkbox control.
        listViewDataAdapter = new ListViewItemCheckboxBaseAdapter(getApplicationContext(), initItemList);
        listViewDataAdapter.notifyDataSetChanged();
        // Set data adapter to list view.
        listViewWithCheckbox.setAdapter(listViewDataAdapter);
        // When list view item is clicked.
        listViewWithCheckbox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long l) {
                // Get user selected item.
                Object itemObject = adapterView.getAdapter().getItem(itemIndex);
                // Translate the selected item to DTO object.
                ListViewItemDTO itemDto = (ListViewItemDTO) itemObject;
                // Get the checkbox.
                CheckBox itemCheckbox = (CheckBox) view.findViewById(R.id.checkBox);
                // Reverse the checkbox and clicked item check state.
                if (itemDto.isChecked()) {
                    itemCheckbox.setChecked(false);
                    itemDto.setChecked(false);
                } else {
                    itemCheckbox.setChecked(true);
                    itemDto.setChecked(true);
                }
                //Toast.makeText(getApplicationContext(), "select item text : " + itemDto.getItemText(), Toast.LENGTH_SHORT).show();
            }
        });

        Button selectAllButton = (Button)findViewById(R.id.toFiles);
        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = initItemList.size();
                List<ListViewItemDTO> dtoChecked = new ArrayList<>();

                for(int i=0;i<size;i++)
                {

                    ListViewItemDTO dto = initItemList.get(i);
                    if(dto.isChecked()){
                        dtoChecked.add(dto);
                    }
                }

                Log.d("Selected friends:", dtoChecked.toString());

                listViewDataAdapter.notifyDataSetChanged();
            }
        });
    }

    // Return an initialize list of ListViewItemDTO.
    private List<ListViewItemDTO> getInitViewItemDtoList()
    {
        List<FriendshipModel> friendshipsList = dataBaseHelper.getFriendshipsList();

        List<ListViewItemDTO> ret = new ArrayList<ListViewItemDTO>();
        int length = friendshipsList.size();
        for(int i=0;i<length;i++)
        {
            ListViewItemDTO dto = new ListViewItemDTO();
            dto.setFriendshipId(friendshipsList.get(i).getFriendshipId());
            dto.setFriendId(friendshipsList.get(i).getFriendId());
            dto.setSymmetricKey(friendshipsList.get(i).getSymmetricKey());

            dto.setChecked(false);
            dto.setItemText(String.valueOf(friendshipsList.get(i).getFriendId()));
            ret.add(dto);
        }
        return ret;
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
        if (intent.getStringExtra("JWT").equals("")){
            Toast.makeText(ctx,
                    "Not connected to server",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
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
                            int savedFiendship = dataBaseHelper.saveFriendshipKey(ctx, friendshipId, requestAccepter, symmetricKeyString);
                            if (savedFiendship == -1){
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

                    List<ListViewItemDTO> newFriendsList = getInitViewItemDtoList();
                    initItemList = newFriendsList;
                    listViewDataAdapter.changeItems(newFriendsList);
                    listViewDataAdapter.notifyDataSetChanged();

                },
                error -> {
                    Log.d(TAG,"ERROR:" + error.toString());

                    if(error.toString().indexOf("JSONException: End of input at character 0")>0){
                        return;
                    }
                    Toast.makeText(ctx,
                            "Could not update the friend list.",
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




/*
public class FriendsListUI extends AppCompatActivity {
    private static final String TAG = FriendsListUI.class.getSimpleName();
    private Intent intent;
    private Context ctx;
    private String HOST_ADDRESS;
    private ListView listViewFriends;
    private List<FriendshipModel> friendsList;
    private DataBaseHelper dataBaseHelper;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        intent = getIntent();

        listViewFriends = (ListView) findViewById(R.id.friendsListView);

        dataBaseHelper = new DataBaseHelper(ctx);
        getFriendsList();

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

        this.listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id){
                FriendshipModel friendToShareWith = friendsList.get(position);
                addOrRemoveFromSelection(friendToShareWith);
            }
        });
    }

    private void getFriendsList() {
        this.friendsList = dataBaseHelper.getFriendshipsList();

        List<Integer> friendshipId = new ArrayList<Integer>();
        List<Integer> friendId = new ArrayList<Integer>();
        List<String> symmetricKey = new ArrayList<String>();

        for (int i=0; i<friendsList.size(); i++){
            friendshipId.add((int) this.friendsList.get(i).getFriendshipId());
            friendId.add((int) this.friendsList.get(i).getFriendId());
            symmetricKey.add(this.friendsList.get(i).getSymmetricKey());
        }

        FriendsListUI.CustomFriendshipsList customCountryList = new FriendsListUI.CustomFriendshipsList(this, friendshipId, friendId, symmetricKey);
        this.listViewFriends.setAdapter(customCountryList);
    }

    private void addOrRemoveFromSelection(FriendshipModel friendToShareWith) {
        Log.d("--->", friendToShareWith.toString());
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

//    public class CustomFriendshipsList extends ArrayAdapter {
//        private List<Integer> friendshipId;
//        private List<Integer> friendId;
//        private List<String> symmetricKey;
//        private Context context;
//
//        public CustomFriendshipsList(Activity context, List<Integer> friendshipId, List<Integer> friendId, List<String> symmetricKey) {
//            super(context, R.layout.row_friends, friendshipId);
//            this.context = context;
//            this.friendshipId = friendshipId;
//            this.friendId = friendId;
//            this.symmetricKey = symmetricKey;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View row = convertView;
//            LayoutInflater inflater = context.getLayoutInflater();
//            if (convertView == null)
//                row = inflater.inflate(R.layout.row_credintials, null, true);
//            TextView textViewCountry = (TextView) row.findViewById(R.id.textViewCredintialName);
//            TextView textViewCapital = (TextView) row.findViewById(R.id.textViewCredintialEmail);
//            ImageView imageFlag = (ImageView) row.findViewById(R.id.logoCredintial);
//
//            textViewCountry.setText(credintialServices.get(position));
//            textViewCapital.setText(credintialNames.get(position));
//            imageFlag.setImageResource(imageid.get(position));
//            return row;
//        }
//    }

    private final ActivityResultLauncher<Intent> launchAddFriend = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to friends list.");
                }
            });
}
*/