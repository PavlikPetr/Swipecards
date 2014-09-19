
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

import com.nostra13.universalimageloader.core.ExtendedImageLoader;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.framework.imageloader.ImageLoaderStaticFactory;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.advertizer.TFOfferSDK;
import com.topface.offerwall.common.TFCredentials;
import com.topface.statistics.ILogger;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.data.AppOptions;
import com.topface.topface.data.Options;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.AmazonProductsRequest;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AppGetOptionsRequest;
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
import com.topface.topface.ui.blocks.BannerBlock;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Connectivity;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.GMSUtils;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.ads.BannersConfig;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.config.Configurations;
import com.topface.topface.utils.config.SessionConfig;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.debug.DebugEmailSender;
import com.topface.topface.utils.debug.HockeySender;
import com.topface.topface.utils.geo.GeoLocationManager;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ACRAConfigurationException;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

@ReportsCrashes(formKey = "817b00ae731c4a663272b4c4e53e4b61")
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


    /**
     * Множественный запрос Options и профиля
     */
    public static void sendProfileAndOptionsRequests(ApiHandler handler) {
        new ParallelApiRequest(App.getContext())
                .addRequest(getOptionsRequest())
                .addRequest(getProductsRequest())
                .addRequest(getPaymentwallProductsRequest())
                .addRequest(getProfileRequest(ProfileRequest.P_ALL))
                .callback(handler)
                .exec();
    }

    private static ApiRequest getPaymentwallProductsRequest() {
        switch (BuildConfig.BILLING_TYPE) {
            //Для амазона и nokia Paymentwall не должен включаться
            case NOKIA_STORE:
            case AMAZON:
                return null;
            case GOOGLE_PLAY:
            default:
                return new PaymentwallProductsRequest(App.getContext()).callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        //При создании нового объекта продуктов, все данные о них записываются в кэш,
                        //поэтому здесь просто создаются два объекта продуктов.
                        new PaymentWallProducts(response, PaymentWallProducts.TYPE.DIRECT);
                        new PaymentWallProducts(response, PaymentWallProducts.TYPE.MOBILE);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {

                    }
                });
        }
    }

    /**
     * Множественный запрос Options и профиля
     */
    public static void sendProfileAndOptionsRequests() {
        sendProfileAndOptionsRequests(new SimpleApiHandler() {
            @Override
            public void success(IApiResponse response) {
                super.success(response);
                LocalBroadcastManager.getInstance(getContext())
                        .sendBroadcast(new Intent(Options.Closing.DATA_FOR_CLOSING_RECEIVED_ACTION));
            }
        });
    }

    private static ApiRequest getProductsRequest() {
        ApiRequest request;
        switch (BuildConfig.BILLING_TYPE) {
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

    private static ApiRequest getOptionsRequest() {
        return new UserGetAppOptionsRequest(App.getContext())
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

    private static ApiRequest getProfileRequest(final int part) {
        mLastProfileUpdate = System.currentTimeMillis();
        return new ProfileRequest(part, App.getContext())
                .callback(new DataApiHandler<Profile>() {

                    @Override
                    protected void success(Profile data, IApiResponse response) {
                        CacheProfile.setProfile(data, (ApiResponse) response, part);
                        CacheProfile.sendUpdateProfileBroadcast();
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

    public static Locale getCurrentLocale() {
        return mContext.getResources().getConfiguration().locale;
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

    @Override
    public void onCreate() {
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
        StatisticsTracker.getInstance().setContext(mContext)
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

        sendAppOptionsRequest();

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

    private void sendAppOptionsRequest() {
        new AppGetOptionsRequest(mContext).callback(new DataApiHandler<AppOptions>() {
            @Override
            protected void success(AppOptions data, IApiResponse response) {
                mAppOptions = data;
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
        }).exec();
    }

    private void sendLocation() {
        new BackgroundThread(Thread.MIN_PRIORITY) {
            @Override
            public void execute() {
                Location curLocation = GeoLocationManager.getLastKnownLocation(mContext);
                if (curLocation != null) {
                    Looper.prepare();
                    SettingsRequest settingsRequest = new SettingsRequest(getContext());
                    settingsRequest.location = curLocation;
                    settingsRequest.exec();
                    Looper.loop();
                }
            }
        };
    }

    private void initAcra() {
        if (!BuildConfig.DEBUG) {
            ACRA.init(this);
            ACRA.getErrorReporter().setReportSender(new HockeySender());
        } else {
            //Если дебажим приложение, то показываем диалог и отправляем на email вместо Hockeyapp
            try {
                //Что бы такая схема работала, сперва выставляем конфиг
                ACRAConfiguration acraConfig = ACRA.getConfig();
                acraConfig.setResDialogTitle(R.string.crash_dialog_title);
                acraConfig.setResDialogText(R.string.crash_dialog_text);
                acraConfig.setResDialogCommentPrompt(R.string.crash_dialog_comment_prompt);
                acraConfig.setMode(ReportingInteractionMode.DIALOG);
                ACRA.setConfig(acraConfig);
                //Потом инитим
                ACRA.init(this);
                //И потом выставляем ReportSender
                ACRA.getErrorReporter().setReportSender(new DebugEmailSender(this));
            } catch (ACRAConfigurationException e) {
                Debug.error("Acra init error", e);
            }
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
            mIsGmsSupported = GMSUtils.checkPlayServices(getContext());
        }
        return mIsGmsSupported;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //Прекращаем слушать подключение к интернету
        if (mConnectionIntent != null && mConnectionReceiver != null) {
            unregisterReceiver(mConnectionReceiver);
        }
    }


}

