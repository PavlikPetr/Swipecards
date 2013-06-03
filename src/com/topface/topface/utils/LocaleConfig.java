package com.topface.topface.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.social.AuthToken;

import java.util.Locale;

public class LocaleConfig {

    private static final String SYSTEM_LOCALE = "com.topface.topface_system_locale";
    private static final String APPLICATION_LOCALE = "com.topface.topface_application_locale";

    private Context mContext;
    private String mSystemLocale;
    private String mApplicationLocale;

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
        fetchToSystemLocale();
        if (mApplicationLocale == null) {
            mApplicationLocale = getPreferences().getString(APPLICATION_LOCALE,Locale.getDefault().getLanguage());
        }
        return mApplicationLocale;
    }

    public boolean setSystemLocale(String locale) {
        mSystemLocale = locale;
        return getPreferences().edit().putString(SYSTEM_LOCALE,mSystemLocale).commit();
    }

    public boolean setApplicationLocale(String locale) {
        mApplicationLocale = locale;
        setSystemLocale(Locale.getDefault().getLanguage());
        return getPreferences().edit().putString(APPLICATION_LOCALE,mApplicationLocale).commit();
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

    public static void changeLocale(final Activity activity, String selectedLocale,final int fragmentId) {
        final ProgressDialog progress = new ProgressDialog(activity);
        progress.setTitle(R.string.locale_changing);
        progress.setMessage(activity.getResources().getString(R.string.general_dialog_loading));
        progress.show();

        LocaleConfig.updateConfiguration(activity.getBaseContext());
        //save application locale to preferences
        App.getConfig().getLocaleConfig().setApplicationLocale(selectedLocale);
        //restart -> open NavigationActivity
        AuthRequest request = new AuthRequest(AuthToken.getInstance(),activity);
        final String locale = LocaleConfig.getServerLocale(activity,selectedLocale);
        request.setLocale(locale);
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Auth auth = new Auth(response);
                Ssid.save(auth.ssid);

                App.sendProfileAndOptionsRequests();

                Intent intent = new Intent(activity,NavigationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(GCMUtils.NEXT_INTENT, fragmentId);
                progress.dismiss();
                activity.startActivity(intent);
                activity.finish();
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                progress.dismiss();
                Toast.makeText(activity, R.string.general_server_error, Toast.LENGTH_SHORT);
            }
        }).exec();
    }
}
