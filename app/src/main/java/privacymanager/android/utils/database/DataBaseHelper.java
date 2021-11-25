package privacymanager.android.utils.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import privacymanager.android.UI.credentials.CredentialsUI;
import privacymanager.android.UI.fileDecryption.SelectDecryptUI;
import privacymanager.android.models.CredentialsModel;
import privacymanager.android.models.FilesModel;
import privacymanager.android.models.FriendshipModel;

public class DataBaseHelper extends SQLiteOpenHelper {
    public final String CREDENTIALS_TABLE = "my_credentials";
    public final String FRIENDSHIP_REQUESTS_SENT_TABLE = "friendship_requests_sent";
    public final String ENCRYPTED_FILES_TABLE = "encrypted_files";
    public final String FRIENDSHIP_KEYS_TABLE = "friendship";
    public final String COLUMN_CREDENTIAL_ID = "credential_id";
    public final String COLUMN_CREDENTIAL_SERVICE = "service";
    public final String COLUMN_CREDENTIAL_LOGIN = "login";
    public final String COLUMN_CREDENTIAL_PASSWORD = "password";
    public final String COLUMN_CREDENTIAL_UPLOADED = "uploaded";
    public final String COLUMN_RECEIVER_ID = "receiverId";
    public final String COLUMN_PRIVATE_KEY = "privateKey";
    public final String COLUMN_FILE_ID = "keyId";
    public final String COLUMN_FILE_NAME = "fileName";
    public final String COLUMN_FILE_MD5 = "fileMD5";
    public final String COLUMN_FILE_PASSWORD = "filePassword";
    public final String COLUMN_FRIENDSHIP_ID = "friendshipId";
    public final String COLUMN_FRIEND_ID = "friendId";
    public final String COLUMN_FRIEND_NAME = "friendName";
    public final String COLUMN_SYMMETRIC_KEY = "symmetricKey";

    public DataBaseHelper(@Nullable Context context) {
        super(context, Environment.getExternalStorageDirectory()+"/PrivacyManager/database/privacyManager.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCredentialsTable = "CREATE TABLE " + CREDENTIALS_TABLE +
                " (" + COLUMN_CREDENTIAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CREDENTIAL_SERVICE + " TEXT, " +
                COLUMN_CREDENTIAL_LOGIN + " TEXT, " +
                COLUMN_CREDENTIAL_PASSWORD + " TEXT," +
                COLUMN_CREDENTIAL_UPLOADED + " TEXT);";
        db.execSQL(createCredentialsTable);

        String createFriendshipRequestSentTable = "CREATE TABLE " + FRIENDSHIP_REQUESTS_SENT_TABLE +
                " (" + COLUMN_RECEIVER_ID + " TEXT, " +
                COLUMN_PRIVATE_KEY + " TEXT);";
        db.execSQL(createFriendshipRequestSentTable);

        String createEncryptedFilesTable = "CREATE TABLE " + ENCRYPTED_FILES_TABLE +
                "(" + COLUMN_FILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FILE_MD5 + " TEXT, " +
                COLUMN_FILE_NAME + " TEXT, " +
                COLUMN_FILE_PASSWORD + " TEXT);";
        db.execSQL(createEncryptedFilesTable);

        String createFriendshipTable = "CREATE TABLE " + FRIENDSHIP_KEYS_TABLE +
                " (" + COLUMN_FRIENDSHIP_ID + " INTEGER, " +
                COLUMN_FRIEND_ID + " INTEGER, " +
                COLUMN_FRIEND_NAME + " TEXT, " +
                COLUMN_SYMMETRIC_KEY + " TEXT);";
        db.execSQL(createFriendshipTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addEncryptedFile(Context ctx, String fileName, String fileMD5, String filePassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_FILE_NAME, fileName);
        cv.put(COLUMN_FILE_MD5, fileMD5);
        cv.put(COLUMN_FILE_PASSWORD, filePassword);

        long insert = db.insert(ENCRYPTED_FILES_TABLE, null, cv);

        db.close();

        if (insert == -1) {
            Toast.makeText(ctx, "Encrypted File data was not added.", Toast.LENGTH_LONG).show();
        }

        return insert != -1;
    }

    public boolean addCredential(Context ctx, CredentialsModel credentialModel) {
        // check database for already existing credentials
        SQLiteDatabase dbRead = this.getReadableDatabase();
        String getCredentialsQuery = "SELECT * FROM " + CREDENTIALS_TABLE +
                " WHERE " + COLUMN_CREDENTIAL_SERVICE + " LIKE \"" + credentialModel.getService() + "\"" +
                " AND " + COLUMN_CREDENTIAL_LOGIN + " LIKE \"" + credentialModel.getLogin() + "\"" +
                " AND " + COLUMN_CREDENTIAL_PASSWORD + " LIKE \"" + credentialModel.getPassword() + "\"";
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        if (cursor.getCount() > 0) {

            Toast.makeText(ctx, "These credentials are already registered.", Toast.LENGTH_LONG).show();
            Log.d(DataBaseHelper.class.toString(), "Credentials already registered.");
            Log.d(DataBaseHelper.class.toString(), "Cursor:" + cursor.getCount());
            return false;
        }

        cursor.close();
        dbRead.close();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CREDENTIAL_SERVICE, credentialModel.getService());
        cv.put(COLUMN_CREDENTIAL_LOGIN, credentialModel.getLogin());
        cv.put(COLUMN_CREDENTIAL_PASSWORD, credentialModel.getPassword());
        cv.put(COLUMN_CREDENTIAL_UPLOADED, credentialModel.getUploaded());

        long insert = db.insert(CREDENTIALS_TABLE, null, cv);

        db.close();

        if (insert == -1) {
            Toast.makeText(ctx, "Credentials were not added.", Toast.LENGTH_LONG).show();
        }

        Log.d("DB PATH", db.getPath());
        return insert != -1;
    }

    public boolean patchCredential(Context ctx, int credentialId, int uploaded) {
        // check database for already existing credentials
        SQLiteDatabase db = this.getWritableDatabase();
        String getCredentialsQuery = "UPDATE " + CREDENTIALS_TABLE +
                " SET " + COLUMN_CREDENTIAL_UPLOADED + " = " + uploaded +
                " WHERE " + CREDENTIALS_TABLE +  "." + COLUMN_CREDENTIAL_ID + " = " + credentialId;
        Cursor cursor = db.rawQuery(getCredentialsQuery, null);

        if (cursor.getCount() > 0) {

            Toast.makeText(ctx, "These credentials are already registered.", Toast.LENGTH_LONG).show();
            Log.d(DataBaseHelper.class.toString(), "Credentials already registered.");
            Log.d(DataBaseHelper.class.toString(), "Cursor:" + cursor.getCount());
            return false;
        }

        cursor.close();
        db.close();
        return true;
    }

    public Integer getCredentialsId(String service, String login, String password) {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        String getCredentialsQuery = "SELECT * FROM " + CREDENTIALS_TABLE +
                " WHERE " + COLUMN_CREDENTIAL_SERVICE + " LIKE \"" + service + "\"" +
                " AND " + COLUMN_CREDENTIAL_LOGIN + " LIKE \"" + login + "\"" +
                " AND " + COLUMN_CREDENTIAL_PASSWORD + " LIKE \"" + password + "\"";
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        Integer credentialsId = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            credentialsId = cursor.getInt(0);
        }

        cursor.close();
        dbRead.close();

        Log.d(DataBaseHelper.class.toString(), "Credentials ID:".concat(credentialsId.toString()));

        return credentialsId;
    }

