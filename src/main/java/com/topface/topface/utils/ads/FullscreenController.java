package com.topface.topface.utils.ads;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdListener;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.framework.utils.config.DailyConfigExtension;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.banners.ad_providers.AppodealProvider;
import com.topface.topface.data.Banner;
import com.topface.topface.data.FullscreenSettings;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FullscreenSettingsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.statistics.AdStatistics;
import com.topface.topface.statistics.TopfaceAdStatistics;
import com.topface.topface.ui.dialogs.OwnFullscreenPopup;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;
import com.topface.topface.utils.http.IRequestClient;

import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_ADMOB;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_ADMOB_FULLSCREEN_START_APP;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_ADMOB_MEDIATION;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_NONE;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_TOPFACE;

/**
 */
public class FullscreenController {
    private static final String TAG = "FullscreenController";

    private static final String ADMOB_INTERSTITIAL_ID = "ca-app-pub-9530442067223936/9732921207";
    private static final String ADMOB_INTERSTITIAL_MEDIATION_ID = "ca-app-pub-9530442067223936/9498586400";
    private static final String ADMOB_INTERSTITIAL_START_APP_ID = "ca-app-pub-9530442067223936/3776010801";
    private static final String BANNER_APPODEAL_FULLSCREEN = "APPODEAL_FULLSCREEN";
    private static boolean isFullScreenBannerVisible = false;
    private Activity mActivity;
    private String mCurrentBannerType;
    private OnNextActionListener mOnNextActionListener;

    private boolean mIsControllerResumed = false;
    private boolean mIsFullscreenSkipped = true;
    private FullScreenBannerListener mFullScreenBannerListener = new FullScreenBannerListener() {
        @Override
        public void onLoaded() {
            isFullScreenBannerVisible = true;
            AdStatistics.sendFullscreenShown(mCurrentBannerType);
        }

        @Override
        public void onFailedToLoad(Integer codeError) {
            AdStatistics.sendFullscreenFailedToLoad(mCurrentBannerType, codeError);
            requestFallbackFullscreen();
        }

        @Override
        public void onClose() {
            isFullScreenBannerVisible = false;
            AdStatistics.sendFullscreenClosed(mCurrentBannerType);
            onFullScreenAdClosed();
        }

        @Override
        public void onClick() {
            AdStatistics.sendFullscreenClicked(mCurrentBannerType);
        }
    };

    private class FullscreenStartAction implements IStartAction {
        private PageInfo startPageInfo;
        private int priority;

        public FullscreenStartAction(int priority, Activity activity) {
            this.priority = priority;
            mActivity = activity;
            if (!CacheProfile.isEmpty()) {
                startPageInfo = App.get().getOptions().getPagesInfo().get(PageInfo.PageName.START.getName());
            }
        }

        @Override
        public void callInBackground() {
            if (startPageInfo != null) {
                Debug.log(TAG, startPageInfo.getBanner());
            }
        }

        @Override
        public void callOnUi() {
            requestFullscreen();
        }

        @Override
        public boolean isApplicable() {
            return canShowFullscreen();
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public String getActionName() {
            return "Fullscreen";
        }

        @Override
        public void setStartActionCallback(OnNextActionListener startActionCallback) {
            mOnNextActionListener = startActionCallback;
        }
    }

    public boolean canShowFullscreen() {
        UserConfig config = App.getUserConfig();
        DailyConfigExtension.DailyConfigField<Integer> configField = config.getFullscreenInterval();
        int interval = configField.getConfigField();
        return interval == 0 || System.currentTimeMillis() - configField.getConfigFieldInfo().getLastWriteTime() >= interval * 1000;
    }

