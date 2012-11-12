package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.DialogListAdapter;
import org.json.JSONObject;

public class DialogsFragment extends FeedFragment<FeedDialog> {

    public DialogsFragment() {
        super();
        setIsDeletable(false);
    }

    @Override
    protected int getTitle() {
        return R.string.general_chat;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.chat_background_text;
    }

    @Override
    protected void makeAllItemsRead() {

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
    protected FeedListData<FeedDialog> getFeedList(JSONObject data) {
        return new FeedListData<FeedDialog>(data, FeedDialog.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.DIALOGS;
    }

}
