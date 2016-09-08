package ru.komaric.birthdayreminder.loader;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import ru.komaric.birthdayreminder.sql.ListBuilder;
import ru.komaric.birthdayreminder.sql.Person;
import ru.komaric.birthdayreminder.sql.Preference;
import ru.komaric.birthdayreminder.sql.VK;

public class PersonsLoaderWithRefresh extends AsyncTaskLoader<ArrayList<Person>> {

    Context context;

    public PersonsLoaderWithRefresh(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public ArrayList<Person> loadInBackground() {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Preference.VK, false) ) {
            VK vk = new VK(context);
            vk.open();
            vk.refresh();
            vk.close();
        }
        return ListBuilder.getPersons(context);
    }
}
