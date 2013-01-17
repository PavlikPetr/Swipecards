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

    @Override
    protected int getVipItemLayout() {
        return R.layout.item_feed_vip_like;
    }

    @Override
    protected int getNewVipItemLayout() {
        return  R.layout.item_new_vip_feed_like;
    }

    @Override
    public ILoaderRetrierFactory<FeedMutual> getLoaderReqtrierFactory() {
        return new ILoaderRetrierFactory<FeedMutual>() {
            @Override
            public FeedMutual getLoader() {
                FeedMutual result = new FeedMutual(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedMutual getRetrier() {
                FeedMutual result = new FeedMutual(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }
}
