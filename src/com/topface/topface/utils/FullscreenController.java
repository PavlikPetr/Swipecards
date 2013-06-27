package com.topface.topface.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.inneractive.api.ads.InneractiveAd;
import com.inneractive.api.ads.InneractiveAdListener;
import com.mobclix.android.sdk.MobclixFullScreenAdView;
import com.mobclix.android.sdk.MobclixFullScreenAdViewListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Banner;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.handlers.BaseApiHandler;
import com.topface.topface.ui.views.ImageViewRemote;
import ru.ideast.adwired.AWView;
import ru.ideast.adwired.events.OnNoBannerListener;
import ru.ideast.adwired.events.OnStartListener;
import ru.ideast.adwired.events.OnStopListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class FullscreenController {

    public static final String URL_SEPARATOR = "::";
    private static boolean isFullScreenBannerVisible = false;
    private static final String MOPUB_INTERSTITIAL_ID = "00db7208a90811e281c11231392559e4";

    private SharedPreferences mPreferences;
    private Activity mActivity;

    private MoPubInterstitial mInterstitial;

    public FullscreenController(Activity activity) {
        mActivity = activity;
    }

    public void requestFullscreen() {
        if (!CacheProfile.isEmpty() && isTimePassed()) {
            Options.Page startPage = CacheProfile.getOptions().pages.get(Options.PAGE_START);
            if (startPage != null) {
                if (startPage.floatType.equals(Options.FLOAT_TYPE_BANNER)) {
                    if (startPage.banner.equals(Options.BANNER_ADWIRED)) {
                        requestAdwiredFullscreen();
                    } else if (startPage.banner.equals(Options.BANNER_TOPFACE)) {
                        requestTopfaceFullscreen();
                    } else if (startPage.banner.equals(Options.BANNER_MOPUB)) {
                        requestMopubFullscreen();
                    } else if (startPage.banner.equals(Options.BANNER_INNERACTIVE)) {
                        requestInneractiveFullscreen();
                    } else if (startPage.banner.equals(Options.BANNER_MOBCLIX)) {
                        requestMobclixFullscreen();
                    }
                }
            }
        }
    }

    private void requestMobclixFullscreen() {
        MobclixFullScreenAdView adview = new MobclixFullScreenAdView(mActivity);
        adview.requestAndDisplayAd();
        adview.addMobclixAdViewListener(new MobclixFullScreenAdViewListener() {
            @Override
            public void onFinishLoad(MobclixFullScreenAdView mobclixFullScreenAdView) {
                addLastFullscreenShowedTime();
            }

            @Override
            public void onFailedLoad(MobclixFullScreenAdView mobclixFullScreenAdView, int i) {
                requestFallbackFullscreen();
            }

            @Override
            public void onPresentAd(MobclixFullScreenAdView mobclixFullScreenAdView) {
            }

            @Override
            public void onDismissAd(MobclixFullScreenAdView mobclixFullScreenAdView) {
            }

            @Override
            public String keywords() {
                return null;
            }

            @Override
            public String query() {
                return null;
            }
        });
    }

    private void requestInneractiveFullscreen() {
        final InneractiveAd iaBanner = new InneractiveAd(mActivity, "Topface_TopfaceAndroid_Android", InneractiveAd.IaAdType.Interstitial, 60);
        ViewGroup container = getFullscreenBannerContainer();
        iaBanner.setInneractiveListener(new InneractiveAdListener() {
            @Override
            public void onIaAdReceived() {
                addLastFullscreenShowedTime();
                Debug.log("Inneractive: onIaAdReceived()");
            }

            @Override
            public void onIaDefaultAdReceived() {
                addLastFullscreenShowedTime();
                Debug.log("Inneractive: onIaDefaultAdReceived()");
            }

            @Override
            public void onIaAdFailed() {
                Debug.log("Inneractive: onIaAdFailed()");
                requestFallbackFullscreen();
            }

            @Override
            public void onIaAdClicked() {
                Debug.log("Inneractive: onIaAdClicked()");
            }

            @Override
            public void onIaAdResize() {
                Debug.log("Inneractive: onIaAdResize()");
            }

            @Override
            public void onIaAdResizeClosed() {
                Debug.log("Inneractive: onIaAdResizeClosed()");
            }

            @Override
            public void onIaAdExpand() {
                Debug.log("Inneractive: onIaAdExpand()");
            }

            @Override
            public void onIaAdExpandClosed() {
                Debug.log("Inneractive: onIaAdExpandClosed()");
            }

            @Override
            public void onIaDismissScreen() {
                Debug.log("Inneractive: onIaDismissScreen()");
            }
        });
        container.addView(iaBanner);
    }

    private void requestFallbackFullscreen() {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addLastFullscreenShowedTime();
                    requestTopfaceFullscreen();
                }
            });
        }
    }

    private void requestMopubFullscreen() {
        if (mInterstitial == null) {
            mInterstitial = new MoPubInterstitial(mActivity, MOPUB_INTERSTITIAL_ID);
        }
        mInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                if (interstitial.isReady()) {
                    interstitial.show();
                    addLastFullscreenShowedTime();
                }
                Debug.log("MoPub: onInterstitialLoaded()");
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                requestFallbackFullscreen();
                Debug.log("MoPub: onInterstitialFailed()");
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {
                Debug.log("MoPub: onInterstitialShown()");
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
                Debug.log("MoPub: onInterstitialClicked()");
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                Debug.log("MoPub: onInterstitialDismissed()");
            }
        });
        mInterstitial.load();
    }

    private void requestAdwiredFullscreen() {
        try {
            if (!CacheProfile.isEmpty()) {
                AWView adwiredView = (AWView) mActivity.getLayoutInflater().inflate(R.layout.banner_adwired, null);
                final ViewGroup bannerContainer = getFullscreenBannerContainer();
                bannerContainer.addView(adwiredView);
                bannerContainer.setVisibility(View.VISIBLE);
                adwiredView.setVisibility(View.VISIBLE);
                adwiredView.setOnNoBannerListener(new OnNoBannerListener() {
                    @Override
                    public void onNoBanner() {
                        requestFallbackFullscreen();
                    }
                });
                adwiredView.setOnStopListener(new OnStopListener() {
                    @Override
                    public void onStop() {
                        hideFullscreenBanner(bannerContainer);
                    }
                });
                adwiredView.setOnStartListener(new OnStartListener() {
                    @Override
                    public void onStart() {
                        isFullScreenBannerVisible = true;
                        addLastFullscreenShowedTime();
                    }
                });
                adwiredView.request('0');
            }
        } catch (Exception ex) {
            Debug.error(ex);
        }
    }

    private boolean showFullscreenBanner(String url) {
        boolean passByTime = isTimePassed();
        boolean passByUrl = passFullScreenByUrl(url);

        return passByUrl && passByTime;
    }

    private boolean isTimePassed() {
        long currentTime = System.currentTimeMillis();
        long lastCall = getPreferences().getLong(Static.PREFERENCES_LAST_FULLSCREEN_TIME, currentTime);
        return !getPreferences().contains(Static.PREFERENCES_LAST_FULLSCREEN_TIME)
                || Math.abs(currentTime - lastCall) > DateUtils.DAY_IN_MILLISECONDS;
    }

    private boolean passFullScreenByUrl(String url) {
        return !getFullscreenUrls().contains(url);
    }

    private Set<String> getFullscreenUrls() {
        String urls = getPreferences().getString(Static.PREFERENCES_FULLSCREEN_URLS_SET, "");
        String[] urlList = TextUtils.split(urls, URL_SEPARATOR);
        return new HashSet<String>(Arrays.asList(urlList));
    }

    private void addLastFullscreenShowedTime() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putLong(Static.PREFERENCES_LAST_FULLSCREEN_TIME, System.currentTimeMillis());
        editor.commit();
    }

    private void addNewUrlToFullscreenSet(String url) {
        Set<String> urlSet = getFullscreenUrls();
        urlSet.add(url);
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(Static.PREFERENCES_FULLSCREEN_URLS_SET, TextUtils.join(URL_SEPARATOR, urlSet));
        editor.commit();
    }

    private void requestTopfaceFullscreen() {
        BannerRequest request = new BannerRequest(App.getContext());
        request.place = Options.PAGE_START;
        request.callback(new BaseApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final Banner banner = Banner.parse(response);

                if (banner.action.equals(Banner.ACTION_URL)) {
                    if (showFullscreenBanner(banner.parameter)) {
                        isFullScreenBannerVisible = true;
                        addLastFullscreenShowedTime();
                        final View fullscreenViewGroup = mActivity.getLayoutInflater().inflate(R.layout.fullscreen_topface, null);
                        final ViewGroup bannerContainer = getFullscreenBannerContainer();
                        bannerContainer.addView(fullscreenViewGroup);
                        bannerContainer.setVisibility(View.VISIBLE);
                        final ImageViewRemote fullscreenImage = (ImageViewRemote) fullscreenViewGroup.findViewById(R.id.ivFullScreen);
                        fullscreenImage.setRemoteSrc(banner.url);
                        fullscreenImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addNewUrlToFullscreenSet(banner.parameter);
                                hideFullscreenBanner(bannerContainer);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                                mActivity.startActivity(intent);
                            }
                        });

                        fullscreenViewGroup.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hideFullscreenBanner(bannerContainer);
                            }
                        });
                    }
                }
            }
        }).exec();
    }

    public void hideFullscreenBanner(final ViewGroup bannerContainer) {
        Animation animation = AnimationUtils.loadAnimation(App.getContext(), android.R.anim.fade_out);
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
        isFullScreenBannerVisible = false;
    }

    private SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = mActivity.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        }
        return mPreferences;
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

    public void onDestroy() {
        if (mInterstitial != null) mInterstitial.destroy();
    }
}
