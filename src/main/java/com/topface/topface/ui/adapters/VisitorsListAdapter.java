package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.Visitor;

public class VisitorsListAdapter extends FeedAdapter<Visitor> {

    public VisitorsListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected FeedAdapter.FeedViewHolder getEmptyHolder(View convertView, Visitor item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);

        holder.time = (TextView) convertView.findViewById(R.id.tvTime);
        holder.text = (TextView) convertView.findViewById(R.id.tvText);
        return holder;
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();

        Visitor visitor = getItem(position);
        holder.time.setText(visitor.createdRelative);
        holder.time.setVisibility(View.VISIBLE);
        holder.city.setText(null);
        holder.text.setText(visitor.user.city.name);
        return convertView;
    }

    @Override
    protected int getItemLayout() {
        return R.layout.item_feed_dialog;
    }

    @Override
    protected int getNewItemLayout() {
        return R.layout.item_feed_new_dialog;
    }

    @Override
    protected int getVipItemLayout() {
        return R.layout.item_feed_vip_dialog;
    }

    @Override
    protected int getNewVipItemLayout() {
        return R.layout.item_feed_vip_new_dialog;
    }

    @Override
    public ILoaderRetrierCreator<Visitor> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<Visitor>() {
            @Override
            public Visitor getLoader() {
                Visitor result = new Visitor(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public Visitor getRetrier() {
                Visitor result = new Visitor(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }
}
