package privacymanager.android.UI.fileEncryption;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.net.URISyntaxException;

import privacymanager.android.R;
import privacymanager.android.utils.file.PathUtil;

public class FileChooserFragment extends Fragment {

    private Context context;
    private String path = null;
    private TextView fileNameView;
    private ImageView check_icon;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_choose_file, container, false);

        Button buttonBrowse = (Button) rootView.findViewById(R.id.button_browse);
        fileNameView = (TextView) rootView.findViewById(R.id.fileName);
        check_icon = (ImageView) rootView.findViewById(R.id.check_icon);
        check_icon.setVisibility(View.GONE);

        buttonBrowse.setOnClickListener(view-> {
            askPermissionAndBrowseFile();
        });
        return rootView;
    }

    /**
     * The context has to be set before using the FileChooserFragment
     *
     * @param applicationContext current application context
     */
    public void setContext(Context applicationContext) {
        this.context = applicationContext;
    }

    /**
     * Accessing the file path and name after selecting one.
     *
     * @return full path of the selected file path
     */
    public String getPath()  {
        return this.path;
    }

    private void askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            // Check if we have Call permission
            int permission = ActivityCompat.checkSelfPermission(this.getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                mPermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                return;
            }
            }
        this.doBrowseFile();
    }

    private void doBrowseFile()  {
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
        launchFileChooser.launch(chooseFileIntent);
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

    private final ActivityResultLauncher<Intent> launchFileChooser = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK ) {
                    if(result.getData() != null)  {
                        Uri uri = result.getData().getData();
                        String filePath= null;
                        try {
                            filePath = PathUtil.getPath(context,uri);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            Log.i(FileChooserFragment.class.toString(), "Could not get the path.");
                            return;
                        }
                        Log.i(FileChooserFragment.class.toString(), filePath);

                        this.path = filePath;
                        String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                        if (fileName.length() > 44) {
                            fileName = fileName.substring(0, 40) + "...";
                        }
                        this.fileNameView.setText(fileName);

                        check_icon.setVisibility(View.VISIBLE);
                    }
                }
            });
}