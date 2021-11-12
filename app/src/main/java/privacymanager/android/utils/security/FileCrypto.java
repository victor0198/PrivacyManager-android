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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.FactoryConfigurationError;

import privacymanager.android.UI.fileEncryption.FIleChooseUI;
import privacymanager.android.models.FilesModel;
import privacymanager.android.utils.database.DataBaseHelper;

public class FileCrypto {

    public boolean encryptFileAddSaveKey(DataBaseHelper dbHelper, Context ctx,
                                         String filePath, String savePath,
                                         Boolean deleteOriginal) throws IOException, NoSuchAlgorithmException {
        File currentFile = new File(filePath);
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
                currentFile.delete();
            } catch (Exception e) {
                Log.d("FileCryptoDeletionError", e.toString());
                return false;
            }
        }

        MessageDigest digest = MessageDigest.getInstance("MD5");
        String md5Hash = CheckSumMD5.checksum(digest, currentFile);
        FilesModel filesModel = new FilesModel(currentFile.getName(), md5Hash, savePath, password);
        dbHelper.addEncryptedFile(ctx, filesModel);
        dbHelper.close();

        return true;
    }
}
