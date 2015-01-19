package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;

import org.json.JSONObject;

/**
 * Created by kirussell on 19/01/15.
 * Response on PhotoAddRequest
 */
public class AddedPhoto extends AbstractData {

    String hash;
    Photo photo;

    public AddedPhoto(ApiResponse data) {
        if (data != null) {
            fillData(data.getJsonResult());
        }
    }

    protected void fillData(JSONObject data) {
        if (data != null) {
            try {
                hash = data.optString("hash");
                photo = new Photo(data.optJSONObject("photo"));
            } catch (Exception e) {
                Debug.error("Verify: Wrong response parsing", e);
            }
        }
    }

}
