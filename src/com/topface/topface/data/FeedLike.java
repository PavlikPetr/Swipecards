package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

public class FeedLike extends AbstractFeedItem {
    public boolean highrate;
    public boolean mutualed;

    public FeedLike() {
        super();
    }

    public FeedLike(IListLoader.ItemType type) {
        super(type);
    }

    public static FeedList<FeedLike> parse(ApiResponse response) {
        FeedList<FeedLike> likesList = new FeedList<FeedLike>();

        try {
            FeedLike.unread_count = response.mJSONResult.getInt("unread");
            FeedLike.more = response.mJSONResult.optBoolean("more");

            JSONArray arr = response.mJSONResult.getJSONArray("feed");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);

                    FeedLike like = new FeedLike();
                    like.type = item.optInt("type");
                    like.id = item.optInt("id");
                    like.uid = item.optInt("uid");
                    like.created = item.optLong("created") * 1000;
                    like.target = item.optInt("target");
                    like.unread = item.optBoolean("unread");
                    like.first_name = item.optString("first_name");
                    like.age = item.optInt("age");
                    like.online = item.optBoolean("online");
                    like.highrate = item.optBoolean("highrate");
                    like.mutualed = false;

                    // city  
                    JSONObject city = item.getJSONObject("city");
                    like.city_id = city.optInt("id");
                    like.city_name = city.optString("name");
                    like.city_full = city.optString("full");

                    initPhotos(item, like);

                    likesList.add(like);
                }
        } catch (Exception e) {
            Debug.log("FeedLike.class", "Wrong response parsing: " + e);
        }

        return likesList;
    }

}
