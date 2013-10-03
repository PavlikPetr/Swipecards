package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

import org.json.JSONObject;

public class SkipRate extends AbstractData {
    // Data
    public int money; // мгновенное количество оставшихся монет текущего пользователя
    public int likes; // мгновенное значение симпатий текущего пользователя
    public boolean completed; // результат выполнения операции

    public SkipRate(JSONObject data) {
        super(data);
    }

    public static SkipRate parse(ApiResponse response) {
        SkipRate skip = new SkipRate(response.getJsonResult());
        return skip;
    }

    @Override
    protected void fillData(JSONObject data) {
        super.fillData(data);
        try {
            money = data.optInt("money");
            likes = data.optInt("likes");
            completed = data.optBoolean("completed");
        } catch (Exception e) {
            Debug.error("SkipRate.class: Wrong response parsing", e);
        }
    }
}
