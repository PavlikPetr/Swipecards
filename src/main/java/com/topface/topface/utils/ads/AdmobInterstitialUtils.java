package com.topface.topface.utils.ads;

import android.app.Activity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.config.UserConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kirussell on 28/04/15.
 * Util methods to run admob interstitials
 */
public class AdmobInterstitialUtils {

    private static final String ADMOB_INTERSTITIAL_FEED = "ca-app-pub-9530442067223936/3710793209";
    private static final String ADMOB_INTERSTITIAL_FEED_NEWBIE = "ca-app-pub-9530442067223936/2234060003";

    private static final int PRELOAD_COUNT = 1;
    private static final AtomicInteger mPreloadingInterstitialsCount = new AtomicInteger(0);
    private static final List<InterstitialAd> loadedInterstitials = Collections.synchronizedList(new ArrayList<InterstitialAd>(PRELOAD_COUNT));

    public static void preloadInterstitials(final Activity activity, final Options.InterstitialInFeeds interstitialInFeed) {
        if (needPreload()) {
            if (interstitialInFeed.canShow()) {
                AdListener listener = new SimpleAdListener() {
                    @Override
                    void onAdLoaded(InterstitialAd interstitial) {
                        loadedInterstitials.add(interstitial);
                        mPreloadingInterstitialsCount.decrementAndGet();
                        if (needPreload()) {
                            preloadInterstitials(activity, interstitialInFeed);
                        }
                    }
                };
                mPreloadingInterstitialsCount.incrementAndGet();
                requestFeedInterstitial(activity, interstitialInFeed.adGroup, listener);
            }
        }
    }

    private static boolean needPreload() {
        return loadedInterstitials.size() + mPreloadingInterstitialsCount.get() < PRELOAD_COUNT;
    }

    public static void releaseInterstitials() {
        for (InterstitialAd interstitial : loadedInterstitials) {
            interstitial.setAdListener(null);
        }
        loadedInterstitials.clear();
    }

    private static void requestFeedInterstitial(Activity activity, String adGroup, AdListener listener) {
        switch (adGroup) {
            case Options.InterstitialInFeeds.FEED:
                requestAdmobFullscreen(activity, ADMOB_INTERSTITIAL_FEED, listener, false);
                break;
            case Options.InterstitialInFeeds.FEED_NEWBIE:
                requestAdmobFullscreen(activity, ADMOB_INTERSTITIAL_FEED_NEWBIE, listener, false);
                break;
            default:
                Debug.log("No adGroup provided for feed interstitial");
        }
    }

    public static InterstitialAd requestAdmobFullscreen(Activity activity, String id, final AdListener listener,
                                                        final boolean showOnAdLoaded) {
        // Создание межстраничного объявления.
        final InterstitialAd interstitial = new InterstitialAd(activity);
        interstitial.setAdUnitId(id);
        // Создание запроса объявления.
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        adRequestBuilder.setGender(
                App.from(activity).getProfile().sex == Profile.BOY ?
                        AdRequest.GENDER_MALE :
                        AdRequest.GENDER_FEMALE
        );
        // AdListener будет использовать обратные вызовы, указанные ниже.
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                if (listener != null) {
                    listener.onAdClosed();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (listener != null) {
                    listener.onAdFailedToLoad(errorCode);
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (listener != null) {
                    listener.onAdLeftApplication();
                }
            }

            @Override
            public void onAdOpened() {
                if (listener != null) {
                    listener.onAdOpened();
                }
            }

            @Override
            public void onAdLoaded() {
                if (showOnAdLoaded) {
                    interstitial.show();
                }
                if (listener != null) {
                    listener.onAdLoaded();
                }
            }
        });
        if (listener instanceof SimpleAdListener) {
            ((SimpleAdListener) listener).setInterstitial(interstitial);
        }
        // Запуск загрузки межстраничного объявления.
        interstitial.loadAd(adRequestBuilder.build());
        return interstitial;
    }

    public static InterstitialAd requestAdmobFullscreen(Activity activity, String id,
                                                        final AdListener listener) {
        return requestAdmobFullscreen(activity, id, listener, true);
    }

    public static void requestPreloadedInterstitial(Activity activity, Options.InterstitialInFeeds interstitialInFeed) {
        if (loadedInterstitials.isEmpty()) {
            preloadInterstitials(activity, interstitialInFeed);
        } else {
            InterstitialAd interstitial = loadedInterstitials.remove(0);
            interstitial.show();
            notifyShow(interstitialInFeed.count);
        }
    }

    public static boolean canShowInterstitialAds() {
        return !loadedInterstitials.isEmpty();
    }

    private static void notifyShow(int count) {
        if (count > 0) {
            UserConfig config = App.getUserConfig();
            int counter = config.incrementInterstitialInFeedsCounter();
            if (counter == 1) {
                config.setInterstitialsInFeedFirstShow(System.currentTimeMillis());
            }
        }
    }

    private static abstract class SimpleAdListener extends AdListener {

        InterstitialAd mInterstitial;

        public void setInterstitial(InterstitialAd interstitial) {
            mInterstitial = interstitial;
        }

        @Override
        public final void onAdLoaded() {
            super.onAdLoaded();
            if (mInterstitial != null) {
                onAdLoaded(mInterstitial);
            }
        }

        abstract void onAdLoaded(InterstitialAd interstitial);
    }

    public static void onLogout() {
        loadedInterstitials.clear();
        mPreloadingInterstitialsCount.set(0);
    }
}
