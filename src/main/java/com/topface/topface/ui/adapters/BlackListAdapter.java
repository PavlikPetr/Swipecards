package com.topface.topface.ui.adapters;

import android.content.Context;

import com.topface.topface.data.BlackListItem;

public class BlackListAdapter extends FeedAdapter<BlackListItem> {

    public static final int LIMIT = 100;

    public BlackListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    public ILoaderRetrierCreator<BlackListItem> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<BlackListItem>() {
            @Override
            public BlackListItem getLoader() {
                BlackListItem result = new BlackListItem();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public BlackListItem getRetrier() {
                BlackListItem result = new BlackListItem();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }

    @Override
    public int getLimit() {
        return LIMIT;
    }

}