    public List<CredentialsModel> getCredentialsList() {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        List<CredentialsModel> credentialsList = new ArrayList<>();
        String getCredentialsQuery = "SELECT * FROM " + CREDENTIALS_TABLE;
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int credentialsId = cursor.getInt(0);
                Log.d(CredentialsUI.class.toString(), "Got id:" + credentialsId);
                String service = cursor.getString(1);
                String login = cursor.getString(2);
                String password = cursor.getString(3);
                int uploaded = cursor.getInt(4);
                CredentialsModel credential = new CredentialsModel(
                        credentialsId,
                        service,
                        login,
                        password,
                        uploaded
                );
                credentialsList.add(credential);
            } while (cursor.moveToNext());

        }

        cursor.close();
        dbRead.close();

        return credentialsList;
    }

    public boolean deleteCredential(CredentialsModel credentialModel) {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        String getCredentialsQuery = "DELETE FROM " + CREDENTIALS_TABLE +
                " WHERE " + COLUMN_CREDENTIAL_ID + " = " + credentialModel.getCredentialId();
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        Integer result = cursor.getCount();
        Log.d(DataBaseHelper.class.toString(), "Credentials delete result:" + result);

        cursor.close();
        dbRead.close();

        return result != -1;
    }

    public boolean addFriendshipReqest(Context ctx, Integer futureFriendId, String privateKeyString) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_RECEIVER_ID, futureFriendId);
        cv.put(COLUMN_PRIVATE_KEY, privateKeyString);

        long insert = db.insert(FRIENDSHIP_REQUESTS_SENT_TABLE, null, cv);

        db.close();

        if (insert == -1) {
            Toast.makeText(ctx, "Friendship request wes not added.", Toast.LENGTH_LONG).show();
        }

        return insert != -1;
    }

    public int saveFriendshipKey(Context context, Integer friendshipId, Integer futureFriendId, String futureFriendName, String symmetricKey) {
        // check database for already existing friendship
        SQLiteDatabase dbRead = this.getReadableDatabase();
        String getFriendshipQuery = "SELECT * FROM " + FRIENDSHIP_KEYS_TABLE +
                " WHERE " + COLUMN_FRIENDSHIP_ID + " = " + friendshipId;
        Cursor cursor = dbRead.rawQuery(getFriendshipQuery, null);

        if (cursor.getCount() > 0) {
            Log.d(DataBaseHelper.class.toString(), "Friendship already registered.");
            return 1;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_FRIENDSHIP_ID, friendshipId);
        cv.put(COLUMN_FRIEND_ID, futureFriendId);
        cv.put(COLUMN_FRIEND_NAME, futureFriendName);
        cv.put(COLUMN_SYMMETRIC_KEY, symmetricKey);

        long insert = db.insert(FRIENDSHIP_KEYS_TABLE, null, cv);

        db.close();

        if (insert == -1){
            Toast.makeText(context, "Friendship key wes not added.", Toast.LENGTH_LONG).show();
            return -1;
        }

        return 0;
    }

    public String getFriendshipPrivateKey(Integer receiverId) {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        String getCredentialsQuery = "SELECT * FROM " + FRIENDSHIP_REQUESTS_SENT_TABLE +
                " WHERE " + COLUMN_RECEIVER_ID + " = " + receiverId;
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        String privateKey = "";
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            privateKey = cursor.getString(1);
        }

        cursor.close();
        dbRead.close();

        Log.d(DataBaseHelper.class.toString(), "Extracted private key:".concat(privateKey));

        return privateKey;
    }

    public List<FriendshipModel> getFriendshipsList() {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        List<FriendshipModel> friendshipsList = new ArrayList<>();
        String getFriendshipsQuery = "SELECT * FROM " + FRIENDSHIP_KEYS_TABLE;
        Cursor cursor = dbRead.rawQuery(getFriendshipsQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int friendshipId = cursor.getInt(0);
                Log.d(CredentialsUI.class.toString(), "Got friendshipId:" + friendshipId);
                int friendId = cursor.getInt(1);
                String friendName = cursor.getString(2);
                String symmetricKey = cursor.getString(3);
                FriendshipModel friendship = new FriendshipModel(
                        friendshipId,
                        friendId,
                        friendName,
                        symmetricKey
                );
                friendshipsList.add(friendship);
            } while (cursor.moveToNext());

        }

        cursor.close();
        dbRead.close();

        return friendshipsList;
    }

    public FilesModel getEncryptedFilesKeys(String md5HashEncryptedFile) {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        FilesModel filesModel = new FilesModel();
        String getFilesQuery = "SELECT * FROM " + ENCRYPTED_FILES_TABLE + " WHERE " + COLUMN_FILE_MD5 + " LIKE \"" + md5HashEncryptedFile + "\"";
        Cursor cursor = dbRead.rawQuery(getFilesQuery, null);

        if (cursor.moveToFirst()) {
            int keyId = cursor.getInt(0);
            String fileName = cursor.getString(2);
            Log.d(SelectDecryptUI.class.toString(), "Got file:" + fileName);
            String fileMD5 = cursor.getString(1);
            String filePassword = cursor.getString(3);
            filesModel = new FilesModel(
                    keyId,
                    fileName,
                    fileMD5,
                    filePassword
            );
        }

        cursor.close();
        dbRead.close();

        return filesModel;
    }

    public List<FilesModel> getEncryptedFiles() {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        List<FilesModel> filesModel = new ArrayList<>();
        String getFilesQuery = "SELECT * FROM " + ENCRYPTED_FILES_TABLE + " WHERE " + COLUMN_FILE_NAME + " <> \'\'";
        Cursor cursor = dbRead.rawQuery(getFilesQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int keyId = cursor.getInt(0);
                String fileName = cursor.getString(2);
                Log.d(SelectDecryptUI.class.toString(), "Got file:" + fileName);
                String fileMD5 = cursor.getString(1);
                String filePassword = cursor.getString(3);
                filesModel.add(new FilesModel(
                        keyId,
                        fileName,
                        fileMD5,
                        filePassword
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        dbRead.close();

        return filesModel;
    }

    public String getSymmetricKeyByFriendshipId(Integer friendshipId) {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        String getCredentialsQuery = "SELECT * FROM " + FRIENDSHIP_KEYS_TABLE +
                " WHERE " + COLUMN_FRIENDSHIP_ID + " = " + friendshipId;
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        String symmetricKey = "";
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            symmetricKey = cursor.getString(3);
        }

        cursor.close();
        dbRead.close();

        Log.d(DataBaseHelper.class.toString(), "Extracted private key:".concat(symmetricKey));

        return symmetricKey;
    }

    public Integer getFriendshipIdByFriendId(Integer friendId) {
        SQLiteDatabase dbRead = this.getReadableDatabase();
        String getCredentialsQuery = "SELECT * FROM " + FRIENDSHIP_KEYS_TABLE +
                " WHERE " + COLUMN_FRIEND_ID + " = " + friendId;
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        Integer friendshipId = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            friendshipId = cursor.getInt(0);
        }

        cursor.close();
        dbRead.close();

        Log.d(DataBaseHelper.class.toString(), "Extracted private key:".concat(friendshipId.toString()));

        return friendshipId;
    }
}
