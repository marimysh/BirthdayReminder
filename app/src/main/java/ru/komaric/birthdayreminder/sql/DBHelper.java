package ru.komaric.birthdayreminder.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {

    final static int VERSION = 1;
    final static String DB_NAME = "BirthdayDB";
    final static String TABLE_LOCAL = "local";
    final static String TABLE_VK = "vk";
    final static String NAME = "name";
    final static String DATE = "date";
    final static String SHOW = "show";
    final static String ID = "_id";
    final static String IMAGE = "image";

    final static String LOCAL_TABLE_CREATE =
            "CREATE TABLE " + TABLE_LOCAL + "(" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                    ", " + NAME + " TEXT NOT NULL" +
                    ", " + DATE + " TEXT NOT NULL" +
                    ", " + IMAGE + " TEXT" +
                    ");";
    final static String VK_TABLE_CREATE =
            "CREATE TABLE " + TABLE_VK + "(" +
                    ID + " INTEGER PRIMARY KEY" +
                    ", " + NAME + " TEXT NOT NULL" +
                    ", " + DATE + " TEXT NOT NULL" +
                    ", " + SHOW + " INTEGER NOT NULL" +
                    ", " + IMAGE + " TEXT" +
                    ");";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOCAL_TABLE_CREATE);
        db.execSQL(VK_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
