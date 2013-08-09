package com.topface.topface.utils.offerwalls;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;
import com.getjar.sdk.*;
import com.getjar.sdk.listener.EnsureUserAuthListener;
import com.getjar.sdk.listener.RecommendedPricesListener;
import com.getjar.sdk.response.PurchaseSucceededResponse;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.tapjoy.TapjoyConnect;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.offerwalls.clickky.ClickkyActivity;

import java.util.ArrayList;
import java.util.Random;

public class Offerwalls {

    private static GetJarContext mGetJarContext;
    private static ConsumableProductHelper mGetJarHelper;

    private final static String GETJAR_APP_KEY = "407c520c-aaba-44e8-9a06-478c2b595437";
    private final static String GETJAR_ENCRYPTION_KEY = "0000MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCEuY/YoY8n/WiXdXqv2+7v+N7B279TpVZi4IUEjZRXZAJMytPPqelWFFDAByVHcCZZGCzXoCRjwsvIPel/X0XpbPNVmgWXyMtCIe3gfGvRL686RCGu+MJSzAsFqV9JMes4eycBgjN6tzqo0nZjzmLTNLEpEzttAwKeRVG/q3txtwIDAQAB";
    private static final String GETJAR_PRODUCT_ID = "id"; //TODO
    private static final String GETJAR_PRODUCT_NAME = "100 coins"; //TODO
    private static final String GETJAR_PRODUCT_DESCRIPTION = "coins"; //TODO
    private static final long GETJAR_PRICE = 1; //TODO
    private static final Float GETJAT_MAX_DISCOUNT = 0.1f; //TODO
    private static final Float GETJAT_MAX_MARKUP = 0.1f; //TODO

    private static String getOfferWallType() {
        return Options.GETJAR;//CacheProfile.getOptions().offerwall;
    }

    public static void init(Context context) {
        String offerwall = getOfferWallType();
        if (offerwall.equals(Options.TAPJOY)) {
            initTapjoy(context);
        } else if (offerwall.equals(Options.SPONSORPAY)) {
            initSponsorpay(context);
        } else if (offerwall.equals(Options.GETJAR)) {
            initGetJar(context);
        }
    }

    public static void startOfferwall(Activity activity) {
        String offerwall = getOfferWallType();
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

    /**
     * Tapjoy
     **/
    private static void initTapjoy(Context context) {
        try {
            TapjoyConnect.requestTapjoyConnect(context, "f0563cf4-9e7c-4962-b333-098810c477d2", "AS0AE9vmrWvkyNNGPsyu");
            TapjoyConnect.getTapjoyConnectInstance().setUserID(Integer.toString(CacheProfile.uid));
        } catch (Exception e) {
            Debug.error(e);
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

    /**
     * Sponsorpay
     **/
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

    /**
     * Clickky
     **/
    public static void startClickky(Activity activity) {
        try {
            Intent offerWallIntent = new Intent(activity, ClickkyActivity.class);
            activity.startActivity(offerWallIntent);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    /**
     * GetJar
     **/
    private static void initGetJar(Context context) {
        try {
            mGetJarContext = GetJarManager.createContext(GETJAR_APP_KEY, GETJAR_ENCRYPTION_KEY, context, new RewardsReceiver(new Handler()));
            mGetJarHelper = new ConsumableProductHelper(mGetJarContext);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public static void startGetJar(Activity activity) {
        if (mGetJarContext == null || mGetJarHelper == null) initGetJar(activity);
        ConsumableProduct consumableProduct = new ConsumableProduct(GETJAR_PRODUCT_ID, GETJAR_PRODUCT_NAME,
                GETJAR_PRODUCT_DESCRIPTION, GETJAR_PRICE);
        mGetJarHelper.buy(activity.getString(R.string.getjar_auth_title), consumableProduct);
    }

    public static class RewardsReceiver extends ResultReceiver {
        public RewardsReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            for (String key : resultData.keySet()) {
                Object value = resultData.get(key);
                if (value instanceof PurchaseSucceededResponse) {
                    PurchaseSucceededResponse response = (PurchaseSucceededResponse) value;
                    String signedData = response.getSignedPayload();
                    String signature = response.getSignature();
                }
            }
        }
    }

    public static class ConsumableProductHelper {
        private ArrayList<Pricing> consumablePricingList = new ArrayList<Pricing>(1);
        private ConsumableProduct consumableProduct;
        private GetJarContext getJarContext;

        ConsumableProductHelper(GetJarContext getJarContext) {
            this.getJarContext = getJarContext;
        }

        void buy(String pickAccountTitle, ConsumableProduct consumableProduct){
            if(consumableProduct==null) {throw new IllegalArgumentException("consumableProduct cannot be null");}
            this.consumableProduct = consumableProduct;

            // Ensure user is authenticated
            UserAuth userAuth = new UserAuth(getJarContext);
            userAuth.ensureUserAsync(pickAccountTitle,
                    consumableUserAuthListener);
        }

        private void startGetJarRewardPage(ConsumableProduct product) {
            GetJarPage consumablePage = new GetJarPage(getJarContext);
            consumablePage.setProduct(product);
            consumablePage.showPage();
        }

        private EnsureUserAuthListener consumableUserAuthListener = new EnsureUserAuthListener() {

            @Override
            public void userAuthCompleted(User user) {
                if (user != null) {
                    Debug.log("consumableUserAuthListener^ success");
                    Localization localization = new Localization (getJarContext);
                    if (consumablePricingList.isEmpty()) {
                        consumablePricingList.add(new Pricing((int) consumableProduct.getAmount(), GETJAT_MAX_DISCOUNT, GETJAT_MAX_MARKUP));
                    }
                    localization.getRecommendedPricesAsync(consumablePricingList, consumableRecommendedPricesListener);
                } else {
                    Debug.log("consumableUserAuthListener: failed");
                }
            }
        };

        private RecommendedPricesListener consumableRecommendedPricesListener = new RecommendedPricesListener() {

            @Override
            public void recommendedPricesEvent(RecommendedPrices prices) {
                Debug.log("consumableRecommendedPricesListener: prices:" + (prices.getRecommendedPrice(consumablePricingList.get(0))));
                consumableProduct = new ConsumableProduct(consumableProduct.getProductId(), consumableProduct.getProductName(),
                        consumableProduct.getProductDescription(), prices.getRecommendedPrice(consumablePricingList.get(0)));
                startGetJarRewardPage(consumableProduct);
            }
        };
    }
}
