package com.pearnode.app.placero.position;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.util.AndroidSystemUtil;

public class PositionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    public static final String TABLE_NAME = "position_master";

    private static final String UNIQUE_ID = "uid";
    private static final String AREA_REF = "ar";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String DESCRIPTION = "desc";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String TAGS = "tags";
    private static final String DIRTY = "dirty";
    private static final String D_ACTION = "d_action";
    private static final String CREATED_ON = "created_on";

    private Context context = null;
    public PositionsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                        NAME + " text," +
                        TYPE + " text," +
                        DESCRIPTION + " text," +
                        LAT + " text, " +
                        LON + " text," +
                        AREA_REF + " text," +
                        UNIQUE_ID + " text," +
                        CREATED_ON + " text," +
                        DIRTY + " integer DEFAULT 0," +
                        D_ACTION + " text," +
                        TAGS + " text)"
        );
    }

    public void dryRun() {
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Position addPostion(Position pe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, pe.getId());
        contentValues.put(NAME, pe.getName());
        contentValues.put(TYPE, pe.getType());
        contentValues.put(DESCRIPTION, pe.getDescription());
        contentValues.put(LAT, pe.getLat());
        contentValues.put(LON, pe.getLng());
        contentValues.put(TAGS, pe.getTags());
        contentValues.put(DIRTY, pe.getDirty());
        contentValues.put(D_ACTION, pe.getDirtyAction());
        contentValues.put(CREATED_ON, pe.getCreatedOn());

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public Position updatePosition(Position pe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, pe.getId());
        contentValues.put(AREA_REF, pe.getAreaRef());
        contentValues.put(NAME, pe.getName());
        contentValues.put(TYPE, pe.getType());
        contentValues.put(DESCRIPTION, pe.getDescription());
        contentValues.put(LAT, pe.getLat());
        contentValues.put(LON, pe.getLng());
        contentValues.put(TAGS, pe.getTags());
        contentValues.put(DIRTY, pe.getDirty());
        contentValues.put(D_ACTION, pe.getDirtyAction());
        contentValues.put(CREATED_ON, pe.getCreatedOn());

        db.update(TABLE_NAME, contentValues, UNIQUE_ID + "=?",
                new String[]{pe.getId()});
        db.close();
        return pe;
    }

    public void deletePosition(Position pe) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, UNIQUE_ID + "=?", new String[]{pe.getId()});
        db.close();
        return;
    }

    public void deletePositionByAreaId(String areaRef) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE "
                + AREA_REF + " = '" + areaRef + "'");
        db.close();
    }

    public ArrayList<Position> getPositionsForArea(Area ae) {
        ArrayList<Position> pes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME
                        + " WHERE " + AREA_REF + "=? AND "
                        + D_ACTION + "<> 'delete'",
                new String[]{ae.getId()});
        if((cursor != null) && (cursor.getCount() > 0)){
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                Position pe = new Position();
                pe.setId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                pe.setAreaRef(cursor.getString(cursor.getColumnIndex(AREA_REF)));
                pe.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                pe.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                String posDesc = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
                if (!posDesc.trim().equalsIgnoreCase("")) {
                    pe.setDescription(posDesc);
                }

                String latStr = cursor.getString(cursor.getColumnIndex(LAT));
                pe.setLat(Double.parseDouble(latStr));

                String lonStr = cursor.getString(cursor.getColumnIndex(LON));
                pe.setLng(Double.parseDouble(lonStr));

                pe.setTags(cursor.getString(cursor.getColumnIndex(TAGS)));
                pe.setCreatedOn(cursor.getString(cursor.getColumnIndex(CREATED_ON)));

                pe.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                pe.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));

                if (!pes.contains(pe)) {
                    pes.add(pe);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return pes;
    }

    public ArrayList<Position> getDirtyPositions() {
        ArrayList<Position> pes = new ArrayList<Position>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME
                + " WHERE " + DIRTY + " = 1", null);
        if((cursor != null) && (cursor.getCount() > 0)){
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                Position pe = new Position();
                pe.setId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                pe.setAreaRef(cursor.getString(cursor.getColumnIndex(AREA_REF)));
                pe.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                pe.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                String posDesc = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
                if (!posDesc.trim().equalsIgnoreCase("")) {
                    pe.setDescription(posDesc);
                }
                String latStr = cursor.getString(cursor.getColumnIndex(LAT));
                pe.setLat(Double.parseDouble(latStr));
                String lonStr = cursor.getString(cursor.getColumnIndex(LON));
                pe.setLng(Double.parseDouble(lonStr));
                pe.setTags(cursor.getString(cursor.getColumnIndex(TAGS)));
                pe.setCreatedOn(cursor.getString(cursor.getColumnIndex(CREATED_ON)));

                pe.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                pe.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));

                if (!pes.contains(pe)) {
                    pes.add(pe);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return pes;
    }

    public Position getPositionById(String positionId) {
        SQLiteDatabase db = getReadableDatabase();
        Position pe = null;
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME
                        + " WHERE " + UNIQUE_ID + "=?",
                new String[]{positionId});
        if((cursor != null) && (cursor.getCount() > 0)){
            cursor.moveToFirst();
            pe = new Position();
            pe.setId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
            pe.setAreaRef(cursor.getString(cursor.getColumnIndex(AREA_REF)));
            pe.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            pe.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
            pe.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));

            String latStr = cursor.getString(cursor.getColumnIndex(LAT));
            pe.setLat(Double.parseDouble(latStr));

            String lonStr = cursor.getString(cursor.getColumnIndex(LON));
            pe.setLng(Double.parseDouble(lonStr));

            pe.setTags(cursor.getString(cursor.getColumnIndex(TAGS)));
            pe.setCreatedOn(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
            pe.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
            pe.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));

            cursor.close();
        }
        db.close();
        return pe;
    }

    private JSONObject preparePostParams(String queryType, Position pe) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "PositionMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID", AndroidSystemUtil.getDeviceId(context));
            postParams.put("lon", pe.getLng() + "");
            postParams.put("lat", pe.getLat() + "");
            postParams.put("desc", pe.getDescription());
            postParams.put("tags", pe.getTags());
            postParams.put("name", pe.getName());
            postParams.put("type", pe.getType());
            postParams.put("area_ref", pe.getAreaRef());
            postParams.put("id", pe.getId());
            postParams.put("created_on", pe.getCreatedOn());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deletePositionsLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, DIRTY + " = 0", null);
        db.close();
    }

}