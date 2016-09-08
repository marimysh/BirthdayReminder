package ru.komaric.birthdayreminder.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.komaric.birthdayreminder.R;
import ru.komaric.birthdayreminder.sql.Person;
import ru.komaric.birthdayreminder.util.Util;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    CircleImageView imageView;
    EditText etName;
    EditText etDate;
    Uri image_uri;
    Person oldPerson;
    int color;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((ImageView) findViewById(R.id.calendar_image)).setColorFilter(Color.GRAY); //серая иконка календаря

        imageView = (CircleImageView) findViewById(R.id.image);
        etName = (EditText) findViewById(R.id.name);
        etDate = (EditText) findViewById(R.id.date);
        etDate.setOnClickListener(this);

        Intent intent = getIntent();
        oldPerson = (Person) intent.getSerializableExtra(Person.KEY);
        if (oldPerson == null) {
            getSupportActionBar().setTitle(R.string.add);
            imageView.setImageResource(R.drawable.ic_account_black);
            color = Util.randomColor();
            imageView.setColorFilter(color);
        } else {
            getSupportActionBar().setTitle(R.string.edit);
            etName.setText(oldPerson.name);
            etDate.setText(oldPerson.date);
            image_uri = oldPerson.getImageUri();
            if (image_uri == null) {
                imageView.setImageResource(R.drawable.ic_account_black);
                imageView.setColorFilter(oldPerson.color);
            } else {
                //TODO: фоты!!!
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.done).setVisible(true);
        if (oldPerson != null) {
            menu.findItem(R.id.delete).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.done: {
                String name = etName.getText().toString();
                String date = etDate.getText().toString();
                if (name.isEmpty() || date.isEmpty()) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(this);
                    adb.setTitle(R.string.error);
                    adb.setMessage(R.string.empty_fields_error);
                    adb.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    adb.create().show();
                    return false;
                }
                Intent intent = new Intent();
                Person person;
                if (oldPerson != null) {
                    person = new Person(oldPerson);
                    person.name = name;
                    person.date = date;
                    person.setImage(image_uri);
                } else {
                    person = new Person(name, date, image_uri, Person.LOCAL_TYPE);
                    person.color = color;
                }
                intent.putExtra(Person.KEY, person); //id и daysLeft потом посчитаем
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
            case R.id.delete: {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(R.string.deleting_title);
                adb.setMessage(R.string.deleting_message);
                adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        oldPerson.name = null;
                        intent.putExtra(Person.KEY, oldPerson);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
                adb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                adb.show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.date:
                final Calendar calendar = Calendar.getInstance();
                final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
                if (!etDate.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(sdf.parse(etDate.getText().toString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        etDate.setText(sdf.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
                break;
        }
    }
}
