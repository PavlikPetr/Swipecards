package com.topface.billing.amazon;

import android.content.Context;

import com.topface.billing.BillingQueue;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Очередь запросов на покупку через Amazon
 */
public class AmazonQueue extends BillingQueue {

    public static final String USER_ID_KEY = "user_id";
    public static final String PURCHASE_TOKEN_KEY = "purchase_token";
    private static AmazonQueue mInstance;
    private static Context mContext;
    private static final String REQUEST_ID_KEY = "request_id";
    private static final String ITEM_SKU_KEY = "sku";

    public static AmazonQueue getInstance(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new AmazonQueue();
        }

        return mInstance;
    }

    @Override
    protected String getQueueName() {
        return mContext.getString(R.string.build_amazon);
    }

    @Override
    public void sendQueueItems() {
        final QueueItem item = getQueueItemObject();
        if (item != null) {
            AmazonPurchaseObserver.validateRequest(
                    null,
                    item.id,
                    item.sku,
                    item.userId,
                    item.purchaseToken,
                    item.requestId,
                    mContext
            );
        }

    }

    public synchronized String addPurchaseToQueue(String sku, String userId, String purchaseToken, String requestId) {
        String id = "";
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put(ITEM_SKU_KEY, sku);
            dataJson.put(USER_ID_KEY, userId);
            dataJson.put(PURCHASE_TOKEN_KEY, purchaseToken);
            dataJson.put(REQUEST_ID_KEY, requestId);
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
            item.sku = object.optString(ITEM_SKU_KEY);
            item.userId = object.optString(USER_ID_KEY);
            item.requestId = object.optString(REQUEST_ID_KEY);
            item.purchaseToken = object.optString(PURCHASE_TOKEN_KEY);
        }
        return (item != null && item.id != null) ? item : null;
    }


    public static class QueueItem {
        public String id;
        public String sku;
        public String userId;
        public String purchaseToken;
        public String requestId;
    }
}
