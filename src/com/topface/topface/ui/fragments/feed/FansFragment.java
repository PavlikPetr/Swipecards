package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.R;
import com.topface.topface.requests.FeedRequest;


public class FansFragment extends BookmarksFragment{
    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.fans);
    }

    @Override
    protected int getTitle() {
        return R.string.general_fans;
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.FANS;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.general_no_fans;
    }
}
