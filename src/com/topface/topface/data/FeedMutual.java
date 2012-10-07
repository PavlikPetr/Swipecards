package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

public class FeedMutual extends AbstractFeedItem {
    public FeedMutual() {
        super();
    }

    public FeedMutual(IListLoader.ItemType type) {
        super(type);
    }

    public static FeedList<FeedMutual> parse(ApiResponse response) {
        FeedList<FeedMutual> symphatyList = new FeedList<FeedMutual>();

        try {
            FeedMutual.unread_count = response.mJSONResult.optInt("unread");
            FeedMutual.more = response.mJSONResult.optBoolean("more");

            JSONArray arr = response.mJSONResult.getJSONArray("feed");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);

                    FeedMutual symphaty = new FeedMutual();
                    symphaty.type = item.optInt("type");
                    symphaty.id = item.optInt("id");
                    symphaty.uid = item.optInt("uid");
                    symphaty.created = item.optLong("created") * 1000;
                    symphaty.target = item.optInt("target");
                    symphaty.unread = item.optBoolean("unread");
                    symphaty.first_name = item.optString("first_name");
                    symphaty.age = item.optInt("age");
                    symphaty.online = item.optBoolean("online");

                    // city  
                    JSONObject city = item.getJSONObject("city");
                    symphaty.city_id = city.optInt("id");
                    symphaty.city_name = city.optString("name");
                    symphaty.city_full = city.optString("full");

                    initPhotos(item, symphaty);

                    symphatyList.add(symphaty);
                }
        } catch (Exception e) {
            Debug.log("FeedSymphaty.class", "Wrong response parsing: " + e);
        }

        return symphatyList;
    }
}
