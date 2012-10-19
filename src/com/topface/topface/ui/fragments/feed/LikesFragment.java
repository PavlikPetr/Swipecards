package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.LikesListAdapter;

public class LikesFragment extends FeedFragment<FeedLike> {

    @Override
    protected int getTitle() {
        return R.string.dashbrd_btn_likes;
    }

    @Override
    protected LikesListAdapter getAdapter() {
        return new LikesListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.likes_background_text;
    }

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.likes_back_icon);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.LIKES;
    }

    @Override
    protected FeedList<FeedLike> parseResponse(ApiResponse response) {
        return FeedLike.parse(response);
    }

}
