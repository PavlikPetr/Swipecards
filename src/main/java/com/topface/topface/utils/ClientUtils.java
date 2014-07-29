package com.topface.topface.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;

import java.util.ArrayList;
import java.util.List;

public class ClientUtils {
    public static final String FALLBACK_LOCALE = "en_US";


    public static String getClientLocale(Context context) {
        String locale;
        //На всякий случай проверяем возможность получить локаль
        try {
            locale = context.getResources().getString(R.string.app_locale);
        } catch (Exception e) {
            locale = FALLBACK_LOCALE;
        }

        return locale;
    }

    public static List<String> getClientAccounts() {
        List<String> result = new ArrayList<>();
        try {
            Account[] accounts = AccountManager.get(App.getContext()).getAccounts();
            for (Account account : accounts) {
                if (Utils.isValidEmail(account.name) && !result.contains(account.name)) {
                    result.add(account.name);
                }
            }
        } catch (Exception ex) {
            Debug.error(ex);
        }
        return result;
    }
}
