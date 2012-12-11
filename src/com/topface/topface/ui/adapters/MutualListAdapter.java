package com.topface.topface.ui.adapters;

import android.content.Context;
import com.topface.topface.R;
import com.topface.topface.data.FeedMutual;

public class MutualListAdapter extends FeedAdapter<FeedMutual> {

    public MutualListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected int getItemLayout() {
        return R.layout.item_feed_like;
    }

    @Override
    protected int getNewItemLayout() {
        return R.layout.item_feed_like;
    }
}
