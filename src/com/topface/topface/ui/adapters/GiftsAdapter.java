package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.views.ImageViewRemote;

public class GiftsAdapter extends LoadingListAdapter<FeedGift> implements AbsListView.OnScrollListener {

    public static final int T_SEND_BTN = 3;
    public static final int T_COUNT = 4;

    public class ViewHolder {
        ImageViewRemote giftImage;
        TextView priceText;
        TextView giftText;
    }

    public GiftsAdapter(Context context, FeedList<FeedGift> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }


    @Override
    public int getViewTypeCount() {
        return T_COUNT;
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
    protected View getContentView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        int type = getItemViewType(position);
        FeedGift item = getItem(position);

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
            if (item.gift.type == Gift.PROFILE || item.gift.type == Gift.PROFILE_NEW) {
                holder.giftImage.setRemoteSrc(item.gift.link);
                holder.giftText.setText(Static.EMPTY);
                holder.giftText.setVisibility(View.VISIBLE);
                holder.priceText.setVisibility(View.GONE);
            } else {
                holder.giftImage.setRemoteSrc(item.gift.link);
                holder.priceText.setVisibility(View.VISIBLE);
                holder.priceText.setText(Integer.toString(item.gift.price));
                holder.giftText.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    protected int getLoaderRetrierLayout() {
        return R.layout.item_grid_loader_retrier;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
            FeedList<FeedGift> data = getData();
            if (mUpdateCallback != null && !data.isEmpty() && data.getLast().isLoader()) {
                mUpdateCallback.onUpdate();
            }
        }
    }

    @Override
    public ILoaderRetrierCreator<FeedGift> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedGift>() {
            @Override
            public FeedGift getLoader() {
                FeedGift result = new FeedGift();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedGift getRetrier() {
                FeedGift result = new FeedGift();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }
}
