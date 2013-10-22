package com.topface.topface;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AppOptionsRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GooglePlayProductsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ParallelApiRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.ui.blocks.BannerBlock;
import com.topface.topface.ui.fragments.closing.LikesClosingFragment;
import com.topface.topface.ui.fragments.closing.MutualClosingFragment;
import com.topface.topface.utils.AppConfig;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.GeoUtils.GeoLocationManager;
import com.topface.topface.utils.GeoUtils.GeoPreferencesManager;
import com.topface.topface.utils.HockeySender;
import com.topface.topface.utils.social.AuthToken;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "817b00ae731c4a663272b4c4e53e4b61")
public class App extends Application {

    public static final String TAG = "Topface";
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final long PROFILE_UPDATE_TIMEOUT = 1000 * 90;

    public static boolean DEBUG = false;
    private static Context mContext;
    private static Intent mConnectionIntent;
    private static ConnectionChangeReceiver mConnectionReceiver;
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

        final Handler handler = new Handler();
        //Выполнение всего, что можно сделать асинхронно, делаем в отдельном потоке
        new BackgroundThread() {
            @Override
            public void execute() {
                onCreateAsync(handler);
            }
        };
    }

    /**
     * Вызывается в onCreate, но выполняется в отдельном потоке
     *
     * @param handler нужен для выполнения запросов
     */
    private void onCreateAsync(Handler handler) {
        Debug.log("App", "+onCreateAsync");
        DateUtils.syncTime();
        Ssid.init();
        CacheProfile.loadProfile();
        //Оповещаем о том, что профиль загрузился
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(CacheProfile.ACTION_PROFILE_LOAD));
        //Инициализируем GCM
        if (Ssid.isLoaded() && !AuthToken.getInstance().isEmpty()) {
            GCMUtils.init(getContext());
        }
        if (!GCMIntentService.isOnMessageReceived.getAndSet(false)) {
            if (!CacheProfile.isEmpty()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sendProfileAndOptionsRequests();
                        sendLocation();
                    }
                });
            }
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

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
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

    /**
     * Множественный запрос Options и профиля
     */
    public static void sendProfileAndOptionsRequests() {
        new ParallelApiRequest(App.getContext())
                .addRequest(getOptionsRequst())
                .addRequest(getProfileRequest(ProfileRequest.P_ALL))
                .addRequest(getGooglePlayProductsRequest())
                .exec();
    }

    private static ApiRequest getGooglePlayProductsRequest() {
        return new GooglePlayProductsRequest(App.getContext()).callback(new DataApiHandler<GooglePlayProducts>() {
            @Override
            protected void success(GooglePlayProducts data, IApiResponse response) {
            }

            @Override
            protected GooglePlayProducts parseResponse(ApiResponse response) {
                return new GooglePlayProducts(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {

            }
        });
    }

    private static ApiRequest getOptionsRequst() {
        return new AppOptionsRequest(App.getContext())
                .callback(new DataApiHandler<Options>() {
                    @Override
                    protected void success(Options data, IApiResponse response) {
                        BannerBlock.init();
                    }

                    @Override
                    protected Options parseResponse(ApiResponse response) {
                        return new Options(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Debug.log("Options::fail");
                    }
                });
    }

    public static void sendProfileRequest() {
        getProfileRequest(ProfileRequest.P_ALL).exec();
    }

    public static ApiRequest getProfileRequest(final int part) {
        mLastProfileUpdate = System.currentTimeMillis();
        return new ProfileRequest(part, App.getContext())
                .callback(new DataApiHandler<Profile>() {

                    @Override
                    protected void success(Profile data, IApiResponse response) {
                        CacheProfile.setProfile(data, (ApiResponse) response, part);
                        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
                        broadcastManager.sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                        broadcastManager.sendBroadcast(new Intent(Options.Closing.DATA_FOR_CLOSING_RECEIVED_ACTION));
                    }

                    @Override
                    protected Profile parseResponse(ApiResponse response) {
                        return Profile.parse(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                    }
                });
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
            getProfileRequest(ProfileRequest.P_NECESSARY_DATA);
        }
    }

    public static AppConfig getConfig() {
        return mBaseConfig;
    }
}

