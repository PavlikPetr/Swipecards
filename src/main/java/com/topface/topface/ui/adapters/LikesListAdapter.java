package com.topface.topface.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONObject;

public class LikesListAdapter extends FeedAdapter<FeedLike> {
    private int mSelectedForMutual = -1;
    private int mPrevSelectedForMutual = -1;

    public static final int T_SELECTED_FOR_MUTUAL = 7;
    public static final int T_SELECTED_FOR_MUTUAL_VIP = 8;
    private int T_COUNT = 2;

    private OnMutualListener mMutualListener;

    public interface OnMutualListener {
        void onMutual(FeedItem item);
    }

    public LikesListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    public int getItemViewType(int position) {
        FeedItem item = getItem(position);
        if (mSelectedForMutual == position && !item.isLoader() && !item.isRetrier() && !((FeedLike) item).mutualed) {
            if (super.getItemViewType(position) == FeedAdapter.T_VIP || super.getItemViewType(position) == FeedAdapter.T_NEW_VIP) {
                return T_SELECTED_FOR_MUTUAL_VIP;
            }
            return T_SELECTED_FOR_MUTUAL;
        } else return super.getItemViewType(position);
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

        holder.heart.setImageResource(like.mutualed ? R.drawable.im_item_dbl_mutual_heart :
                (like.highrate ? R.drawable.im_item_mutual_heart_top : R.drawable.im_item_mutual_heart));

        final ViewFlipper vf = holder.flipper;

        holder.heart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMutualListener != null) {
                    mMutualListener.onMutual(like);
                }
            }
        });

        if (position == mSelectedForMutual) {
            vf.setInAnimation(getContext(), R.anim.slide_in_from_right);
            vf.setOutAnimation(getContext(), android.R.anim.fade_out);
            vf.setDisplayedChild(1);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                convertView.setActivated(true);
            } else {
                if (super.getItemViewType(position) == T_VIP || super.getItemViewType(position) == T_NEW_VIP) {
                    convertView.setBackgroundResource(R.drawable.im_item_list_vip_bg);
                } else {
                    convertView.setBackgroundResource(R.drawable.im_item_list_bg_activated);
                }
            }
            holder.flippedBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mMutualListener != null) {
                        mMutualListener.onMutual(like);
                        setSelectedForMutual(-1);
                        like.mutualed = true;
                    }
                }
            });

        } else {
            if (mPrevSelectedForMutual == position) {
                vf.setInAnimation(getContext(), android.R.anim.fade_in);
                vf.setOutAnimation(getContext(), R.anim.slide_out_right);
                vf.setDisplayedChild(0);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    convertView.setActivated(false);
                } else {
                    if (super.getItemViewType(position) == T_VIP || super.getItemViewType(position) == T_NEW_VIP) {
                        convertView.setBackgroundResource(R.drawable.item_list_vip_selector);
                    } else {
                        convertView.setBackgroundResource(R.drawable.item_list_selector);
                    }
                }
                mPrevSelectedForMutual = -1;
            }
        }

        return convertView;
    }

    public void setSelectedForMutual(int position) {
        if (position != -1) {
            //noinspection ConstantConditions
            if (getItem(position) instanceof FeedLike) {
                if (!getItem(position).mutualed) {
                    mPrevSelectedForMutual = mSelectedForMutual;
                    mSelectedForMutual = position;
                    notifyDataSetChanged();
                }
            }
        } else {
            mPrevSelectedForMutual = mSelectedForMutual;
            mSelectedForMutual = position;
            notifyDataSetChanged();
        }
    }

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView) {
        FeedViewHolder holder = super.getEmptyHolder(convertView);
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
                FeedLike result = new FeedLike((JSONObject) null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedLike getRetrier() {
                FeedLike result = new FeedLike((JSONObject) null);
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
