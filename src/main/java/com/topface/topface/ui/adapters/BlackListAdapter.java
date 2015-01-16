package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.topface.topface.R;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONObject;

public class BlackListAdapter extends FeedAdapter<BlackListItem> {

    public static final int LIMIT = 100;

    public static final int ITEM_LAYOUT = R.layout.item_feed_black_list;
    public static final int ITEM_VIP_LAYOUT = R.layout.item_feed_vip_black_list;

    public BlackListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected int getItemLayout() {
        return ITEM_LAYOUT;
    }

    @Override
    protected int getNewItemLayout() {
        return ITEM_LAYOUT;
    }

    @Override
    protected int getVipItemLayout() {
        return ITEM_VIP_LAYOUT;
    }

    @Override
    protected int getNewVipItemLayout() {
        return ITEM_VIP_LAYOUT;
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        final FeedViewHolder holder = (FeedViewHolder) convertView.getTag();

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.dataLayout.getLayoutParams();

        holder.dataLayout.setLayoutParams(params);

        return convertView;
    }

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView) {
        FeedViewHolder holder = super.getEmptyHolder(convertView);
        holder.dataLayout = convertView.findViewById(R.id.animationLayout);

        return holder;
    }

    @Override
    public ILoaderRetrierCreator<BlackListItem> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<BlackListItem>() {
            @Override
            public BlackListItem getLoader() {
                BlackListItem result = new BlackListItem((JSONObject) null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public BlackListItem getRetrier() {
                BlackListItem result = new BlackListItem((JSONObject) null);
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
}
