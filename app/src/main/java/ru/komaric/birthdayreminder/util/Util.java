package ru.komaric.birthdayreminder.util;

import android.graphics.Color;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class Util {
    private static Random rnd;

    public static int randomColor() {
        if (rnd == null) {
            synchronized (Util.class) {
                if (rnd == null) {
                    rnd = new Random();
                }
            }
        }
        int red = 0, green = 0, blue = 0;
        while (red < 20 || red > 230)
            red = rnd.nextInt(256);
        while (green < 20 || green > 230)
            green = rnd.nextInt(256);
        while (blue < 20 || blue > 230)
            blue = rnd.nextInt(256);
        return Color.argb(255, red, green, blue);
    }

    public static int countDaysLeft(String date) {
        SimpleDateFormat sdf_default = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        SimpleDateFormat sdf_short = new SimpleDateFormat("dd.MM", Locale.ENGLISH);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar calendar = Calendar.getInstance();
        if (date.length() == 10) try {
            calendar.setTime(sdf_default.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        else try {
            calendar.setTime(sdf_short.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
        if (calendar.compareTo(today) < 0) calendar.add(Calendar.YEAR, 1);
        return (int) ((calendar.getTimeInMillis() - today.getTimeInMillis()) / 86400000);

    }

    public static String normalizeDate(String date) {
        if (date.length() == 10 || date.length() == 5) {
            return date;
        }
        int firstDot = date.indexOf('.');
        String day, month, yearWithDot;
        day = date.substring(0, firstDot);
        if (day.length() == 1)
            day = "0" + day;
        int secondDot = date.indexOf('.', firstDot + 1);
        if (secondDot == -1) {
            yearWithDot = "";
            month = date.substring(firstDot + 1);
        } else {
            yearWithDot = date.substring(secondDot);
            month = date.substring(firstDot + 1, secondDot);
        }
        if (month.length() == 1) {
            month = "0" + month;
        }
        return day + "." + month + yearWithDot;
    }
}
