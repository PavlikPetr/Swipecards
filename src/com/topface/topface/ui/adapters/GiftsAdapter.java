package com.topface.topface.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Gift;
import com.topface.topface.utils.GiftGalleryManager;

public class GiftsAdapter extends LoadingListAdapter {

    private LayoutInflater mInflater;

    private GiftGalleryManager<Gift> mGalleryManager;

    private Drawable mCoins;

    public GiftsAdapter(Context context, GiftGalleryManager<Gift> galleryManager) {
        mInflater = LayoutInflater.from(context);
        mGalleryManager = galleryManager;

        mLoaderRetrier = mInflater.inflate(R.layout.item_grid_loader_retrier, null, false);
        mLoaderRetrierText = (TextView) mLoaderRetrier.findViewById(R.id.tvLoaderText);
        mLoaderRetrierProgress = (ProgressBar) mLoaderRetrier.findViewById(R.id.prsLoader);

        mCoins = context.getResources().getDrawable(R.drawable.coins);
    }

    @Override
    public int getCount() {
        return mGalleryManager.size();
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

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
                convertView = (ViewGroup) mInflater.inflate(R.layout.item_gift, null, false);

                holder = new ViewHolder();
                holder.mGiftImage = (ImageView) convertView.findViewById(R.id.giftImage);
                holder.mGiftMask = (ImageView) convertView.findViewById(R.id.giftMask);
                holder.mPriceText = (TextView) convertView.findViewById(R.id.giftPrice);
                holder.mGiftText = (TextView) convertView.findViewById(R.id.giftText);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mGift = (getItem(position));
            type = holder.mGift.type;

            if (type == Gift.PROFILE || type == Gift.PROFILE_NEW) {
                mGalleryManager.getImage(position, (ImageView) holder.mGiftImage);
                holder.mGiftMask.setVisibility(View.VISIBLE);
                holder.mGiftText.setText(Static.EMPTY);
                holder.mGiftText.setVisibility(View.VISIBLE);
                holder.mPriceText.setVisibility(View.GONE);
            } else if (type == Gift.SEND_BTN) {
                holder.mGiftImage.setImageResource(R.drawable.chat_gift_selector);
                holder.mGiftMask.setVisibility(View.GONE);
                holder.mGiftText.setText(R.string.gifts_send_btn);
                holder.mGiftText.setVisibility(View.VISIBLE);
                holder.mPriceText.setVisibility(View.GONE);
            } else {
                mGalleryManager.getImage(position, (ImageView) holder.mGiftImage);
                holder.mGiftMask.setVisibility(View.VISIBLE);
                holder.mPriceText.setVisibility(View.VISIBLE);
                holder.mPriceText.setText(Integer.toString(holder.mGift.price));
                holder.mGiftText.setVisibility(View.GONE);
            }

            return convertView;
        }
    }

    @Override
    public Gift getItem(int position) {
        return (Gift) mGalleryManager.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        ImageView mGiftImage;
        ImageView mGiftMask;
        TextView mPriceText;
        TextView mGiftText;
        public Gift mGift;
    }

    public void release() {
        mInflater = null;
        mGalleryManager.release();
    }
}
