package ru.komaric.birthdayreminder.sql;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.komaric.birthdayreminder.activity.MainActivity;
import ru.komaric.birthdayreminder.util.Util;

public final class ListBuilder {

    public static ArrayList<Person> getPersons(Context context) {
        Local local = new Local(context);
        local.open();
        ArrayList<Person> persons = local.getPersons();
        local.close();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean(Preference.CONTACTS, false)) {
            persons.addAll(getContacts(context));
        }

        if (sp.getBoolean(Preference.VK, false)) {
            VK vk = new VK(context);
            vk.open();
            persons.addAll(vk.getPersons());
            vk.close();
        }

        for (Person person : persons) {
            person.color = Util.randomColor();
            person.daysLeft = Util.countDaysLeft(person.date);
        }

        Collections.sort(persons, new Comparator<Person>() {
            @Override
            public int compare(Person l, Person r) {
                return l.daysLeft - r.daysLeft;
            }
        });
        return persons;
    }

    private static List<Person> getContacts(Context context) {
        List<Person> persons = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MainActivity.REQUEST_PERMISSIONS_CONTACTS);
            return persons;
        }

        String[] projection = new String[]{
                ContactsContract.Data._ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.Data.PHOTO_THUMBNAIL_URI
        };
        String selection = ContactsContract.Data.MIMETYPE + " = ?" + " AND " +
                ContactsContract.CommonDataKinds.Event.TYPE + " = " +
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                new String[]{ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE},
                null);
        if (cursor != null) {
            int id_ind = cursor.getColumnIndex(ContactsContract.Data._ID);
            int name_ind = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
            int date_ind = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
            int image_ind = cursor.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(id_ind);
                String name = cursor.getString(name_ind);
                String date = cursor.getString(date_ind);
                date = date.substring(8, 10) + "." +
                        date.substring(5, 7) + "." +
                        date.substring(0, 4);
                String image = cursor.getString(image_ind);
                persons.add(new Person(id, name, date, 0, image, Person.CONTACTS_TYPE));
            }
            cursor.close();
        }
        return persons;
    }
}
