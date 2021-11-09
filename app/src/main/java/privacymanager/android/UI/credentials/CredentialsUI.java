package privacymanager.android.UI.credentials;

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
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import privacymanager.android.R;

public class CredentialsUI extends AppCompatActivity {
    private static final String TAG = CredentialsUI.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);

        findViewById(R.id.addCredentials).setOnClickListener(view -> {
            Intent intent = new Intent(this, AddCredentialsUI.class);
            launchAddCredentials.launch(intent);
        });

        //dispatchPopulateAccessibilityEvent();
    }

    public void dispatchPopulateAccessibilityEvent() {
        ListView listView=(ListView)findViewById(R.id.credintialsList);
        String credintialNames[] = {
                "erfghghhjh",
                "fsgffcgcbc",
                "xfghgfhgkjgh,",
                "ydxcgjcfjhvjh"
        };

        String credintialEmail[] = {
                "foo1@mail.com",
                "foo2@mail.com",
                "foo2@mail.com",
                "foo2@mail.com"
        };

        Integer imageid[] = {
                R.drawable.ic_default_logo,
                R.drawable.ic_facebook_logo,
                R.drawable.ic_logo_google,
                R.drawable.ic_default_logo

        };

        CustomCredintialList customCountryList = new CustomCredintialList(this, credintialNames, credintialEmail, imageid);
        listView.setAdapter(customCountryList);
    }

    public class CustomCredintialList extends ArrayAdapter {
        private String[] credintialNames;
        private String[] credintialsEmail;
        private Integer[] imageid;
        private Activity context;

        public CustomCredintialList(Activity context, String[] credintialNames, String[] credintialsEmail, Integer[] imageid) {
            super(context, R.layout.row_credintials, credintialNames);
            this.context = context;
            this.credintialNames = credintialNames;
            this.credintialsEmail = credintialsEmail;
            this.imageid = imageid;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row=convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if(convertView==null)
                row = inflater.inflate(R.layout.row_credintials, null, true);
            TextView textViewCountry = (TextView) row.findViewById(R.id.textViewCredintialName);
            TextView textViewCapital = (TextView) row.findViewById(R.id.textViewCredintialEmail);
            ImageView imageFlag = (ImageView) row.findViewById(R.id.logoCredintial);

            textViewCountry.setText(credintialNames[position]);
            textViewCapital.setText(credintialsEmail[position]);
            imageFlag.setImageResource(imageid[position]);
            return  row;
        }
    }

    private final ActivityResultLauncher<Intent> launchAddCredentials = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to activities list.");
                }
            });
}