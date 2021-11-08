package privacymanager.android.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import privacymanager.android.models.CredentialsModel;

public class DataBaseHelper extends SQLiteOpenHelper {
    public final String CREDENTIALS_TABLE = "my_credentials";
    public final String FRIENDSHIP_REQUESTS_SENT_TABLE = "friendship_requests_sent";
    public final String COLUMN_CREDENTIAL_SERVICE = "service";
    public final String COLUMN_CREDENTIAL_LOGIN = "login";
    public final String COLUMN_CREDENTIAL_PASSWORD = "password";
    public final String COLUMN_RECEIVER_ID = "receiverId";
    public final String COLUMN_PRIVATE_KEY = "privateKey";

    public DataBaseHelper(@Nullable Context context) {
        super(context, "privacyManager.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCredentialsTable = "CREATE TABLE " + CREDENTIALS_TABLE +
                " (credential_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CREDENTIAL_SERVICE + " TEXT, " +
                COLUMN_CREDENTIAL_LOGIN + " TEXT, " +
                COLUMN_CREDENTIAL_PASSWORD + " TEXT);";
        db.execSQL(createCredentialsTable);

        String createFriendshipRequestSentTable = "CREATE TABLE " + FRIENDSHIP_REQUESTS_SENT_TABLE +
                " (" + COLUMN_RECEIVER_ID + " TEXT, " +
                COLUMN_PRIVATE_KEY + " TEXT);";
        db.execSQL(createFriendshipRequestSentTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addCredential(Context ctx, CredentialsModel credentialModel){
        // check database for already existing credentials
        SQLiteDatabase dbRead = this.getReadableDatabase();
        List<CredentialsModel> credentialsList = new ArrayList<>();
        String getCredentialsQuery = "SELECT * FROM " + CREDENTIALS_TABLE +
                " WHERE " + COLUMN_CREDENTIAL_SERVICE + " LIKE \"" + credentialModel.getService() + "\"" +
                " AND " + COLUMN_CREDENTIAL_LOGIN + " LIKE \"" + credentialModel.getLogin() + "\"" +
                " AND " + COLUMN_CREDENTIAL_PASSWORD + " LIKE \"" +  credentialModel.getPassword() + "\"";
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        if (cursor.getCount() > 0){
            Toast.makeText(ctx, "These credentials are already registered.",Toast.LENGTH_LONG).show();
            Log.d(DataBaseHelper.class.toString(), "Credentials already registered.");
            return false;
        }

        cursor.close();
        dbRead.close();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CREDENTIAL_SERVICE, credentialModel.getService());
        cv.put(COLUMN_CREDENTIAL_LOGIN, credentialModel.getLogin());
        cv.put(COLUMN_CREDENTIAL_PASSWORD, credentialModel.getPassword());

        long insert = db.insert(CREDENTIALS_TABLE, null, cv);

        db.close();

        if (insert == -1){
            Toast.makeText(ctx, "Credentials were not added.", Toast.LENGTH_LONG).show();
        }

        return insert != -1;
    }

    public Integer getCredentialsId(String service, String login, String password){
        // check database for already existing credentials
        SQLiteDatabase dbRead = this.getReadableDatabase();
        List<CredentialsModel> credentialsList = new ArrayList<>();
        String getCredentialsQuery = "SELECT * FROM " + CREDENTIALS_TABLE +
                " WHERE " + COLUMN_CREDENTIAL_SERVICE + " LIKE \"" + service + "\"" +
                " AND " + COLUMN_CREDENTIAL_LOGIN + " LIKE \"" + login + "\"" +
                " AND " + COLUMN_CREDENTIAL_PASSWORD + " LIKE \"" +  password + "\"";
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        Integer credentialsId = 0;
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            credentialsId = cursor.getInt(0);
        }

        cursor.close();
        dbRead.close();

        Log.d(DataBaseHelper.class.toString(), "Credentials ID:".concat(credentialsId.toString()));

        return credentialsId;
    }

    public boolean addFriendshipReqest(Context ctx, Integer futureFriendId, String privateKeyString){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_RECEIVER_ID, futureFriendId);
        cv.put(COLUMN_PRIVATE_KEY, privateKeyString);

        long insert = db.insert(FRIENDSHIP_REQUESTS_SENT_TABLE, null, cv);

        db.close();

        if (insert == -1){
            Toast.makeText(ctx, "Friendship request wes not added.", Toast.LENGTH_LONG).show();
        }

        return insert != -1;
    }
}
