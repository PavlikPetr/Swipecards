package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.R;
import com.topface.topface.data.FeedMutual;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.MutualListAdapter;

public class MutualFragment extends FeedFragment<FeedMutual> {
    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.mutual_back_icon);
    }

    @Override
    protected int getTitle() {
        return R.string.mutual_header_title;
    }

    @Override
    protected MutualListAdapter getAdapter() {
        return new MutualListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.MUTUAL;
    }


    @Override
    protected int getEmptyFeedText() {
        return R.string.mutual_background_text;
    }
}
