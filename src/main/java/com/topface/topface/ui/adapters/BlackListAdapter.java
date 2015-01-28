package com.topface.topface.ui.adapters;

import android.content.Context;

import com.topface.topface.data.BlackListItem;
import com.topface.topface.utils.ad.NativeAd;

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
    protected INativeAdItemCreator<BlackListItem> getNativeAdItemCreator() {
        return new INativeAdItemCreator<BlackListItem>() {
            @Override
            public BlackListItem getAdItem(NativeAd nativeAd) {
                return new BlackListItem(nativeAd);
            }
        };
    }

    @Override
    public int getLimit() {
        return LIMIT;
    }

    @Override
    public boolean isNeedFeedAd() {
        return false;
    }
}
