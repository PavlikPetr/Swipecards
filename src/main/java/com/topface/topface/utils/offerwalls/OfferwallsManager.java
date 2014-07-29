package com.topface.topface.utils.offerwalls;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.widget.Toast;

import com.getjar.sdk.ConsumableProduct;
import com.getjar.sdk.GetJarContext;
import com.getjar.sdk.GetJarManager;
import com.getjar.sdk.GetJarPage;
import com.getjar.sdk.Localization;
import com.getjar.sdk.Pricing;
import com.getjar.sdk.RecommendedPrices;
import com.getjar.sdk.User;
import com.getjar.sdk.UserAuth;
import com.getjar.sdk.listener.EnsureUserAuthListener;
import com.getjar.sdk.listener.RecommendedPricesListener;
import com.getjar.sdk.response.PurchaseSucceededResponse;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.tapjoy.TapjoyConnect;
import com.topface.framework.utils.Debug;
import com.topface.offer.TFOfferSDK;
import com.topface.offerwall.TFOfferwallSDK;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ValidateGetJarRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.offerwalls.supersonicads.SupersonicWallActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class OfferwallsManager {

    /**
     * Идентификаторы для типов офферволлов
     */
    public static final String TAPJOY = "TAPJOY";
    public static final String SPONSORPAY = "SPONSORPAY";
    public static final String CLICKKY = "CLICKKY";
    public static final String RANDOM = "RANDOM";
    public static final String GETJAR = "GETJAR";
    public static final String SUPERSONIC = "SUPERSONIC";
    public static final String TFOFFERWALL = "TOPFACE";
    @SuppressWarnings("UnusedDeclaration")
    public final static String[] OFFERWALLS = new String[]{
            TAPJOY,
            SPONSORPAY,
            CLICKKY,
            GETJAR,
            SUPERSONIC,
            TFOFFERWALL,
            RANDOM
    };

    private final static String GETJAR_APP_KEY = "407c520c-aaba-44e8-9a06-478c2b595437";
    private static final Float GETJAT_MAX_DISCOUNT = 0.1f;
    private static final Float GETJAT_MAX_MARKUP = 0.1f;

    private static GetJarContext mGetJarContext;
    private static ConsumableProductHelper mGetJarHelper;
    private static Dialog mProgressDialog;

    private static String getOfferWallType() {
        return CacheProfile.getOptions().offerwall;
    }

    public static void init(Context context) {
        String offerwall = getOfferWallType();
        if (!TextUtils.isEmpty(offerwall)) {
            switch (offerwall) {
                case TAPJOY:
                    initTapjoy(context);
                    break;
                case SPONSORPAY:
                    initSponsorpay(context);
                    break;
                case GETJAR:
                    initGetJar(context);
                    break;
                case TFOFFERWALL:
                    initTfOfferwall(context);
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
            case TAPJOY:
                startTapjoy(activity);
                break;
            case SPONSORPAY:
                startSponsorpay(activity);
                break;
            case GETJAR:
                startGetJar(activity);
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
            case 0:
                startTapjoy(activity);
                break;
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
     * Tapjoy
     */
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

    private static void initTfOfferwall(Context context) {
        TFOfferSDK.initialize(context);
    }

    public static void startTfOfferwall(Context context) {
        TFOfferwallSDK.initialize(context, Integer.toString(CacheProfile.uid), "53c7c07b1937c");
        TFOfferwallSDK.setTarget(new TFOfferwallSDK.Target().setAge(CacheProfile.age).setSex(CacheProfile.sex));
        TFOfferwallSDK.showOffers(context, true, context.getResources().getString(R.string.general_bonus));
    }

    /**
     * GetJar
     */
    private static void initGetJar(Context context) {
        try {
            mGetJarContext = GetJarManager.createContext(GETJAR_APP_KEY, context, new RewardsReceiver(new Handler()));
            mGetJarHelper = new ConsumableProductHelper(mGetJarContext);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public static void startGetJar(Activity activity) {
        showProgressDialog(activity);
        if (mGetJarContext == null || mGetJarHelper == null) {
            initGetJar(activity);
        }
        Options options = CacheProfile.getOptions();
        if (options != null && options.getJar != null) {
            ConsumableProduct consumableProduct = new ConsumableProduct(
                    options.getJar.getId(),
                    options.getJar.getName(),
                    options.getJar.getName(),
                    options.getJar.getPrice(),
                    R.drawable.ic_coins
            );
            mGetJarHelper.buy(activity.getString(R.string.getjar_auth_title), consumableProduct);
        } else {
            hideProgressBar();
        }
    }

    private static void showProgressDialog(Context context) {
        mProgressDialog = ProgressDialog.show(
                context,
                context.getString(R.string.general_dialog_loading),
                context.getString(R.string.general_dialog_loading),
                false,
                true
        );
    }

    private static void hideProgressBar() {
        if (mProgressDialog != null) mProgressDialog.dismiss();
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
                    ValidateGetJarRequest request = new ValidateGetJarRequest(
                            mGetJarContext.getAndroidContext(),
                            response.getSignedPayload(),
                            response.getSignature(),
                            response.getTransactionId());
                    request.callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            JSONObject result = response.getJsonResult();
                            if (result != null) {
                                boolean valid = result.optBoolean("valid", false);
                                if (valid) {
                                    String msg = String.format(getContext().getString(R.string.youve_earned),
                                            CacheProfile.getOptions().getJar.getName());
                                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                                } else {
                                    showToast(R.string.general_server_error);
                                }
                            } else {
                                showToast(R.string.general_server_error);
                            }
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            showToast(R.string.general_server_error);
                        }
                    }).exec();
                }
            }
        }
    }

    public static class ConsumableProductHelper {
        private ArrayList<Pricing> mConsumablePricingList = new ArrayList<>(1);
        private ConsumableProduct mConsumableProduct;
        private GetJarContext mGetJarContext;

        ConsumableProductHelper(GetJarContext getJarContext) {
            this.mGetJarContext = getJarContext;
        }

        void buy(String pickAccountTitle, ConsumableProduct consumableProduct) {
            if (consumableProduct == null) {
                hideProgressBar();
                throw new IllegalArgumentException("consumableProduct cannot be null");
            }
            this.mConsumableProduct = consumableProduct;

            // Ensure user is authenticated
            UserAuth userAuth = new UserAuth(mGetJarContext);
            userAuth.ensureUserAsync(pickAccountTitle,
                    consumableUserAuthListener);
        }

        private void startGetJarRewardPage(ConsumableProduct product) {
            GetJarPage consumablePage = new GetJarPage(mGetJarContext);
            consumablePage.setProduct(product);
            consumablePage.showPage();
            hideProgressBar();
        }

        private EnsureUserAuthListener consumableUserAuthListener = new EnsureUserAuthListener() {

            @Override
            public void userAuthCompleted(User user) {
                if (user != null) {
                    Debug.log("consumableUserAuthListener^ success");
                    Localization localization = new Localization(mGetJarContext);
                    if (mConsumablePricingList.isEmpty()) {
                        mConsumablePricingList.add(new Pricing((int) mConsumableProduct.getAmount(), GETJAT_MAX_DISCOUNT, GETJAT_MAX_MARKUP));
                    }
                    localization.getRecommendedPricesAsync(mConsumablePricingList, consumableRecommendedPricesListener);
                } else {
                    Debug.log("consumableUserAuthListener: failed");
                    hideProgressBar();
                }
            }
        };

        private RecommendedPricesListener consumableRecommendedPricesListener = new RecommendedPricesListener() {

            @Override
            public void recommendedPricesEvent(RecommendedPrices prices) {
                Debug.log("consumableRecommendedPricesListener: prices:" + (prices.getRecommendedPrice(mConsumablePricingList.get(0))));
                mConsumableProduct = new ConsumableProduct(mConsumableProduct.getProductId(), mConsumableProduct.getProductName(),
                        mConsumableProduct.getProductDescription(), prices.getRecommendedPrice(mConsumablePricingList.get(0)));
                startGetJarRewardPage(mConsumableProduct);
            }
        };
    }

    private static void startSupersonic(Activity activity) {
        activity.startActivity(new Intent(activity, SupersonicWallActivity.class));
    }
}
