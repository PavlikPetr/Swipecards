package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.GiftsRequest;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsStripAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by saharuk on 06.04.15.
 */
public abstract class AbstractFormListAdapter extends BaseAdapter {
    private static final int GIFTS_TYPE = 0;
    private static final int FORM_TYPE = 1;

    private static final int TYPE_COUNT = 2;

    private GiftsStripAdapter mGiftsAdapter;

    // Data
    private LayoutInflater mInflater;
    private float mGiftSize;
    private LinkedList<FormItem> mForms = new LinkedList<>();
    private Profile.Gifts mGifts;
    private View.OnClickListener mOnGiftsClickListener;

    public AbstractFormListAdapter(Context context) {
        mGiftsAdapter = new GiftsStripAdapter(context, new FeedList<FeedGift>(), null);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mGiftSize = context.getResources().getDimension(R.dimen.form_gift_size);
    }

    public void setUserData(LinkedList<FormItem> forms, Profile.Gifts gifts) {
        mForms = prepareForm(forms);
        mGifts = gifts;
        mGiftsAdapter.getData().clear();
        for (Gift gift: gifts) {
            FeedGift feedGift = new FeedGift();
            feedGift.gift = gift;
            mGiftsAdapter.add(feedGift);
        }
    }

    protected abstract LinkedList<FormItem> prepareForm(LinkedList<FormItem> forms);

    public LinkedList<FormItem> getFormItems() {
        return mForms;
    }

    @Override
    public int getCount() {
        int size = mForms.size();
        if (mGiftsAdapter.isEmpty()) {
            return size;
        } else {
            return size + 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return !mGiftsAdapter.isEmpty() && position == 0 ? GIFTS_TYPE : FORM_TYPE;
    }

    @Override
    public FormItem getItem(int position) {
        if (mGiftsAdapter.isEmpty()) {
            return mForms.get(position);
        } else {
            if (position == 0) {
                return null;
            } else {
                return mForms.get(position - 1);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        FormItem item = getItem(position);
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();

            switch (type) {
                case FORM_TYPE:
                    convertView = mInflater.inflate(R.layout.item_user_list, parent, false);
                    break;
                case GIFTS_TYPE:
                    convertView = mInflater.inflate(R.layout.form_gifts, parent, false);
                    break;
            }

            holder.value = (TextView) convertView.findViewById(R.id.tvValue);
            holder.title = (TextView) convertView.findViewById(R.id.tvTitle);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == GIFTS_TYPE) {
            LinearLayout giftsStrip = (LinearLayout) convertView;
            giftsStrip.removeAllViews();
            int giftsNumber = (parent.getWidth() - giftsStrip.getPaddingLeft() -
                    giftsStrip.getPaddingRight()) / (int) mGiftSize;

            int giftsCount = mGiftsAdapter.getCount();
            for (int i = 0; i < giftsCount && i < giftsNumber; i++) {
                View giftView = mGiftsAdapter.getView(i, null, giftsStrip);
                giftView.setLayoutParams(new ViewGroup.LayoutParams((int) mGiftSize, (int) mGiftSize));
                giftsStrip.addView(giftView);
            }
            if (giftsCount > giftsNumber) {
                TextView giftsCounter = (TextView) mInflater.inflate(R.layout.remained_gifts_counter,
                        giftsStrip, false);
                giftsCounter.setText("+" + (giftsCount - giftsNumber));
                giftsStrip.addView(giftsCounter);
            } else if (giftsCount < giftsNumber && mGifts.more) {

            }
            if (mOnGiftsClickListener != null) {
                giftsStrip.setOnClickListener(mOnGiftsClickListener);
            }
            return giftsStrip;
        }

        String itemTitle = item.getTitle();
        holder.title.setText(itemTitle);
        if (TextUtils.isEmpty(item.value)) {
            holder.value.setText(R.string.form_not_specified);
        } else if (App.getContext().getResources().getString(R.string.form_main_about_status_2).equals(itemTitle) ||
                item.type == FormItem.NAME || item.type == FormItem.STATUS) {
            holder.value.setText(item.value);
        } else if (item.type == FormItem.CITY ) {
            holder.value.setText(JsonUtils.fromJson(item.value, City.class).name);
        } else {
            holder.value.setText(item.value.toLowerCase(Locale.getDefault()));
        }

        configureHolder(holder, item);

        return convertView;
    }

    protected abstract void configureHolder(ViewHolder holder, FormItem item);

    protected void setOnGiftsClickListener(View.OnClickListener clickListener) {
        mOnGiftsClickListener = clickListener;
    }

    public ArrayList<FormItem> saveState() {
        return mForms != null ? new ArrayList<>(mForms) : null;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public void restoreState(ArrayList<Parcelable> userForms) {
        mForms = new LinkedList<>();
        for (Parcelable form : userForms) {
            mForms.add((FormItem) form);
        }
    }

    // class ViewHolder
    protected static class ViewHolder {
        public TextView title;
        public TextView value;
    }
}
