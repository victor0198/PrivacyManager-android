package privacymanager.android.UI.fileEncryption;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import privacymanager.android.R;

public class FIleChoose extends AppCompatActivity {

        private FileChooserFragment fragment;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_encrypt_choose_file);

            FragmentManager fragmentManager = this.getSupportFragmentManager();
            this.fragment = (FileChooserFragment) fragmentManager.findFragmentById(R.id.fragmentContainerView);
            this.fragment.setContext(getApplicationContext());
            findViewById(R.id.infoBtn).setOnClickListener(view -> {
                showInfo();
            });

        }

        private void showInfo()  {
            String fullPath = this.fragment.getPath();
            if (fullPath != null) {
                String fileLocation = fullPath.substring(0,fullPath.lastIndexOf("/")+1);
                CheckBox deleteFileCheckbox = (CheckBox) findViewById(R.id.delete_original_check);
                boolean deleteOriginal = false;
                if (deleteFileCheckbox.isChecked()) deleteOriginal = true;
                //TODO: encrypt the file


                String toastMessage = "File path: " + fullPath + "\n\nOutput in: " + fileLocation + "\n\nDelete original: " + deleteOriginal;
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(this, "Choose a file", Toast.LENGTH_LONG).show();
            }

        }
    }