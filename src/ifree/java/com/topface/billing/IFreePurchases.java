package com.topface.billing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ifree.monetize.core.LibraryInitListener;
import com.ifree.monetize.core.Monetization;
import com.ifree.monetize.core.PaymentState;
import com.ifree.monetize.core.PurchaseListener;
import com.ifree.monetize.core.PurchaseResponse;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by ppetr on 21.07.15.
 * Base fragment i-free purchases
 */
public class IFreePurchases extends BaseFragment implements LibraryInitListener {

    private static final String CHARSET_NAME = "utf-8";

    protected Monetization mMonetization;
    private PurchaseListener mPurchaseListener;
    private boolean mIsLibraryInitialised = false;

    @Override
    public void onLibraryInitStarted() {
        Debug.log("IFreePurchases onLibraryInitStarted");
    }

    @Override
    public void onLibraryInitialised() {
        Debug.log("IFreePurchases onLibraryInitialised");
        mIsLibraryInitialised = true;
    }

    @Override
    public void onLibraryReleased() {
        Debug.log("IFreePurchases onLibraryReleased");
    }

    private PurchaseListener purchaseListener = new PurchaseListener() {

        @Override
        public void onPurchaseEventReceive(PurchaseResponse purchaseResponse) {
            if (mPurchaseListener != null) {
                mPurchaseListener.onPurchaseEventReceive(purchaseResponse);
            }
            Debug.log("IFreePurchases response: " + purchaseResponse);
            if (purchaseResponse != null) {
                int toastId;
                if (purchaseResponse.getCode() == PaymentState.PURCHASE_CONFIRMED) {
                    try {
                        CountersManager countersManager = CountersManager.getInstance(App.getContext());
                        countersManager.setLastRequestMethod(purchaseResponse.getPaymentMethod().toString());
                        countersManager.setBalanceCounters(new JSONObject(URLDecoder
                                .decode(purchaseResponse.getAnswerFromApplicationServer(), CHARSET_NAME)));
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    toastId = R.string.buying_store_ok;
                } else {
                    toastId = R.string.buying_store_fail;
                }
                Toast.makeText(App.getContext(), toastId, Toast.LENGTH_SHORT).show();
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
        String price;
        for (Iterator<BuyButtonData> product = products.iterator(); product.hasNext(); ) {
            BuyButtonData entry = product.next();
            price = null;
            try {
                price = mMonetization.getPriceTariffGroup(entry.id);
            } catch (Exception ignored) {
                Debug.error("IFreePurchases Exception = " + ignored);
            }
            Debug.error("IFreePurchases price = " + price + " id = " + entry.id);
            if (price != null) {
                entry.setTitleByPrice(price);
            } else {
                product.remove();
            }
        }
        return products;
    }

    public void buyProduct(String name, String place) {
        Debug.log("IFreePurchases product: " + name + " metaData: " + getMetaInfo(place));
        mMonetization.monetizeMethod(name, "default", "", getMetaInfo(place));
    }

    private String getMetaInfo(String place) {
        JSONObject metaData = new JSONObject();
        try {
            metaData.put("uid", CacheProfile.getProfile().uid);
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

    public boolean isLibraryInitialised() {
        return mIsLibraryInitialised;
    }
}