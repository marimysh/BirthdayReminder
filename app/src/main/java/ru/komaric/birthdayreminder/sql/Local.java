package ru.komaric.birthdayreminder.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Local {

    private final static String TABLE = DBHelper.TABLE_LOCAL;
    private final static String NAME =  DBHelper.NAME;
    private final static String DATE = DBHelper.DATE;
    private final static String ID = DBHelper.ID;
    private final static String IMAGE_NAME = DBHelper.IMAGE;

    private Context context;
    private DBHelper dbhelper;
    private SQLiteDatabase db;

    public Local(Context context) {
        this.context = context.getApplicationContext();
    }

    public void open() {
        dbhelper = new DBHelper(context);
        db = dbhelper.getWritableDatabase();
    }

    public void close() {
        if (dbhelper != null) dbhelper.close();
    }

    public ArrayList<Person> getPersons() {
        ArrayList<Person> persons = new ArrayList<>();
        Cursor cursor = db.query(TABLE, null, null, null, null, null, null);

        int id_ind = cursor.getColumnIndex(ID);
        int name_ind = cursor.getColumnIndex(NAME);
        int date_ind = cursor.getColumnIndex(DATE);
        int image_ind = cursor.getColumnIndex(IMAGE_NAME);

        while (cursor.moveToNext()) {
            persons.add(new Person(
                    cursor.getLong(id_ind),
                    cursor.getString(name_ind),
                    cursor.getString(date_ind),
                    0,
                    null,
                    Person.LOCAL_TYPE));
        }

        cursor.close();
        return persons;

        //TODO: фоты!!!
    }

    public long insert(Person person) {
        ContentValues cv = new ContentValues();
        cv.put(NAME, person.name);
        cv.put(DATE, person.date);
        //TODO: фоты!!!
        cv.put(IMAGE_NAME, (String) null);
        return db.insert(TABLE, null, cv);
    }

    public void update(Person person) {
        ContentValues cv = new ContentValues();
        cv.put(NAME, person.name);
        cv.put(DATE, person.date);
        //TODO: фоты!!!
        cv.put(IMAGE_NAME, (String) null);
        db.update(TABLE, cv, ID + " = " + person.id, null);
    }

    public boolean remove(Person person) {
        return 0 == db.delete(TABLE, ID + " = " + person.id, null);
    }
}
