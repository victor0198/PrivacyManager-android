package privacymanager.android.utils.security;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SymbolTable;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.xml.parsers.FactoryConfigurationError;

import privacymanager.android.UI.fileEncryption.FIleChooseUI;
import privacymanager.android.models.FilesModel;
import privacymanager.android.utils.database.DataBaseHelper;

public class FileCrypto {

    private int fileID = 0;

    public boolean encryptFileAddSaveKey(DataBaseHelper dbHelper, Context ctx,
                                         String filePath, String savePath,
                                         Boolean deleteOriginal) {

        GeneratePassword passwordGenerator = new GeneratePassword();
        String password = passwordGenerator.generateSecurePassword(32, 10, 10, 5, 7);
        try {
            FileSecurityUtils.encryptFile(filePath, savePath, password);
        } catch (GeneralSecurityException | IOException e) {
            Log.d("FileCryptoEncryptionError", e.toString());
            return false;
        }

        if (deleteOriginal) {
            try {
                File toDelete = new File(filePath);
                toDelete.delete();
            } catch (Exception e) {
                Log.d("FileCryptoDeletionError", e.toString());
                return false;
            }
        }

        FilesModel filesModel = new FilesModel(fileID, savePath, password);

        dbHelper.addEncryptedFile(ctx, filesModel);
        dbHelper.close();

        return true;
    }
}
