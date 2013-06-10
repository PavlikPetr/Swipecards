package com.topface.topface.utils.offerwalls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.tapjoy.TapjoyConnect;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.offerwalls.clickky.ClickkyActivity;

import java.util.Random;

public class Offerwalls {

    public static void init(Context context) {
        try {
            if (CacheProfile.uid > 0) {
                initTapjoy(context);
                initSponsorpay(context);
            }
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    private static void initSponsorpay(Context context) {
        try {
            SponsorPay.start("11625", Integer.toString(CacheProfile.uid), "0a4c64db64ed3c1ca14a5e5d81aaa23c", context);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    private static void initTapjoy(Context context) {
        try {
            TapjoyConnect.requestTapjoyConnect(context, "f0563cf4-9e7c-4962-b333-098810c477d2", "AS0AE9vmrWvkyNNGPsyu");
            TapjoyConnect.getTapjoyConnectInstance().setUserID(Integer.toString(CacheProfile.uid));
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public static void startOfferwall(Activity activity) {
        String offerwall = CacheProfile.getOptions().offerwall;
        offerwall = offerwall == null ? "" : offerwall;

        if (CacheProfile.uid <= 0) {
            Toast.makeText(activity, R.string.general_server_error, Toast.LENGTH_SHORT);
            return;
        }

        if (offerwall.equals(Options.TAPJOY)) {
            startTapjoy(activity);
        } else if (offerwall.equals(Options.SPONSORPAY)) {
            startSponsorpay(activity);
        } else if (offerwall.equals(Options.CLICKKY)) {
            startClickky(activity);
        } else if (offerwall.equals(Options.RANDOM)) {
            startRandomOfferwall(activity);
        } else {
            startSponsorpay(activity);
        }
    }

    private static void startRandomOfferwall(Activity activity) {
        Random random = new Random();
        if (random.nextBoolean()) {
            startTapjoy(activity);
        } else {
            startSponsorpay(activity);
        }
    }

    public static void startTapjoy(Context context) {
        try {
            TapjoyConnect.getTapjoyConnectInstance().showOffers();
        } catch (Exception e) {
            Debug.error(e);
            if (context != null) initTapjoy(context);
        }
    }

    public static void startTapjoy() {
        startTapjoy(null);
    }

    public static void startSponsorpay(Activity activity) {
        try {
            Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity(activity.getApplicationContext(), true);
            activity.startActivityForResult(offerWallIntent, SponsorPayPublisher.DEFAULT_OFFERWALL_REQUEST_CODE);
        } catch (Exception e) {
            Debug.error(e);
            initSponsorpay(activity);
        }
    }

    public static void startClickky(Activity activity) {
        try {
            Intent offerWallIntent = new Intent(activity, ClickkyActivity.class);
            activity.startActivity(offerWallIntent);
        } catch (Exception e) {
            Debug.error(e);
        }

    }
}
