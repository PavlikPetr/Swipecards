package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.GiftGalleryManager;

public class GiftsAdapter extends LoadingListAdapter implements AbsListView.OnScrollListener {

    public static final int T_SEND_BTN = 3;

    private GiftGalleryManager<FeedGift> mGalleryManager;

    public GiftsAdapter(Context context, GiftGalleryManager<FeedGift> galleryManager, Updater updateCallback) {
        super(context,updateCallback);
        mGalleryManager = galleryManager;
    }

    @Override
    public int getCount() {
        return mGalleryManager.size();
    }

    @Override
    public int getViewTypeCount() {
        return (super.getViewTypeCount() + 1);
    }

    @Override
    public int getItemViewType(int position) {
        FeedGift item = getItem(position);
        if (item != null && item.gift != null && item.gift.type == Gift.SEND_BTN) {
            return T_SEND_BTN;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        int type = getItemViewType(position);

        if (type == T_LOADER) {
            mLoaderRetrierProgress.setVisibility(View.VISIBLE);
            mLoaderRetrierText.setVisibility(View.INVISIBLE);
            return mLoaderRetrier;
        } else if (type == T_RETRIER) {
            mLoaderRetrierProgress.setVisibility(View.INVISIBLE);
            mLoaderRetrierText.setVisibility(View.VISIBLE);
            return mLoaderRetrier;
        } else {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_gift, null, false);

                holder = new ViewHolder();
                holder.giftImage = (ImageViewRemote) convertView.findViewById(R.id.giftImage);
                holder.priceText = (TextView) convertView.findViewById(R.id.giftPrice);
                holder.giftText = (TextView) convertView.findViewById(R.id.giftText);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (type == T_SEND_BTN) {
                holder.giftImage.setImageBitmap(null);
                holder.giftImage.setBackgroundResource(R.drawable.chat_gift_selector);
                holder.giftText.setText(R.string.gifts_send_btn);
                holder.giftText.setVisibility(View.VISIBLE);
                holder.priceText.setVisibility(View.GONE);
            } else {
                FeedGift item = getItem(position);
                if (item.gift.type == Gift.PROFILE || item.gift.type == Gift.PROFILE_NEW) {
                    mGalleryManager.getImage(position, holder.giftImage);
                    holder.giftText.setText(Static.EMPTY);
                    holder.giftText.setVisibility(View.VISIBLE);
                    holder.priceText.setVisibility(View.GONE);
                } else {
                    mGalleryManager.getImage(position, holder.giftImage);
                    holder.priceText.setVisibility(View.VISIBLE);
                    holder.priceText.setText(Integer.toString(item.gift.price));
                    holder.giftText.setVisibility(View.GONE);
                }
            }
        }

        return convertView;
    }

    @Override
    public FeedGift getItem(int position) {
        return mGalleryManager.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        ImageViewRemote giftImage;
        TextView priceText;
        TextView giftText;
    }

    @Override
    protected View getLoaderRetrier() {
        return mInflater.inflate(R.layout.item_grid_loader_retrier, null, false);
    }

    @Override
    protected TextView getLoaderRetrierText() {
        return (TextView) mLoaderRetrier.findViewById(R.id.tvLoaderText);
    }

    @Override
    protected ProgressBar getLoaderRetrierProgress() {
        return (ProgressBar) mLoaderRetrier.findViewById(R.id.prsLoader);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
            if (mUpdateCallback != null && !mGalleryManager.isEmpty() && mGalleryManager.getLast().isLoader()) {
                mUpdateCallback.onFeedUpdate();
            }
        }
    }
}
