package passwordmanager.android.UI.credentials;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import passwordmanager.android.R;

public class CredentialsUI extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);
    }
}