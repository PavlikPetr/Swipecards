package com.topface.billing.googleplay;

import android.content.Context;

import com.topface.billing.BillingQueue;
import com.topface.framework.utils.Debug;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Очередь запросов на покупку в Google Play
 */
public class GooglePlayV2Queue extends BillingQueue {
    public static final String SIGNATURE_KEY = "signature";
    public static final String DATA_KEY = "data";
    private static final java.lang.String PRODUCT_ID = "product";
    private static final String TEST_PRODUCT_ID = "test_product";
    private static Context mContext;
    private static GooglePlayV2Queue mInstance;

    public static GooglePlayV2Queue getInstance(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new GooglePlayV2Queue();
        }

        return mInstance;
    }

    /**
     * Отправляет запросы из очереди на проверку на сервер, если в ней что-то есть
     */
    @Override
    public void sendQueueItems() {
        final QueueItem item = getQueueItemObject();
        if (item != null) {
            ResponseHandler.verifyPurchase(mContext, item.data, item.signature, item.id,
                    item.productId, item.testProductId);
        }

    }

    public synchronized String addPurchaseToQueue(String data, String signature, String productId, String testProductId) {
        String id = "";
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put(DATA_KEY, data);
            dataJson.put(SIGNATURE_KEY, signature);
            dataJson.put(PRODUCT_ID, productId);
            dataJson.put(TEST_PRODUCT_ID, testProductId);
            id = super.addPurchaseToQueue(dataJson);
        } catch (JSONException e) {
            Debug.error(e);
        }

        return id;
    }

    public synchronized QueueItem getQueueItemObject() {
        JSONObject object = super.getQueueItem();
        QueueItem item = null;
        if (object != null) {
            item = new QueueItem();
            item.id = object.optString(ITEM_ID_KEY);
            item.data = object.optString(DATA_KEY);
            item.signature = object.optString(SIGNATURE_KEY);
            item.productId = object.optString(PRODUCT_ID);
            item.testProductId = object.optString(TEST_PRODUCT_ID);
        }
        return (item != null && item.id != null) ? item : null;
    }


    public static class QueueItem {
        public String id;
        public String signature;
        public String data;
        public String productId;
        public String testProductId;
    }
}
