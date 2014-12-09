package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.FeedLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

public class GiftsAdapter extends LoadingListAdapter<FeedGift> implements AbsListView.OnScrollListener {

    public static final int T_SEND_BTN = 3;
    public static final int T_COUNT = 4;

    private OnGridClickLIstener mOnGridClickLIstener;

    public void setOnGridClickLIstener(OnGridClickLIstener mOnGridClickLIstener) {
        this.mOnGridClickLIstener = mOnGridClickLIstener;
    }

    public interface OnGridClickLIstener {
        public void onGridClick(FeedGift item);
    }

    public class ViewHolder {
        ImageViewRemote giftImage;
        TextView priceText;
        TextView giftText;
    }

    public GiftsAdapter(Context context, FeedList<FeedGift> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    @Override
    protected LoadController initLoadController() {
        return new FeedLoadController();
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

    @SuppressWarnings("deprecation")
    @Override
    protected View getContentView(final int position, View convertView, ViewGroup parent) {
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
        if ((mContext instanceof GiftsActivity)) {
            holder.giftImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            int color = mContext.getResources().getColor(R.color.blue_60_percent);
                            ((ImageView) v).setColorFilter(color);
                            Utils.setBackground(R.color.blue_60_percent, (ImageView) v);
                            break;
                        case MotionEvent.ACTION_UP:
                            ((ImageView) v).setColorFilter(null);
                            Utils.setBackground(R.color.text_white, (ImageView) v);
                            mOnGridClickLIstener.onGridClick(mData.get(position));
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            ((ImageView) v).setColorFilter(null);
                            Utils.setBackground(R.color.text_white, (ImageView) v);
                            break;
                    }
                    return true;
                }
            });
        }

        if (type == T_SEND_BTN) {
            holder.giftImage.setImageBitmap(null);
            holder.giftImage.setBackgroundResource(R.drawable.chat_gift_selector);
            holder.giftText.setText(R.string.gifts_send_btn);
            holder.giftText.setVisibility(View.VISIBLE);
            holder.priceText.setVisibility(View.GONE);
        } else if (item.gift.type == Gift.PROFILE || item.gift.type == Gift.PROFILE_NEW) {
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
