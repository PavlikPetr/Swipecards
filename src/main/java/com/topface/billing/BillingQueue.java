package com.topface.billing;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.utils.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Класс хранения очередей запросов на покупку,
 * помогающий уменьшить колличество неудавшихся платежей и негодующих пользователей
 */
abstract public class BillingQueue {
    public static final String ITEM_ID_KEY = "id";
    private SharedPreferences mPreferences;

    protected String getQueueName() {
        return BuildConfig.BILLING_TYPE.getClientType();
    }

    protected synchronized String addPurchaseToQueue(JSONObject data) {
        JSONArray queue = getQueue();
        String id = UUID.randomUUID().toString();

        //Добавляем id в данные
        try {
            data.put(ITEM_ID_KEY, id);
            queue.put(data);
            getPreferences()
                    .edit()
                    .putString(getQueueKey(), queue.toString())
                    .commit();
        } catch (JSONException e) {
            Debug.error(e);
        }

        return id;
    }

    protected synchronized JSONArray getQueue() {
        SharedPreferences pref = getPreferences();
        JSONArray queue = null;
        if (pref.contains(getQueueKey())) {
            try {
                queue = new JSONArray(pref.getString(getQueueKey(), "[]"));
            } catch (JSONException e) {
                Debug.error(e);
            }
        }

        queue = queue == null ? new JSONArray() : queue;

        return queue;
    }

    private String getQueueKey() {
        return "PurchaseQueue" + getQueueName();
    }

    protected synchronized JSONObject getQueueItem() {
        JSONArray queue = getQueue();
        JSONObject item = null;
        try {
            if (queue != null && queue.length() > 0) {
                item = queue.getJSONObject(0);
            }
        } catch (JSONException e) {
            Debug.error(e);
            item = null;
        }

        return item;
    }

    public synchronized boolean deleteQueueItem(String itemId) {
        boolean result = false;
        if (!TextUtils.isEmpty(itemId)) {
            JSONArray queue = getQueue();
            for (int i = 0; i < queue.length(); i++) {
                try {
                    JSONObject item = queue.getJSONObject(i);
                    if (itemId.equals(item.optString(ITEM_ID_KEY, ""))) {
                        queue = removeItemFromJSONArray(i, queue);
                        getPreferences()
                                .edit()
                                .putString(getQueueKey(), queue.toString())
                                .commit();
                        result = true;
                        break;
                    }
                } catch (JSONException e) {
                    Debug.error(e);
                }
            }

        }
        return result;
    }

    protected SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        }

        return mPreferences;
    }

    protected static JSONArray removeItemFromJSONArray(final int idx, final JSONArray from) {
        final List<JSONObject> objs = getJSONArrayAsList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    protected static List<JSONObject> getJSONArrayAsList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    abstract public void sendQueueItems();

}
