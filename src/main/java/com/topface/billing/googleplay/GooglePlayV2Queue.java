package com.topface.billing.googleplay;

import android.content.Context;
import com.topface.billing.BillingQueue;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Очередь запросов на покупку в Google Play
 */
public class GooglePlayV2Queue extends BillingQueue {
    public static final String SIGNATURE_KEY = "signature";
    public static final String DATA_KEY = "data";
    private static Context mContext;
    private static GooglePlayV2Queue mInstance;

    public static GooglePlayV2Queue getInstance(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new GooglePlayV2Queue();
        }

        return mInstance;
    }

    @Override
    protected String getQueueName() {
        return mContext.getString(R.string.build_google_play_v2);
    }

    /**
     * Отправляет запросы из очереди на проверку на сервер, если в ней что-то есть
     */
    @Override
    public void sendQueueItems() {
        final QueueItem item = getQueueItemObject();
        if (item != null) {
            ResponseHandler.verifyPurchase(mContext, item.data, item.signature, item.id);
        }

    }

    public synchronized String addPurchaseToQueue(String data, String signature) {
        String id = "";
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put(DATA_KEY, data);
            dataJson.put(SIGNATURE_KEY, signature);
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
        }
        return (item != null && item.id != null) ? item : null;
    }


    public static class QueueItem {
        public String id;
        public String signature;
        public String data;
    }
}
