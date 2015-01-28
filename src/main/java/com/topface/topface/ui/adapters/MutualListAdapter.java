package com.topface.topface.ui.adapters;

import android.content.Context;

import com.topface.topface.data.FeedMutual;
import com.topface.topface.utils.ad.NativeAd;

public class MutualListAdapter extends FeedAdapter<FeedMutual> {

    public MutualListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    public ILoaderRetrierCreator<FeedMutual> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedMutual>() {
            @Override
            public FeedMutual getLoader() {
                FeedMutual result = new FeedMutual();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedMutual getRetrier() {
                FeedMutual result = new FeedMutual();
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
