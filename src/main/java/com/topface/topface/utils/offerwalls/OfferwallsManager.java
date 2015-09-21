package com.topface.topface.utils.offerwalls;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.common.OfferwallPayload;
import com.topface.offerwall.common.TFCredentials;
import com.topface.offerwall.publisher.TFOfferwallSDK;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.utils.Utils;
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

    public static final String SPONSORPAY_APP_ID = "11625";
    public static final String SPONSORPAY_SECURITY_TOKEN = "0a4c64db64ed3c1ca14a5e5d81aaa23c";
    private static final int SPONSORPAY_OFFERWALL_REQUEST_CODE = 856;

    private static String getOfferWallType(String offerwall) {
        return offerwall;
    }

    public static void init(Activity activity, Options options) {
        String offerwall = getOfferWallType(options.offerwall);
        if (!TextUtils.isEmpty(offerwall)) {
            switch (offerwall) {
                case SPONSORPAY:
                    initSponsorpay(activity);
                    break;
            }
        }
    }


    public static void startOfferwall(Activity activity, Options options) {
        startOfferwall(activity, getOfferWallType(options.offerwall), options);
    }

    public static void startOfferwall(Activity activity, String offerwall, Options options) {
        offerwall = offerwall == null ? "" : offerwall;

        if (App.from(activity).getProfile().uid <= 0) {
            Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
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
                TopfaceOfferwallRedirect topfaceOfferwallRedirect = options.topfaceOfferwallRedirect;
                if (topfaceOfferwallRedirect != null && topfaceOfferwallRedirect.isEnabled()) {
                    OfferwallPayload offerwallPayload = new OfferwallPayload();
                    offerwallPayload.experimentGroup = topfaceOfferwallRedirect.getGroup();
                    startTfOfferwall(activity, offerwallPayload);
                } else {
                    startTfOfferwall(activity);
                }
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
     * Инициализация Sponsorpay, без этого офферволл не стартанет
     */
    private static void initSponsorpay(Activity activity) {
        try {
            SponsorPay.start(
                    SPONSORPAY_APP_ID,
                    Integer.toString(App.from(activity).getProfile().uid),
                    SPONSORPAY_SECURITY_TOKEN,
                    activity
            );
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public static void startSponsorpay(Activity activity) {
        try {
            Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity(activity, true);
            activity.startActivityForResult(offerWallIntent, SPONSORPAY_OFFERWALL_REQUEST_CODE);
        } catch (Exception e) {
            Debug.error(e);
            if (activity != null && App.from(activity).getProfile().uid > 0) {
                initSponsorpay(activity);
                Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity(activity, true);
                activity.startActivityForResult(offerWallIntent, SPONSORPAY_OFFERWALL_REQUEST_CODE);
            }
        }
    }

    public static void initTfOfferwall(Context context, TFCredentials.OnInitializeListener listener) {
        Profile profile = App.from(context).getProfile();
        TFOfferwallSDK.initialize(context, Integer.toString(profile.uid), "53edb54b0fdc7", listener);
        TFOfferwallSDK.setTarget(new TFOfferwallSDK.Target().setAge(profile.age).setSex(profile.sex));
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
