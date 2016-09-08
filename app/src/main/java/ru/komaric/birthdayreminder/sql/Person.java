package ru.komaric.birthdayreminder.sql;

import android.net.Uri;

import java.io.Serializable;

public class Person implements Serializable {
    public static final int CONTACTS_TYPE = 0;
    public static final int LOCAL_TYPE = 1;
    public static final int VK_TYPE = 2;
    public static final int FACEBOOK_TYPE = 3;

    public static final String KEY = "person";

    public long id;
    public String name;
    public String date;
    public int daysLeft;
    public int type;
    public int color;
    public String image;

    public Person(Person person) {
        this.id = person.id;
        this.name = person.name;
        this.date = person.date;
        this.daysLeft = person.daysLeft;
        this.image = person.image;
        this.type = person.type;
        this.color = person.color;
    }

    public Person(long id, String name, String date, int daysLeft, String image, int type) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.daysLeft = daysLeft;
        this.type = type;
        this.image = image;
    }

    public Person(String name, String date, Uri image, int type) {
        this.name = name;
        this.date = date;
        this.type = type;
        setImage(image);
    }

    public Uri getImageUri() {
        return image == null ? null : Uri.parse(image);
    }

    public void setImage(Uri uri) {
        image = uri == null ? null : uri.toString();
    }

}
