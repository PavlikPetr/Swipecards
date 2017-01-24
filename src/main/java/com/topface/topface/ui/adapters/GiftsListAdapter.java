package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.extensions.UiTestsExtensionKt;

public class GiftsListAdapter extends GiftsAdapter {

    public GiftsListAdapter(Context context, FeedList<FeedGift> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    private OnGridClickLIstener mOnGridClickLIstener;
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imgView = (ImageView)v.findViewById(R.id.giftImage);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setHighlight(imgView, true);
                    break;
                case MotionEvent.ACTION_UP:
                    setHighlight(imgView, false);
                    Integer pos = (Integer) imgView.getTag();
                    mOnGridClickLIstener.onGridClick(getData().get(pos));
                    break;
                case MotionEvent.ACTION_CANCEL:
                    setHighlight(imgView, false);
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
        LinearLayout descriptionRoot;
        LinearLayout viewRoot;
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
            holder.descriptionRoot = (LinearLayout) convertView.findViewById(R.id.descriptionRoot);
            holder.viewRoot = (LinearLayout) convertView.findViewById(R.id.viewRoot);
            holder.viewRoot.setOnTouchListener(mOnTouchListener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.giftImage.setTag(position);
        holder.giftImage.setRemoteSrc(item.gift.link);
        holder.priceText.setText(Integer.toString(item.gift.price));
        UiTestsExtensionKt.setUiTestTag(holder.descriptionRoot, UiTestsExtensionKt.getGiftTag(item.gift));
        return convertView;
    }

    private void setHighlight(ImageView view, boolean isSelected) {
        if (isSelected) {
            view.setColorFilter(mContext.getResources().getColor(R.color.blue_60_percent));
            Utils.setBackground(view, R.color.blue_60_percent);
        } else {
            view.setColorFilter(null);
            Utils.setBackground(view, -1);
        }
    }
}
