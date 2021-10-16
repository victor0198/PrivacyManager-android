package passwordmanager.android;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import passwordmanager.android.data.login.Login;
import passwordmanager.android.UI.login.LoginUI;

public class MainActivity extends AppCompatActivity {

    static final int AUTH_ME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void authenticateActivity(View v){
        Intent i = new Intent(this, LoginUI.class);
        startActivityForResult(i, AUTH_ME);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_ME) {
            if (resultCode == RESULT_OK) {
                String username = data.getStringExtra("username");
                int id = data.getIntExtra("id", -1);
                TextView welcomingText = (TextView) findViewById(R.id.textViewWelcoming);
                welcomingText.setText(getString(R.string.welcoming).
                        concat(username).
                        concat("! ID:").
                        concat(String.valueOf(id))
                );
            }
        }
    }

}
