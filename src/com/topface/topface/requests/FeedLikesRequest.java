package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedLikesRequest extends ApiRequest {
    // Data
    private String service = "feedLike";
    public int limit;   // максимальный размер выборки
    public int from;    // идентификатор лайка, от которого делать выборку
    public boolean only_new;  // осуществлять выборку только по новым лайкам, или по всем

    public FeedLikesRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            JSONObject data = new JSONObject().put("limit", limit);
            if (from > 0)
                data.put("from", from);
            if (only_new)
                data.put("new", only_new);
            root.put("data", data);
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}
