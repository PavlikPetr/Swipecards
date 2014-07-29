package com.topface.topface.data;

import android.text.TextUtils;
import android.util.SparseArray;

import com.google.android.gms.analytics.HitBuilders;

import org.json.JSONObject;

/**
 * Класс для записи ключей экспериментов в статистику GA
 */
public class ExperimentTags {
    private SparseArray<String> experiments;

    /**
     * @param object JSON объект experimentTags из Options
     */
    public ExperimentTags(JSONObject object) {
        experiments = new SparseArray<>();
        addTag(7, object.optString("experiment1"));
        addTag(8, object.optString("experiment2"));
        addTag(9, object.optString("experiment3"));
        addTag(10, object.optString("experiment4"));
        addTag(11, object.optString("experiment5"));
        addTag(12, object.optString("experiment6"));
    }

    private void addTag(int key, String tag) {
        if (!TextUtils.isEmpty(tag)) {
            experiments.append(key, tag);
        }
    }

    /**
     * Устанавливает ключи экспериментов в статистику GA для разделению юзеров по экспериментам
     */
    public void setToStatistics(HitBuilders.AppViewBuilder builder) {
        for (int i = 0; i < experiments.size(); i++) {
            int key = experiments.keyAt(i);
            builder.setCustomDimension(key, experiments.get(key));
        }
    }
}
