package privacymanager.android.UI.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import privacymanager.android.UI.credentials.AddCredentialsUI;
import privacymanager.android.UI.credentials.CredentialsDetails;
import privacymanager.android.UI.credentials.CredentialsUI;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.utils.database.DataBaseHelper;

public class ConfirmCredentialsDelete extends DialogFragment {
    private static String EXIT_MESSAGE = "Delete credentials for ";
    private static String POSITIVE_MESSAGE = "Delete";
    private static String NEGATIVE_MESSAGE = "Cancel";
    private CredentialsModel credentialsToDelete;
    private Context ctx;
    private CredentialsDetails ctxCD;
    private Intent intent;

    public ConfirmCredentialsDelete(CredentialsDetails ctxCD, Intent intent, Context ctx, CredentialsModel credentialsToDelete){
        this.credentialsToDelete = credentialsToDelete;
        this.ctx = ctx;
        this.ctxCD = ctxCD;
        this.intent = intent;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(EXIT_MESSAGE.concat(credentialsToDelete.getService().concat(" service ?")))
                .setPositiveButton(POSITIVE_MESSAGE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
                        boolean deletitionResult = dataBaseHelper.deleteCredential(credentialsToDelete);
                        if(deletitionResult){
                            Toast.makeText(ctx,
                                    credentialsToDelete.getService() + " credentials removed.",
                                    Toast.LENGTH_LONG)
                                    .show();
                            ctxCD.setResult(ctxCD.RESULT_OK, intent);
                            ctxCD.finish();
                        }else{
                            Toast.makeText(ctx,
                                    "Could not remove.",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                })
                .setNegativeButton(NEGATIVE_MESSAGE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        return builder.create();
    }
}