package com.topface.topface.data;

import com.topface.topface.requests.IApiResponse;
import org.json.JSONObject;

public class ModerationResponse extends AbstractData {
    public Boolean completed;

    public ModerationResponse(IApiResponse response) {
        fillData(response.getJsonResult());
    }

    protected void fillData (JSONObject data) {
        if (data != null) {
            this.completed = data.optBoolean("completed");
        }
    }
}
