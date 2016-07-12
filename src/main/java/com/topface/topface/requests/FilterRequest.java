package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.edit.filter.model.FilterData;
import com.topface.topface.utils.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class FilterRequest extends ApiRequest {
    private static final String SERVICE_NAME = "search.setFilter";

    protected DatingFilter filter;
    protected FilterData filterData;

    public FilterRequest(DatingFilter filter, Context context) {
        super(context);
        this.filter = filter;
        filterData = null;
    }

    public FilterRequest(FilterData filter, Context context) {
        super(context);
        this.filter = null;
        filterData = filter;
    }

    private JSONObject getOldJsonData() throws JSONException {
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
        if (filter.maxWeight != 0 && filter.minWeight != 0) {
            data.put("maxWeight", filter.maxWeight)
                    .put("minWeight", filter.minWeight);
        }
        if (filter.maxHeight != 0 && filter.minHeight != 0) {
            data.put("maxHeight", filter.maxHeight)
                    .put("minHeight", filter.minHeight);
        }
        if (filter.sex == Profile.GIRL) {
            data.put("breast", filter.breast);
            data.put("finances", filter.finances);
        } else {
            data.put("finances", filter.finances);
        }

        return data;
    }

    private JSONObject getJsonData() throws JSONException {
        JSONObject data = new JSONObject()
                .put("beautiful", filterData.isPreetyOnly)
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
    protected JSONObject getRequestData() throws JSONException {
        return filter != null ? getOldJsonData() : getJsonData();
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
