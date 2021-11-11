package privacymanager.android.UI.friendship;

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
import privacymanager.android.UI.notifications.NotificationsUI;

public class FriendsListUI extends AppCompatActivity {
    private static final String TAG = FriendsListUI.class.getSimpleName();
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        intent = getIntent();

        setListeners();

        dispatchPopulateAccessibilityEvent();
    }

    private void setListeners() {
        findViewById(R.id.addFriend).setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchUI.class);
            launchAddFriend.launch(intent);
        });

        findViewById(R.id.backFriendsBtn).setOnClickListener(view -> {
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private final ActivityResultLauncher<Intent> launchAddFriend = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "StartActivityForResult() :: result -> Back to friends list.");
                }
            });

    public void dispatchPopulateAccessibilityEvent() {
        ListView listView = (ListView) findViewById(R.id.FriendsList);
        String Names[] = {
                "erfghghhjh",
                "fsgffcgcbc",
                "xfghgfhgkjgh,",
                "ydxcgjcfjhvjh"
        };
        System.out.println(Names[0]);
        FriendsListUI.CustomFriendsList customFriendsList = new CustomFriendsList(this, Names);
        listView.setAdapter(customFriendsList);
    }

    public class CustomFriendsList extends ArrayAdapter {
        private String[] Name;
        private Activity context;

        public CustomFriendsList(Activity context, String[] Name) {
            super(context, R.layout.row_friends, Name);
            this.Name = Name;
            this.context = context;
            System.out.println(Name[0]);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row=convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if(convertView==null)
                row = inflater.inflate(R.layout.row_friends, null, true);
            TextView textViewName = (TextView) row.findViewById(R.id.textViewFriendName);
            textViewName.setText(Name[position]);
            ImageView imageFlag = (ImageView) row.findViewById(R.id.logoFriend);
            imageFlag.setImageResource(R.drawable.ic_icon_user_default);

            return  row;
        }
    }
}
