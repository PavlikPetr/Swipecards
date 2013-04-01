package com.topface.topface.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.tapjoy.TapjoyConnect;
import com.topface.topface.data.Options;

import java.util.Random;

public class Offerwalls {

    public static void init(Context context) {
        try {
            TapjoyConnect.requestTapjoyConnect(context, "f0563cf4-9e7c-4962-b333-098810c477d2", "AS0AE9vmrWvkyNNGPsyu");
            TapjoyConnect.getTapjoyConnectInstance().setUserID(Integer.toString(CacheProfile.uid));
            SponsorPay.start("11625", Integer.toString(CacheProfile.uid), "0a4c64db64ed3c1ca14a5e5d81aaa23c", context);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public static void startOfferwall(Activity activity) {
        String offerwall = CacheProfile.getOptions().offerwall;

        if (offerwall.equals(Options.TAPJOY)) {
            startTapjoy();
        } else if (offerwall.equals(Options.SPONSORPAY)) {
            startSponsorpay(activity);
        } else if (offerwall.equals(Options.RANDOM)) {
            startRandomOfferwall(activity);
        } else {
            startTapjoy();
        }
    }

    private static void startRandomOfferwall(Activity activity) {
        Random random = new Random();
        if (random.nextBoolean()) {
            startTapjoy();
        } else {
            startSponsorpay(activity);
        }
    }

    public static void startTapjoy() {
        try {
            TapjoyConnect.getTapjoyConnectInstance().showOffers();
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public static void startSponsorpay(Activity activity) {
        try {
            Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity(activity.getApplicationContext(), true);
            activity.startActivityForResult(offerWallIntent, SponsorPayPublisher.DEFAULT_OFFERWALL_REQUEST_CODE);
        } catch (Exception e) {
            Debug.error(e);
        }
    }
}
