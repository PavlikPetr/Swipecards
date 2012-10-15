package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;

public class Visitors extends AbstractData {

    public static FeedList<Visitor> parse(ApiResponse response) {
        FeedList<Visitor> visitors = null;
        try {
            visitors = new FeedList<Visitor>();
            JSONArray array = response.mJSONResult.getJSONArray("visitors");
            for (int i = 0; i < array.length(); i++) {
                visitors.add(
                        Visitor.parse(array.getJSONObject(i))
                );
            }
        } catch (JSONException e) {
            Debug.error(e);
        }
        return visitors;
    }

}
