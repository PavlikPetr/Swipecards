package com.topface.topface.utils.offerwalls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;
import com.getjar.sdk.GetJarContext;
import com.getjar.sdk.GetJarManager;
import com.getjar.sdk.GetJarPage;
import com.getjar.sdk.response.PurchaseSucceededResponse;
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

    private static GetJarContext mGetJarContext;
    private static GetJarPage mRewardPage;

    private final static String GETJAR_APP_KEY = ""; //TODO
    private final static String GETJAR_ENCRYPTION_KEY = ""; //TODO

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

    private static void initGetJar(Context context) {
        try {
            // appKey: Application Key provided by Getjar
            // encryptionKey: Key provided if you are to use licensing
            mGetJarContext = GetJarManager.createContext(GETJAR_APP_KEY, GETJAR_ENCRYPTION_KEY, context, new RewardsReceiver(new Handler()));
            mRewardPage = new GetJarPage(mGetJarContext);
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
        } else if (offerwall.equals(Options.GETJAR)) {
            startGetJar(activity);
        } else if (offerwall.equals(Options.RANDOM)) {
            startRandomOfferwall(activity);
        } else {
            startSponsorpay(activity);
        }
    }

    private static void startRandomOfferwall(Activity activity) {
        Random random = new Random();
        switch (random.nextInt(2)) {
            case 0:
                startTapjoy(activity);
                break;
            case 1:
                startSponsorpay(activity);
                break;
            case 2:
                startClickky(activity);
                break;
            default:
                startSponsorpay(activity);
                break;
        }
    }

    public static void startTapjoy(Context context) {
        try {
            TapjoyConnect.getTapjoyConnectInstance().showOffers();
        } catch (Exception e) {
            Debug.error(e);
            if (context != null && CacheProfile.uid > 0) initTapjoy(context);
            TapjoyConnect.getTapjoyConnectInstance().showOffers();
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
            if (activity != null && CacheProfile.uid > 0) {
                initSponsorpay(activity);
                Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity(activity.getApplicationContext(), true);
                activity.startActivityForResult(offerWallIntent, SponsorPayPublisher.DEFAULT_OFFERWALL_REQUEST_CODE);
            }
        }
    }

    public static void startGetJar(Activity activity) {
        mRewardPage.showPage();
    }

    public static void startClickky(Activity activity) {
        try {
            Intent offerWallIntent = new Intent(activity, ClickkyActivity.class);
            activity.startActivity(offerWallIntent);
        } catch (Exception e) {
            Debug.error(e);
        }

    }

    public static class RewardsReceiver extends ResultReceiver {
        public RewardsReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult (int resultCode, Bundle resultData) {
            for(String key : resultData.keySet()) {
                Object value = resultData.get(key);
                if (value instanceof PurchaseSucceededResponse) {
                    PurchaseSucceededResponse response = (PurchaseSucceededResponse) value;
                    String productName = response.getProductName();
                    long amount = response.getAmount();// TODO: Handle a successful purchase here
                }
            }
        }
    }
}
