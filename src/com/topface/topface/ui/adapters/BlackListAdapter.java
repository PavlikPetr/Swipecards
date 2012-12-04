package com.topface.topface.ui.adapters;

import android.content.Context;
import com.topface.topface.data.BlackListItem;

/**
 * Created with IntelliJ IDEA.
 * User: gildor
 * Date: 04.12.12
 * Time: 19:53
 * To change this template use File | Settings | File Templates.
 */
public class BlackListAdapter extends FeedAdapter<BlackListItem> {
    public BlackListAdapter(Context context, FeedList<BlackListItem> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    @Override
    protected int getItemLayout() {
        return 0;
    }

    @Override
    protected int getNewItemLayout() {
        return 0;
    }
}
