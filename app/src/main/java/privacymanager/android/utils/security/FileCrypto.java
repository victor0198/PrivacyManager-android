package privacymanager.android.utils.security;

import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SymbolTable;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.xml.parsers.FactoryConfigurationError;

import privacymanager.android.UI.menu.MainActivity;
import privacymanager.android.utils.database.DataBaseHelper;

public class FileCrypto extends AppCompatActivity {

    public boolean encryptFileAddSaveKey(String filePath, String savePath, Boolean deleteOriginal) {
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
        return true;
    }
}
