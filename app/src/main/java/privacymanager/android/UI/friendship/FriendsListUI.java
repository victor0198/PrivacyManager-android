package privacymanager.android.UI.friendship;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import privacymanager.android.R;

public class FriendsListUI extends AppCompatActivity {
    private static final String TAG = FriendsListUI.class.getSimpleName();
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        intent = getIntent();

        setListeners();
    }

    private void setListeners() {
        findViewById(R.id.addFriend).setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchUI.class);
            intent.putExtra("JWT", this.intent.getStringExtra("JWT"));
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
}
