package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
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
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.GiftsStripAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

import java.util.LinkedList;
import java.util.Locale;

public class ProfileFormListAdapter extends BaseAdapter {

    private static final int GIFTS_TYPE = 0;
    private static final int FORM_TYPE = 1;

    private static final int TYPE_COUNT = 2;

    private GiftsStripAdapter mGiftsAdapter;

    // Data
    private LayoutInflater mInflater;
    private float mGiftSize;
    private LinkedList<FormItem> mProfileForms = new LinkedList<>();
    private View.OnClickListener mOnFillListener;

    private String mSavingText = App.getContext().getString(R.string.saving_in_progress);
    private int mSavingColor = App.getContext().getResources().getColor(R.color.text_color_gray_transparent);
    private int mMainValueColor = App.getContext().getResources().getColor(R.color.text_color_gray);

    public ProfileFormListAdapter(Context context) {
        mGiftsAdapter = new GiftsStripAdapter(context, new FeedList<FeedGift>(), null);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mGiftSize = context.getResources().getDimension(R.dimen.form_gift_size);
        refillData();
    }

    public void refillData() {
        mProfileForms.clear();
        if (CacheProfile.forms != null) {
            // fake forms for profile main data
            mProfileForms.add(new FormItem(R.string.edit_name, CacheProfile.first_name, FormItem.NAME) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.first_name = formItem.value;
                }
            });
            String sex = App.getContext().getString(CacheProfile.sex == Static.BOY ? R.string.boy : R.string.girl);
            mProfileForms.add(new FormItem(R.string.general_sex, sex, FormItem.SEX) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.sex = formItem.dataId;
                }
            });
            mProfileForms.add(new FormItem(R.string.edit_age, String.valueOf(CacheProfile.age), FormItem.AGE) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.age = Integer.valueOf(formItem.value);
                }
            });
            mProfileForms.add(new FormItem(R.string.general_city, JsonUtils.toJson(CacheProfile.city), FormItem.CITY) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.city = JsonUtils.fromJson(formItem.value, City.class);
                }
            });
            mProfileForms.add(new FormItem(R.string.edit_status, CacheProfile.getStatus(), FormItem.STATUS) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.setStatus(formItem.value);
                }
            });

            // real forms
            mProfileForms.addAll(CacheProfile.forms);

            for (Gift gift : CacheProfile.gifts) {
                FeedGift item = new FeedGift();
                item.gift = gift;
                mGiftsAdapter.add(item);
            }
        }
    }

    public LinkedList<FormItem> getFormItems() {
        return mProfileForms;
    }

    @Override
    public int getCount() {
        int size = mProfileForms.size();
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
            return mProfileForms.get(position);
        } else {
            if (position == 0) {
                return null;
            } else {
                return mProfileForms.get(position - 1);
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
            int giftsNumber = giftsStrip.getWidth() / (int) mGiftSize;

            for (int i = 0; i < mGiftsAdapter.getCount() && i < giftsNumber; i++) {
                View giftView = mGiftsAdapter.getView(i, null, giftsStrip);
                giftView.setLayoutParams(new ViewGroup.LayoutParams((int) mGiftSize, (int) mGiftSize));
                giftsStrip.addView(giftView);
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
        holder.value.setOnClickListener(mOnFillListener);
        holder.value.setTag(item);
        holder.title.setOnClickListener(mOnFillListener);
        holder.title.setTag(item);

        if (item.isEditing) {
            holder.title.setEnabled(false);
            holder.value.setEnabled(false);
            holder.value.setText(mSavingText);
            holder.value.setTextColor(mSavingColor);
        } else {
            holder.title.setEnabled(true);
            holder.value.setEnabled(true);
            holder.value.setTextColor(mMainValueColor);
        }

        return convertView;
    }

    public void setOnFillListener(View.OnClickListener onFillListener) {
        mOnFillListener = onFillListener;
    }

    // class ViewHolder
    private static class ViewHolder {
        public TextView title;
        public TextView value;
    }
}

