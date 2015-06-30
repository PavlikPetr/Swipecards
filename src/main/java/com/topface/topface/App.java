package com.topface.topface;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.appsflyer.AppsFlyerLib;
import com.comscore.analytics.comScore;
import com.google.gson.JsonSyntaxException;
import com.nostra13.universalimageloader.core.ExtendedImageLoader;
import com.topface.billing.OpenIabHelperManager;
import com.topface.framework.JsonUtils;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.framework.imageloader.ImageLoaderStaticFactory;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.common.TFCredentials;
import com.topface.statistics.ILogger;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.data.AppOptions;
import com.topface.topface.data.AppSocialAppsIds;
import com.topface.topface.data.AppsFlyerData;
import com.topface.topface.data.Options;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.AmazonProductsRequest;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AppGetOptionsRequest;
import com.topface.topface.requests.AppGetSocialAppsIdsRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GooglePlayProductsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ParallelApiRequest;
import com.topface.topface.requests.PaymentwallProductsRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.UserGetAppOptionsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.requests.transport.HttpApiTransport;
import com.topface.topface.requests.transport.scruffy.ScruffyApiTransport;
import com.topface.topface.requests.transport.scruffy.ScruffyRequestManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Connectivity;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.ad.NativeAdManager;
import com.topface.topface.utils.ads.BannersConfig;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.config.Configurations;
import com.topface.topface.utils.config.SessionConfig;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.debug.HockeySender;
import com.topface.topface.utils.geo.GeoLocationManager;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

@ReportsCrashes(formUri = "817b00ae731c4a663272b4c4e53e4b61")
public class App extends Application {

    public static final String TAG = "Topface";
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final long PROFILE_UPDATE_TIMEOUT = 1000 * 120;

    private static Context mContext;
    private static Intent mConnectionIntent;
    private static ConnectionChangeReceiver mConnectionReceiver;
    private static long mLastProfileUpdate;
    private static Configurations mBaseConfig;
    private static AppOptions mAppOptions;

    private static Boolean mIsGmsSupported;
    private static String mStartLabel;
    private static Location mCurLocation;
    private static AppsFlyerData.ConversionHolder mAppsFlyerConversionHolder;

    private static OpenIabHelperManager mOpenIabHelperManager = new OpenIabHelperManager();
    private static boolean mAppOptionsObtainedFromServer = false;
    private static boolean mUserOptionsObtainedFromServer = false;
    private static AppSocialAppsIds mAppSocialAppsIds;

    /**
     * Множественный запрос Options и профиля
     */
    public static void sendProfileAndOptionsRequests(ApiHandler handler) {
        new ParallelApiRequest(App.getContext())
                .addRequest(getUserOptionsRequest())
                .addRequest(getProductsRequest())
                .addRequest(getPaymentwallProductsRequest())
                .addRequest(getProfileRequest(ProfileRequest.P_ALL))
                .callback(handler)
                .exec();
    }

