package com.topface.topface;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.OptionsRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.HockeySender;
import com.topface.topface.utils.social.AuthToken;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "817b00ae731c4a663272b4c4e53e4b61")
public class App extends Application {

    public static final String TAG = "Topface";
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    public static boolean DEBUG = false;
    private static Context mContext;
    private static Intent mConnectionIntent;
    private static ConnectionChangeReceiver mConnectionReceiver;

    public static boolean isDebugMode() {
        boolean debug = false;
        PackageInfo packageInfo = null;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Debug.error(e);
        }
        if (packageInfo != null) {
            int flags = packageInfo.applicationInfo.flags;
            debug = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        return debug;
    }

    @Override
    public void onCreate() {
        //android.os.Debug.startMethodTracing("topface_create");
        ACRA.init(this);
        ACRA.getErrorReporter().setReportSender(new HockeySender());
        super.onCreate();
        mContext = getApplicationContext();

        //Включаем отладку, если это дебаг версия
        checkDebugMode();
        //Включаем строгий режим, если это Debug версия
        checkStrictMode();
        //Для Android 2.1 и ниже отключаем Keep-Alive
        checkKeepAlive();

        Debug.log("App", "+onCreate");
        Ssid.init();
        DateUtils.syncTime();

        CacheProfile.loadProfile();

        //Начинаем слушать подключение к интернету
        if (mConnectionIntent == null) {
            mConnectionReceiver = new ConnectionChangeReceiver(mContext);
            mConnectionIntent = registerReceiver(mConnectionReceiver, new IntentFilter(CONNECTIVITY_CHANGE_ACTION));
        }

        if (CacheProfile.isLoaded()) {
            sendProfileAndOptionsRequests();
        }
        //Если приходим с нотификации незалогинеными, нужно вернуться в AuthActivity
        if (Ssid.isLoaded() && AuthToken.getInstance().isEmpty()) {
            // GCM
            GCMUtils.init(getContext());
        }
    }

    private void checkKeepAlive() {
        //На устройствах раньше чем Froyo (2.1),
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private void checkStrictMode() {
        //Для разработчиков включаем StrictMode, что бы не расслоблялись
        if (DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.enableDefaults();
        }
    }

    private void checkDebugMode() {
        DEBUG = isDebugMode();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //Прекращаем слушать подключение к интернету
        if (mConnectionIntent != null && mConnectionReceiver != null) {
            unregisterReceiver(mConnectionReceiver);
        }
    }

    public static void sendProfileAndOptionsRequests() {

        OptionsRequest request = new OptionsRequest(App.getContext());
        request.callback(new DataApiHandler() {
            @Override
            protected void success(Object data, ApiResponse response) {
            }

            @Override
            protected Object parseResponse(ApiResponse response) {
                return Options.parse(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Debug.log("options::fail");
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);
                //После окончания запроса options запрашиваем профиль
                sendProfileRequest();
            }
        }).exec();
    }

    public static void sendProfileRequest() {
        ProfileRequest profileRequest = new ProfileRequest(App.getContext());
        profileRequest.part = ProfileRequest.P_ALL;
        profileRequest.callback(new DataApiHandler<Profile>() {

            @Override
            protected void success(Profile data, ApiResponse response) {
                CacheProfile.setProfile(data, response);
            }

            @Override
            protected Profile parseResponse(ApiResponse response) {
                return Profile.parse(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
            }

        }).exec();
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean isOnline() {
        return mConnectionReceiver.isConnected();
    }

}

