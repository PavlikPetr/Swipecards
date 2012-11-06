package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Visitor;
import com.topface.topface.utils.Utils;

public class VisitorsListAdapter extends FeedAdapter<Visitor> {

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
		return getItemLayout();
	}
}