    private static ApiRequest getPaymentwallProductsRequest() {
        switch (BuildConfig.MARKET_API_TYPE) {
            case GOOGLE_PLAY:
                return new PaymentwallProductsRequest(App.getContext()).callback(new DataApiHandler<PaymentWallProducts>() {
                    @Override
                    protected void success(PaymentWallProducts data, IApiResponse response) {
                        //ВНИМАНИЕ! Сюда возвращается только Direct продукты,
                        //парсим и записываем в кэш мы их внутри конструктора PaymentWallProducts
                    }

                    @Override
                    protected PaymentWallProducts parseResponse(ApiResponse response) {
                        //При создании нового объекта продуктов, все данные о них записываются в кэш,
                        //поэтому здесь просто создаются два объекта продуктов.
                        new PaymentWallProducts(response, PaymentWallProducts.TYPE.MOBILE);
                        return new PaymentWallProducts(response, PaymentWallProducts.TYPE.DIRECT);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {

                    }
                });
            //Для амазона и nokia Paymentwall не должен включаться
            case NOKIA_STORE:
            case AMAZON:
            default:
                return null;
        }
    }

    /**
     * Множественный запрос Options и профиля
     */
    public static void sendProfileAndOptionsRequests() {
        sendProfileAndOptionsRequests(new SimpleApiHandler());
    }

    private static ApiRequest getProductsRequest() {
        ApiRequest request;
        switch (BuildConfig.MARKET_API_TYPE) {
            case AMAZON:
                request = new AmazonProductsRequest(App.getContext());
                break;
            case GOOGLE_PLAY:
                request = new GooglePlayProductsRequest(App.getContext());
                break;
            case NOKIA_STORE:
            default:
                request = null;
                break;
        }

        if (request != null) {
            request.callback(new DataApiHandler<Products>() {
                @Override
                protected void success(Products data, IApiResponse response) {
                }

                @Override
                protected Products parseResponse(ApiResponse response) {
                    return new Products(response);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {

                }
            });
        }

        return request;
    }

    public static void sendUserOptionsAndPurchasesRequest() {
        new ParallelApiRequest(App.getContext())
                .addRequest(getProfileRequest(ProfileRequest.P_ALL))
                .addRequest(getUserOptionsRequest())
                .addRequest(getPaymentwallProductsRequest())
                .addRequest(getProductsRequest())
                .exec();
    }

    private static ApiRequest getUserOptionsRequest() {
        return new UserGetAppOptionsRequest(App.getContext())
                .callback(new DataApiHandler<Options>() {
                    @Override
                    protected void success(Options data, IApiResponse response) {
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Options.OPTIONS_RECEIVED_ACTION));
                        mUserOptionsObtainedFromServer = true;
                        NativeAdManager.init();
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

    private static ApiRequest getProfileRequest(final int part) {
        mLastProfileUpdate = System.currentTimeMillis();
        return new ProfileRequest(part, App.getContext())
                .callback(new DataApiHandler<Profile>() {

                    @Override
                    protected void success(Profile data, IApiResponse response) {
                        if (data.photosCount == 0) {
                            App.getConfig().getUserConfig().setUserAvatarAvailable(false);
                            App.getConfig().getUserConfig().saveConfig();
                        }
                        CacheProfile.setProfile(data, response.getJsonResult(), part);
                        CacheProfile.sendUpdateProfileBroadcast();
                    }

                    @Override
                    protected Profile parseResponse(ApiResponse response) {
                        return new Profile(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                    }
                });
    }

    public static Context getContext() {
        return mContext;
    }

    public static Locale getCurrentLocale() {
        return mContext.getResources().getConfiguration().locale;
    }

    public static Location getLastKnownLocation() {
        return mCurLocation;
    }

    public static boolean isOnline() {
        return mConnectionReceiver.isConnected();
    }

    public static void checkProfileUpdate() {
        if (System.currentTimeMillis() > mLastProfileUpdate + PROFILE_UPDATE_TIMEOUT) {
            mLastProfileUpdate = System.currentTimeMillis();
            getProfileRequest(ProfileRequest.P_NECESSARY_DATA).exec();
        }
    }

    public static Configurations getConfig() {
        if (mBaseConfig == null) {
            mBaseConfig = new Configurations(App.getContext());
        }
        return mBaseConfig;
    }

    public static OpenIabHelperManager getOpenIabHelperManager() {
        return mOpenIabHelperManager;
    }

    public static AppConfig getAppConfig() {
        return getConfig().getAppConfig();
    }

    public static SessionConfig getSessionConfig() {
        return getConfig().getSessionConfig();
    }

    public static UserConfig getUserConfig() {
        return getConfig().getUserConfig();
    }

    public static LocaleConfig getLocaleConfig() {
        return getConfig().getLocaleConfig();
    }

    public static BannersConfig getBannerConfig() {
        return getConfig().getBannerConfig();
    }

    public static Novice getNovice() {
        return getConfig().getNovice();
    }

    public static AppOptions getAppOptions() {
        if (mAppOptions == null) {
            AppConfig config = App.getAppConfig();
            String appOptionsCache = config.getAppOptions();
            if (!TextUtils.isEmpty(appOptionsCache)) {
                try {
                    mAppOptions = new AppOptions(new JSONObject(appOptionsCache));
                } catch (JSONException e) {
                    config.resetAppOptionsData();
                    Debug.error(e);
                }
            }
            if (mAppOptions == null) {
                mAppOptions = new AppOptions(null);
            }
        }
        return mAppOptions;
    }

    public static AppSocialAppsIds getAppSocialAppsIds() {
        if (mAppSocialAppsIds == null) {
            AppConfig config = App.getAppConfig();
            String cached = config.getAppSocialAppsIds();
            try {
                mAppSocialAppsIds = JsonUtils.optFromJson(cached, AppSocialAppsIds.class,
                        new AppSocialAppsIds(null));

            } catch (JsonSyntaxException e) {
                config.resetAppOptionsData();
                Debug.error(e);
            }
        }
        return mAppSocialAppsIds;
    }

    public static void setStartLabel(String startLabel) {
        mStartLabel = startLabel;
    }

    public static String getStartLabel() {
        return mStartLabel;
    }

    @Override
    public void onCreate() {
        /**
         * Баг Admob и Google Play Services, пробуем исправить
         * @see https://code.google.com/p/android/issues/detail?id=81083
         */
        try {
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore) {
        }

        super.onCreate();

        mContext = getApplicationContext();
        //Включаем отладку, если это дебаг версия
        enableDebugLogs();
        //Включаем логирование ошибок
        initAcra();
        //Базовые настройки приложения, инитим их один раз при старте приложения
        Configurations baseConfig = getConfig();
        Editor.setConfig(baseConfig.getAppConfig());

        //Включаем строгий режим, если это Debug версия
        checkStrictMode();
        //Для Android 2.1 и ниже отключаем Keep-Alive
        checkKeepAlive();

        String msg = "+onCreate\n" + baseConfig.toString();
        //noinspection ConstantConditions
        if (BuildConfig.BUILD_TIME > 0) {
            msg += "\nBuild Time: " + SimpleDateFormat.getInstance().format(BuildConfig.BUILD_TIME);
        }
        if (!TextUtils.isEmpty(BuildConfig.GIT_HEAD_SHA)) {
            msg += "\nCommit: " + BuildConfig.GIT_HEAD_SHA;
        }
        Debug.log("App", msg);

        //Начинаем слушать подключение к интернету
        if (mConnectionIntent == null) {
            mConnectionReceiver = new ConnectionChangeReceiver(mContext);
            mConnectionIntent = registerReceiver(mConnectionReceiver, new IntentFilter(CONNECTIVITY_CHANGE_ACTION));
        }

        // Инициализируем общие срезы для статистики
        StatisticsTracker.getInstance()
                .setContext(mContext)
                .putPredefinedSlice("app", BuildConfig.STATISTICS_APP)
                .putPredefinedSlice("cvn", BuildConfig.VERSION_NAME);
        if (BuildConfig.DEBUG) {
            StatisticsTracker.getInstance().setLogger(new ILogger() {
                public void log(String msg) {
                    Debug.log(StatisticsTracker.TAG, msg);
                }
            });
        }
        // Settings extenede image loader to send statistics
        ImageLoaderStaticFactory.setExtendedImageLoader(ExtendedImageLoader.getInstance());
        // Settings common image to display error
        DefaultImageLoader.getInstance(getContext()).setErrorImageResId(R.drawable.im_photo_error);

        sendUnauthorizedRequests();

        mAppsFlyerConversionHolder = new AppsFlyerData.ConversionHolder();
        AppsFlyerLib.registerConversionListener(mContext, new AppsFlyerData.ConversionListener(mAppsFlyerConversionHolder));

        initComScore();

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
        Ssid.load();
        CacheProfile.loadProfile();
        //Оповещаем о том, что профиль загрузился
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(CacheProfile.ACTION_PROFILE_LOAD));
        if (!GcmIntentService.isOnMessageReceived.getAndSet(false) && !CacheProfile.isEmpty()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sendProfileAndOptionsRequests();
                    sendLocation();
                }
            });
        }
        String adId = TFCredentials.getAdId(mContext);
        AppConfig config = getAppConfig();
        config.setAdId(adId);
        config.saveConfig();
    }

    private void sendUnauthorizedRequests() {
        new ParallelApiRequest(getContext()) { @Override public boolean isNeedAuth() { return false; } }
                .addRequest(new AppGetOptionsRequest(getContext()).callback(new DataApiHandler<AppOptions>() {
                    @Override
                    protected void success(AppOptions data, IApiResponse response) {
                        mAppOptions = data;
                        mAppOptionsObtainedFromServer = true;
                        StatisticsTracker.getInstance()
                                .setConfiguration(data.getStatisticsConfiguration(Connectivity.getConnType(mContext)));
                    }

                    @Override
                    protected AppOptions parseResponse(ApiResponse response) {
                        return new AppOptions(response.jsonResult);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                    }
                }))
                .addRequest(new AppGetSocialAppsIdsRequest(getContext()).callback(new DataApiHandler<AppSocialAppsIds>() {
                    @Override public void fail(int codeError, IApiResponse response) {
                    }

                    @Override protected void success(AppSocialAppsIds data, IApiResponse response) {
                        mAppSocialAppsIds = data;
                    }

                    @Override protected AppSocialAppsIds parseResponse(ApiResponse response) {
                        return new AppSocialAppsIds(response.getJsonResult());
                    }
                }))
                .exec();
    }

    private void sendLocation() {
        new BackgroundThread(Thread.MIN_PRIORITY) {
            @Override
            public void execute() {
                mCurLocation = GeoLocationManager.getLastKnownLocation(mContext);
                if (mCurLocation != null) {
                    Looper.prepare();
                    SettingsRequest settingsRequest = new SettingsRequest(getContext());
                    settingsRequest.location = mCurLocation;
                    settingsRequest.exec();
                    Looper.loop();
                }
            }
        };
    }

    private void initAcra() {
        ACRA.init(this);
        ACRA.getErrorReporter().setReportSender(new HockeySender());
    }

    private void initComScore() {
        comScore.setAppContext(mContext);
        comScore.setCustomerC2(Static.COMSCORE_C2);
        comScore.setPublisherSecret(Static.COMSCORE_SECRET_KEY);
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
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.enableDefaults();
        }
    }

