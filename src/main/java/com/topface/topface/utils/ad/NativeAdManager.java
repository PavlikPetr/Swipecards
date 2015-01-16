package com.topface.topface.utils.ad;

import com.topface.topface.utils.ad.pubnative.PubnativeAdvertising;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saharuk on 15.01.15.
 */
public class NativeAdManager {

    private static List<Advertising> advertisings = new ArrayList<>();

    public static boolean hasAvailableAd() {
        for (Advertising adv : advertisings) {
            if (adv.hasAd() && adv.hasShowsRemained()) {
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
            if (adv.hasShowsRemained()) {
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
