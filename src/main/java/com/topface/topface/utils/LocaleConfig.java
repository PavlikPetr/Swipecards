package com.topface.topface.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.UserSetLocaleRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.social.AuthToken;

import java.util.Locale;

public class LocaleConfig {

    private static final String SYSTEM_LOCALE = "com.topface.topface_system_locale";
    private static final String APPLICATION_LOCALE = "com.topface.topface_application_locale";

    private Context mContext;
    private String mSystemLocale;
    private String mApplicationLocale;
    public static boolean localeChangeInitiated = false;

    public LocaleConfig(Context context) {
        mContext = context;
    }

    public boolean fetchToSystemLocale() {
        Locale currentSystemLocale = new Locale(Locale.getDefault().getLanguage());
        Locale savedSystemLocale = new Locale(getSystemLocale());
        if (!savedSystemLocale.equals(currentSystemLocale)) {
            setSystemLocale(currentSystemLocale.getLanguage());
            setApplicationLocale(currentSystemLocale.getLanguage());
            return true;
        }
        return false;
    }

    public static void updateConfiguration(Context baseContext) {
        Resources res = baseContext.getResources();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(App.getConfig().getLocaleConfig().getApplicationLocale());
        res.updateConfiguration(conf, res.getDisplayMetrics());
    }

    public String getSystemLocale() {
        if (mSystemLocale == null) {
            mSystemLocale = getPreferences().getString(SYSTEM_LOCALE, Locale.getDefault().getLanguage());
        }
        return mSystemLocale;
    }

    public String getApplicationLocale() {
        if (mApplicationLocale == null) {
            mApplicationLocale = getPreferences().getString(APPLICATION_LOCALE, Locale.getDefault().getLanguage());
        }
        return mApplicationLocale;
    }

    public boolean setSystemLocale(String locale) {
        mSystemLocale = locale;
        return getPreferences().edit().putString(SYSTEM_LOCALE, mSystemLocale).commit();
    }

    public boolean setApplicationLocale(String locale) {
        mApplicationLocale = locale;
        setSystemLocale(Locale.getDefault().getLanguage());
        return getPreferences().edit().putString(APPLICATION_LOCALE, mApplicationLocale).commit();
    }

    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(
                AppConfig.BASE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }

    public static String getServerLocale(Activity activity, String selectedLocale) {
        Configuration conf = activity.getResources().getConfiguration();
        conf.locale = new Locale(selectedLocale);
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources resources = new Resources(activity.getAssets(), metrics, conf);
        return resources.getString(R.string.app_locale);
    }

    public static void changeLocale(final Activity activity, String selectedLocale) {
        localeChangeInitiated = true;
        final ProgressDialog progress = new ProgressDialog(activity);
        progress.setTitle(R.string.locale_changing);
        progress.setMessage(activity.getResources().getString(R.string.general_dialog_loading));
        progress.show();

        LocaleConfig.updateConfiguration(activity.getBaseContext());
        //save application locale to preferences
        App.getConfig().getLocaleConfig().setApplicationLocale(selectedLocale);
        //restart -> open NavigationActivity
        final String locale = LocaleConfig.getServerLocale(activity, selectedLocale);

        if (!AuthToken.getInstance().isEmpty()) {
            UserSetLocaleRequest request = new UserSetLocaleRequest(activity, locale);
            request.callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    App.sendProfileAndOptionsRequests();
                    NavigationActivity.restartNavigationActivity(BaseFragment.FragmentId.F_DATING);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Toast.makeText(activity, R.string.general_server_error, Toast.LENGTH_SHORT);
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    progress.dismiss();
                }
            }).exec();
        } else {
            progress.dismiss();
            NavigationActivity.restartNavigationActivity(BaseFragment.FragmentId.F_DATING);
        }
    }
}
