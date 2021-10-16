package passwordmanager.android.UI.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import passwordmanager.android.R;
import passwordmanager.android.data.login.Login;

public class LoginUI extends AppCompatActivity {
    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        i = getIntent();
    }

    public void authenticate(View v){
        EditText name = (EditText) findViewById(R.id.editTextPersonName);
        EditText pass = (EditText) findViewById(R.id.editTexPassword);

        int result = Login.authLogic(
                name.getText().toString(),
                pass.getText().toString()
        );

        if (result >= 0){
            passAuth();
            i.putExtra("username", name.getText().toString());
            i.putExtra("id", result);
            setResult(RESULT_OK, this.i);
            finish();
        }else{
            failAuth();
        }
    }

    public void failAuth(){
        TextView errMessage = findViewById(R.id.textViewFail);
        errMessage.setVisibility(View.VISIBLE);
    }

    public void passAuth(){
        TextView errMessage = findViewById(R.id.textViewFail);
        errMessage.setVisibility(View.INVISIBLE);
    }


}
