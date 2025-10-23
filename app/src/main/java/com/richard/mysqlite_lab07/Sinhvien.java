package com.richard.mysqlite_lab07;

import android.content.ContentValues;
import android.database.Cursor;

public class Sinhvien {
    private int id;
    private String ho;
    private String ten;
    private String lop;

    public Sinhvien() { }

    // For inserts (no id)
    public Sinhvien(String ho, String ten, String lop) {
        this.ho = ho;
        this.ten = ten;
        this.lop = lop;
    }

    // Full constructor
    public Sinhvien(int id, String ho, String ten, String lop) {
        this.id = id;
        this.ho = ho;
        this.ten = ten;
        this.lop = lop;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getHo() { return ho; }
    public void setHo(String ho) { this.ho = ho; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }

    @Override
    public String toString() {
        return id + ". " + ho + " " + ten + " - " + lop;
    }

    // Convert to ContentValues for DB operations (column names must match Quanlysinhvien)
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(Quanlysinhvien.KEY_HO, ho);
        cv.put(Quanlysinhvien.KEY_TEN, ten);
        cv.put(Quanlysinhvien.KEY_LOP, lop);
        return cv;
    }

    // Build a Sinhvien from a Cursor (assumes columns exist)
    public static Sinhvien fromCursor(Cursor c) {
        if (c == null) return null;
        int idxId = c.getColumnIndex(Quanlysinhvien.KEY_ID);
        int idxHo = c.getColumnIndex(Quanlysinhvien.KEY_HO);
        int idxTen = c.getColumnIndex(Quanlysinhvien.KEY_TEN);
        int idxLop = c.getColumnIndex(Quanlysinhvien.KEY_LOP);
        int id = (idxId != -1) ? c.getInt(idxId) : 0;
        String ho = (idxHo != -1) ? c.getString(idxHo) : "";
        String ten = (idxTen != -1) ? c.getString(idxTen) : "";
        String lop = (idxLop != -1) ? c.getString(idxLop) : "";
        return new Sinhvien(id, ho, ten, lop);
    }
}
