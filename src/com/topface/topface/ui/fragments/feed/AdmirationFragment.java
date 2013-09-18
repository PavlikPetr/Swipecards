package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.requests.FeedRequest;

public class AdmirationFragment extends LikesFragment{

    @Override
    protected int getTitle() {
        return R.string.general_admirations;
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.ADMIRATIONS;
    }
}
