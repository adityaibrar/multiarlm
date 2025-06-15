package com.example.mutiralmm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // Database
    public static final String DATABASE_NAME = "dokumen.db";
    public static final int DATABASE_VERSION = 1;

    // Tabel Users
    public static final String TABLE_USER = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Tabel Dokumen
    public static final String TABLE_DOC = "dokumen";
    public static final String COLUMN_DOC_ID = "id";
    public static final String COLUMN_DOC_NAME = "doc_name";
    public static final String COLUMN_DOC_DATE = "doc_date";
    public static final String COLUMN_DOC_NUMBER = "doc_number";
    public static final String COLUMN_DOC_DESC = "doc_desc";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_DOC_YEAR = "doc_year";

    // SQL CREATE TABLE
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT)";

    private static final String SQL_CREATE_DOCS =
            "CREATE TABLE " + TABLE_DOC + " (" +
                    COLUMN_DOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DOC_NAME + " TEXT, " +
                    COLUMN_DOC_DATE + " TEXT, " +
                    COLUMN_DOC_NUMBER + " TEXT, " +
                    COLUMN_DOC_DESC + " TEXT, " +
                    COLUMN_IMAGE_PATH + " TEXT, " +
                    COLUMN_DOC_YEAR + " TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_DOCS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOC);
        onCreate(db);
    }

    // ========== USER METHODS ==========

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USER, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ========== DOCUMENT METHODS ==========

    public boolean insertDocument(String name, String date, String number, String desc, String imagePath, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DOC_NAME, name);
        values.put(COLUMN_DOC_DATE, date);
        values.put(COLUMN_DOC_NUMBER, number);
        values.put(COLUMN_DOC_DESC, desc);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_DOC_YEAR, year);
        long result = db.insert(TABLE_DOC, null, values);
        return result != -1;
    }

//    public
}
