package com.topface.topface.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;

public class LikesListAdapter extends FeedAdapter<FeedLike> {
    private static final int T_COUNT = 2;

    private OnMutualListener mMutualListener;

    public interface OnMutualListener {
        void onMutual(FeedItem item);
    }

    public LikesListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    public int getViewTypeCount() {
        Debug.log(Integer.toString(super.getViewTypeCount() + T_COUNT));
        return (super.getViewTypeCount() + T_COUNT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected View getContentView(final int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();
        final FeedLike like = getItem(position);

        holder.heart.setImageResource(like.mutualed ? R.drawable.im_item_dbl_mutual_heart_selector :
                (like.highrate ? R.drawable.im_item_mutual_heart_top_selector : R.drawable.im_item_mutual_heart_selector));

        holder.heart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMutualListener != null) {
                    mMutualListener.onMutual(like);
                }
            }
        });

        return convertView;
    }

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView, FeedLike item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.heart = (ImageView) convertView.findViewById(R.id.ivHeart);
        holder.heart.setVisibility(View.VISIBLE);
        return holder;
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

    public void setOnMutualListener(OnMutualListener listener) {
        mMutualListener = listener;
    }

    @Override
    public ILoaderRetrierCreator<FeedLike> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedLike>() {
            @Override
            public FeedLike getLoader() {
                FeedLike result = new FeedLike(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedLike getRetrier() {
                FeedLike result = new FeedLike(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }
}
