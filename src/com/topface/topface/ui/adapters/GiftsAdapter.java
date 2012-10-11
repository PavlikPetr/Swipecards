package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.GiftGalleryManager;

public class GiftsAdapter extends LoadingListAdapter {

    private LayoutInflater mInflater;

    private GiftGalleryManager<Gift> mGalleryManager;

    public GiftsAdapter(Context context, GiftGalleryManager<Gift> galleryManager) {
        mInflater = LayoutInflater.from(context);
        mGalleryManager = galleryManager;

        mLoaderRetrier = mInflater.inflate(R.layout.item_grid_loader_retrier, null, false);
        mLoaderRetrierText = (TextView) mLoaderRetrier.findViewById(R.id.tvLoaderText);
        mLoaderRetrierProgress = (ProgressBar) mLoaderRetrier.findViewById(R.id.prsLoader);
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
            holder.gift = (getItem(position));
            type = holder.gift.type;

            if (type == Gift.PROFILE || type == Gift.PROFILE_NEW) {
                mGalleryManager.getImage(position, holder.giftImage);
                holder.giftText.setText(Static.EMPTY);
                holder.giftText.setVisibility(View.VISIBLE);
                holder.priceText.setVisibility(View.GONE);
            } else if (type == Gift.SEND_BTN) {
                holder.giftImage.setImageResource(R.drawable.chat_gift_selector);
                holder.giftText.setText(R.string.gifts_send_btn);
                holder.giftText.setVisibility(View.VISIBLE);
                holder.priceText.setVisibility(View.GONE);
            } else {
                mGalleryManager.getImage(position, holder.giftImage);
                holder.priceText.setVisibility(View.VISIBLE);
                holder.priceText.setText(Integer.toString(holder.gift.price));
                holder.giftText.setVisibility(View.GONE);
            }

            return convertView;
        }
    }

    @Override
    public Gift getItem(int position) {
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
        public Gift gift;
    }

}
