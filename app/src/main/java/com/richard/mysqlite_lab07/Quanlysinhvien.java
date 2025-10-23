package com.richard.mysqlite_lab07;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Quanlysinhvien extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "dbsvdemo";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "sinhvien";

    public static final String KEY_ID = "_id";
    public static final String KEY_HO = "Hosv";
    public static final String KEY_TEN = "Tensv";
    public static final String KEY_LOP = "Lop";

    public Quanlysinhvien(Context context) {
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
        // ...existing code...
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Add a Sinhvien
    public long addSinhvien(Sinhvien sv) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = sv.toContentValues();
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // Return all Sinhvien as a List
    public List<Sinhvien> getAllSv() {
        List<Sinhvien> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                Sinhvien sv = Sinhvien.fromCursor(cursor);
                if (sv != null) list.add(sv);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}

