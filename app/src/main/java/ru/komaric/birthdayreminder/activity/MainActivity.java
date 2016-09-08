package ru.komaric.birthdayreminder.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vk.sdk.VKSdk;

import java.util.ArrayList;

import ru.komaric.birthdayreminder.R;
import ru.komaric.birthdayreminder.adapter.RecyclerViewAdapter;
import ru.komaric.birthdayreminder.loader.PersonsLoader;
import ru.komaric.birthdayreminder.loader.PersonsLoaderWithRefresh;
import ru.komaric.birthdayreminder.sql.FileManager;
import ru.komaric.birthdayreminder.sql.Local;
import ru.komaric.birthdayreminder.sql.Person;
import ru.komaric.birthdayreminder.sql.Preference;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.ClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<Person>>,
        SwipeRefreshLayout.OnRefreshListener {

    private final int PERSONS_LOADER_ID = 0;
    private final int PERSONS_LOADER_WITH_REFRESH_ID = 1;

    public final static int IN_MAIN_ACTIVITY = 0;

    final int EDIT_ACTIVITY_ADD = 0;
    final int EDIT_ACTIVITY_EDIT = 1;
    final int PREFERENCE_ACTIVITY = 2;

    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    RecyclerViewAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    int clickedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Preference.VK, false)
                && !VKSdk.wakeUpSession(this)) {
            VKSdk.login(this);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            adapter.setData((ArrayList<Person>) savedInstanceState.getSerializable(Person.KEY));
            adapter.notifyDataSetChanged();
        } else {
            Loader<ArrayList<Person>> loader = getSupportLoaderManager().initLoader(PERSONS_LOADER_ID, null, this);
            loader.forceLoad();
        }

        swipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        Loader<ArrayList<Person>> loader = getSupportLoaderManager().initLoader(PERSONS_LOADER_WITH_REFRESH_ID, null, this);
        loader.forceLoad();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Person> persons = adapter.getData();
        outState.putSerializable(Person.KEY, persons);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case IN_MAIN_ACTIVITY:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Loader<ArrayList<Person>> loader = getSupportLoaderManager().restartLoader(PERSONS_LOADER_ID, null, this);
                    loader.forceLoad();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.add).setVisible(true);
        menu.findItem(R.id.settings).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.add:
                intent = new Intent(this, EditActivity.class);
                startActivityForResult(intent, EDIT_ACTIVITY_ADD);
                return true;
            case R.id.settings:
                intent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(intent, PREFERENCE_ACTIVITY);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(int position, View v) {
        Person person = adapter.getData().get(position);
        switch (person.type) {
            case Person.LOCAL_TYPE:
                clickedPosition = position;
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra(Person.KEY, person);
                startActivityForResult(intent, EDIT_ACTIVITY_EDIT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Person person;
            Local local;
            switch (requestCode) {
                case EDIT_ACTIVITY_ADD:
                    //TODO: фоты!!!
                    person = (Person) data.getSerializableExtra(Person.KEY);
                    local = new Local(this);
                    local.open();
                    person.id = local.insert(person);
                    local.close();
                    adapter.insertAndNotify(person);
                    break;
                case EDIT_ACTIVITY_EDIT:
                    person = (Person) data.getSerializableExtra(Person.KEY);
                    if (person.name == null) { //тк local.remove(>>>PERSON<<<)
                        //TODO: фоты!!!
                        local = new Local(this);
                        local.open();
                        local.remove(person);
                        local.close();
                        adapter.removeAndNotify(clickedPosition);
                    } else {
                        //TODO: фоты!!!
                        local = new Local(this);
                        local.open();
                        local.update(person);
                        local.close();
                        adapter.updateAndNotify(person, clickedPosition);
                    }
                    break;
                case PREFERENCE_ACTIVITY:
                    Loader<ArrayList<Person>> loader = getSupportLoaderManager().initLoader(PERSONS_LOADER_WITH_REFRESH_ID, null, this);
                    loader.forceLoad();
            }
        }
    }

    @Override
    public Loader<ArrayList<Person>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case PERSONS_LOADER_ID:
                return new PersonsLoader(this);
            case PERSONS_LOADER_WITH_REFRESH_ID:
                return new PersonsLoaderWithRefresh(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Person>> loader, ArrayList<Person> data) {
        adapter.setData(data);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        if (loader.getId() == PERSONS_LOADER_WITH_REFRESH_ID) {
            AsyncTask<ArrayList<Person>, Integer, Void> task = new AsyncTask<ArrayList<Person>, Integer, Void>() {
                @Override
                @SafeVarargs
                protected final Void doInBackground(ArrayList<Person>... arrayLists) {
                    ArrayList<Person> persons = arrayLists[0];
                    FileManager fileManager = FileManager.getFileManager();
                    for (int i = 0; i < persons.size(); ++i) {
                        Person person = persons.get(i);
                        if (person.type == Person.VK_TYPE
                                && fileManager.Vk.getImageFileIfExists(person.image) == null) {
                            fileManager.Vk.download(person.image);
                            publishProgress(i);
                        }
                    }
                    return null;
                }

                @Override
                protected final void onProgressUpdate(Integer... values) {
                    super.onProgressUpdate(values);
                    adapter.notifyItemChanged(values[0]);
                }
            };
            task.execute(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Person>> loader) {

    }
}
