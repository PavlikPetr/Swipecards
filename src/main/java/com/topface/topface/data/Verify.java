package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

import org.json.JSONObject;

public class Verify extends AbstractData {
    /**
     * Флаг, является ли пользователь после покупки випом
     */
    public boolean premium;
    /**
     * выручка в долларах от данной покупки
     */
    public double revenue;

    public Verify(ApiResponse data) {
        if (data != null) {
            fillData(data.getJsonResult());
        }
    }

    protected void fillData(JSONObject data) {
        if (data != null) {
            try {
                premium = data.optBoolean("premium");
                revenue = data.optDouble("revenue");
            } catch (Exception e) {
                Debug.error("Verify: Wrong response parsing", e);
            }
        }
    }
}
