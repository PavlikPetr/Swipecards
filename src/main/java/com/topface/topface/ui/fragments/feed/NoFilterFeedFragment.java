package com.topface.topface.ui.fragments.feed;

import com.topface.topface.R;
import com.topface.topface.data.FeedItem;

public abstract class NoFilterFeedFragment<T extends FeedItem> extends FeedFragment<T> {

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.feed_context_empty;
    }
}
