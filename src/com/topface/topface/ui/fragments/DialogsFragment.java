package com.topface.topface.ui.fragments;

import android.graphics.drawable.Drawable;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.CacheProfile;

public class DialogsFragment extends FeedFragment<FeedDialog> {
    @Override
    protected boolean isHasUnread() {
        return CacheProfile.unread_messages > 0;
    }

    @Override
    protected int getTitle() {
        return R.string.dashbrd_btn_chat;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.inbox_background_text;
    }

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.dialogs_back_icon);
    }

    @Override
    protected DialogListAdapter getAdapter() {
        return new DialogListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.DIALOGS;
    }

    @Override
    protected FeedList<FeedDialog> parseResponse(ApiResponse response) {
        return FeedDialog.parse(response);
    }

}
