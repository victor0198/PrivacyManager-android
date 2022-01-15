package privacymanager.android.UI.credentials;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import privacymanager.android.R;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.utils.database.DBFacade;
import privacymanager.android.utils.database.DataBaseHelper;

public class CredentialsUI extends AppCompatActivity {
    private static final String TAG = CredentialsUI.class.getSimpleName();
    private List<CredentialsModel> credentialsList;
    private ListView listView;
    private Intent intent;
    private DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);
        intent = getIntent();
        dataBaseHelper = new DataBaseHelper(CredentialsUI.this);

        listView = (ListView) findViewById(R.id.credintialsList);

        dispatchPopulateAccessibilityEvent();

        setListeners();
    }

    private void setListeners() {
        findViewById(R.id.addCredentials).setOnClickListener(view -> {
            Intent intent = new Intent(this, AddCredentialsUI.class);
            intent.putExtra("JWT", this.intent.getStringExtra("JWT"));
            intent.putExtra("password", this.intent.getStringExtra("password"));
            launchAddCredentials.launch(intent);
        });

        findViewById(R.id.backNewCredintials).setOnClickListener(view -> {
            setResult(RESULT_OK, this.intent);
            finish();
        });

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id){
                CredentialsModel currentCredentials = credentialsList.get(position);
                openDetails(currentCredentials);
            }
        });
    }

    private void openDetails(CredentialsModel currentCredentials) {
        Intent intent = new Intent(this, CredentialsDetails.class);
        intent.putExtra("credentialsId", currentCredentials.getCredentialId());
        intent.putExtra("service", currentCredentials.getService());
        intent.putExtra("login", currentCredentials.getLogin());
        intent.putExtra("passwordToShow", currentCredentials.getPassword());
        intent.putExtra("password", this.intent.getStringExtra("password"));
        intent.putExtra("JWT", this.intent.getStringExtra("JWT"));
        intent.putExtra("uploaded", currentCredentials.getUploaded());
        launchDetails.launch(intent);
    }

    public void dispatchPopulateAccessibilityEvent() {
        DBFacade dbFacade = new DBFacade(dataBaseHelper, this.intent.getStringExtra("password"));
        this.credentialsList = dbFacade.getCredentialsList();

        List<String> credintialServices = new ArrayList<String>();
        List<String> credintialNames = new ArrayList<String>();
        List<Integer> imageid = new ArrayList<Integer>();
        List<Integer> uploaded = new ArrayList<Integer>();

        for (int i=0; i<credentialsList.size(); i++){
            credintialServices.add(this.credentialsList.get(i).getService());
            credintialNames.add(this.credentialsList.get(i).getLogin());
            if (this.credentialsList.get(i).getService().contains("facebook")){
                imageid.add(R.drawable.ic_facebook_logo);
            }else if(this.credentialsList.get(i).getService().contains("google")){
                imageid.add(R.drawable.ic_logo_google);
            }else{
                imageid.add(R.drawable.ic_default_logo);
            }
            uploaded.add(this.credentialsList.get(i).getUploaded());
        }

        CustomCredintialList customCountryList = new CustomCredintialList(this, credintialServices, credintialNames, imageid, uploaded);
        this.listView.setAdapter(customCountryList);
    }

    public class CustomCredintialList extends ArrayAdapter {
        private List<String> credintialServices;
        private List<String> credintialNames;
        private List<Integer> imageid;
        private Activity context;
        private List<Integer> uploaded;

        public CustomCredintialList(Activity context, List<String> credintialServices, List<String> credintialNames, List<Integer> imageid, List<Integer> uploaded) {
            super(context, R.layout.row_credintials, credintialNames);
            this.context = context;
            this.credintialServices = credintialServices;
            this.credintialNames = credintialNames;
            this.imageid = imageid;
            this.uploaded = uploaded;
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
            ImageView cloud = (ImageView) row.findViewById(R.id.imageViewCloud);

            textViewCountry.setText(credintialServices.get(position));
            textViewCapital.setText(credintialNames.get(position));
            imageFlag.setImageResource(imageid.get(position));
            if (uploaded.get(position) == 0) {
                cloud.setVisibility(View.INVISIBLE);
            }
            return  row;
        }
    }

    private final ActivityResultLauncher<Intent> launchAddCredentials = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData().getStringExtra("JWT") == null){
                        Log.d("CredentialsUI result ::", "JWT it is null");
                    }else{
                        this.intent.putExtra("JWT", result.getData().getStringExtra("JWT"));
                    }
                    dispatchPopulateAccessibilityEvent();
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to credentials list.");
                }
            });

    private final ActivityResultLauncher<Intent> launchDetails = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    dispatchPopulateAccessibilityEvent();
                }
            });
}