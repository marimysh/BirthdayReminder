package ru.komaric.birthdayreminder.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

import ru.komaric.birthdayreminder.sql.ListBuilder;
import ru.komaric.birthdayreminder.sql.Person;

public class PersonsLoader extends AsyncTaskLoader<ArrayList<Person>> {

    Context context;

    public PersonsLoader(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public ArrayList<Person> loadInBackground() {
        return ListBuilder.getPersons(context);
    }
}
