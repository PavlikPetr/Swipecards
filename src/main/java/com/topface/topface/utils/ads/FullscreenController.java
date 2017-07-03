package com.topface.topface.utils.ads;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.utils.Log;
import com.google.android.gms.ads.AdActivity;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.framework.utils.config.DailyConfigExtension;
import com.topface.topface.App;
import com.topface.topface.banners.AdProvidersFactory;
import com.topface.topface.banners.providers.AmpiriProvider;
import com.topface.topface.banners.providers.appodeal.AppodealProvider;
import com.topface.topface.data.AdsSettings;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.FullscreenSettingsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.statistics.AdStatistics;
import com.topface.topface.ui.dialogs.OwnFullscreenPopup;
import com.topface.topface.ui.external_libs.appodeal.AppodealManager;
import com.topface.topface.utils.IStateSaverRegistratorKt;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.http.IRequestClient;
import com.topface.topface.utils.popups.PopupManager;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 */
public class FullscreenController {

    private static final String TAG = "FullscreenController";
    private static final String BANNER_APPODEAL_FULLSCREEN = "APPODEAL_FULLSCREEN";
    public static final String ADMOB_NEW = "ADMOB";
    public static final String APPODEAL_NEW = "APPODEAL";
    public static final String AMPIRI = "AMPIRI";
    private static final String FROM = "from";
    private static final String APPODEAL_IN_PROGRESS = "appodeal_in_progress";

    @Inject
    AppodealManager mAppodealManager;

    private String mFrom;
    private Activity mActivity;
    private String mCurrentBannerType;

    private static boolean isFullScreenBannerVisible = false;
    private boolean mIsAppodealInProgress = false;

