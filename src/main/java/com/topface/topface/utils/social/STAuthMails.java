package com.topface.topface.utils.social;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.topface.topface.App;
import com.topface.topface.utils.config.AppConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by mbautin on 20.10.14.
 * <p/>
 * набор методов для работы с сохраняемыми email
 * посредством которых прошла успешная авторизация в st акк
 */
public class STAuthMails {
    // разделитель email'оф
    private static final String SEPARATOR = "::";
    // ограничение для максимального кол-ва "запоминаемых" email для одной буквы
    private static final int LIMIT_PER_LETTER = 5;

    public static void addEmail(String email) {
        ArrayList<String> list = getSavedEmailsList();

        // последнее удачное мыло всегда добавляется в конец, дабы "обновить" время его ввода
        list.remove(email);
        list.add(email);

        AppConfig appConfig = App.getAppConfig();
        appConfig.setSavedEmailList(TextUtils.join(SEPARATOR, list));
        appConfig.saveConfig();
    }

    public static ArrayList<String> getSavedEmailsList() {
        String emails = App.getAppConfig().getSavedEmailList();
        String[] emailsList = TextUtils.split(emails, SEPARATOR);
        return new ArrayList<>(Arrays.asList(emailsList));
    }

    public static void initInputField(Context context, AutoCompleteTextView view) {
        ArrayList<String> list = getSavedEmailsList();
        // что бы была корректная "сортировка" по времени использования
        // вверх лезут те, которые были использованы последними
        Collections.reverse(list);
        HashMap<Character, Integer> counter = new HashMap<>();
        ArrayList<String> converted = new ArrayList<>();

        // сортировка всего списка мыл по принципу - более @LIMIT_PER_LETTER на одну букву - нельзя
        for (String email : list) {
            if (TextUtils.isEmpty(email)) {
                Character c = email.charAt(0);
                boolean add = false;

                if (counter.containsKey(c)) {
                    if (counter.get(c) < LIMIT_PER_LETTER) {
                        add = true;
                        counter.put(c, counter.get(c) + 1);
                    }
                } else {
                    add = true;
                    counter.put(c, 1);
                }

                if (add) {
                    converted.add(email);
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, converted);
        view.setAdapter(adapter);
    }
}
