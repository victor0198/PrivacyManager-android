package privacymanager.android.UI.fileEncryption;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import privacymanager.android.R;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.security.FileCrypto;

public class FIleChooseUI extends AppCompatActivity {

    private FileChooserFragment fragment;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt_choose_file);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.fragment = (FileChooserFragment) fragmentManager.findFragmentById(R.id.fragmentContainerView);
        this.fragment.setContext(getApplicationContext());
        findViewById(R.id.infoBtn).setOnClickListener(view -> {
            askPermissionAndShowInfo();
        });

    }

    private void askPermissionAndShowInfo() {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            // Check if we have Call permission
            int permission = ActivityCompat.checkSelfPermission(FIleChooseUI.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                mPermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
        try {
            this.showInfo();
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e("askPermissionAndShowInfo: ", e.toString());
        }
    }

    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if(result) {
                    Log.e(FileChooserFragment.class.toString(), "onActivityResult: Permission granted");
                } else {
                    Log.e(FileChooserFragment.class.toString(), "onActivityResult: Permission denied");
                }
            });

    private void showInfo() throws IOException, NoSuchAlgorithmException {
        String fullPath = this.fragment.getPath();
        if (fullPath != null) {
            String fileLocation = fullPath.substring(0, fullPath.lastIndexOf("/") + 1);
            CheckBox deleteFileCheckbox = (CheckBox) findViewById(R.id.delete_original_check);
            boolean deleteOriginal = false;
            if (deleteFileCheckbox.isChecked()) deleteOriginal = true;

            FileCrypto cryptoUtil = new FileCrypto();
            DataBaseHelper dbHelper = new DataBaseHelper(FIleChooseUI.this);
            Boolean status = cryptoUtil.encryptFileAddSaveKey(dbHelper, ctx, fullPath, fullPath, deleteOriginal);

            String toastMessage = "File path: " + fullPath + "\n\nOutput in: " + fileLocation + "\n\nDelete original: " + deleteOriginal;
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Choose a file", Toast.LENGTH_LONG).show();
        }

    }
}