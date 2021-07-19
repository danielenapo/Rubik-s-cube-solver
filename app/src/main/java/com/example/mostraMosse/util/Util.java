package com.example.mostraMosse.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Util {
    private static String DIMENSION_KEY = "dim";
    private static String DIMENSION_SAVED = "saved";


    private static void savePref(SharedPreferences prefs, String key1, int value1,
                                    String key2, boolean value2) {
        Editor edit = prefs.edit();
        edit.putInt(key1, value1);
        edit.putBoolean(key2, value2);
        edit.apply();
    }

    public static void saveDimension(SharedPreferences prefs, int value) {
        savePref(prefs, DIMENSION_KEY, value, DIMENSION_SAVED, true);
    }

    public static int getDimension(SharedPreferences prefs) {
        return prefs.getInt(DIMENSION_KEY, 3);
    }

    public static boolean dimensionSaved(SharedPreferences prefs) {
        return prefs.getBoolean(DIMENSION_SAVED, false);
    }

}
