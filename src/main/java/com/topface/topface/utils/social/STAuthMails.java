package com.topface.topface.utils.social;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.topface.topface.App;
import com.topface.topface.utils.config.AppConfig;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * Created by mbautin on 20.10.14.
 * <p/>
 * набор методов для работы с сохраняемыми email
 * посредством которых прошла успешная авторизация в st акк
 */
public class STAuthMails {
    // разделитель email'оф
    private static final String SEPARATOR = "::";

    public static void addEmail(String email) {
        TreeSet<String> treeSet = getSavedEmailsList();

        treeSet.add(email);

        AppConfig appConfig = App.getAppConfig();
        appConfig.setSavedEmailList(TextUtils.join(SEPARATOR, treeSet));
        appConfig.saveConfig();
    }

    public static TreeSet<String> getSavedEmailsList() {
        String emails = App.getAppConfig().getSavedEmailList();
        String[] emailsList = TextUtils.split(emails, SEPARATOR);
        return new TreeSet<>(Arrays.asList(emailsList));
    }

    public static void initInputField(Context context, AutoCompleteTextView view) {
        TreeSet<String> treeSet = getSavedEmailsList();
        String[] emails = treeSet.toArray(new String[treeSet.size()]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, emails);
        view.setAdapter(adapter);
        view.setThreshold(1);
    }
}
