package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Visitor;
import com.topface.topface.utils.Utils;

public class VisitorsListAdapter extends FeedAdapter<Visitor> {

    private boolean allowUpdating = false;

    public VisitorsListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected FeedAdapter.FeedViewHolder getEmptyHolder(View convertView, Visitor item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.time = (TextView) convertView.findViewById(R.id.tvTime);
        return holder;
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();

        Visitor visitor = getItem(position);
        holder.time.setText(Utils.formatTime(getContext(), visitor.created));
        holder.time.setVisibility(View.VISIBLE);

        return convertView;
    }

    @Override
    protected int getItemLayout() {
        return R.layout.item_feed_like;
    }

    @Override
	protected int getNewItemLayout() {		
		return R.layout.item_new_feed_like;
	}

    @Override
    protected int getVipItemLayout() {
        return R.layout.item_feed_vip_like;
    }

    @Override
    protected int getNewVipItemLayout() {
        return R.layout.item_new_vip_feed_like;
    }

    @Override
    public boolean isNeedUpdate() {
        return super.isNeedUpdate() && !allowUpdating;
    }

    public void allowUpdating(boolean allow) {
        allowUpdating = allow;
    }

    @Override
    public ILoaderRetrierFactory<Visitor> getLoaderReqtrierFactory() {
        return new ILoaderRetrierFactory<Visitor>() {
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
