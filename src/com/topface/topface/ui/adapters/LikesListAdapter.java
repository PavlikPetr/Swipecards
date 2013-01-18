package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.utils.Debug;

public class LikesListAdapter extends FeedAdapter<FeedLike> {
    private int mSelectedForMutual = -1;
    private int mPrevSelectedForMutual = -1;

    public static final int T_SELECTED_FOR_MUTUAL = 6;
    public static final int T_SELECTED_FOR_MUTUAL_VIP = 7;
    private int T_COUNT = 2;

    private OnMutualListener mMutualListener;

    public interface OnMutualListener {
        void onMutual(int userId, int rate, int mutualId);
    }

    public LikesListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    public int getItemViewType(int position) {
        FeedItem item = getItem(position);
        if (mSelectedForMutual == position && !item.isLoader() && !item.isLoaderRetry() && ((FeedLike) item).mutualed) {
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
                setSelectedForMutual(position);
            }
        });

        if (position == mSelectedForMutual) {
            vf.setInAnimation(getContext(), R.anim.slide_in_from_right);
            vf.setOutAnimation(getContext(), android.R.anim.fade_out);
            vf.setDisplayedChild(1);
            if (android.os.Build.VERSION.SDK_INT > 11) {
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
                        mMutualListener.onMutual(getItem(position).user.id, 9, like.id);
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
                if (android.os.Build.VERSION.SDK_INT > 11) {
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
