package com.topface.topface.ui.adapters;

import android.content.Context;

public class PeopleCloseAdapter extends FeedAdapter{
    public PeopleCloseAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected int getItemLayout() {
        return 0;
    }

    @Override
    protected int getNewItemLayout() {
        return 0;
    }

    @Override
    protected int getVipItemLayout() {
        return 0;
    }

    @Override
    protected int getNewVipItemLayout() {
        return 0;
    }

    @Override
    public ILoaderRetrierCreator getLoaderRetrierCreator() {
        return null;
    }
}
