package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.Static;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.utils.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class FilterRequest extends ApiRequest {
    private static final String SERVICE_NAME = "search.setFilter";

    protected DatingFilter filter;

    public FilterRequest(DatingFilter filter, Context context) {
        super(context);
        this.filter = filter;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject()
                .put("beautiful", filter.beautiful)
                .put("sex", filter.sex)
                .put("city", filter.city.id)
                .put("ageStart", filter.ageStart)
                .put("ageEnd", filter.ageEnd)
                .put("xstatus", filter.xstatus)
                .put("marriage", filter.marriage)
                .put("character", filter.character)
                .put("alcohol", filter.alcohol);

        if (filter.sex == Static.GIRL) {
            data.put("breast", filter.breast);
            data.put("finances", filter.finances);
        } else {
            data.put("finances", filter.finances);
        }

        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.sendEvent("Dating", "ChangeFilter", "", 1L);
    }
}
