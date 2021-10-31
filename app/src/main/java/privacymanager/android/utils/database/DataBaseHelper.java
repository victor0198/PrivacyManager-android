package privacymanager.android.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import privacymanager.android.models.CredentialModel;

public class DataBaseHelper extends SQLiteOpenHelper {
    public String CREDENTIALS_TABLE = "my_credentials";
    public String COLUMN_CREDENTIAL_SERVICE = "service";
    public String COLUMN_CREDENTIAL_LOGIN = "login";
    public String COLUMN_CREDENTIAL_PASSWORD = "password";

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addCredential(Context ctx, CredentialModel credentialModel){
        // check database for already existing credentials
        SQLiteDatabase dbRead = this.getReadableDatabase();
        List<CredentialModel> credentialsList = new ArrayList<>();
        String getCredentialsQuery = "SELECT * FROM " + CREDENTIALS_TABLE +
                " WHERE " + COLUMN_CREDENTIAL_SERVICE + " LIKE \"" + credentialModel.getService() + "\"" +
                " AND " + COLUMN_CREDENTIAL_LOGIN + " LIKE \"" + credentialModel.getLogin() + "\"" +
                " AND " + COLUMN_CREDENTIAL_PASSWORD + " LIKE \"" +  credentialModel.getPassword() + "\"";
        Cursor cursor = dbRead.rawQuery(getCredentialsQuery, null);

        if (cursor.getCount() > 0){
            Toast.makeText(ctx, "These credentials are already registered.",Toast.LENGTH_LONG).show();
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

        return insert != -1;
    }

    public Integer getCredentialsId(String service, String login, String password){
        // check database for already existing credentials
        SQLiteDatabase dbRead = this.getReadableDatabase();
        List<CredentialModel> credentialsList = new ArrayList<>();
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

        return credentialsId;
    }
}
