package com.topface.topface.utils.offerwalls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.common.OfferwallPayload;
import com.topface.offerwall.common.TFCredentials;
import com.topface.offerwall.publisher.TFOfferwallSDK;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.offerwalls.supersonicads.SupersonicWallActivity;

import java.util.Random;

public class OfferwallsManager {

    /**
     * Идентификаторы для типов офферволлов
     */

    /**
     * TapJoy благополучно выпилен.
     */
    public static final String TAPJOY = "TAPJOY";
    public static final String SPONSORPAY = "SPONSORPAY";
    public static final String RANDOM = "RANDOM";
    public static final String SUPERSONIC = "SUPERSONIC";
    public static final String TFOFFERWALL = "TOPFACE";

    public final static String[] OFFERWALLS = new String[]{
            TAPJOY,
            SPONSORPAY,
            SUPERSONIC,
            TFOFFERWALL,
            RANDOM
    };

    private static String getOfferWallType() {
        return CacheProfile.getOptions().offerwall;
    }

    public static void init(Context context) {
        String offerwall = getOfferWallType();
        if (!TextUtils.isEmpty(offerwall)) {
            switch (offerwall) {
                case SPONSORPAY:
                    initSponsorpay(context);
                    break;
            }
        }
    }


    public static void startOfferwall(Activity activity) {
        startOfferwall(activity, getOfferWallType());
    }

    public static void startOfferwall(Activity activity, String offerwall) {
        offerwall = offerwall == null ? "" : offerwall;

        if (CacheProfile.uid <= 0) {
            Toast.makeText(activity, R.string.general_server_error, Toast.LENGTH_SHORT).show();
            return;
        }

        switch (offerwall) {
            case SPONSORPAY:
                startSponsorpay(activity);
                break;
            case SUPERSONIC:
                startSupersonic(activity);
                break;
            case TFOFFERWALL:
                startTfOfferwall(activity);
                break;
            case RANDOM:
                startRandomOfferwall(activity);
                break;
            default:
                startDefault(activity);
                break;
        }
    }

    /**
     * Стартует Offerwall по умолчанию
     */
    private static void startDefault(Activity activity) {
        startSponsorpay(activity);
    }

    private static void startRandomOfferwall(Activity activity) {
        Random random = new Random();
        switch (random.nextInt(2)) {
            case 1:
                startSponsorpay(activity);
                break;
            case 2:
                startTfOfferwall(activity);
            default:
                startDefault(activity);
                break;
        }
    }

    /**
     * Sponsorpay
     */
    private static void initSponsorpay(Context context) {
        try {
            SponsorPay.start("11625", Integer.toString(CacheProfile.uid), "0a4c64db64ed3c1ca14a5e5d81aaa23c", context);
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
            if (activity != null && CacheProfile.uid > 0) {
                initSponsorpay(activity);
                Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity(activity.getApplicationContext(), true);
                activity.startActivityForResult(offerWallIntent, SponsorPayPublisher.DEFAULT_OFFERWALL_REQUEST_CODE);
            }
        }
    }

    public static void initTfOfferwall(Context context, TFCredentials.OnInitializeListener listener) {
        TFOfferwallSDK.initialize(context, Integer.toString(CacheProfile.uid), "53edb54b0fdc7", listener);
        TFOfferwallSDK.setTarget(new TFOfferwallSDK.Target().setAge(CacheProfile.age).setSex(CacheProfile.sex));
    }

    public static void startTfOfferwall(Context context) {
        TFOfferwallSDK.showOffers(context, true, context.getResources().getString(R.string.general_bonus));
    }

    public static void startTfOfferwall(Context context, OfferwallPayload payload) {
        TFOfferwallSDK.showOffers(context, true, context.getResources().getString(R.string.general_bonus), payload);
    }

    private static void startSupersonic(Activity activity) {
        activity.startActivity(new Intent(activity, SupersonicWallActivity.class));
    }
}
