package com.topface.topface;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.*;
import com.topface.topface.ui.fragments.closing.LikesClosingFragment;
import com.topface.topface.ui.fragments.closing.MutualClosingFragment;
import com.topface.topface.utils.*;
import com.topface.topface.utils.GeoUtils.GeoLocationManager;
import com.topface.topface.utils.GeoUtils.GeoPreferencesManager;
import com.topface.topface.utils.social.AuthToken;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.concurrent.atomic.AtomicBoolean;

@ReportsCrashes(formKey = "817b00ae731c4a663272b4c4e53e4b61")
public class App extends Application {

    public static final String TAG = "Topface";
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final long PROFILE_UPDATE_TIMEOUT = 1000 * 90;

    public static boolean DEBUG = false;
    private static Context mContext;
    private static Intent mConnectionIntent;
    private static ConnectionChangeReceiver mConnectionReceiver;
    private static AtomicBoolean mProfileUpdating = new AtomicBoolean(false);
    private static long mLastProfileUpdate;
    private static AppConfig mBaseConfig;

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

        super.onCreate();
        mContext = getApplicationContext();
        //Включаем отладку, если это дебаг версия
        checkDebugMode();
        //Включаем логирование ошибок
        initAcra();

        //Базовые настройки приложения, инитим их один раз при старте приложения
        mBaseConfig = new AppConfig(this);
        Editor.setConfig(mBaseConfig);

        //Включаем строгий режим, если это Debug версия
        checkStrictMode();
        //Для Android 2.1 и ниже отключаем Keep-Alive
        checkKeepAlive();

        Debug.log("App", "+onCreate\n" + mBaseConfig.toString());

        //Начинаем слушать подключение к интернету
        if (mConnectionIntent == null) {
            mConnectionReceiver = new ConnectionChangeReceiver(mContext);
            mConnectionIntent = registerReceiver(mConnectionReceiver, new IntentFilter(CONNECTIVITY_CHANGE_ACTION));
        }

        MutualClosingFragment.usersProcessed = false;
        LikesClosingFragment.usersProcessed = false;

        //Выполнение всего, что можно сделать асинхронно, делаем в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                onCreateAsync();
            }
        }).start();
    }

    /**
     * Вызывается в onCreate, но выполняется в отдельном потоке
     */
    private void onCreateAsync() {
        DateUtils.syncTime();

        Ssid.init();

        CacheProfile.loadProfile();

        //Оповещаем о том, что профиль загрузился
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                new Intent(CacheProfile.ACTION_PROFILE_LOAD)
        );

        if (!CacheProfile.isEmpty()) {
            Looper.prepare();
            sendProfileAndOptionsRequests();
            sendLocation();
            Looper.loop();
        }

        if (Ssid.isLoaded() && AuthToken.getInstance().isEmpty()) {
            // GCM
            GCMUtils.init(getContext());
        }

    }

    private void sendLocation() {
        GeoLocationManager locationManager = new GeoLocationManager(App.getContext());
        Location curLocation = locationManager.getLastKnownLocation();

        GeoPreferencesManager preferencesManager = new GeoPreferencesManager(App.getContext());
        preferencesManager.saveLocation(curLocation);

        if (curLocation != null) {
            SettingsRequest settingsRequest = new SettingsRequest(this);
            settingsRequest.location = curLocation;
            settingsRequest.exec();
        }
    }

    private void initAcra() {
        if (!DEBUG) {
            ACRA.init(this);
            ACRA.getErrorReporter().setReportSender(new HockeySender());
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
        request.callback(new DataApiHandler<Options>() {
            @Override
            protected void success(Options data, ApiResponse response) {
            }

            @Override
            protected Options parseResponse(ApiResponse response) {
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
        sendProfileRequest(ProfileRequest.P_ALL);
    }

    public static void sendProfileRequest(final int part) {
        if (mProfileUpdating.compareAndSet(false, true)) {
            mLastProfileUpdate = System.currentTimeMillis();
            final ProfileRequest profileRequest = new ProfileRequest(App.getContext());
            profileRequest.part = part;
            profileRequest.callback(new DataApiHandler<Profile>() {

                @Override
                protected void success(Profile data, ApiResponse response) {
                    CacheProfile.setProfile(data, response, part);
                    LocalBroadcastManager.getInstance(getContext())
                            .sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                    LocalBroadcastManager.getInstance(getContext())
                            .sendBroadcast(new Intent(Options.Closing.DATA_FOR_CLOSING_RECEIVED_ACTION));
                }

                @Override
                protected Profile parseResponse(ApiResponse response) {
                    return Profile.parse(response);
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                }

                @Override
                public void always(ApiResponse response) {
                    super.always(response);
                    mProfileUpdating.set(false);
                }
            }).exec();
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean isOnline() {
        return mConnectionReceiver.isConnected();
    }

    public static void checkProfileUpdate() {
        if (System.currentTimeMillis() > mLastProfileUpdate + PROFILE_UPDATE_TIMEOUT) {
            mLastProfileUpdate = System.currentTimeMillis();
            sendProfileRequest(ProfileRequest.P_NECESSARY_DATA);
        }
    }

    public static AppConfig getConfig() {
        return mBaseConfig;
    }
}

