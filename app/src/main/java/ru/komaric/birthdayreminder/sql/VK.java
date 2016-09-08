package ru.komaric.birthdayreminder.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.komaric.birthdayreminder.util.Util;

public class VK {

    private final static String TABLE = DBHelper.TABLE_VK;
    private final static String ID = DBHelper.ID;
    private final static String NAME = DBHelper.NAME;
    private final static String DATE = DBHelper.DATE;
    private final static String SHOW = DBHelper.SHOW;
    private final static String IMAGE = DBHelper.IMAGE;

    private Context context;
    private DBHelper dbhelper;
    private SQLiteDatabase db;

    public VK(Context context) {
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
        int show_ind = cursor.getColumnIndex(SHOW);
        int image_ind = cursor.getColumnIndex(IMAGE);

        while (cursor.moveToNext()) {
            if (cursor.getInt(show_ind) == 1) {
                persons.add(new Person(
                        cursor.getLong(id_ind),
                        cursor.getString(name_ind),
                        cursor.getString(date_ind),
                        0,
                        cursor.getString(image_ind),
                        Person.VK_TYPE));
            }
        }
        cursor.close();
        return persons;
    }

    public boolean isEmpty() {
        Cursor cursor = db.query(TABLE, null, null, null, null, null, null);
        boolean empty = true;
        if (cursor.moveToFirst()) {
            empty = false;
        }
        cursor.close();
        return empty;
    }

    public void clearDbAndImages() {
        db.delete(TABLE, null, null);
        FileManager.getFileManager().Vk.deleteAll();
    }

    public void refresh() {
        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,bdate,photo_50"));
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Set<Long> hidden = new HashSet<>();
                Cursor cursor = db.query(TABLE, new String[]{ID}, SHOW + " = 0", null, null, null, null);
                int ind = cursor.getColumnIndex(ID);
                while (cursor.moveToNext()) {
                    hidden.add(cursor.getLong(ind));
                }
                cursor.close();
                db.delete(TABLE, null, null);
                List<String> imageNames = FileManager.getFileManager().Vk.getFileNamesList();
                VKUsersArray usersArray = (VKUsersArray) response.parsedModel;
                for (VKApiUserFull userFull : usersArray) {
                    String date = userFull.bdate;
                    if (date.isEmpty()) {
                        continue;
                    }
                    date = Util.normalizeDate(date);
                    long id = (long) userFull.id;
                    String name = userFull.first_name + " " + userFull.last_name;
                    String imageUrl = userFull.photo_50;
                    String imageName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                    imageNames.remove(imageName);

                    ContentValues cv = new ContentValues();
                    cv.put(ID, id);
                    cv.put(NAME, name);
                    cv.put(DATE, date);
                    cv.put(SHOW, !hidden.contains(id));
                    cv.put(IMAGE, imageUrl);
                    db.insert(TABLE, null, cv);
                }
                FileManager.getFileManager().Vk.delete(imageNames);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
    }
}
