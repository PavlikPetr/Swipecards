package com.topface.topface.utils.ads;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.Banner;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.statistics.TopfaceAdStatistics;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.controllers.AbstractStartAction;
import com.topface.topface.utils.controllers.IStartAction;

import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_ADMOB;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_NONE;
import static com.topface.topface.banners.ad_providers.AdProvidersFactory.BANNER_TOPFACE;

/**
 */
public class FullscreenController {

    private static final String TAG = "FullscreenController";
    private static final String ADMOB_INTERSTITIAL_ID = "ca-app-pub-9530442067223936/9732921207";
    private static boolean isFullScreenBannerVisible = false;
    private Activity mActivity;

    private class FullscreenStartAction extends AbstractStartAction {
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
            if (startPageInfo != null) {
                FullscreenController.this.requestFullscreen(startPageInfo.getBanner());
            }
        }

        @Override
        public boolean isApplicable() {
            return CacheProfile.show_ad &&
                    FullscreenController.this.isTimePassed() &&
                    startPageInfo != null &&
                    startPageInfo.floatType.equals(PageInfo.FLOAT_TYPE_BANNER);
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public String getActionName() {
            return "Fullscreen";
        }
    }

    public FullscreenController(Activity activity) {
        mActivity = activity;
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
            return Math.abs(currentTime - lastCall) > DateUtils.DAY_IN_MILLISECONDS;
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
            switch (type) {
                case BANNER_NONE:
                    return;
                case BANNER_ADMOB:
                    requestAdmobFullscreen();
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

    public void requestAdmobFullscreen() {
        // Создание межстраничного объявления.
        final InterstitialAd interstitial = new InterstitialAd(mActivity);
        interstitial.setAdUnitId(ADMOB_INTERSTITIAL_ID);
        // Создание запроса объявления.
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        adRequestBuilder.setGender(
                CacheProfile.getProfile().sex == Static.BOY ?
                        AdRequest.GENDER_MALE :
                        AdRequest.GENDER_FEMALE
        );
        // Запуск загрузки межстраничного объявления.
        interstitial.loadAd(adRequestBuilder.build());
        // AdListener будет использовать обратные вызовы, указанные ниже.
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                isFullScreenBannerVisible = false;
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                requestFallbackFullscreen();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                isFullScreenBannerVisible = true;
            }

            @Override
            public void onAdLoaded() {
                interstitial.show();
                addLastFullscreenShowedTime();
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
                        isFullScreenBannerVisible = true;
                        addLastFullscreenShowedTime();
                        final View fullscreenViewGroup = mActivity.getLayoutInflater().inflate(R.layout.fullscreen_topface, null);
                        final ViewGroup bannerContainer = getFullscreenBannerContainer();
                        bannerContainer.addView(fullscreenViewGroup);
                        bannerContainer.setVisibility(View.VISIBLE);
                        final ImageViewRemote fullscreenImage = (ImageViewRemote) fullscreenViewGroup.findViewById(R.id.ivFullScreen);
                        fullscreenImage.setRemoteSrc(data.url);
                        fullscreenImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppConfig config = App.getAppConfig();
                                config.addFullscreenUrl(data.parameter);
                                config.saveConfig();
                                hideFullscreenBanner(bannerContainer);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.parameter));
                                mActivity.startActivity(intent);
                                TopfaceAdStatistics.sendFullscreenClicked(data);
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
        isFullScreenBannerVisible = false;
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
}
