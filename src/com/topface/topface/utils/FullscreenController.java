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
        if (!CacheProfile.isEmpty()) {
            Options.Page startPage = CacheProfile.getOptions().pages.get(Options.PAGE_START);
            if (startPage != null) {
                if (startPage.floatType.equals(Options.FLOAT_TYPE_BANNER)) {
                    if (startPage.banner.equals(Options.BANNER_ADWIRED)) {
                        requestAdwiredFullscreen();
                    } else if (startPage.banner.equals(Options.BANNER_TOPFACE)) {
                        requestTopfaceFullscreen();
                    } else if (startPage.banner.equals(Options.BANNER_MOPUB)) {
                        requestMopubFullscreen();
                    }
                }
            }
        }
    }

    private void requestMopubFullscreen() {
        if (mInterstitial == null) {
            mInterstitial = new MoPubInterstitial(mActivity, MOPUB_INTERSTITIAL_ID);
        }
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
                        requestTopfaceFullscreen();
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
                    }
                });
                adwiredView.request('0');
            }
        } catch (Exception ex) {
            Debug.error(ex);
        }
    }

    private boolean showFullscreenBanner(String url) {
        long currentTime = System.currentTimeMillis();
        long lastCall = getPreferences().getLong(Static.PREFERENCES_LAST_FULLSCREEN_TIME, currentTime);
        boolean passByTime = !getPreferences().contains(Static.PREFERENCES_LAST_FULLSCREEN_TIME)
                || Math.abs(currentTime - lastCall) > DateUtils.DAY_IN_MILLISECONDS;
        boolean passByUrl = passFullScreenByUrl(url);

        return passByUrl && passByTime;
    }

    private boolean passFullScreenByUrl(String url) {
        return !getFullscrenUrls().contains(url);
    }

    private Set<String> getFullscrenUrls() {
        String urls = getPreferences().getString(Static.PREFERENCES_FULLSCREEN_URLS_SET, "");
        String[] urlList = TextUtils.split(urls, URL_SEPARATOR);
        return new HashSet<String>(Arrays.asList(urlList));
    }

    private void addLastFullsreenShowedTime() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putLong(Static.PREFERENCES_LAST_FULLSCREEN_TIME, System.currentTimeMillis());
        editor.commit();
    }

    private void addNewUrlToFullscreenSet(String url) {
        Set<String> urlSet = getFullscrenUrls();
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
                        addLastFullsreenShowedTime();
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
            ((ViewGroup) mActivity.findViewById(R.id.NavigationLayout)).addView(fullscreenContainer);
        }
        return fullscreenContainer;
    }

    public void onDestroy() {
        if (mInterstitial != null) mInterstitial.destroy();
    }
}
