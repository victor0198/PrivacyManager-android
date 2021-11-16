package privacymanager.android.UI.menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import privacymanager.android.R;
import privacymanager.android.UI.credentials.AddCredentialsUI;
import privacymanager.android.UI.credentials.CredentialsUI;
import privacymanager.android.UI.dialogs.ConfirmExit;
import privacymanager.android.UI.fileEncryption.FIleChooseUI;
import privacymanager.android.UI.friendship.FriendsListUI;
import privacymanager.android.UI.login.LoginUI;
import privacymanager.android.UI.notifications.NotificationsUI;
import privacymanager.android.utils.account.SharedPreferencesEditor;
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
            Intent intent = new Intent(this, CredentialsUI.class);
            intent.putExtra("JWT", JWT);
            intent.putExtra("password", password);
            launchFunctionality.launch(intent);
        });
        findViewById(R.id.encryptBtn).setOnClickListener(view->{
            Intent intent = new Intent(this, FIleChooseUI.class);
            launchFunctionality.launch(intent);
        });
        findViewById(R.id.friendsBtn).setOnClickListener(view->{
            Intent intent = new Intent(this, FriendsListUI.class);
            intent.putExtra("JWT", JWT);
            intent.putExtra("password", password);
            launchFunctionality.launch(intent);
        });

        findViewById(R.id.notificationsBtn).setOnClickListener(view -> {
            Intent intent = new Intent(this, NotificationsUI.class);
            intent.putExtra("JWT", JWT);
            intent.putExtra("password", password);
            launchFunctionality.launch(intent);
        });

        findViewById(R.id.serverConnection).setOnClickListener(view -> {
            Button btn = (Button) findViewById(R.id.serverConnection);
            Drawable imgSync = ctx.getResources().getDrawable(R.drawable.sync, getApplicationContext().getTheme());
            Drawable imgServer = ctx.getResources().getDrawable(R.drawable.server_icon, getApplicationContext().getTheme());
            btn.setCompoundDrawablesWithIntrinsicBounds(imgServer, null, imgSync, null);
            getJWT();
        });

        File file = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager");

        if(!file.exists()) {
            Log.d(TAG, "Directory does not exist, create it");
        }
        if (!file.mkdirs()) {
            file.mkdirs();
            Log.d(TAG, "Directory already created");
        }

        file = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/encrypted");
        if(!file.exists()) {
            Log.d(TAG, "Directory 'encrypted' does not exist, create it");
        }
        if (!file.mkdirs()) {
            file.mkdirs();
            Log.d(TAG, "Directory 'encrypted' already created");
        }

        file = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/decrypted");
        if(!file.exists()) {
            Log.d(TAG, "Directory 'decrypted' does not exist, create it");
        }
        if (!file.mkdirs()) {
            file.mkdirs();
            Log.d(TAG, "Directory 'decrypted' already created");
        }

        file = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/database");
        if(!file.exists()) {
            Log.d(TAG, "Directory 'database' does not exist, create it");
        }
        if (!file.mkdirs()) {
            file.mkdirs();
            Log.d(TAG, "Directory 'database' already created");
        }
    }

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

}
