package privacymanager.android.UI.fileDecryption;

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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import privacymanager.android.R;
import privacymanager.android.UI.credentials.AddCredentialsUI;
import privacymanager.android.UI.credentials.CredentialsDetails;
import privacymanager.android.UI.credentials.CredentialsUI;
import privacymanager.android.UI.friendship.FriendsListUI;
import privacymanager.android.UI.friendship.ListViewItemCheckboxBaseAdapter;
import privacymanager.android.UI.friendship.ListViewItemDTO;
import privacymanager.android.UI.friendship.SearchUI;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.models.FilesModel;
import privacymanager.android.models.FriendshipModel;
import privacymanager.android.utils.database.DBFacade;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.AsymmetricCryptography;
import privacymanager.android.utils.security.Crypto;
import privacymanager.android.utils.security.CryptoUtils;


public class SelectDecryptUI extends AppCompatActivity {
    private static final String TAG = SelectDecryptUI.class.getSimpleName();
    private ListView listView;
    private Intent intent;
    private Context ctx;
    private List<String> encryptedFilesPaths;
    private DataBaseHelper dataBaseHelper;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        intent = getIntent();
        ctx = getApplicationContext();
        dataBaseHelper = new DataBaseHelper(ctx);

        encryptedFilesPaths = getMyFilesFromStorage();

        listView = (ListView) findViewById(R.id.decryptFileListView);

        dispatchPopulateAccessibilityEvent();

        setListeners();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setListeners() {
        findViewById(R.id.backDecryptSelectFileBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                String decryptFileName = encryptedFilesPaths.get(position);
                openDetails(decryptFileName);
            }
        });
    }


    private List<String> getMyFilesFromStorage() {
        String path = Environment.getExternalStorageDirectory().toString()+"/PrivacyManager/encrypted";
        Log.d("Files", "Path: " + path);

        //Creating a File object for directory
        File directoryPath = new File(path);
        //List of all files and directories
        String contents[] = directoryPath.list();

        Log.d("Files", "File paths to show:" + contents.toString());

        return Arrays.asList(contents.clone());
    }

    private void openDetails(String decryptFileName) {
        Intent intent = new Intent(this, DecryptionUI.class);
        intent.putExtra("fileName", decryptFileName);
        intent.putExtra("password", this.intent.getStringExtra("password"));
        intent.putExtra("JWT", this.intent.getStringExtra("JWT"));
        launchDetails.launch(intent);
    }

    public void dispatchPopulateAccessibilityEvent() {
        Log.d("Files", "Files to show:" + encryptedFilesPaths.toString());


        List<String> fileName = new ArrayList<String>();
        List<Integer> image1id = new ArrayList<Integer>();
        List<Integer> image2id = new ArrayList<Integer>();

        for (int i=0; i<encryptedFilesPaths.size(); i++){
            String[] pathBits = encryptedFilesPaths.get(i).split("/");
            String fileNameStr = pathBits[pathBits.length-1];
            fileName.add(fileNameStr);
            image1id.add(R.drawable.encrypted_files_logo);
            image2id.add(R.drawable.ic_arrow_forward);
        }

        Log.d("3 params", fileName.toString());
        Log.d("3 params", image1id.toString());
        Log.d("3 params", image2id.toString());

        SelectDecryptUI.CustomFilesList customFilesList = new SelectDecryptUI.CustomFilesList(this, fileName, image1id, image2id);
        this.listView.setAdapter(customFilesList);
    }

    public class CustomFilesList extends ArrayAdapter {
        private List<String> fileName;
        private List<Integer> image1id;
        private List<Integer> image2id;
        private Activity context;

        public CustomFilesList(Activity context, List<String> fileName, List<Integer> image1id, List<Integer> image2id) {
            super(context, R.layout.row_decrypt_file, fileName);
            this.context = context;
            this.fileName = fileName;
            this.image1id = image1id;
            this.image2id = image2id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row=convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if(convertView==null)
                row = inflater.inflate(R.layout.row_decrypt_file, null, true);

            TextView fileNameObj = (TextView) row.findViewById(R.id.decryptFileName);
            ImageView image1Obj = (ImageView) row.findViewById(R.id.imageViewFileLogo);
            ImageView image2Obj = (ImageView) row.findViewById(R.id.imageViewToDecrypt);

            String displayedFIleName = fileName.get(position);
            if (fileName.get(position).length() > 35) {
                displayedFIleName = fileName.get(position).substring(0, 30) + "...";
            }
            fileNameObj.setText(displayedFIleName);
            image1Obj.setImageResource(image1id.get(position));
            image2Obj.setImageResource(image2id.get(position));
            return  row;
        }
    }

    private final ActivityResultLauncher<Intent> launchDetails = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_FIRST_USER) {
                    setResult(RESULT_FIRST_USER, this.intent);
                    finish();
                }
            });
}
