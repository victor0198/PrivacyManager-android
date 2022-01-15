package privacymanager.android.utils.security;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SymbolTable;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.xml.parsers.FactoryConfigurationError;

import privacymanager.android.UI.fileEncryption.FIleChooseUI;
import privacymanager.android.models.FilesModel;
import privacymanager.android.utils.database.DBFacade;
import privacymanager.android.utils.database.DataBaseHelper;

public class FileCrypto {

    public boolean encryptFileAddSaveKey(DataBaseHelper dbHelper, String userPassword, Context ctx,
                                         String filePath,
                                         Boolean deleteOriginal) throws IOException, NoSuchAlgorithmException {
        Log.d("EF STEP: ", "1");
        File currentFile = new File(filePath);
        Log.d("EF STEP: ", "2");
        GeneratePassword passwordGenerator = new GeneratePassword();
        Log.d("EF STEP: ", "3");
        String password = passwordGenerator.generateSecurePassword(32, 10, 10, 5, 7);
        Log.d("EF STEP: ", "4");
        String md5HashEncryptedFile = null;
        try {
            Log.d("EF STEP: ", "5");
            String[] pathBits = filePath.split("/");
            String fileName = pathBits[pathBits.length-1];
            Log.d("ENCRYPTED FILE NAME:", fileName+".pm");
            Log.d("Saving in:", "/PrivacyManager/encrypted/" + fileName + ".enc");
            md5HashEncryptedFile = FileSecurityUtils.encryptFile(filePath,
                    Environment.getExternalStorageDirectory()+"/PrivacyManager/encrypted/" + fileName + ".enc",
                    password);
        } catch (GeneralSecurityException | IOException e) {
            Log.d("FileCryptoEncryptionError", e.toString());
            return false;
        }
        Log.d("EF STEP: ", "6");

        if (deleteOriginal) {
            try {
                Log.d("EF STEP: ", "7");
                currentFile.delete();
            } catch (Exception e) {
                Log.d("FileCryptoDeletionError", e.toString());
                return false;
            }
        }

        Log.d("EF STEP: ", "8");
        DBFacade dbFacade = new DBFacade(dbHelper, userPassword);
        dbFacade.addEncryptedFile(ctx, currentFile.getName(), md5HashEncryptedFile, password);
        Log.d("EF STEP: ", "9");
        dbHelper.close();

        return true;
    }
}
