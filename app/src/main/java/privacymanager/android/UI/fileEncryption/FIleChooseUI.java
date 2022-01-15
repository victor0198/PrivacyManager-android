package privacymanager.android.UI.fileEncryption;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import privacymanager.android.R;
import privacymanager.android.utils.database.DataBaseHelper;
import privacymanager.android.utils.security.FileCrypto;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FIleChooseUI extends AppCompatActivity {

    private FileChooserFragment fragment;
    private Context ctx;
    private boolean hasWritePermission = false;
    private Intent intent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt_choose_file);
        ctx = getApplicationContext();
        intent = getIntent();

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.fragment = (FileChooserFragment) fragmentManager.findFragmentById(R.id.fragmentContainerView);
        this.fragment.setContext(getApplicationContext());
        findViewById(R.id.infoBtn).setOnClickListener(view -> {
            try {
                this.showInfo();
            } catch (IOException | NoSuchAlgorithmException e) {
                Log.e("askPermissionAndShowInfo: ", e.toString());
            }
        });

        findViewById(R.id.backEncryptBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, getIntent());
            finish();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showInfo() throws IOException, NoSuchAlgorithmException {
        String fullPath = this.fragment.getPath();
        if (fullPath != null) {
            Log.d("SI STEP: ", "1");
            String fileLocation = fullPath.substring(0, fullPath.lastIndexOf("/") + 1);
            Log.d("SI STEP: ", "2");
            CheckBox deleteFileCheckbox = (CheckBox) findViewById(R.id.delete_original_check);
            boolean deleteOriginal = false;
            if (deleteFileCheckbox.isChecked()) deleteOriginal = true;
            Log.d("SI STEP: ", "3");
            FileCrypto cryptoUtil = new FileCrypto();
            Log.d("SI STEP: ", "4");
            DataBaseHelper dbHelper = new DataBaseHelper(FIleChooseUI.this);
            String userPassword = this.intent.getStringExtra("password");
            Log.d("SI STEP: ", "5");
            Boolean status = cryptoUtil.encryptFileAddSaveKey(dbHelper, userPassword, ctx, fullPath, deleteOriginal);
            Log.d("SI STEP: ", "6");
            if (status) {
                Toast.makeText(this, "File encrypted successfully", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK, getIntent());
                finish();
            }else {
                Toast.makeText(this, "Could not encrypt file!", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "Choose a file", Toast.LENGTH_LONG).show();
        }

    }
}