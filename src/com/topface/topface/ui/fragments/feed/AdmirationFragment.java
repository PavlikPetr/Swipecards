package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.R;
import com.topface.topface.requests.FeedRequest;

public class AdmirationFragment extends LikesFragment{

    @Override
    protected int getTitle() {
        return R.string.general_admirations;
    }

    @Override
    protected Drawable getBackIcon() {
        return getActivity().getResources().getDrawable(R.drawable.background_admiration);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.ADMIRATIONS;
    }
}
