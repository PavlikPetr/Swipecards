package com.topface.topface.ui.adapters;

import android.content.Context;

import com.topface.topface.R;
import com.topface.topface.data.FeedBookmark;
import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONObject;

public class BookmarksListAdapter extends FeedAdapter<FeedBookmark> {

    public BookmarksListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected int getItemLayout() {
        return R.layout.item_feed_like;
    }

    @Override
    protected int getNewItemLayout() {
        return R.layout.item_feed_new_like;
    }

    @Override
    protected int getVipItemLayout() {
        return R.layout.item_feed_vip_like;
    }

    @Override
    protected int getNewVipItemLayout() {
        return R.layout.item_feed_vip_new_like;
    }

    @Override
    public ILoaderRetrierCreator<FeedBookmark> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedBookmark>() {
            @Override
            public FeedBookmark getLoader() {
                FeedBookmark result = new FeedBookmark((JSONObject) null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedBookmark getRetrier() {
                FeedBookmark result = new FeedBookmark((JSONObject) null);
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
