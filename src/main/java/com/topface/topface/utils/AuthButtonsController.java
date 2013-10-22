package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.topface.Static;
import com.topface.topface.utils.social.AuthToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;

public class AuthButtonsController {

    public static final String BUTTON_SETTINGS = "ButtonSettings";
    private Context mContext;
    private HashSet<String> activeButtons; // Те кнопки, которые реально показываются пользователю в данный момент
    private HashSet<String> realButtons; // Те кнопки, которые изначально показываются пользователю
    private final HashSet<String> allSocials = new HashSet<String>();
    private LinkedList<HashSet<String>> allScreenSocials;
    private String locale;

    private SharedPreferences mPreferences;

    public AuthButtonsController(Context context, final OnButtonsSettingsLoadedListener listener) {
        mContext = context;
        getAllSocialsForLocale();

        loadButtons(new OnButtonsSettingsLoadedListener() {
            @Override
            public void buttonSettingsLoaded(HashSet<String> settings) {
                if(settings.size() == 0) {
                    realButtons = getButtonsSettings();
                    activeButtons = realButtons;
                    saveButtons();
                }
                createLocale();
                listener.buttonSettingsLoaded(activeButtons);
            }
        });
    }

    private void createLocale() {
        locale = getLocale();
        for (String sn : realButtons) {
            locale += sn;
        }
    }

    private void getAllSocialsForLocale() {
        Collections.addAll(allSocials, AuthToken.SN_FACEBOOK, AuthToken.SN_VKONTAKTE, AuthToken.SN_ODNOKLASSNIKI);
        allScreenSocials = new LinkedList<HashSet<String>>();
        allScreenSocials.add(new HashSet<String>(Arrays.asList(AuthToken.SN_FACEBOOK)));
        allScreenSocials.add(new HashSet<String>(Arrays.asList(AuthToken.SN_FACEBOOK, AuthToken.SN_VKONTAKTE)));
        locale = getLocale();
        if (locale.equals("Ru")) {
            allScreenSocials.add(new HashSet<String>(Arrays.asList(AuthToken.SN_FACEBOOK, AuthToken.SN_VKONTAKTE, AuthToken.SN_ODNOKLASSNIKI)));
        }
    }

    private String getLocale() {
        Locale lang = Locale.getDefault();
        HashSet<Locale> ruLocales = new HashSet<Locale>();
        ruLocales.add(new Locale("ru", "RU"));
        ruLocales.add(new Locale("uk", "UA"));
        ruLocales.add(new Locale("be", "BY"));
        return ruLocales.contains(lang)?"Ru":"Other";
    }

    public AuthButtonsController(Context context, HashSet<String> settings) {
        mContext = context;
        activeButtons = settings;
        getAllSocialsForLocale();
        mPreferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
//        saveButtons();
    }

    private void saveButtons() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPreferences = mContext.getSharedPreferences(Static.PREFERENCES_TAG_BUTTONS, Context.MODE_PRIVATE);
                mPreferences.edit().putString(BUTTON_SETTINGS, toJson()).commit();

            }


        }).start();
    }

    private void loadButtons(final OnButtonsSettingsLoadedListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPreferences = mContext.getSharedPreferences(Static.PREFERENCES_TAG_BUTTONS, Context.MODE_PRIVATE);
                String json = mPreferences.getString(BUTTON_SETTINGS, "");
                activeButtons = fromJson(json);
                listener.buttonSettingsLoaded(activeButtons);
            }
        }).start();
    }

    public boolean needSN(String sn) {
        return activeButtons.contains(sn);
    }

    public HashSet<String> getOhters() {
        HashSet<String> others = new HashSet<String>();
        for(String sn: allSocials) {
            if(!activeButtons.contains(sn)) {
                others.add(sn);
            }
        }
        return others;
    }

    public void setAllSettings() {
        activeButtons = allSocials;
    }

    public void switchSettings() {
        activeButtons = getOhters();
    }

    public void addSocialNetwork(String sn) {
        realButtons.add(sn);
        activeButtons.add(sn);
        saveButtons();
    }

    public HashSet<String> getButtonsSettings() {
        HashSet<String> settings = new HashSet<String>();
        if (mContext != null) {
            String android_id = android.provider.Settings.Secure.getString(mContext.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

            if (android_id == null || android_id.length() == 0) {
                int number = (new Random(System.currentTimeMillis())).nextInt(allScreenSocials.size());
                settings = allScreenSocials.get(number);
            } else {
                int value = android_id.hashCode();
                int number = Math.abs(value) % allScreenSocials.size();
                settings = allScreenSocials.get(number);
            }
        }
        if (settings.size() == 0) {
            Collections.addAll(settings, AuthToken.SN_FACEBOOK, AuthToken.SN_VKONTAKTE, AuthToken.SN_ODNOKLASSNIKI);
        }
        return settings;
    }

    private String toJson() {
        JSONArray object = new JSONArray();
        for (String sn : realButtons) {
            object.put(sn);
        }
        return object.toString();
    }

    private HashSet<String> fromJson(String json) {
        realButtons = new HashSet<String>();
        try {
            if (json != "") {
                JSONArray object = new JSONArray(json);
                for(int i = 0; i < object.length(); i++) {
                    realButtons.add(object.optString(i));
                }

            }
        } catch (JSONException e) {
            Debug.error(e);
        }
        return realButtons;
    }

    public static interface OnButtonsSettingsLoadedListener {
        public void buttonSettingsLoaded(HashSet<String> settings);
    }


    public String getLocaleTag() {
        if (locale == null) {
            createLocale();
        }
        return locale;
    }
}
