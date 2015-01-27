package com.topface.topface.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nineoldandroids.view.ViewHelper;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.ui.views.FeedItemViewConstructor;
import com.topface.topface.utils.ad.NativeAd;

public class LikesListAdapter extends FeedAdapter<FeedLike> {

    private OnMutualListener mMutualListener;

    public interface OnMutualListener {
        void onMutual(FeedItem item);
    }

    public LikesListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected FeedItemViewConstructor.TypeAndFlag getViewCreationFlag() {
        return new FeedItemViewConstructor.TypeAndFlag(FeedItemViewConstructor.Type.HEART);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected View getContentView(final int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();
        final FeedLike like = getItem(position);

        holder.heart.setImageResource(like.mutualed ? R.drawable.im_item_dbl_mutual_heart :
                (like.highrate ? R.drawable.im_item_mutual_heart_top : R.drawable.im_item_mutual_heart));

        ViewHelper.setAlpha(holder.heart, (like.user.deleted || like.user.banned) ? 0.1f : 1f);

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
        holder.heart = (ImageView) convertView.findViewById(R.id.ifp_heart);
        return holder;
    }

    public void setOnMutualListener(OnMutualListener listener) {
        mMutualListener = listener;
    }

    @Override
    public ILoaderRetrierCreator<FeedLike> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedLike>() {
            @Override
            public FeedLike getLoader() {
                FeedLike result = new FeedLike();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedLike getRetrier() {
                FeedLike result = new FeedLike();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }

    @Override
    protected INativeAdItemCreator<FeedLike> getNativeAdItemCreator() {
        return new INativeAdItemCreator<FeedLike>() {
            @Override
            public FeedLike getAdItem(NativeAd nativeAd) {
                return new FeedLike(nativeAd);
            }
        };
    }
}
