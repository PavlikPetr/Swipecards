package com.topface.topface;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.appsflyer.AppsFlyerLib;
import com.comscore.analytics.comScore;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.nostra13.universalimageloader.core.ExtendedImageLoader;
import com.squareup.leakcanary.LeakCanary;
import com.topface.billing.OpenIabHelperManager;
import com.topface.billing.StoresManager;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.framework.imageloader.ImageLoaderStaticFactory;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.common.TFCredentials;
import com.topface.statistics.ILogger;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.data.AppOptions;
import com.topface.topface.data.AppsFlyerData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.social.AppSocialAppsIds;
import com.topface.topface.modules.TopfaceModule;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AppGetOptionsRequest;
import com.topface.topface.requests.AppGetSocialAppsIdsRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ParallelApiRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.ReferrerRequest;
import com.topface.topface.requests.UserGetAppOptionsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.requests.transport.HttpApiTransport;
import com.topface.topface.requests.transport.scruffy.ScruffyApiTransport;
import com.topface.topface.requests.transport.scruffy.ScruffyRequestManager;
import com.topface.topface.statistics.AppStateStatistics;
import com.topface.topface.ui.ApplicationBase;
import com.topface.topface.ui.external_libs.AdjustManager;
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Connectivity;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.RunningStateManager;
import com.topface.topface.utils.ad.NativeAdManager;
import com.topface.topface.utils.ads.BannersConfig;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.config.Configurations;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.config.SessionConfig;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.debug.HockeySender;
import com.topface.topface.utils.geo.FindAndSendCurrentLocation;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;
import com.topface.topface.utils.social.FbAuthorizer;
import com.topface.topface.utils.social.VkAuthorizer;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import dagger.ObjectGraph;

@ReportsCrashes(formUri = "817b00ae731c4a663272b4c4e53e4b61")
public class App extends ApplicationBase {

    public static final String TAG = "Topface";
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String COMSCORE_C2 = "19015876";
    public static final String COMSCORE_SECRET_KEY = "b83e932c608c2e08273eeddf01c2a70e";
    public static final String PREFERENCES_TAG_SHARED = "preferences_general";
    public static final String INTENT_REQUEST_KEY = "requestCode";
    private static final long PROFILE_UPDATE_TIMEOUT = 1000 * 120;

    @Inject
    static RunningStateManager mStateManager;
    @Inject
    AdjustManager mAdjustManager;
    private ObjectGraph mGraph;
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

    public static OpenIabHelperManager mOpenIabHelperManager = new OpenIabHelperManager();
    private static boolean mAppOptionsObtainedFromServer = false;
    private static boolean mUserOptionsObtainedFromServer = false;
    private static AppSocialAppsIds mAppSocialAppsIds;

    public static boolean isNeedShowTrial = true;

    /**
     * Множественный запрос Options и профиля
     */
    public static void sendProfileAndOptionsRequests(ApiHandler handler) {
        new ParallelApiRequest(App.getContext())
                .addRequest(getUserOptionsRequest())
                .addRequest(getProductsRequest())
                .addRequest(StoresManager.getPaymentwallProductsRequest())
                .addRequest(getProfileRequest(ProfileRequest.P_ALL))
                .callback(handler)
                .exec();
    }

    private void initObjectGraphForInjections() {
        mGraph = ObjectGraph.create(new TopfaceModule());
        mGraph.injectStatics();
        mGraph.inject(this);
    }

    public static void onActivityStarted(String activityName) {
        mStateManager.onActivityStarted(activityName);
    }

    public static void onActivityStoped(String activityName) {
        mStateManager.onActivityStoped(activityName);
    }

    public void inject(Object obj) {
        mGraph.inject(obj);
    }

    public static App from(Context context) {
        return (App) context.getApplicationContext();
    }

    /**
     * Множественный запрос Options и профиля
     */
    public static void sendProfileAndOptionsRequests() {
        sendProfileAndOptionsRequests(new SimpleApiHandler());
    }

