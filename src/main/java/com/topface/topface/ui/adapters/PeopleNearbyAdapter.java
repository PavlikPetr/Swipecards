package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.data.FeedGeo;

public class PeopleNearbyAdapter extends FeedAdapter<FeedGeo> {
    public PeopleNearbyAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();
        FeedGeo item = getItem(position);
        double distance = 0;
        if (item.distance >= 1000) {
            distance = item.distance / 1000;
            holder.city.setText(String.format(getContext().getString(R.string.general_distance_km), distance));
        } else {
            distance = item.distance >= 1 ? item.distance : 1;
            holder.city.setText(String.format(getContext().getString(R.string.general_distance_m), (int) distance));
        }
        holder.city.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.geo_ico),
                null, null, null);
        holder.city.setTextColor(getContext().getResources().getColor(R.color.geo_color));
        return convertView;
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
    public ILoaderRetrierCreator<FeedGeo> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedGeo>() {
            @Override
            public FeedGeo getLoader() {
                FeedGeo result = new FeedGeo(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedGeo getRetrier() {
                FeedGeo result = new FeedGeo(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }
}
