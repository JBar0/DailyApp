package com.example.jakuba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "news";
    public static final String COL_ID = "_id";
    public static final String COL_DATE = "date";
    public static final String COL_NAME = "name";
    public static final String COL_ARTIST = "artist";
    public static final String COL_SUMMARY = "summary";

    public DbHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTabel = "CREATE TABLE IF NOT EXISTS news " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, date DATETIME DEFAULT (strftime('%d-%m-%Y', 'now', 'localtime')), " +
                "name TEXT, artist TEXT, summary TEXT, UNIQUE(name, artist, summary));";
        db.execSQL(createTabel);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//        onCreate(db);
    }

    public boolean addData(String name, String artist, String summary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_ARTIST, artist);
        contentValues.put(COL_SUMMARY, summary);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor retrieveByDate(String date) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT name FROM news WHERE date = '" + date + "';", null);
        return cursor;
    }

    public Cursor retrieveByTitle(String title) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT name, artist, summary FROM news WHERE name = \"" + title + "\";", null);
        return cursor;
    }

    public Cursor retrieveDate() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT DISTINCT date FROM news;", null);
        return cursor;
    }

    public Cursor retrieveTitle() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT name FROM news;", null);
        return cursor;
    }


}
