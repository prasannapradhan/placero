package lm.pkp.com.landmap.user;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.tags.TagsDBHelper;
import lm.pkp.com.landmap.util.AndroidSystemUtil;
import lm.pkp.com.landmap.weather.db.WeatherDBHelper;
import android.provider.Settings.Secure;

public class UserDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private Context localContext;

    public static final String USER_TABLE_NAME = "user_master";
    public static final String USER_COLUMN_DISPLAY_NAME = "display_name";
    public static final String USER_COLUMN_EMAIL = "email";
    public static final String USER_COLUMN_FAMILY_NAME = "family_name";
    public static final String USER_COLUMN_GIVEN_NAME = "given_name";
    public static final String USER_COLUMN_PHOTO_URL = "photo_url";
    public static final String USER_COLUMN_AUTH_SYS_ID = "auth_sys_id";

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.localContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + USER_TABLE_NAME + "(" +
                        USER_COLUMN_DISPLAY_NAME + " text, " +
                        USER_COLUMN_EMAIL + " text," +
                        USER_COLUMN_FAMILY_NAME + " text," +
                        USER_COLUMN_GIVEN_NAME + " text, " +
                        USER_COLUMN_PHOTO_URL + " text, " +
                        USER_COLUMN_AUTH_SYS_ID + " text )"
        );

        new AreaDBHelper(localContext).onCreate(db);
        new PositionsDBHelper(localContext).onCreate(db);
        new DriveDBHelper(localContext).onCreate(db);
        new PermissionsDBHelper(localContext).onCreate(db);
        new WeatherDBHelper(localContext).onCreate(db);
        new TagsDBHelper(localContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        this.onCreate(db);
    }

    public void dryRun(){
        SQLiteDatabase db = getReadableDatabase();
        db.close();
    }

    public void insertUserToServer(UserElement user) {
        JSONObject postParams = this.preparePostParams("insert", user);
        new LMSRestAsyncTask().execute(postParams);
    }

    private JSONObject preparePostParams(String queryType, UserElement user) {
        JSONObject postParams = new JSONObject();
        try {

            postParams.put("queryType", queryType);
            postParams.put("deviceID", AndroidSystemUtil.getDeviceId(localContext));
            postParams.put("requestType", "UserMaster");
            postParams.put(USER_COLUMN_DISPLAY_NAME, user.getDisplayName());
            postParams.put(USER_COLUMN_FAMILY_NAME, user.getFamilyName());
            postParams.put(USER_COLUMN_GIVEN_NAME, user.getGivenName());
            postParams.put(USER_COLUMN_AUTH_SYS_ID, user.getAuthSystemId());
            postParams.put(USER_COLUMN_EMAIL, user.getEmail());
            postParams.put(USER_COLUMN_PHOTO_URL, user.getPhotoUrl());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }
}