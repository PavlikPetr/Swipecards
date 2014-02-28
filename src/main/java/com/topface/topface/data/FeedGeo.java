package com.topface.topface.data;

import org.json.JSONObject;

public class FeedGeo  extends FeedLike {

    public double distance;

    public FeedGeo(JSONObject data) {
        super(data);
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        distance = item.optDouble("distance");
    }
}
