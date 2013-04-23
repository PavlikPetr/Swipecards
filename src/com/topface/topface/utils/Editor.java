package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;

public class Editor {

    private static final String EDITOR_PREFERENCES = "editor_preferences";
    private static boolean sEditor;

    public static void init(Profile profile) {
        sEditor = profile.isEditor();
        if (sEditor) {
            Debug.setDebugStatus(sEditor);
            initSettings();
        }
   }

    private static void initSettings() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = App.getContext()
                        .getSharedPreferences(EDITOR_PREFERENCES, Context.MODE_PRIVATE);
                preferences.getString("api_url", Static.API_URL);
            }
        }).start();
    }

    public static boolean isEditor() {
        return sEditor;
    }
}
