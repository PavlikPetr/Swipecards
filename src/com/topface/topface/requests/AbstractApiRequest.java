package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Абстрактная версия ApiRequest для упрощенной релизации запросов к API
 *
 * достаточно определить getServiceName, который возвращает название сервиса
 * и вернуть данные через getRequestData (можно вернуть null, тогда поле data на сервер отправлять не нужно)
 */
public abstract class AbstractApiRequest extends ApiRequest {

    public AbstractApiRequest(Context context) {
        super(context);
    }

    protected JSONObject getRequest() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", getServiceName());
            root.put("ssid", ssid);
            JSONObject data = getRequestData();
            if (data != null) {
                root.put("data", data);
            }
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root;
    }

    protected abstract JSONObject getRequestData() throws JSONException;

    abstract protected String getServiceName();

    @Override
    public String toString() {
        return getRequest().toString();
    }
}
