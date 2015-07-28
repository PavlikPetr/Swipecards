package com.topface.billing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ifree.monetize.core.LibraryInitListener;
import com.ifree.monetize.core.Monetization;
import com.ifree.monetize.core.PaymentState;
import com.ifree.monetize.core.PurchaseListener;
import com.ifree.monetize.core.PurchaseResponse;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;

/**
 * Created by ppetr on 21.07.15.
 * Base fragment i-free purchases
 */
public class IFreePurchases extends BaseFragment implements LibraryInitListener {

    private static final String CHARSET_NAME = "utf-8";

    protected Monetization mMonetization;
    private PurchaseListener mPurchaseListener;

    @Override
    public void onLibraryInitStarted() {
        Debug.error("IFreePurchases onLibraryInitStarted");
    }

    @Override
    public void onLibraryInitialised() {
        Debug.error("IFreePurchases onLibraryInitialised");
    }

    @Override
    public void onLibraryReleased() {
        Debug.error("IFreePurchases onLibraryReleased");
    }

    private PurchaseListener purchaseListener = new PurchaseListener() {

        @Override
        public void onPurchaseEventReceive(PurchaseResponse purchaseResponse) {
            if (mPurchaseListener != null) {
                mPurchaseListener.onPurchaseEventReceive(purchaseResponse);
            }
            Debug.log("IFreePurchases response: " + purchaseResponse);
            Debug.log("IFreePurchases response.toString: " + purchaseResponse.toString());
            Debug.log("IFreePurchases answerFromServer: " + purchaseResponse.getAnswerFromApplicationServer());
            Debug.log("IFreePurchases metaInfo: " + purchaseResponse.getMetaInfo());
            Debug.log("IFreePurchases tarifGroup: " + purchaseResponse.getTariffGroupName());
            Debug.log("IFreePurchases transactionId: " + purchaseResponse.getTransactionId());
            Debug.log("IFreePurchases code: " + purchaseResponse.getCode());
            Debug.log("IFreePurchases details: " + purchaseResponse.getDetails());
            Debug.log("IFreePurchases paymentMethod: " + purchaseResponse.getPaymentMethod());

            try {
                CountersManager countersManager = CountersManager
                        .getInstance(App.getContext())
                        .setMethod(purchaseResponse.getCode().toString());
                countersManager.setBalanceCounters(new JSONObject(URLDecoder.decode(purchaseResponse.getAnswerFromApplicationServer(), CHARSET_NAME)));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (purchaseResponse.getCode() == PaymentState.PURCHASE_CONFIRMED) {
                // handle confirmed status (successful response from server)
            } else if (purchaseResponse.getCode() == PaymentState.PURCHASE_UNCONFIRMED) {
                // handle unconfirmed status (server not answered)
            } else if (purchaseResponse.getCode() == PaymentState.MONEY_CHARGED) {
                // handle money charged status (sms was received)
            } else if (purchaseResponse.getCode() == PaymentState.CANCELLED) {
                // handle canceled status (no money or user canceled action)
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMonetization = new Monetization(getActivity(), purchaseListener, this);
        mMonetization.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMonetization.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            PurchaseResponse response = (PurchaseResponse) data.getSerializableExtra(PurchaseResponse.EXTRA_PURHCASE_INFO);
            purchaseListener.onPurchaseEventReceive(response);
        }
    }

    public LinkedList<BuyButtonData> validateProducts(LinkedList<BuyButtonData> products) {
        String price = null;
        for (BuyButtonData product : products) {
            price = null;
            try {
                price = mMonetization.getPriceTariffGroup(product.id);
            } catch (Exception ignored) {
            }
            if (price != null) {
                product.setTitleByPrice(price);
            } else {
                products.remove(product);
            }
        }
        return products;
    }

    public void buyProduct(String name, String place) {
        Debug.error("IFreePurchases product: " + name + " metaData: " + getMetaInfo(place));
        mMonetization.monetizeMethod(name, "default", "", getMetaInfo(place));
    }

    private String getMetaInfo(String place) {
        JSONObject metaData = new JSONObject();
        try {
            metaData.put("uid", CacheProfile.uid);

            metaData.put("place", place);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String result = "";
        try {
            result = URLEncoder.encode(metaData.toString(), CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected Monetization getMonetization() {
        return mMonetization;
    }

    public void setPurchaseListener(PurchaseListener purchaseListener) {
        mPurchaseListener = purchaseListener;
    }
}