package com.topface.topface.ui.adapters;

import android.content.Context;

import com.topface.topface.R;
import com.topface.topface.data.FeedMutual;
import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONObject;

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
    public ILoaderRetrierCreator<FeedMutual> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedMutual>() {
            @Override
            public FeedMutual getLoader() {
                FeedMutual result = new FeedMutual((JSONObject) null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedMutual getRetrier() {
                FeedMutual result = new FeedMutual((JSONObject) null);
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }

    @Override
    protected INativeAdItemCreator<FeedMutual> getNativeAdItemCreator() {
        return new INativeAdItemCreator<FeedMutual>() {
            @Override
            public FeedMutual getAdItem(NativeAd nativeAd) {
                return new FeedMutual(nativeAd);
            }
        };
    }
}
