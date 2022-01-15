package privacymanager.android.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import privacymanager.android.UI.credentials.CredentialsUI;
import privacymanager.android.UI.fileDecryption.SelectDecryptUI;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.models.FilesModel;
import privacymanager.android.models.FriendshipModel;
import privacymanager.android.utils.security.FileSecurityUtils;
import privacymanager.android.utils.security.GeneratePassword;

public class DBFacade {
    private DataBaseHelper dataBaseHelper;
    private String password;
    private String TAG;

    public DBFacade(DataBaseHelper dataBaseHelper, String password) {
        this.dataBaseHelper = dataBaseHelper;
        this.password = password;
        this.TAG = DBFacade.class.toString();
    }

    public void databaseDecryption() {
        byte[] encData;
        byte[] decData;
        File inFile = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/database/privacyManager.db.enc");

        if (inFile == null){
            return;
        }

        //Generate the cipher using password
        Cipher cipher = null;
        try {
            cipher = FileSecurityUtils.makeCipher(this.password, false);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        //Read in the encrypted db
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(inFile);
            encData = new byte[(int) inFile.length()];
            inStream.read(encData);
            inStream.close();
            //Decrypt
            decData = cipher.doFinal(encData);
            //Figure out how much padding to remove
            int padCount = (int) decData[decData.length - 1];
            if (padCount >= 1 && padCount <= 8) {
                decData = Arrays.copyOfRange(decData, 0, decData.length - padCount);
            }

            FileOutputStream target = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/database/privacyManager.db"));
            target.write(decData);
            target.close();
        } catch (IOException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "Could not Decrypt DB");
            e.printStackTrace();
        }
    }

    public void databaseEncryption() {
        Log.d("EF STEP: ", "1");
        File currentFile = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/database/privacyManager.db");
        Log.d("EF STEP: ", "2");
        GeneratePassword passwordGenerator = new GeneratePassword();
        Log.d("EF STEP: ", "3");
        Log.d("EF STEP: ", "4");
        String md5HashEncryptedFile = null;
        try {
            Log.d("EF STEP: ", "5");
            md5HashEncryptedFile = FileSecurityUtils.encryptFile(Environment.getExternalStorageDirectory()+"/PrivacyManager/database/privacyManager.db",
                    Environment.getExternalStorageDirectory()+"/PrivacyManager/database/privacyManager.db.enc",
                    this.password);
        } catch (GeneralSecurityException | IOException e) {
            Log.d("FileCryptoEncryptionError", e.toString());
            return;
        }
        Log.d("EF STEP: ", "6");

        try {
            Log.d("EF STEP: ", "7");
            currentFile.delete();
        } catch (Exception e) {
            Log.d("FileCryptoDeletionError", e.toString());
            return;
        }
        try {
            File journalFile = new File(Environment.getExternalStorageDirectory()+"/PrivacyManager/database/privacyManager.db-journal");
            Log.d("EF STEP: ", "8");
            journalFile.delete();
        } catch (Exception e) {
            Log.d("FileCryptoDeletionError", e.toString());
            return;
        }
    }

    public boolean addEncryptedFile(Context ctx, String fileName, String fileMD5, String filePassword) {
        databaseDecryption();
        boolean result = this.dataBaseHelper.addEncryptedFile(ctx, fileName, fileMD5, filePassword);
        databaseEncryption();

        return result;
    }

    public boolean addCredential(Context ctx, CredentialsModel credentialModel) {
        databaseDecryption();
        boolean result = this.dataBaseHelper.addCredential(ctx, credentialModel);
        databaseEncryption();

        return result;
    }

    public boolean patchCredential(Context ctx, int credentialId, int uploaded) {
        databaseDecryption();
        boolean result = this.dataBaseHelper.patchCredential(ctx, credentialId, uploaded);
        databaseEncryption();

        return result;
    }

    public Integer getCredentialsId(String service, String login, String password) {
        databaseDecryption();
        Integer result = this.dataBaseHelper.getCredentialsId(service, login, password);
        databaseEncryption();

        return result;
    }

    public List<CredentialsModel> getCredentialsList() {
        databaseDecryption();
        List<CredentialsModel> result = this.dataBaseHelper.getCredentialsList();
        databaseEncryption();

        return result;
    }

    public boolean deleteCredential(CredentialsModel credentialModel) {
        databaseDecryption();
        boolean result = this.dataBaseHelper.deleteCredential(credentialModel);
        databaseEncryption();

        return result;
    }

    public boolean addFriendshipReqest(Context ctx, Integer futureFriendId, String privateKeyString) {
        databaseDecryption();
        boolean result = this.dataBaseHelper.addFriendshipReqest(ctx, futureFriendId, privateKeyString);
        databaseEncryption();

        return result;
    }

    public int saveFriendshipKey(Context context, Integer friendshipId, Integer futureFriendId, String futureFriendName, String symmetricKey) {
        databaseDecryption();
        int result = this.dataBaseHelper.saveFriendshipKey(context, friendshipId, futureFriendId, futureFriendName, symmetricKey);
        databaseEncryption();

        return result;
    }

    public String getFriendshipPrivateKey(Integer receiverId) {
        databaseDecryption();
        String result = this.dataBaseHelper.getFriendshipPrivateKey(receiverId);
        databaseEncryption();

        return result;
    }

    public List<FriendshipModel> getFriendshipsList() {
        databaseDecryption();
        List<FriendshipModel> result = this.dataBaseHelper.getFriendshipsList();
        databaseEncryption();

        return result;
    }

    public FilesModel getEncryptedFilesKeys(String md5HashEncryptedFile) {
        databaseDecryption();
        FilesModel result = this.dataBaseHelper.getEncryptedFilesKeys(md5HashEncryptedFile);
        databaseEncryption();

        return result;
    }

    public List<FilesModel> getEncryptedFiles() {
        databaseDecryption();
        List<FilesModel> result = this.dataBaseHelper.getEncryptedFiles();
        databaseEncryption();

        return result;
    }

    public String getSymmetricKeyByFriendshipId(Integer friendshipId) {
        databaseDecryption();
        String result = this.dataBaseHelper.getSymmetricKeyByFriendshipId(friendshipId);
        databaseEncryption();

        return result;
    }

    public Integer getFriendshipIdByFriendId(Integer friendId) {
        databaseDecryption();
        Integer result = this.dataBaseHelper.getFriendshipIdByFriendId(friendId);
        databaseEncryption();

        return result;
    }
}
