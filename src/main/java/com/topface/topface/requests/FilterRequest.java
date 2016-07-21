package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.data.Profile;
import com.topface.topface.ui.edit.filter.model.FilterData;
import com.topface.topface.utils.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class FilterRequest extends ApiRequest {
    private static final String SERVICE_NAME = "search.setFilter";

    protected FilterData filterData;

    public FilterRequest(FilterData filter, Context context) {
        super(context);
        filterData = filter;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject()
                .put("beautiful", filterData.isPrettyOnly)
                .put("sex", filterData.sex)
                .put("city", filterData.city.id)
                .put("ageStart", filterData.ageStart)
                .put("ageEnd", filterData.ageEnd)
                .put("xstatus", 0)
                .put("marriage", 0)
                .put("character", 0)
                .put("alcohol", 0);
        if (filterData.sex == Profile.GIRL) {
            data.put("breast", 0);
        }
        data.put("finances", 0);

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
