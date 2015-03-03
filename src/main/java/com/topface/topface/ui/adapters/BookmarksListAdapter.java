package com.topface.topface.ui.adapters;

import android.content.Context;

import com.topface.topface.data.FeedBookmark;
import com.topface.topface.utils.ad.NativeAd;

public class BookmarksListAdapter extends FeedAdapter<FeedBookmark> {

    public BookmarksListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    public ILoaderRetrierCreator<FeedBookmark> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedBookmark>() {
            @Override
            public FeedBookmark getLoader() {
                FeedBookmark result = new FeedBookmark();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedBookmark getRetrier() {
                FeedBookmark result = new FeedBookmark();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }

    @Override
    protected INativeAdItemCreator<FeedBookmark> getNativeAdItemCreator() {
        return new INativeAdItemCreator<FeedBookmark>() {
            @Override
            public FeedBookmark getAdItem(NativeAd nativeAd) {
                return new FeedBookmark(nativeAd);
            }
        };
    }
}
