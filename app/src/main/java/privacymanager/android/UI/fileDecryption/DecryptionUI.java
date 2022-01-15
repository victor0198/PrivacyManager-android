package privacymanager.android.UI.fileDecryption;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import privacymanager.android.R;
import privacymanager.android.UI.dialogs.ConfirmCredentialsDelete;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.models.FilesModel;
import privacymanager.android.utils.database.DBFacade;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.props.Props;
import privacymanager.android.utils.security.CheckSumMD5;
import privacymanager.android.utils.security.Crypto;
import privacymanager.android.utils.security.FileSecurityUtils;

public class DecryptionUI  extends AppCompatActivity {
    private Intent intent;
    private Context ctx;
    private String fileName;
    private DataBaseHelper dataBaseHelper;
    private TextView status;
    private TextView keyStatus;
    private TextView fileNameDetails;
    private Button decryptButton;
    private FilesModel fileData;
    private String TAG;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_decryption);
        intent = getIntent();
        ctx = getApplicationContext();
        dataBaseHelper = new DataBaseHelper(ctx);
        TAG = DecryptionUI.class.toString();

        fileName = intent.getStringExtra("fileName");
        status = (TextView) findViewById(R.id.fileStatus);
        keyStatus = (TextView) findViewById(R.id.textViewKeyInfo);
        fileNameDetails = (TextView) findViewById(R.id.fileNameDetails);
        String fileNameDisplayed = fileName;
        if (fileNameDisplayed.length() > 37) {
            fileNameDisplayed = fileNameDisplayed.substring(0, 35) + "...";
        }
        fileNameDetails.setText(fileNameDisplayed);

        decryptButton = (Button) findViewById(R.id.decryptButton);

        setListeners();

        try {
            checkFileAndLoadData(false);
        } catch (IOException | NoSuchAlgorithmException e) {
            Toast.makeText(this.ctx,
                    "Could not get file details.",
                    Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getNewKeys() {
        if (intent.getStringExtra("JWT").equals("")){
            return;
        }

        String url = Props.getAppProperty(ctx,"HOST_ADDRESS").concat(Props.getAppProperty(ctx,"KEYS_FOR_ME"));

        JSONObject bodyParameters = new JSONObject();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, bodyParameters,
                response -> {
                    Log.d(SelectDecryptUI.class.toString(), "Getting new keys");
                    Log.d(SelectDecryptUI.class.toString(), "Response:" + response.toString());
                    JSONArray responsesList = null;
                    try {
                        responsesList = response.getJSONArray("keysForMe");
                    } catch (JSONException e) {
                        Log.d(TAG,"ERROR: There is no 'keysForMe' field in response object.");
                        return;
                    }

                    try {
                        for (int i = 0 ; i < responsesList.length(); i++) {
                            JSONObject obj = responsesList.getJSONObject(i);

                            Integer ownerId = obj.getInt("ownerId");
                            if (ownerId == 0){
                                break;
                            }

                            DBFacade dbFacade = new DBFacade(dataBaseHelper, intent.getStringExtra("password"));
                            Integer friendshipId = dbFacade.getFriendshipIdByFriendId(ownerId);

                            String symmetricKey = dbFacade.getSymmetricKeyByFriendshipId(friendshipId);

                            String filePasswordEncrypted = obj.getString("fileKey");
                            String filePassword = "";

                            filePassword = Crypto.decrypt(filePasswordEncrypted, symmetricKey);

                            String fileMD5 = obj.getString("fileChecksum");

                            boolean savedNewKey = dbFacade.addEncryptedFile(ctx, "", fileMD5, filePassword);
                            if (savedNewKey == false){
                                Toast.makeText(ctx,
                                        "Could not save a new key.",
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

                    try {
                        checkFileAndLoadData(true);
                    } catch (IOException | NoSuchAlgorithmException e) {
                        Toast.makeText(this.ctx,
                                "Could not get file details.",
                                Toast.LENGTH_LONG)
                                .show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(ctx,
                            "Could not get keys from friends.",
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
    private void setListeners() {
        findViewById(R.id.backDecryptFileBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });

        findViewById(R.id.decryptButton).setOnClickListener(view -> {
            Log.d(DecryptionUI.class.toString(), "Start  decrypting:" + fileName);

            String decryptedFileName = fileName.substring(0, fileName.lastIndexOf("."));

            try {
                FileSecurityUtils.decryptFile(ctx,
                        Environment.getExternalStorageDirectory()+"/PrivacyManager/encrypted/" + fileName,
                        Environment.getExternalStorageDirectory()+"/PrivacyManager/decrypted/" + decryptedFileName,
                        fileData.getFilePassword()
                        );
            } catch (GeneralSecurityException | IOException e) {
                Toast.makeText(this.ctx,
                        "Could not decrypt file!",
                        Toast.LENGTH_LONG)
                        .show();
            }

            Toast.makeText(this.ctx,
                    "Decrypted",
                    Toast.LENGTH_LONG)
                    .show();
            setResult(RESULT_FIRST_USER, this.intent);
            finish();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkFileAndLoadData(boolean checkedOnServer) throws IOException, NoSuchAlgorithmException {
        File inFile = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/encrypted/" + fileName);
        if(inFile.exists() && !inFile.isDirectory()) {
            byte[] encData;

            FileInputStream inStream = new FileInputStream(inFile);

            int blockSize = 8;

            //Figure out how many bytes are padded
            int paddedCount = blockSize - ((int) inFile.length() % blockSize);

            //Figure out full size including padding
            int padded = (int) inFile.length() + paddedCount;

            encData = new byte[padded];

            inStream.read(encData);

            inStream.close();

            MessageDigest digest = MessageDigest.getInstance("MD5");
            String md5HashEncryptedFile = CheckSumMD5.checksum(digest, inFile);

            DBFacade dbFacade = new DBFacade(dataBaseHelper, intent.getStringExtra("password"));
            fileData = dbFacade.getEncryptedFilesKeys(md5HashEncryptedFile);

            Log.d(DecryptionUI.class.toString(), fileData.toString());

            if (fileData.getFilePassword() == null){
                if (intent.getStringExtra("JWT").equals("")){
                    keyStatus.setText("unavailable");
                    Toast.makeText(this.ctx,
                            "Try connecting to the server",
                            Toast.LENGTH_LONG)
                            .show();
                }else{
                    if (!checkedOnServer)
                        getNewKeys();
                }
                if (checkedOnServer) {
                    status.setVisibility(View.VISIBLE);
                    keyStatus.setText("unavailable");
                }
            }else{
                decryptButton.setEnabled(true);
                keyStatus.setText("available");
                keyStatus.setTextColor(Color.argb(255, 215, 92, 35));
            }
        }
    }
}
