package privacymanager.android.UI.menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IntRange;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import privacymanager.android.R;
import privacymanager.android.UI.credentials.AddCredentialsUI;
import privacymanager.android.UI.credentials.CredentialsUI;
import privacymanager.android.UI.dialogs.ConfirmExit;
import privacymanager.android.UI.fileDecryption.SelectDecryptUI;
import privacymanager.android.UI.fileEncryption.FIleChooseUI;
import privacymanager.android.UI.fileEncryption.FileChooserFragment;
import privacymanager.android.UI.friendship.FriendsListUI;
import privacymanager.android.UI.login.LoginUI;
import privacymanager.android.UI.notifications.NotificationsUI;
import privacymanager.android.utils.account.SharedPreferencesEditor;
import privacymanager.android.utils.file.BuildAppDirs;
import privacymanager.android.utils.props.Props;

public class MenuUI extends AppCompatActivity {

    private static final String TAG = MenuUI.class.getSimpleName();
    private static final String USERNAME_SP = "username";
    private String HOST_ADDRESS;
    private String JWT;
    private String password;
    private Context ctx;
    private Intent intent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = getApplicationContext();
        intent = getIntent();
        HOST_ADDRESS = Props.getAppProperty(ctx,"HOST_ADDRESS");

        authenticate();

