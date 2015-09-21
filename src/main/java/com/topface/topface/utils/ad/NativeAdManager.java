package com.topface.topface.utils.ad;

import android.content.Context;

import com.topface.topface.data.Options;
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
            if (adv.isEnabled() && adv.hasAd() && adv.getRemainedShows() > 0) {
                return true;
            }
        }
        return false;
    }

    public static void init(Options options, Context context) {
        advertisings.clear();
        advertisings.add(new PubnativeAdvertising(options, context));
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
                if (adv.isEnabled() && adv.hasAd()) {
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