    private AmpiriProvider.AmpiriInterstitialLifeCycler mAmpiriInterstitialLifeCycler;

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
            continuePopupSequence();
        }

        @Override
        public void onClick() {
            AdStatistics.sendFullscreenClicked(mCurrentBannerType);
        }
    };

    private class FullscreenStartAction implements IStartAction {
        private int priority;

        public FullscreenStartAction(int priority, Activity activity, String from) {
            this.priority = priority;
            mActivity = activity;
            mFrom = from;
        }

        @Override
        public void callInBackground() {
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
            return getClass().getSimpleName();
        }
    }

    public boolean canShowFullscreen() {
        UserConfig config = App.getUserConfig();
        DailyConfigExtension.DailyConfigField<Integer> configField = config.getFullscreenInterval();
        int interval = configField.getConfigField();
        return interval == 0 || System.currentTimeMillis() - configField.getConfigFieldInfo().getLastWriteTime() >= interval * 1000;
    }

    private void showOwnFullscreen(AdsSettings settings) {
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
        mFullScreenBannerListener.onLoaded();
    }

    private void handleFullscreenSettings(AdsSettings settings) {
        if (settings.nextRequestNoEarlierThen != 0) {
            App.getUserConfig().setFullscreenInterval(settings.nextRequestNoEarlierThen);
        }
        if (!settings.isEmpty()) {
            if (settings.banner.type.equals(AdsSettings.SDK)) {
                Debug.log("FullscreenController : FullscreenSettingsRequest AD " + settings.banner.name);
                FullscreenController.this.requestFullscreenByServerSettings(settings);
            } else {
                Debug.log("FullscreenController : showOwnFullscreen ");
                showOwnFullscreen(settings);
            }
        } else {
            continuePopupSequence();
        }
    }

    private void requestFullscreenByServerSettings(AdsSettings settings) {
        switch (settings.banner.name) {
            case APPODEAL_NEW:
                Debug.log("BANNER_SETTINGS : new appodeal segment " + settings.banner.adAppId);
                App.getAppComponent().weakStorage().setAppodealFullscreenSegmentName(settings.banner.adAppId);
                AppodealProvider.setCustomSegment();
                requestAppodealFullscreen();
            case AMPIRI:
                Debug.log("BANNER_SETTINGS : new ampiri segment " + settings.banner.adAppId);
                App.getAppComponent().weakStorage().setAmpiriFullscreenSegmentName(settings.banner.adAppId);
                requestAmpiriFullscreen();
        }
    }

    public void requestFullscreen() {
        mCurrentBannerType = OwnFullscreenPopup.IMPROVED_BANNER_TOPFACE;
        UserConfig config = App.getUserConfig();
        long amount = config.getFullscreenInterval().getConfigFieldInfo().getAmount();
        Debug.log("FullscreenController : FullscreenSettingsRequest exec  amount " + amount);
        ApiRequest request = new FullscreenSettingsRequest(mActivity.getApplicationContext(), amount);
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                handleFullscreenSettings(JsonUtils.fromJson(response.toString(), AdsSettings.class));
                Debug.log("FullscreenController : FullscreenSettingsRequest success ");
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Debug.log("FullscreenController : FullscreenSettingsRequest error " + codeError + " response " + response);
            }
        });
        if (mActivity instanceof IRequestClient) {
            ((IRequestClient) mActivity).registerRequest(request);
        }
        request.exec();
    }

    private void continuePopupSequence() {
        if (!isFullScreenBannerVisible && mFrom != null) {
            Debug.log("FullscreenController : FullscreenSettingsRequest continue popup sequence");
            PopupManager.INSTANCE.informManager(mFrom);
        }
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
                case AdProvidersFactory.BANNER_NONE:
                    return;
                case BANNER_APPODEAL_FULLSCREEN:
                    requestAppodealFullscreen();
                    break;
                case AdProvidersFactory.BANNER_AMPIRI:
                    requestAmpiriFullscreen();
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
        Appodeal.setTesting(false);
        Appodeal.setLogLevel(Log.LogLevel.verbose);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            App.get().registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        }
        Appodeal.cache(mActivity, Appodeal.INTERSTITIAL);
        Appodeal.setInterstitialCallbacks(createAppodealInterstitialCallbacks());
    }

    private InterstitialCallbacks createAppodealInterstitialCallbacks() {
        return new InterstitialCallbacks() {
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
                mIsAppodealInProgress = true;
            }

            public void onInterstitialClicked() {
                mIsRedirected = true;
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onClick();
                }
            }

            public void onInterstitialClosed() {
                mIsAppodealInProgress = false;
                if (mFullScreenBannerListener != null) {
                    mFullScreenBannerListener.onClose();
                }
            }
        };
    }

    private void requestAmpiriFullscreen() {
        if (mAmpiriInterstitialLifeCycler != null) {
            IStateSaverRegistratorKt.unregisterLifeCycleDelegate(mActivity, mAmpiriInterstitialLifeCycler);
        }
        mAmpiriInterstitialLifeCycler = new AmpiriProvider.AmpiriInterstitialLifeCycler(
                AmpiriProvider.Companion.createFullScreen(mActivity, createAmpiriInterstitialCallbacks())
        );
        IStateSaverRegistratorKt.registerLifeCycleDelegate(mActivity, mAmpiriInterstitialLifeCycler);
        mAmpiriInterstitialLifeCycler.getAds().loadAndShow();
    }

    private InterstitialCallbacks createAmpiriInterstitialCallbacks() {
        return new InterstitialCallbacks() {
            public void onInterstitialLoaded(boolean isPrecache) {
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
        };
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(FROM, mFrom);
        outState.putBoolean(APPODEAL_IN_PROGRESS, mIsAppodealInProgress);
    }

    public void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        mFrom = savedInstanceState.getString(FROM);
        mIsAppodealInProgress = savedInstanceState.getBoolean(APPODEAL_IN_PROGRESS);
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

    public void onResume() {
        if (mIsAppodealInProgress) {
            Appodeal.setInterstitialCallbacks(createAppodealInterstitialCallbacks());
        }
        continuePopupSequence();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            App.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        }
    }

    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            App.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
            mActivityLifecycleCallbacks = null;
        }
        if (mAmpiriInterstitialLifeCycler != null) {
            IStateSaverRegistratorKt.unregisterLifeCycleDelegate(mActivity, mAmpiriInterstitialLifeCycler);
        }
        mActivity = null;
        mFullScreenBannerListener = null;
    }

    public IStartAction createFullscreenStartAction(final int priority, Activity activity, String from) {
        return new FullscreenStartAction(priority, activity, from);
    }

    private interface FullScreenBannerListener {
        void onLoaded();

        void onFailedToLoad(Integer codeError);

        void onClose();

        void onClick();
    }
}
