package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Filter extends AbstractData {
    // Data
    public boolean completed; // всегда TRUE
    //---------------------------------------------------------------------------
    public static Filter parse(ApiResponse response) {
        Filter filter = new Filter();

        try {
            filter.completed = response.mJSONResult.optBoolean("completed");
        } catch(Exception e) {
            Debug.log("Filter.class", "Wrong response parsing: " + e);
        }

        return filter;
    }
    //---------------------------------------------------------------------------
}
