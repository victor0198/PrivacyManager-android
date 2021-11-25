package privacymanager.android.UI.fileSelection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import privacymanager.android.R;
import privacymanager.android.models.FilesModel;
import privacymanager.android.models.FriendshipModel;
import privacymanager.android.utils.database.DBFacade;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.Crypto;

public class SelectFileUI extends AppCompatActivity {
    private static final String TAG = SelectFileUI.class.getSimpleName();
    private ListView listView;
    private Intent intent;
    private Context ctx;
    private List<FilesModel> encryptedFiles;
    private DataBaseHelper dataBaseHelper;
    private List<FriendshipModel> friendshipModelList;
    List<Integer> keyId;
    List<String> fileName;
    List<String> fileMD5;
    List<String> filePassword;
    private String HOST_ADDRESS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_share_select);
        intent = getIntent();
        ctx = getApplicationContext();

        dataBaseHelper = new DataBaseHelper(SelectFileUI.this);
        getMyFilesFromDB();

        listView = (ListView) findViewById(R.id.shareFileKeyListView);

        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");

        dispatchPopulateAccessibilityEvent();//

        getFriendsListFromIntent();

        setListeners();
    }

    private void getFriendsListFromIntent() {
        String friendsNumber = intent.getStringExtra("friendsNumber");
        Long n = Long.parseLong(friendsNumber);
        friendshipModelList = new ArrayList<FriendshipModel>();
        for(int i=0;i<n;i++)
        {
            FriendshipModel friendshipModel = new FriendshipModel();
            friendshipModel.setFriendshipId(
                    Long.parseLong(
                            intent.getStringExtra(
                                    "friendshipId"+String.valueOf(i)
                            )
                    )
            );
            friendshipModel.setFriendId(
                    Long.parseLong(
                            intent.getStringExtra(
                                    "friendId"+String.valueOf(i)
                            )
                    )
            );
            friendshipModelList.add(friendshipModel);
        }
    }

    private void setListeners() {
        findViewById(R.id.backFileShareBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Log.d("TO SHARE", friendshipModelList.toString() + "|"
                                + keyId.get(position) + "|"
                                + fileName.get(position) + "|"
                                + fileMD5.get(position) + "|"
                                + filePassword.get(position) + "|");
                sendKeyToFriends(keyId.get(position), fileName.get(position), fileMD5.get(position), filePassword.get(position));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendKeyToFriends(Integer keyId, String fileName, String fileMD5, String filePassword) {
        if (intent.getStringExtra("JWT").equals("")){
            Toast.makeText(ctx,
                    "Not connected to server",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        for(int i=0;i<friendshipModelList.size();i++){
            DBFacade dbFacade = new DBFacade(dataBaseHelper, intent.getStringExtra("password"));
            String symmetricKey = dbFacade.getSymmetricKeyByFriendshipId((int) friendshipModelList.get(i).getFriendshipId());
            Log.d("SENDING", "Got symmetric key" + symmetricKey);
            String url = HOST_ADDRESS.concat(Props.getAppProperty(ctx,"SHARE_KEY"));

            String encryptedPassword;
            try{
                encryptedPassword = Crypto.encrypt(filePassword.getBytes(StandardCharsets.UTF_8),
                        symmetricKey);
            }catch (Exception e){
                Toast.makeText(ctx,
                        "Could not prepare key for sharing.",
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }

            JSONObject bodyParameters = new JSONObject();
            try {
                bodyParameters.put("friendshipId", friendshipModelList.get(i).getFriendshipId());
                bodyParameters.put("user2Id", friendshipModelList.get(i).getFriendId());
                bodyParameters.put("keyId", keyId);
                bodyParameters.put("fileKey", encryptedPassword);
                bodyParameters.put("fileChecksum", fileMD5);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("SENDING", "Sharing key" + bodyParameters.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                    response -> {
                    },
                    error -> {
                        Toast.makeText(ctx,
                                "Could not send key",
                                Toast.LENGTH_LONG)
                                .show();
                    }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headerMap = new HashMap<String, String>();
                    headerMap.put("Content-Type", "application/json");
                    String authorisationValue = "Bearer " + intent.getStringExtra("JWT");;
                    headerMap.put("Authorization", authorisationValue);
                    return headerMap;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(ctx);
            requestQueue.add(jsonObjectRequest);
        }
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }

    private void getMyFilesFromDB() {
        DBFacade dbFacade = new DBFacade(dataBaseHelper, intent.getStringExtra("password"));
        this.encryptedFiles = dbFacade.getEncryptedFiles();
    }


    public void dispatchPopulateAccessibilityEvent() {
        Log.d("Files", "Files to show:" + encryptedFiles.toString());

        keyId = new ArrayList<Integer>();
        fileName = new ArrayList<String>();
        fileMD5 = new ArrayList<String>();
        filePassword = new ArrayList<String>();

        for (int i=0; i<encryptedFiles.size(); i++){
            keyId.add(encryptedFiles.get(i).getKeyId());
            fileName.add(encryptedFiles.get(i).getFileName());
            fileMD5.add(encryptedFiles.get(i).getFileMD5());
            filePassword.add(encryptedFiles.get(i).getFilePassword());
        }

        Log.d("3 params", keyId.toString());
        Log.d("3 params", fileName.toString());
        Log.d("3 params", fileMD5.toString());
        Log.d("3 params", filePassword.toString());

        SelectFileUI.CustomFileShareList customFilesList = new SelectFileUI.CustomFileShareList(this, keyId, fileName, fileMD5, filePassword);
        this.listView.setAdapter(customFilesList);
    }

    public class CustomFileShareList extends ArrayAdapter {
        private List<Integer> keyIdAdapter;
        private List<String> fileNameAdapter;
        private List<String> fileMD5Adapter;
        private List<String> filePasswordAdapter;
        private Activity contextAdapter;

        public CustomFileShareList(Activity context, List<Integer> keyId, List<String> fileName, List<String> fileMD5, List<String> filePassword) {
            super(context, R.layout.row_share_file, keyId);//
            this.contextAdapter = context;
            this.keyIdAdapter = keyId;
            this.fileNameAdapter = fileName;
            this.fileMD5Adapter = fileMD5;
            this.filePasswordAdapter = filePassword;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row=convertView;
            LayoutInflater inflater = contextAdapter.getLayoutInflater();
            if(convertView==null)
                row = inflater.inflate(R.layout.row_share_file, null, true);
            TextView fileNameObj = (TextView) row.findViewById(R.id.shareFileName);
            ImageView image1Obj = (ImageView) row.findViewById(R.id.imageViewFileLogoShare);

            String displayedFIleName = fileNameAdapter.get(position);
            if (fileName.get(position).length() > 38) {
                displayedFIleName = fileName.get(position).substring(0, 33) + "...";
            }
            fileNameObj.setText(displayedFIleName);
            image1Obj.setImageResource(R.drawable.encrypted_files_logo);
            return  row;
        }
    }

}
