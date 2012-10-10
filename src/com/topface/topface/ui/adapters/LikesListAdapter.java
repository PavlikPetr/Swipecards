package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;

public class LikesListAdapter extends FeedAdapter<FeedLike> {
    public LikesListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();
        FeedLike like = getItem(position);

        holder.heart.setImageResource(like.highrate ?
                R.drawable.im_item_mutual_heart_top :
                R.drawable.im_item_mutual_heart
        );

        return convertView;
    }

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView, FeedLike item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.heart = (ImageView) convertView.findViewById(R.id.ivHeart);
        return holder;
    }

    @Override
    protected FeedLike getNewItem(IListLoader.ItemType type) {
        return new FeedLike(type);
    }

    @Override
    protected int getItemLayout() {
        return R.layout.item_feed_like;
    }
}
