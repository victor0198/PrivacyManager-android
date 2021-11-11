package privacymanager.android.UI.encryptedFiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import privacymanager.android.R;


public class EFChoosen extends AppCompatActivity {

    private void setListeners(){
        private static final String TAG = EFChoosen.class.getSimpleName();
        private Intent intent;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ef_choosen);
            intent = getIntent();

            setListeners();

            //dispatchPopulateAccessibilityEvent();
        }

        findViewById(R.id.backNewEF).setOnClickListener(view -> {
            setResult(RESULT_OK, intent);
            finish();
        });


    }

}
