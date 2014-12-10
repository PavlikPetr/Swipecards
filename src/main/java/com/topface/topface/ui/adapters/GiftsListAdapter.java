package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;

public class GiftsListAdapter extends GiftsAdapter {

    public GiftsListAdapter(Context context, FeedList<FeedGift> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    private OnGridClickLIstener mOnGridClickLIstener;

    /* Кастомный листенер для GridView нужен из за того, что на ImageViewRemote навешен
     OnTouchListener и сандартный обработчик нажатий GridView не будет работать
      */
    public void setOnGridClickLIstener(OnGridClickLIstener mOnGridClickLIstener) {
        this.mOnGridClickLIstener = mOnGridClickLIstener;
    }

    public interface OnGridClickLIstener {
        public void onGridClick(FeedGift item);
    }

    public class ViewHolder {
        ImageViewRemote giftImage;
        TextView priceText;
    }

    @Override
    protected View getContentView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        FeedGift item = getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gift, null, false);
            holder = new ViewHolder();
            holder.giftImage = (ImageViewRemote) convertView.findViewById(R.id.giftImage);
            holder.priceText = (TextView) convertView.findViewById(R.id.giftPrice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.giftImage.setRemoteSrc(item.gift.link);
        holder.priceText.setText(Integer.toString(item.gift.price));
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
        return convertView;
    }


}
