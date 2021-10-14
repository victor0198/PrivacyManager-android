package passwordmanager.android.data.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import passwordmanager.android.R;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void authenticate(View v){
        Intent i = getIntent();
        EditText name = (EditText) findViewById(R.id.editTextPersonName);
        i.putExtra("NICKNAME", name.getText().toString());

        EditText pass = (EditText) findViewById(R.id.editTexPassword);

        if (name.getText().toString().equals("me") && pass.getText().toString().equals("pass")){
            passAuth();
            setResult(RESULT_OK, i);
            finish();
        }
        else{
            failAuth();
        }
    }

    public void failAuth(){
        TextView txtView = (TextView)findViewById(R.id.textViewFail);
        txtView.setVisibility(View.VISIBLE);
    }

    public void passAuth(){
        TextView txtView = (TextView)findViewById(R.id.textViewFail);
        txtView.setVisibility(View.INVISIBLE);
    }
}