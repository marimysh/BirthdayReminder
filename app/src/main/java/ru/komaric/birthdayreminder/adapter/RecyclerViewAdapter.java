package ru.komaric.birthdayreminder.adapter;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.komaric.birthdayreminder.R;
import ru.komaric.birthdayreminder.util.Util;
import ru.komaric.birthdayreminder.sql.FileManager;
import ru.komaric.birthdayreminder.sql.Person;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<Person> persons;

    public RecyclerViewAdapter() {
        persons = new ArrayList<>();
    }

    public void setData(ArrayList<Person> persons) {
        this.persons = persons;
    }

    public ArrayList<Person> getData() {
        return persons;
    }

    public void insertAndNotify(Person person) {
        person.daysLeft = Util.countDaysLeft(person.date);
        for (int i = 0; i < persons.size(); ++i) {
            if (person.daysLeft < persons.get(i).daysLeft) {
                persons.add(i, person);
                notifyDataSetChanged();
                return;
            }
        }
        persons.add(person);
        notifyDataSetChanged();
    }

    public void updateAndNotify(Person person, int position) {
        persons.remove(position);
        insertAndNotify(person);
    }

    public void removeAndNotify(int position) {
        persons.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String cake_emoji = new String(Character.toChars(0x1F382));

        Person person = persons.get(position);

        holder.name.setText(person.name);
        holder.date.setText(person.date);

        if (person.daysLeft == 0) {
            holder.daysLeft.setText(cake_emoji);
        } else {
            holder.daysLeft.setText(String.valueOf(person.daysLeft) + "d");
        }

        holder.typeImage.setColorFilter(Color.GRAY);
        switch (person.type) {

            case Person.LOCAL_TYPE: {
                holder.typeImage.setImageURI(null);
                Uri image = person.getImageUri();
                if (image != null) {
                    holder.imageView.setImageURI(image);
                    holder.imageView.clearColorFilter();
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_account_black);
                    holder.imageView.setColorFilter(person.color);
                }
                break;
            }

            case Person.CONTACTS_TYPE: {
                holder.typeImage.setImageResource(R.drawable.book);
                Uri image = person.getImageUri();
                if (image != null) {
                    holder.imageView.setImageURI(image);
                    holder.imageView.clearColorFilter();
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_account_black);
                    holder.imageView.setColorFilter(person.color);
                }
                break;
            }

            case Person.VK_TYPE: {
                holder.typeImage.setImageResource(R.drawable.vk);
                String imageUrl = person.image;
                if (FileManager.getFileManager().Vk.isEmptyImage(imageUrl)) {
                    holder.imageView.setImageResource(R.drawable.ic_account_black);
                    holder.imageView.setColorFilter(person.color);
                } else {
                    holder.imageView.clearColorFilter();
                    File imageFile = FileManager.getFileManager().Vk.getImageFileIfExists(imageUrl);
                    if (imageFile != null) {
                        Uri imageUri = Uri.fromFile(imageFile);
                        holder.imageView.setImageURI(imageUri);
                    } else {
                        holder.imageView.setImageURI(null);
                    }
                }
                break;
            }
        }
    }

    //ClickListener
    private static ClickListener clickListener;

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        RecyclerViewAdapter.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CircleImageView imageView;
        private TextView name;
        private TextView date;
        private TextView daysLeft;
        private CircleImageView typeImage;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (CircleImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            daysLeft = (TextView) itemView.findViewById(R.id.days_left);
            typeImage = (CircleImageView) itemView.findViewById(R.id.type);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }
}
