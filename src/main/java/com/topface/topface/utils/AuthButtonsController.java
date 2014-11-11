package com.topface.topface.utils;

import android.content.Context;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.utils.config.AppConfig;
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

    private Context mContext;
    private HashSet<String> activeButtons = new HashSet<>(); // Те кнопки, которые реально показываются пользователю в данный момент
    private HashSet<String> realButtons = new HashSet<>(); // Те кнопки, которые изначально показываются пользователю
    private final HashSet<String> allSocials = new HashSet<>();
    private LinkedList<HashSet<String>> allScreenSocials = new LinkedList<>();
    private String locale;

    private AppConfig mAppConfig;

    public AuthButtonsController(Context context) {
        mContext = context;
        mAppConfig = App.getAppConfig();
        initAllSocialsForLocale();
        loadButtons();
    }

    private void createLocale() {
        locale = getLocale();
        for (String sn : realButtons) {
            locale += sn;
        }
    }

    private void initAllSocialsForLocale() {
        locale = getLocale();
        Collections.addAll(allSocials, AuthToken.SN_FACEBOOK, AuthToken.SN_VKONTAKTE, AuthToken.SN_ODNOKLASSNIKI);
        allScreenSocials = new LinkedList<>();
        if (locale.equals("Ru")) {
            allScreenSocials.add(new HashSet<>(Arrays.asList(AuthToken.SN_FACEBOOK, AuthToken.SN_VKONTAKTE, AuthToken.SN_ODNOKLASSNIKI)));
        } else {
            allScreenSocials.add(new HashSet<>(Arrays.asList(AuthToken.SN_FACEBOOK)));

        }
    }

    private String getLocale() {
        Locale lang = Locale.getDefault();
        HashSet<Locale> ruLocales = new HashSet<>();
        ruLocales.add(new Locale("ru", "RU"));
        ruLocales.add(new Locale("uk", "UA"));
        ruLocales.add(new Locale("be", "BY"));
        return ruLocales.contains(lang) ? "Ru" : "Other";
    }

    private void saveButtons() {
        mAppConfig.setSocialButtonsSettings(toJson());
        mAppConfig.saveConfig();
    }

    private void loadButtons() {
        String json = mAppConfig.getSocialButtonsSettings();
        activeButtons = fromJson(json);

        if (activeButtons.size() == 0) {
            realButtons = getButtonsSettings();
            activeButtons = realButtons;
            saveButtons();
        }
        createLocale();
    }

    public boolean isSocialNetworkActive(String sn) {
        return activeButtons.contains(sn);
    }

    public HashSet<String> getOthers() {
        HashSet<String> others = new HashSet<>();
        for (String sn : allSocials) {
            if (!activeButtons.contains(sn)) {
                others.add(sn);
            }
        }
        return others;
    }

    public void switchSettings() {
        activeButtons = getOthers();
    }

    public void addSocialNetwork(String sn) {
        realButtons.add(sn);
        activeButtons.add(sn);
        saveButtons();
    }

    public HashSet<String> getButtonsSettings() {
        HashSet<String> settings = new HashSet<>();
        if (mContext != null) {
            String android_id = android.provider.Settings.Secure.getString(mContext.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

            if (android_id == null || android_id.length() == 0) {
                int number = (new Random()).nextInt(allScreenSocials.size());
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
        realButtons = new HashSet<>();
        try {
            if (!TextUtils.isEmpty(json)) {
                JSONArray object = new JSONArray(json);
                for (int i = 0; i < object.length(); i++) {
                    realButtons.add(object.optString(i));
                }

            }
        } catch (JSONException e) {
            Debug.error(e);
        }
        return realButtons;
    }

    public String getLocaleTag() {
        if (locale == null) {
            createLocale();
        }
        return locale;
    }
}
