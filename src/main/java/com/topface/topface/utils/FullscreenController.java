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

import com.ivengo.adv.AdvListener;
import com.ivengo.adv.AdvView;
import com.lifestreet.android.lsmsdk.BannerAdapter;
import com.lifestreet.android.lsmsdk.BasicSlotListener;
import com.lifestreet.android.lsmsdk.InterstitialAdapter;
import com.lifestreet.android.lsmsdk.InterstitialSlot;
import com.lifestreet.android.lsmsdk.SlotView;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Banner;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.blocks.BannerBlock;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.views.ImageViewRemote;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ru.ideast.adwired.AWView;
import ru.ideast.adwired.events.OnNoBannerListener;
import ru.ideast.adwired.events.OnStartListener;
import ru.ideast.adwired.events.OnStopListener;

/**
 */
public class FullscreenController {

    public static final String URL_SEPARATOR = "::";
    private static boolean isFullScreenBannerVisible = false;
    private static final String MOPUB_INTERSTITIAL_ID = "00db7208a90811e281c11231392559e4";
    private static final String IVENGO_APP_ID = "aggeas97392g";
    private static final String LIFESTREET_TAG = "http://mobile-android.lfstmedia.com/m2/slot76331?ad_size=320x480&adkey=a25";

    private SharedPreferences mPreferences;
    private Activity mActivity;

    private MoPubInterstitial mInterstitial;
    private AdvView advViewIvengo;

    public FullscreenController(Activity activity) {
        mActivity = activity;
    }

    public void requestFullscreen() {
        if (!CacheProfile.isEmpty() && isTimePassed()) {
            Options.Page startPage = CacheProfile.getOptions().pages.get(Options.PAGE_START);
            if (startPage != null) {
                if (startPage.floatType.equals(FloatBlock.FLOAT_TYPE_BANNER)) {
                    requestFullscreen(startPage.banner);
                }
            }
        }
    }

    private void requestLifestreetFullscreen() {
        InterstitialSlot slot = new InterstitialSlot(mActivity);
        slot.setSlotTag(LIFESTREET_TAG);
        slot.setListener(new BasicSlotListener() {
            @Override
            public void onFailedToLoadSlotView(SlotView slotView) {
                requestGagFullscreen();
            }

            @Override
            public void onReceiveInterstitialAd(InterstitialAdapter<?> adapter, Object ad) {
                addLastFullscreenShowedTime();
            }

            @Override
            public void onPresentInterstitialScreen(InterstitialAdapter<?> adapter, Object ad) {
                addLastFullscreenShowedTime();
            }

            @Override
            public void onFailedToReceiveAd(BannerAdapter<?> adapter, View view) {
                requestGagFullscreen();
            }

            @Override
            public void onFailedToReceiveInterstitialAd(InterstitialAdapter<?> adapter, Object ad) {
                requestGagFullscreen();
            }
        });
        slot.setShowCloseButton(true);
        slot.loadAd();
    }

    private void requestIvengoFullscreen() {
        advViewIvengo = AdvView.create(mActivity, IVENGO_APP_ID);
        advViewIvengo.showBanner();
        advViewIvengo.setAdvListener(new AdvListener() {
            @Override
            public void onDisplayAd() {
                addLastFullscreenShowedTime();
            }

            @Override
            public void onAdClick(String s) {
            }

            @Override
            public void onFailedToReceiveAd(AdvView.ErrorCode errorCode) {
                requestFallbackFullscreen();
            }

            @Override
            public void onCloseAd(int i) {
            }
        });
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
        new BackgroundThread() {
            @Override
            public void execute() {
                SharedPreferences.Editor editor = getPreferences().edit();
                editor.putLong(Static.PREFERENCES_LAST_FULLSCREEN_TIME, System.currentTimeMillis());
                editor.commit();
            }
        };
    }

    private void addNewUrlToFullscreenSet(String url) {
        Set<String> urlSet = getFullscreenUrls();
        urlSet.add(url);
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(Static.PREFERENCES_FULLSCREEN_URLS_SET, TextUtils.join(URL_SEPARATOR, urlSet));
        editor.commit();
    }

    private void requestGagFullscreen() {
        requestFullscreen(CacheProfile.getOptions().gagTypeFullscreen);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void requestFullscreen(String type) {
        if (type.equals(BannerBlock.BANNER_NONE)) {
            return;
        } else if (type.equals(BannerBlock.BANNER_ADWIRED)) {
            requestAdwiredFullscreen();
        } else if (type.equals(BannerBlock.BANNER_TOPFACE)) {
            requestTopfaceFullscreen();
        } else if (type.equals(BannerBlock.BANNER_MOPUB)) {
            requestMopubFullscreen();
        } else if (type.equals(BannerBlock.BANNER_IVENGO)) {
            requestIvengoFullscreen();
        } else if (type.equals(BannerBlock.BANNER_LIFESTREET)) {
            requestLifestreetFullscreen();
        }
    }

    private void requestTopfaceFullscreen() {
        BannerRequest request = new BannerRequest(App.getContext());
        request.place = Options.PAGE_START;
        request.callback(new DataApiHandler<Banner>() {
            @Override
            public void success(final Banner data, IApiResponse response) {
                if (data.action.equals(Banner.ACTION_URL)) {
                    if (showFullscreenBanner(data.parameter)) {
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
                                addNewUrlToFullscreenSet(data.parameter);
                                hideFullscreenBanner(bannerContainer);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.parameter));
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

            @Override
            protected Banner parseResponse(ApiResponse response) {
                return Banner.parse(response);
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
            ((ViewGroup) mActivity.findViewById(BaseFragmentActivity.getContentViewCompat())).addView(fullscreenContainer);
        }
        return fullscreenContainer;
    }

    public void onDestroy() {
        if (mInterstitial != null) mInterstitial.destroy();
    }

    public void onPause() {
        if (advViewIvengo != null) {
            advViewIvengo.dismiss();
        }
    }
}