    private void showOwnFullscreen(FullscreenSettings settings) {
        OwnFullscreenPopup popup = OwnFullscreenPopup.newInstance(settings);
        popup.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onClose();
                }
            }
        });
        popup.show(((FragmentActivity) mActivity).getSupportFragmentManager(), OwnFullscreenPopup.TAG);
    }

    private void handleFullscreenSettings(FullscreenSettings settings) {
        UserConfig config = App.getUserConfig();
        PageInfo startPageInfo = App.get().getOptions().getPagesInfo().get(PageInfo.PageName.START.getName());
        if (settings.nextRequestNoEarlierThen != 0) {
            config.setFullscreenInterval(settings.nextRequestNoEarlierThen);
        }
        if (!settings.isEmpty()) {
            mIsFullscreenSkipped = false;
            if (settings.banner.type.equals(FullscreenSettings.SDK)) {
                if (App.get().getOptions().interstitial.enabled) {
                    FullscreenController.this.requestFullscreen(BANNER_ADMOB_FULLSCREEN_START_APP);
                } else if (startPageInfo != null) {
                    FullscreenController.this.requestFullscreen(startPageInfo.getBanner());
                }
            } else {
                showOwnFullscreen(settings);
            }
        }
    }

    public void requestFullscreen() {
        mCurrentBannerType = OwnFullscreenPopup.IMPROVED_BANNER_TOPFACE;
        final UserConfig config = App.getUserConfig();
        ApiRequest request = new FullscreenSettingsRequest(mActivity.getApplicationContext(), config.getFullscreenInterval().getConfigFieldInfo().getAmount());
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                handleFullscreenSettings(JsonUtils.fromJson(response.toString(), FullscreenSettings.class));
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onLoaded();
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Debug.log("FullscreenController : FullscreenSettingsRequest error " + codeError + " response " + response);
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (!isFullScreenBannerVisible && mOnNextActionListener != null) {
                    mOnNextActionListener.onNextAction();
                }
            }
        });
        if (mActivity instanceof IRequestClient) {
            ((IRequestClient) mActivity).registerRequest(request);
        }
        request.exec();
    }

    private boolean mIsRedirected;

    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks;

    public FullscreenController(Activity activity) {
        mActivity = activity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mActivityLifecycleCallbacks = new Utils.ActivityLifecycleCallbacksAdapter() {
                @Override
                public void onActivityResumed(Activity activity) {
                    if (activity instanceof AdActivity && isFullScreenBannerVisible() && mIsRedirected) {
                        mIsRedirected = false;
                        activity.finish();
                    }
                }
            };
        }
    }

    private void requestFallbackFullscreen() {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    requestGagFullscreen();
                }
            });
        }
    }

    private void requestGagFullscreen() {
        requestFullscreen(App.get().getOptions().gagTypeFullscreen);
    }

    private void requestFullscreen(String type) {
        try {
            mCurrentBannerType = type;
            switch (type) {
                case BANNER_NONE:
                    return;
                case BANNER_ADMOB:
                    requestAdmobFullscreen(ADMOB_INTERSTITIAL_ID);
                    break;
                case BANNER_ADMOB_MEDIATION:
                    requestAdmobFullscreen(ADMOB_INTERSTITIAL_MEDIATION_ID);
                    break;
                case BANNER_ADMOB_FULLSCREEN_START_APP:
                    requestAdmobFullscreen(ADMOB_INTERSTITIAL_START_APP_ID);
                    break;
                case BANNER_APPODEAL_FULLSCREEN:
                    requestAppodealFullscreen();
                    break;
                case BANNER_TOPFACE:
                    requestTopfaceFullscreen();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Debug.error("Request fullscreen error", e);
        }
    }

    private void requestAppodealFullscreen() {
        Appodeal.setAutoCache(Appodeal.INTERSTITIAL, false);
        Appodeal.initialize(mActivity, AppodealProvider.APPODEAL_APP_KEY, Appodeal.INTERSTITIAL);
        if (BuildConfig.DEBUG) {
            Appodeal.setTesting(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            App.get().registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        }
        Appodeal.cache(mActivity, Appodeal.INTERSTITIAL);
        Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
            public void onInterstitialLoaded(boolean isPrecache) {
                Appodeal.show(mActivity, Appodeal.INTERSTITIAL);
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onLoaded();
                }
            }

            public void onInterstitialFailedToLoad() {
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onFailedToLoad(null);
                }
            }

            public void onInterstitialShown() {
            }

            public void onInterstitialClicked() {
                mIsRedirected = true;
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onClick();
                }
            }

            public void onInterstitialClosed() {
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onClose();
                }
            }
        });
    }

    public void requestAdmobFullscreen(String id) {
        AdmobInterstitialUtils.requestAdmobFullscreen(mActivity, id, new AdListener() {
            @Override
            public void onAdClosed() {
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onClose();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onFailedToLoad(errorCode);
                }
            }

            @Override
            public void onAdLeftApplication() {
                mIsRedirected = true;
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onClick();
                }
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdLoaded() {
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onLoaded();
                }
            }
        });
    }

    private void requestTopfaceFullscreen() {
        BannerRequest request = new BannerRequest(App.getContext());
        request.place = PageInfo.PageName.START.getName();
        request.callback(new DataApiHandler<Banner>() {
            @Override
            public void success(final Banner data, IApiResponse response) {
                if (data.action.equals(Banner.ACTION_URL)) {
                    if (!App.getAppConfig().getFullscreenUrlsSet().contains(data.parameter)) {
                        TopfaceAdStatistics.sendFullscreenShown(data);
                        if (mFullScreenBannerListener != null) {
                            mFullScreenBannerListener.onLoaded();
                        }
                        final View fullscreenViewGroup = mActivity.getLayoutInflater().inflate(R.layout.fullscreen_topface, null);
                        final ViewGroup bannerContainer = getFullscreenBannerContainer();
                        bannerContainer.addView(fullscreenViewGroup);
                        bannerContainer.setVisibility(View.VISIBLE);
                        final ImageViewRemote fullscreenImage = (ImageViewRemote) fullscreenViewGroup.findViewById(R.id.ivFullScreen);
                        fullscreenImage.setRemoteSrc(data.url);
                        fullscreenImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mFullScreenBannerListener != null) {
                                    mFullScreenBannerListener.onClick();
                                }
                                TopfaceAdStatistics.sendFullscreenClicked(data);
                                AppConfig config = App.getAppConfig();
                                config.addFullscreenUrl(data.parameter);
                                config.saveConfig();
                                hideFullscreenBanner(bannerContainer);
                                Utils.goToUrl(mActivity, data.parameter);
                            }
                        });

                        fullscreenViewGroup.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hideFullscreenBanner(bannerContainer);
                                TopfaceAdStatistics.sendFullscreenClosed(data);
                                if (mFullScreenBannerListener != null) {
                                    mFullScreenBannerListener.onClose();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            protected Banner parseResponse(ApiResponse response) {
                return new Banner(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onFailedToLoad(codeError);
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (!isFullScreenBannerVisible) {
                    mOnNextActionListener.onNextAction();
                }
            }
        }).exec();
    }

    public void hideFullscreenBanner(final ViewGroup bannerContainer) {
        if (bannerContainer != null) {
            Animation animation = AnimationUtils.loadAnimation(App.getContext(), android.R.anim.fade_out);
            if (animation != null) {
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        bannerContainer.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                bannerContainer.startAnimation(animation);
            } else {
                bannerContainer.setVisibility(View.GONE);
            }
        }
        if (mFullScreenBannerListener != null) {
            mFullScreenBannerListener.onClose();
        }
    }

    public boolean isFullScreenBannerVisible() {
        return isFullScreenBannerVisible;
    }

    public ViewGroup getFullscreenBannerContainer() {
        ViewGroup fullscreenContainer = (ViewGroup) mActivity.findViewById(R.id.loBannerContainer);
        if (fullscreenContainer == null) {
            fullscreenContainer = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.layout_fullscreen, null);
            ((ViewGroup) mActivity.findViewById(android.R.id.content)).addView(fullscreenContainer);
        }
        return fullscreenContainer;
    }

    public void onResume() {
        mIsControllerResumed = true;
        if (!isFullScreenBannerVisible()) {
            onNextAction();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            App.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        }
    }

    public void onPause() {
        mIsControllerResumed = false;
    }


    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            App.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
            mActivityLifecycleCallbacks = null;
        }
        mActivity = null;
        mOnNextActionListener = null;
        mFullScreenBannerListener = null;
    }

    public IStartAction createFullscreenStartAction(final int priority, Activity activity) {
        return new FullscreenStartAction(priority, activity);
    }

    @SuppressWarnings("unused")
    private void onFullScreenAdClosed() {
        isFullScreenBannerVisible = false;
        if (mIsControllerResumed) {
            onNextAction();
        }
    }

    private void onNextAction() {
        if (mOnNextActionListener != null && !mIsFullscreenSkipped) {
            mOnNextActionListener.onNextAction();
            mIsFullscreenSkipped = true;
        }
    }

    private interface FullScreenBannerListener {
        void onLoaded();

        void onFailedToLoad(Integer codeError);

        void onClose();

        void onClick();
    }
}