    public static void sendUserOptionsAndPurchasesRequest() {
        new ParallelApiRequest(App.getContext())
                .addRequest(getProfileRequest(ProfileRequest.P_ALL))
                .addRequest(getUserOptionsRequest())
                .addRequest(StoresManager.getPaymentwallProductsRequest())
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

    public static void sendReferreRequest(final AdjustAttributeData attribution) {
        Debug.log("Adjust:: check settings before send AdjustAttributeData to server");
        final AppConfig config = getAppConfig();
        if (!AuthToken.getInstance().isEmpty() && !attribution.isEmpty() && !config.isAdjustAttributeDataSent()) {
            Debug.log("Adjust:: send AdjustAttributeData");
            new ReferrerRequest(App.getContext(), attribution).callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    Debug.log("Adjust:: attribution sent success");
                    config.setAdjustAttributeDataSent(true);
                    config.saveConfig();
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Debug.log("Adjust:: fail while send AdjustAttributeData");
                }
            }).exec();
        }
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

    public static void setLastKnownLocation(Location location) {
        mCurLocation = location;
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

    public static FeedsCache getFeedsCache() {
        return getConfig().getFeedsCache();
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
        return mAppSocialAppsIds != null ? mAppSocialAppsIds : AppSocialAppsIds.getDefault();
    }

    public static void setStartLabel(String startLabel) {
        mStartLabel = startLabel;
    }

    public static String getStartLabel() {
        return mStartLabel;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initVkSdk() {
        VkAuthorizer.initVkSdk();
        VKAccessTokenTracker vkTokenTracker = new VKAccessTokenTracker() {
            @Override
            public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
                if (newToken == null && AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
                    new AuthorizationManager().logout();
                }
            }
        };
        vkTokenTracker.startTracking();
        if (AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_VKONTAKTE) && VKAccessToken.currentToken() == null) {
            new AuthorizationManager().logout();
        }
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
        LeakCanary.install(this);
        FlurryManager.getInstance().init();
        // Отправка ивента о запуске приложения, если пользователь авторизован в FB
        if (AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            FbAuthorizer.initFB();
            AppEventsLogger.newLogger(App.getContext()).logEvent(AppEventsConstants.EVENT_NAME_ACTIVATED_APP);
        }
        initVkSdk();
        initObjectGraphForInjections();
        inject(this);
        mAdjustManager.initAdjust();
        // подписываемся на события о переходе приложения в состояние background/foreground
        mStateManager.registerAppChangeStateListener(new RunningStateManager.OnAppChangeStateListener() {
            @Override
            public void onAppForeground(long timeOnStart) {
                AppStateStatistics.sendAppForegroundState();
                FlurryManager.getInstance().sendAppInForegroundEvent();
            }

            @Override
            public void onAppBackground(long timeOnStop, long timeOnStart) {
                AppStateStatistics.sendAppBackgroundState();
                FlurryManager.getInstance().sendAppInBackgroundEvent();
            }
        });
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
        App.sendReferreRequest(getAppConfig().getAdjustAttributeData());
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
                    new FindAndSendCurrentLocation();
                }
            });
        }
        String adId = TFCredentials.getAdId(mContext);
        AppConfig config = getAppConfig();
        config.setAdId(adId);
        config.saveConfig();
    }

    private void sendUnauthorizedRequests() {
        new ParallelApiRequest(getContext()) {
            @Override
            public boolean isNeedAuth() {
                return false;
            }
        }
                .addRequest(createAppOptionsRequest())
                .addRequest(createAppSocialAppsIdsRequest(null))
                .exec();
    }

    private ApiRequest createAppOptionsRequest() {
        return new AppGetOptionsRequest(getContext()).callback(new DataApiHandler<AppOptions>() {
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
        });
    }

    public ApiRequest createAppSocialAppsIdsRequest(final ApiHandler handler) {
        return new AppGetSocialAppsIdsRequest(getContext()).callback(new DataApiHandler<AppSocialAppsIds>() {
            @Override
            public void fail(int codeError, IApiResponse response) {
                if (handler != null) {
                    handler.fail(codeError, response);
                }
            }

            @Override
            protected void success(AppSocialAppsIds data, IApiResponse response) {
                mAppSocialAppsIds = data;
                if (handler != null) {
                    handler.success(response);
                }
            }

            @Override
            protected AppSocialAppsIds parseResponse(ApiResponse response) {
                return new AppSocialAppsIds(response.getJsonResult());
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (handler != null) {
                    handler.always(response);
                }
            }
        });
    }

    private void initAcra() {
        ACRA.init(this);
        ACRA.getErrorReporter().setReportSender(new HockeySender());
    }

    private void initComScore() {
        comScore.setAppContext(mContext);
        comScore.setCustomerC2(COMSCORE_C2);
        comScore.setPublisherSecret(COMSCORE_SECRET_KEY);
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

