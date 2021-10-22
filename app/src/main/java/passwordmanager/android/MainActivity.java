package passwordmanager.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import passwordmanager.android.UI.login.LoginUI;
import passwordmanager.android.data.account.SharedPreferencesEditor;

public class MainActivity extends AppCompatActivity {

    static final int AUTH_ME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoginUI.class);
        startActivityForResult(intent, AUTH_ME);

        findViewById(R.id.deleteAccount).setOnClickListener((view)->{
            SharedPreferencesEditor.clear(this);
        });
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTH_ME) {
            if (resultCode == RESULT_OK) {
                Context ctx = getApplicationContext();
                String username = SharedPreferencesEditor.getFromSharedPreferences(ctx, "username");
                String id = SharedPreferencesEditor.getFromSharedPreferences(ctx, "id");

                TextView welcomingText = (TextView) findViewById(R.id.textViewWelcoming);
                welcomingText.setText("Hi ".
                        concat(username)
                        .concat(" ID:")
                        .concat(id)
                );
            }
        }
    }

}
