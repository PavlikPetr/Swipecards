package com.topface.topface;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.*;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formUri = "https://api.zubhium.com/api2/acra/?secret_key=26d677a706e68e841ab85b286c5556", formKey = "")
public class App extends Application {
    // Constants
    public static final String TAG = "Topface";
    public static boolean DEBUG = false;
    private static Context mContext;
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
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
//    private static PluralResources mPluralResources;

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
        mContext = getApplicationContext();
        checkDebugMode();

        //Для разработчиков включаем StrictMode, что бы не расслоблялись
        if (DEBUG) {
            StrictMode.enableDefaults();
        }

        Debug.log("App", "+onCreate");
        Data.init(getApplicationContext());

        CacheProfile.loadProfile();

        //Начинаем слушать подключение к интернету
        if (mConnectionIntent == null) {
            mConnectionReceiver = new ConnectionChangeReceiver(mContext);
            mConnectionIntent = registerReceiver(mConnectionReceiver, new IntentFilter(CONNECTIVITY_CHANGE_ACTION));
        }

        if (CacheProfile.isLoaded()) {
            sendProfileRequest();
        }
        //Если приходим с нотификации незалогинеными, нужно вернуться в AuthActivity
        if (Data.isSSID() && (new AuthToken(getApplicationContext())).isEmpty()) {
            // GCM
            GCMUtils.init(getContext());
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

        OptionsRequest request = new OptionsRequest(App.getContext());
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Options.parse(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Debug.log("options::fail");
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

