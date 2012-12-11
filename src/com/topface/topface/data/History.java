package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import org.json.JSONObject;


public class History extends FeedDialog {

    public History(JSONObject data) {
        super(data);
    }

    public History(ApiResponse response) {
        super(response.jsonResult.optJSONObject("item"));
    }

    public History() {
        super(null);
    }
}
