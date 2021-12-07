package com.hidroponik.farm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "data_sensor";
    public static final String DATABASE_TABLE = "log_sensor";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    public static final String COLUMN_PPM = "PPM_VALUE";
    public static final String COLUMN_PH = "PH_VALUE";
    public static final String COLUMN_SUHU = "SUHU_VALUE";
    public static final String COLUMN_SETPOINT = "SETPOINT_VALUE";
    public static final String COLUMN_PABMIX = "P_ABMIX";
    public static final String COLUMN_PAIR = "P_AIR";
    public static final String COLUMN_PUP = "P_PH_UP";
    public static final String COLUMN_PDN = "P_PH_DOWN";

    public SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
        db = getReadableDatabase();
        db.setLocale(new Locale("id", "ID"));
//        Log.i(TAG, "Database: " + getDatabaseName() + "Version: " + db.getVersion());
    }

    // CREATING TABLES
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + DATABASE_TABLE + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_TIMESTAMP + " DATETIME DEFAULT (datetime('now','localtime')),"
                        + COLUMN_PPM + " TEXT,"
                        + COLUMN_PH + " TEXT,"
                        + COLUMN_SUHU + " TEXT,"
                        + COLUMN_SETPOINT + " TEXT,"
                        + COLUMN_PABMIX + " TEXT,"
                        + COLUMN_PAIR + " TEXT,"
                        + COLUMN_PUP + " TEXT,"
                        + COLUMN_PDN + " TEXT)";
        db.execSQL(query);
//        Log.i(TAG, "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int i, int i1) {
        throw new SQLiteException("Can't downgrade database from version " + i + " to " + i1);
    }

    public void insertData(ContentValues values) {
        db.insert(DATABASE_TABLE, null, values);
//        Log.i(TAG, "insertData: " + DATABASE_TABLE + values);
    }

}