        setListeners();
        BuildAppDirs.build();
    }

    @Override
    public void onBackPressed() {
        ConfirmExit confirmExit = new ConfirmExit();
        FragmentManager fragmentManager = getSupportFragmentManager();
        confirmExit.show(fragmentManager, "exitDialog");
    }

    private void authenticate() {
        Intent intent = new Intent(this, LoginUI.class);
        launchAuthentication.launch(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setListeners() {
        findViewById(R.id.credentialsBtn).setOnClickListener(view->{
            checkPermissions();
            BuildAppDirs.build();
            Intent intent = new Intent(this, CredentialsUI.class);
            intent.putExtra("JWT", JWT);
            intent.putExtra("password", password);
            launchFunctionality.launch(intent);
        });
        findViewById(R.id.encryptBtn).setOnClickListener(view->{
            checkPermissions();
            BuildAppDirs.build();
            Intent intent = new Intent(this, FIleChooseUI.class);
            intent.putExtra("password", password);
            launchFunctionality.launch(intent);
        });
        findViewById(R.id.friendsBtn).setOnClickListener(view->{
            checkPermissions();
            BuildAppDirs.build();
            Intent intent = new Intent(this, FriendsListUI.class);
            intent.putExtra("JWT", JWT);
            intent.putExtra("password", password);
            launchFriendsList.launch(intent);
        });

        findViewById(R.id.notificationsBtn).setOnClickListener(view -> {
            checkPermissions();
            BuildAppDirs.build();
            Intent intent = new Intent(this, NotificationsUI.class);
            intent.putExtra("JWT", JWT);
            intent.putExtra("password", password);
            launchFunctionality.launch(intent);
        });

        findViewById(R.id.mainEncryptedBtn).setOnClickListener(view -> {
            checkPermissions();
            BuildAppDirs.build();
            Intent intent = new Intent(this, SelectDecryptUI.class);
            intent.putExtra("JWT", JWT);
            intent.putExtra("password", password);
            launchFunctionality.launch(intent);
        });

        findViewById(R.id.serverConnection).setOnClickListener(view -> {
            checkPermissions();
            BuildAppDirs.build();
            getJWT();
        });

        findViewById(R.id.clearSessionBtn).setOnClickListener(view -> {
            checkPermissions();
            BuildAppDirs.build();
            String path = Environment.getExternalStorageDirectory().toString()+"/PrivacyManager/decrypted";
            Log.d("Files", "Path: " + path);
            List<String> encryptedFilesPaths = new ArrayList<>();

            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files == null){
                return;
            }
            Log.d("Files", "Size: "+ files.length + "|" + files.toString());

            for (File file : files) {
                encryptedFilesPaths.add(file.getName());
                Log.d("Files", "Deleting file:" + file.getName());
                try {
                    boolean resultStatus = file.delete();
                    if (resultStatus){
                        Log.d("Deleted file:", file.getName());
                    }
                } catch (Exception e) {
                    Log.d("Could not delete file:", file.getName());
                }
            }

            ConfirmExit confirmExit = new ConfirmExit();
            FragmentManager fragmentManager = getSupportFragmentManager();
            confirmExit.show(fragmentManager, "exitDialog");
        });

        askPermission();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void askPermission() {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            // Check if we have Call permission
            int permission = ActivityCompat.checkSelfPermission(MenuUI.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                mWritePermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private ActivityResultLauncher<String> mWritePermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if(result) {
                    Log.e(FileChooserFragment.class.toString(), "onActivityResult: Permission granted.");
                } else {
                    Toast.makeText(this, "Don't have permission to save file.", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getJWT() {
        // authentication url
        String url = HOST_ADDRESS.concat(Props.getAppProperty(ctx,"AUTHENTICATION"));

        // authentication payload
        JSONObject bodyParameters = new JSONObject();
        try {
            bodyParameters.put(USERNAME_SP, SharedPreferencesEditor.getFromSharedPreferences(ctx, USERNAME_SP));
            bodyParameters.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // build the request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, bodyParameters,
                response -> {
                    String jwt;
                    try {
                        jwt = response.get("token").toString();
                        Log.d(AddCredentialsUI.class.toString(), "AuthLogin() :: Got JWT: " + jwt);
                        Button btn = (Button) findViewById(R.id.serverConnection);
                        btn.setVisibility(View.INVISIBLE);
                        ImageView connImg = (ImageView) findViewById(R.id.imageConnectionStatus);
                        connImg.setVisibility(View.VISIBLE);
                        TextView tvs = (TextView) findViewById(R.id.textView14Mode);
                        tvs.setText("online");
                        tvs.setTextColor(Color.argb(255, 215, 92, 35));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    this.JWT = jwt;

                    Button btn = (Button) findViewById(R.id.serverConnection);
                    Drawable imgSync = ctx.getResources().getDrawable(R.drawable.done, getApplicationContext().getTheme());
                    Drawable imgServer = ctx.getResources().getDrawable(R.drawable.server_icon, getApplicationContext().getTheme());
                    btn.setCompoundDrawablesWithIntrinsicBounds(imgServer, null, imgSync, null);

                    Toast.makeText(ctx,
                            "Connected",
                            Toast.LENGTH_LONG)
                            .show();
                },
                error -> {
                    Button btn = (Button) findViewById(R.id.serverConnection);
                    Drawable imgSync = ctx.getResources().getDrawable(R.drawable.not, getApplicationContext().getTheme());
                    Drawable imgServer = ctx.getResources().getDrawable(R.drawable.server_icon, getApplicationContext().getTheme());
                    btn.setCompoundDrawablesWithIntrinsicBounds(imgServer, null, imgSync, null);

                    if (error.networkResponse != null && error.networkResponse.statusCode == 401){
                        Toast.makeText(ctx,
                                "This account is not registered.",
                                Toast.LENGTH_LONG)
                                .show();
                    }else{
                        Toast.makeText(ctx,
                                "Server error.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }
        );

        // make request
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(jsonObjectRequest);

    }

    private final ActivityResultLauncher<Intent> launchAuthentication = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Context ctx = getApplicationContext();
                    String username = SharedPreferencesEditor.getFromSharedPreferences(ctx, USERNAME_SP);

                    TextView welcomingText = (TextView) findViewById(R.id.textViewWelcoming);
                    welcomingText.setText(username);

                    if (result.getData().getStringExtra("JWT") == null){
                        Log.d("LoginUI result ::", "JWT it is null");
                    }else{
                        this.JWT = result.getData().getStringExtra("JWT");
                    }

                    if (result.getData().getStringExtra("password") == null){
                        Log.d("LoginUI result ::", "password it is null");
                    }else{
                        this.password = result.getData().getStringExtra("password");
                    }
                }else{
                    Log.d(TAG, "StartAuthForResult() :: result -> skipped");
                    this.finish();
                    System.exit(0);
                }
            });

    private final ActivityResultLauncher<Intent> launchFriendsList = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    if (result.getData().getStringExtra("JWT") == null){
                        Log.d("Activity result ::", "JWT it is null");
                    }else {
                        this.JWT = result.getData().getStringExtra("JWT");
                    }

                    Log.d(TAG, "StartActivityForResult() :: result -> Back to menu");
                }
            });

    private final ActivityResultLauncher<Intent> launchFunctionality = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    if (result.getData().getStringExtra("JWT") == null){
                        Log.d("Activity result ::", "JWT it is null");
                    }else {
                        this.JWT = result.getData().getStringExtra("JWT");
                    }

                    Log.d(TAG, "StartActivityForResult() :: result -> Back to menu");
                }
            });

    public void checkPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            // Check if we have Call permission
            int permission = ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                mPermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if(result) {
                    Log.e(FileChooserFragment.class.toString(), "onActivityResult: Permission granted");
                } else {
                    Toast.makeText(this, "Don't have permission to save file.", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            });
}
