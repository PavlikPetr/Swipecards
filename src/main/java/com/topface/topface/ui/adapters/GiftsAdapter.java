package com.topface.topface.ui.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.loadcontollers.FeedLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

public class GiftsAdapter extends LoadingListAdapter<FeedGift> implements AbsListView.OnScrollListener {

    public static class ViewHolder {
        public ImageViewRemote giftImage;
    }

    public GiftsAdapter(Context context, FeedList<FeedGift> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    @Override
    String getPlc() {
        return mContext.getResources().getString(R.string.gift_list_loader);
    }

    @Override
    protected LoadController initLoadController() {
        return App.getAppComponent().feedLoadController();
    }


    @Override
    protected View getContentView(final int position, View convertView, ViewGroup parent) {
        FeedGift item = getItem(position);

        ViewHolder holder = convertView != null ? (ViewHolder) convertView.getTag() : null;
        if (holder == null) {
            convertView = mInflater.inflate(getGiftItemLayoutRes(), null, false);
            holder = new ViewHolder();
            holder.giftImage = (ImageViewRemote) convertView.findViewById(R.id.profileGiftImage);
            convertView.setTag(holder);
        }
        if (item.gift.type == Gift.SEND_BTN) {
            holder.giftImage.setImageBitmap(null);
            holder.giftImage.setBackgroundResource(R.drawable.chat_gift_selector);
        } else {
            holder.giftImage.setRemoteSrc(item.gift.link);
        }
        return convertView;
    }

    @LayoutRes
    protected int getGiftItemLayoutRes() {
        return R.layout.profile_item_gift_grid;
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
