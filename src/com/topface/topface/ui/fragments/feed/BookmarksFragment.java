package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.FeedAdapter;
import org.json.JSONObject;

public class BookmarksFragment extends FeedFragment<FeedItem> {
    @Override
    protected Drawable getBackIcon() {
        return null;
    }

    @Override
    protected int getTitle() {
        return 0;
    }

    @Override
    protected int getTypeForGCM() {
        return 0;
    }

    @Override
    protected int getTypeForCounters() {
        return 0;
    }

    @Override
    protected FeedAdapter<FeedItem> getNewAdapter() {
        return null;
    }

    @Override
    protected FeedListData<FeedItem> getFeedList(JSONObject response) {
        return null;
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return null;
    }

    @Override
    protected int getEmptyFeedText() {
        return 0;
    }
}
