package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;

public class GiftsListAdapter extends GiftsAdapter {

    public GiftsListAdapter(Context context, FeedList<FeedGift> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    private OnGridClickLIstener mOnGridClickLIstener;
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setHighlight(v, true);
                    break;
                case MotionEvent.ACTION_UP:
                    setHighlight(v, false);
                    Integer pos = (Integer) v.getTag();
                    mOnGridClickLIstener.onGridClick(getData().get(pos));
                    break;
                case MotionEvent.ACTION_CANCEL:
                    setHighlight(v, false);
                    break;
            }
            return true;
        }
    };

    /* Кастомный листенер для GridView нужен из за того, что на ImageViewRemote навешен
     OnTouchListener и сандартный обработчик нажатий GridView не будет работать
      */
    public void setOnGridClickLIstener(OnGridClickLIstener mOnGridClickLIstener) {
        this.mOnGridClickLIstener = mOnGridClickLIstener;
    }

    public interface OnGridClickLIstener {
        void onGridClick(FeedGift item);
    }

    public class ViewHolder {
        ImageViewRemote giftImage;
        TextView priceText;
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        FeedGift item = getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gift, null, false);
            holder = new ViewHolder();
            holder.giftImage = (ImageViewRemote) convertView.findViewById(R.id.giftImage);
            holder.priceText = (TextView) convertView.findViewById(R.id.giftPrice);
            holder.giftImage.setOnTouchListener(mOnTouchListener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.giftImage.setTag(position);
        holder.giftImage.setRemoteSrc(item.gift.link);
        holder.priceText.setText(Integer.toString(item.gift.price));
        return convertView;
    }

    private void setHighlight(View view, boolean isSelected) {
        if (isSelected) {
            ((ImageView) view).setColorFilter(mContext.getResources().getColor(R.color.blue_60_percent));
            Utils.setBackground((ImageView) view, R.color.blue_60_percent);
        } else {
            ((ImageView) view).setColorFilter(null);
            Utils.setBackground((ImageView) view, -1);
        }
    }
}
