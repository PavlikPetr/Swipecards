package com.topface.topface.utils.ad;

import com.topface.topface.utils.ad.pubnative.PubnativeAdvertising;

import java.util.ArrayList;
import java.util.List;

/**
 * Master native ad manager.
 */
public class NativeAdManager {

    private static List<Advertising> advertisings = new ArrayList<>();

    public static boolean hasAvailableAd() {
        for (Advertising adv : advertisings) {
            if (adv.hasAd() && adv.getRemainedShows() > 0) {
                return true;
            }
        }
        return false;
    }

    public static void init() {
        advertisings.add(new PubnativeAdvertising());
        loadAd();
    }

    public static void loadAd() {
        for (Advertising adv : advertisings) {
            if (adv.needMoreAds() && !adv.isLoading()) {
                adv.requestAd();
            }
        }
    }

    public static NativeAd getNativeAd() {
        NativeAd ad = null;
        if (hasAvailableAd()) {
            for (Advertising adv : advertisings) {
                if (adv.hasAd()) {
                    ad = adv.popAd();
                    break;
                }
            }
            if (!hasAvailableAd()) {
                loadAd();
            }
        } else {
            loadAd();
        }
        return ad;
    }
}
