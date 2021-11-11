package privacymanager.android.UI.encryptedFiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import privacymanager.android.R;
import privacymanager.android.UI.fileEncryption.FIleChooseUI;
import privacymanager.android.UI.encryptedFiles.EFChoosen;

public class EncryptedFilesListUI extends AppCompatActivity {
    private static final String TAG = EncryptedFilesListUI.class.getSimpleName();
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypted_files);
        intent = getIntent();

        setListeners();

        dispatchPopulateAccessibilityEvent();
    }

    private void setListeners() {
        findViewById(R.id.addEF).setOnClickListener(view -> {
            Intent intent = new Intent(this, FIleChooseUI.class);
            launchAddEF.launch(intent);
        });

        findViewById(R.id.backNewEF).setOnClickListener(view -> {
            setResult(RESULT_OK, intent);
            finish();
        });


    }

    private final ActivityResultLauncher<Intent> launchAddEF = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to encrypted files list.");
                }
            });

    public void dispatchPopulateAccessibilityEvent() {
        ListView listView = (ListView) findViewById(R.id.encryptedFilesList);
        String Names[] = {
                "erfghghhjh",
                "fsgffcgcbc",
                "xfghgfhgkjgh,",
                "ydxcgjcfjhvjh"
        };
        System.out.println(Names[0]);
        EncryptedFilesListUI.CustomEFList customEFList = new CustomEFList(this, Names);
        listView.setAdapter(customEFList);

    }


    private final ActivityResultLauncher<Intent> launchChosenEF = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to menu");
                }
            });

    public class CustomEFList extends ArrayAdapter {
        private String[] Name;
        private Activity context;

        public CustomEFList(Activity context, String[] Name) {
            super(context, R.layout.row_encrypted_files2, Name);
            this.Name = Name;
            this.context = context;
            System.out.println(Name[0]);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row=convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if(convertView==null)
                row = inflater.inflate(R.layout.row_encrypted_files2, null, true);
            TextView textViewName = (TextView) row.findViewById(R.id.textViewEFName);
            textViewName.setText(Name[position]);
            ImageView imageFlag = (ImageView) row.findViewById(R.id.logoEF);
            imageFlag.setImageResource(R.drawable.encrypted_files_logo);
            CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBox);
            Button sendFilesBtn = (Button) row.findViewById(R.id.sendFilesBtn);
            Button deleteFilesBtn = (Button) row.findViewById(R.id.deleteFilesBtn);
            return  row;
        }

    }
}