    private void enableDebugLogs() {
        if (BuildConfig.DEBUG) {
            FragmentManager.enableDebugLogging(true);
        }
    }

    public static boolean isGmsEnabled() {
        if (mIsGmsSupported == null) {
            mIsGmsSupported = new GoogleMarketApiManager().isMarketApiAvailable();
        }
        return mIsGmsSupported;
    }

    public static AppsFlyerData.ConversionHolder getConversionHolder() {
        return mAppsFlyerConversionHolder;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //Прекращаем слушать подключение к интернету
        if (mConnectionIntent != null && mConnectionReceiver != null) {
            unregisterReceiver(mConnectionReceiver);
        }
    }

    public static String getApiTransport() {
        if (!mAppOptionsObtainedFromServer && !mUserOptionsObtainedFromServer) {
            return HttpApiTransport.TRANSPORT_NAME;
        } else {
            boolean userOptions = mAppOptionsObtainedFromServer && CacheProfile.getOptions().isScruffyEnabled();
            boolean appOptions = mUserOptionsObtainedFromServer && getAppOptions().isScruffyEnabled();
            if (appOptions || userOptions) {
                if (ScruffyRequestManager.getInstance().isAvailable()) {
                    return ScruffyApiTransport.TRANSPORT_NAME;
                }
            }
            return HttpApiTransport.TRANSPORT_NAME;
        }
    }
}

