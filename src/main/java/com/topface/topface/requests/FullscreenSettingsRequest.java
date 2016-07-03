package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Запрос на получение опций фулскрина
 * Created by tiberal on 17.06.16.
 */
public class FullscreenSettingsRequest extends ApiRequest {

    private long mStartNumber;

    /**
     * {String} startNumber - порядковый номер запуска приложения за календарные сутки, нумерация с 1
     */

    public FullscreenSettingsRequest(Context context, long startNumber) {
        super(context);
        mStartNumber = startNumber;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("startNumber", mStartNumber);
        return object;
    }

    @Override
    public String getServiceName() {
        return "banner.getFullScreen";
    }
}
