package com.topface.topface.utils.offerwalls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.tapjoy.TapjoyConnect;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.offerwalls.clickky.ClickkyActivity;

public class Offerwalls {

    public static final String TAPJOY = "TAPJOY";
    public static final String SPONSORPAY = "SPONSORPAY";
    public static final String CLICKKY = "CLICKKY";
    private static String nextOfferwall = "TAPJOY";

    public static void init(Context context) {
        TapjoyConnect.requestTapjoyConnect(context, "f0563cf4-9e7c-4962-b333-098810c477d2", "AS0AE9vmrWvkyNNGPsyu");
        TapjoyConnect.getTapjoyConnectInstance().setUserID(Integer.toString(CacheProfile.uid));
        SponsorPay.start("11625", Integer.toString(CacheProfile.uid), "0a4c64db64ed3c1ca14a5e5d81aaa23c", context);
    }

    public static void startOfferwall(Activity activity) {
        if (nextOfferwall.equals(TAPJOY)) {
            startTapjoy();
            nextOfferwall = SPONSORPAY;
        } else if (nextOfferwall.equals(SPONSORPAY)){
            startSponsorpay(activity);
            nextOfferwall = CLICKKY;
        } else if (nextOfferwall.equals(CLICKKY)){
            startClickky(activity);
            nextOfferwall = TAPJOY;
        }
    }

    public static void startTapjoy() {
        TapjoyConnect.getTapjoyConnectInstance().showOffers();
    }

    public static void startSponsorpay(Activity activity) {
        Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity(activity.getApplicationContext(), true);
        activity.startActivityForResult(offerWallIntent, SponsorPayPublisher.DEFAULT_OFFERWALL_REQUEST_CODE);
    }

    public static void startClickky(Activity activity) {
        Intent offerWallIntent = new Intent(activity, ClickkyActivity.class);
        activity.startActivity(offerWallIntent);
    }
}
