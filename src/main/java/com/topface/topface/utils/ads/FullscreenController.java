package com.topface.topface.utils.ads;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.appintop.init.AdToApp;
import com.appintop.interstitialads.DefaultInterstitialListener;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.google.android.gms.ads.AdListener;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.banners.ad_providers.AdToAppProvider;
import com.topface.topface.banners.ad_providers.AppodealProvider;
import com.topface.topface.data.Banner;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.statistics.AdStatistics;
import com.topface.topface.statistics.TopfaceAdStatistics;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import static com.topface.topface.banners.ad_providers.AdProvidersFactory.*;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_ADMOB;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_ADMOB_FULLSCREEN_START_APP;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_ADMOB_MEDIATION;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_ADTOAPP_FULLSCREEN;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_APPODEAL_FULLSCREEN;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_NONE;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_TOPFACE;

/**
 */
public class FullscreenController {
    private static final String TAG = "FullscreenController";

    private static final String ADMOB_INTERSTITIAL_ID = "ca-app-pub-9530442067223936/9732921207";
    private static final String ADMOB_INTERSTITIAL_MEDIATION_ID = "ca-app-pub-9530442067223936/9498586400";
    private static final String ADMOB_INTERSTITIAL_START_APP_ID = "ca-app-pub-9530442067223936/3776010801";
    private static boolean isFullScreenBannerVisible = false;
    private final Options mOptions;
    private Activity mActivity;
    private String mCurrentBannerType;
    private FullScreenBannerListener mFullScreenBannerListener = new FullScreenBannerListener() {
        @Override
        public void onLoaded() {
            addLastFullscreenShowedTime();
            isFullScreenBannerVisible = true;
            AdStatistics.sendFullscreenShown(mCurrentBannerType);
        }

        @Override
        public void onFailedToLoad() {
            requestFallbackFullscreen();
        }

        @Override
        public void onClose() {
            isFullScreenBannerVisible = false;
            AdStatistics.sendFullscreenClosed(mCurrentBannerType);
        }

        @Override
        public void onClick() {
            AdStatistics.sendFullscreenClicked(mCurrentBannerType);
        }
    };

    private class FullscreenStartAction implements IStartAction {
        private PageInfo startPageInfo;
        private int priority;

        public FullscreenStartAction(int priority) {
            this.priority = priority;
            if (!CacheProfile.isEmpty()) {
                startPageInfo = CacheProfile.getOptions().getPagesInfo().get(PageInfo.PageName.START.getName());
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
            if (CacheProfile.getOptions().interstitial.enabled) {
                FullscreenController.this.requestFullscreen(BANNER_ADMOB_FULLSCREEN_START_APP);
            } else if (startPageInfo != null) {
                FullscreenController.this.requestFullscreen(startPageInfo.getBanner());
            }
        }

        @Override
        public boolean isApplicable() {
            return CacheProfile.getOptions().interstitial.enabled || CacheProfile.show_ad &&
                    FullscreenController.this.isTimePassed() && startPageInfo != null
                    && startPageInfo.floatType.equals(PageInfo.FLOAT_TYPE_BANNER);
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

        }
    }

    public FullscreenController(Activity activity, Options options) {
        mActivity = activity;
        mOptions = options;
    }

