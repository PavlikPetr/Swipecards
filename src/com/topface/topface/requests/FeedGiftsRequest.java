package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class FeedGiftsRequest extends ApiRequest {
    // Data
    private String service = "feedGifts";
    public int limit; // максимальный размер выбираемых подарков
    public int from; // идентификатор подарка, от которого делать выборку    
    public int uid; // идентификатор пользователя для выборки подарков.

    public FeedGiftsRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            JSONObject data = new JSONObject().put("limit", limit).put("uid", uid);
            if (from > 0)
                data.put("from", from);            
            root.put("data", data);
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
