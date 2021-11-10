package privacymanager.android.UI.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class ConfirmExit extends DialogFragment {
    private static String EXIT_MESSAGE = "You want to exit the app?";
    private static String POSITIVE_MESSAGE = "Exit";
    private static String NEGATIVE_MESSAGE = "Cancel";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(EXIT_MESSAGE)
                .setPositiveButton(POSITIVE_MESSAGE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                })
                .setNegativeButton(NEGATIVE_MESSAGE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dianfirmlog, int id) {
                        // User cancelled the dialog
                    }
                });

        return builder.create();
    }
}

