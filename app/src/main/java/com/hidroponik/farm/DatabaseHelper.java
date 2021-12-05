package com.hidroponik.farm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "data_sensor";
    public static final String DATABASE_TABLE = "log_sensor";
    public static final String COL_ID = "ID";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_PPM = "ppm_value";
    public static final String COL_PH = "ph_value";
    public static final String COL_SUHU = "suhu_value";
    public static final String COL_SETPOINT = "setpoint_value";
    public static final String COL_PABMIX = "p_abmix";
    public static final String COL_PAIR = "p_air";
    public static final String COL_PUP = "p_phu";
    public static final String COL_PDN = "p_phd";

    public SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
        db.setLocale(new Locale("id", "ID"));
    }

    // CREATING TABLES
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + DATABASE_TABLE + "("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COL_TIMESTAMP + " TEXT,"
                        + COL_PPM + " TEXT,"
                        + COL_PH + " TEXT,"
                        + COL_SUHU + " TEXT,"
                        + COL_SETPOINT + " TEXT,"
                        + COL_PABMIX + " TEXT,"
                        + COL_PAIR + " TEXT,"
                        + COL_PUP + " TEXT,"
                        + COL_PDN + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
    }

    //Get All SQLite Data
    public Cursor allData() {
        Cursor cur = db.rawQuery(
                "SELECT * FROM " + DATABASE_TABLE, null);
        return cur;
    }

    //Get 1 Data By ID
    public Cursor oneData(Long id) {
        Cursor cur = db.rawQuery(
                "SELECT * FROM " + DATABASE_TABLE + " WHERE " + COL_ID + "=" + id, null);
        return cur;
    }

    public void insertData(ContentValues values) {
        db.insert(DATABASE_TABLE, null, values);
    }

    public void insertAllData() {}

    //Update Data
    public void updateData(ContentValues values, long id) {
        db.update(DATABASE_TABLE, values, COL_ID + "=" + id, null);
    }

    //Delete Data
    public void deleteData(long id) {
        db.delete(DATABASE_TABLE, COL_ID + "=" + id, null);
    }
}