    private void requestFallbackFullscreen() {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addLastFullscreenShowedTime();
                    requestGagFullscreen();
                }
            });
        }
    }

    private boolean showFullscreenBanner(String url) {
        boolean passByTime = isTimePassed();
        boolean passByUrl = passFullScreenByUrl(url);

        return passByUrl && passByTime;
    }

    private boolean isTimePassed() {
        long currentTime = System.currentTimeMillis();
        long lastCall = App.getAppConfig().getLastFullscreenTime();
        if (lastCall == 0) {
            addLastFullscreenShowedTime();
            return false;
        } else {
            return Math.abs(currentTime - lastCall) >
                    (mOptions != null
                            ? 1000 * mOptions.fullscreenInterval
                            : DateUtils.DAY_IN_MILLISECONDS);
        }
    }

    private boolean passFullScreenByUrl(String url) {
        return !App.getAppConfig().getFullscreenUrlsSet().contains(url);
    }

    private void addLastFullscreenShowedTime() {
        AppConfig config = App.getAppConfig();
        config.setLastFullscreenTime(System.currentTimeMillis());
        config.saveConfig();
    }

    private void requestGagFullscreen() {
        requestFullscreen(CacheProfile.getOptions().gagTypeFullscreen);
    }

    public void requestFullscreen(String type) {
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
                case BANNER_ADTOAPP_FULLSCREEN:
                    requestAdToAppFullscreen();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Debug.error("Request fullscreen error", e);
        }
    }

    private void requestAdToAppFullscreen() {
        requestAdToAppFullscreen(AdToApp.MASK_INTERSTITIAL, new DefaultInterstitialListener() {
            @Override
            public void onFirstInterstitialLoad(String s, String s1) {
                addLastFullscreenShowedTime();
            }

            @Override
            public void onInterstitialStarted(String s, String s1) {
            }

            @Override
            public void onInterstitialClicked(String s, String s1) {
            }

            @Override
            public void onInterstitialClosed(String s, String s1) {
                isFullScreenBannerVisible = false;
            }
        });
    }

    public void showVideoAdToApp(DefaultInterstitialListener listener) {
        requestAdToAppFullscreen(AdToApp.MASK_VIDEO, listener);
    }

    private void requestAdToAppFullscreen(int mask, DefaultInterstitialListener listener) {
        AdToApp.initializeSDK(mActivity, AdToAppProvider.ADTOAPP_APP_KEY, mask);
        AdToApp.showInterstitialAd();
        AdToApp.setInterstitialListener(listener);
    }

    private void requestAppodealFullscreen() {
        Appodeal.setAutoCache(Appodeal.INTERSTITIAL, false);
        Appodeal.initialize(mActivity, AppodealProvider.APPODEAL_APP_KEY, Appodeal.INTERSTITIAL);
        if (BuildConfig.DEBUG) {
            Appodeal.setTesting(true);
        }
        Appodeal.cache(mActivity, Appodeal.INTERSTITIAL);
        Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
            public void onInterstitialLoaded(boolean isPrecache) {
                Appodeal.show(mActivity, Appodeal.INTERSTITIAL);
                mFullScreenBannerListener.onLoaded();
            }

            public void onInterstitialFailedToLoad() {
                mFullScreenBannerListener.onFailedToLoad();
            }

            public void onInterstitialShown() {
            }

            public void onInterstitialClicked() {
                mFullScreenBannerListener.onClick();
            }

            public void onInterstitialClosed() {
                mFullScreenBannerListener.onClose();
            }
        });
    }

    public void requestAdmobFullscreen(String id) {
        AdmobInterstitialUtils.requestAdmobFullscreen(mActivity, id, new AdListener() {
            @Override
            public void onAdClosed() {
                mFullScreenBannerListener.onClose();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                mFullScreenBannerListener.onFailedToLoad();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                mFullScreenBannerListener.onClick();
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdLoaded() {
                mFullScreenBannerListener.onLoaded();
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
                    if (showFullscreenBanner(data.parameter)) {
                        TopfaceAdStatistics.sendFullscreenShown(data);
                        mFullScreenBannerListener.onLoaded();
                        final View fullscreenViewGroup = mActivity.getLayoutInflater().inflate(R.layout.fullscreen_topface, null);
                        final ViewGroup bannerContainer = getFullscreenBannerContainer();
                        bannerContainer.addView(fullscreenViewGroup);
                        bannerContainer.setVisibility(View.VISIBLE);
                        final ImageViewRemote fullscreenImage = (ImageViewRemote) fullscreenViewGroup.findViewById(R.id.ivFullScreen);
                        fullscreenImage.setRemoteSrc(data.url);
                        fullscreenImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mFullScreenBannerListener.onClick();
                                TopfaceAdStatistics.sendFullscreenClicked(data);
                                AppConfig config = App.getAppConfig();
                                config.addFullscreenUrl(data.parameter);
                                config.saveConfig();
                                hideFullscreenBanner(bannerContainer);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.parameter));
                                mActivity.startActivity(intent);
                            }
                        });

                        fullscreenViewGroup.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hideFullscreenBanner(bannerContainer);
                                TopfaceAdStatistics.sendFullscreenClosed(data);
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
                if (codeError == ErrorCodes.BANNER_NOT_FOUND) {
                    addLastFullscreenShowedTime();
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
        mFullScreenBannerListener.onClose();
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

    public void onPause() {
        //Пока не требуется, но на будущее
    }


    public void onDestroy() {
        //Пока не требуется, но на будущее
    }

    public IStartAction createFullscreenStartAction(final int priority) {
        return new FullscreenStartAction(priority);
    }

    private interface FullScreenBannerListener {
        void onLoaded();

        void onFailedToLoad();

        void onClose();

        void onClick();
    }
}
