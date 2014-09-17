package com.topface.topface.data;

import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.CacheProfile;

import org.json.JSONObject;

public class FortumoProducts extends Products {

    public FortumoProducts(IApiResponse data) {
        super(data);
    }

    @Override
    protected void updateCache(JSONObject data) {
        //Обновляем кэш
        if (data != null) {
            CacheProfile.setFortumoProducts(this, data.toString());

        }
    }

    public FortumoProducts(JSONObject jsonObject) {
        super(jsonObject);
    }
}
