package com.richard.mysqlite_lab07;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Sinhvien extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dbsvdemo";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "sinhvien";

    private static final String KEY_ID = "_id";
    private static final String KEY_HO = "Hosv";
    private static final String KEY_TEN = "Tensv";
    private static final String KEY_LOP = "Lop";

    public Sinhvien(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_HO + " TEXT,"
                + KEY_TEN + " TEXT,"
                + KEY_LOP + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void createSv(String ho, String ten, String lop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_HO, ho);
        values.put(KEY_TEN, ten);
        values.put(KEY_LOP, lop);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public Cursor getAllSv() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
