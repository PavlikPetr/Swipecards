package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.view.View;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.ui.adapters.GiftsStripAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

import java.util.LinkedList;

public class ProfileFormListAdapter extends AbstractFormListAdapter {

    private String mSavingText;
    private int mSavingColor;
    private int mMainValueColor;
    private View.OnClickListener mOnEditListener;

    public ProfileFormListAdapter(Context context, GiftsStripAdapter giftsAdapter) {
        super(context, giftsAdapter);
        mSavingText = context.getString(R.string.saving_in_progress);
        mSavingColor = context.getResources().getColor(R.color.text_color_gray_transparent);
        mMainValueColor = context.getResources().getColor(R.color.text_color_gray);
    }

    @Override
    protected LinkedList<FormItem> prepareForm(LinkedList<FormItem> forms) {
        forms.clear();
        if (CacheProfile.forms != null) {
            // fake forms for profile main data
            forms.add(new FormItem(R.string.edit_name, CacheProfile.first_name, FormItem.NAME) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.first_name = formItem.value;
                }
            });
            String sex = App.getContext().getString(CacheProfile.sex == Static.BOY ? R.string.boy : R.string.girl);
            forms.add(new FormItem(R.string.general_sex, sex, FormItem.SEX) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.sex = formItem.dataId;
                }
            });
            forms.add(new FormItem(R.string.edit_age, String.valueOf(CacheProfile.age), FormItem.AGE) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.age = Integer.valueOf(formItem.value);
                }
            });
            forms.add(new FormItem(R.string.general_city, JsonUtils.toJson(CacheProfile.city), FormItem.CITY) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.city = JsonUtils.fromJson(formItem.value, City.class);
                }
            });
            forms.add(new FormItem(R.string.edit_status, CacheProfile.getStatus(), FormItem.STATUS) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.setStatus(formItem.value);
                }
            });

            // real forms
            forms.addAll(CacheProfile.forms);
        }

        return forms;
    }

    public void setOnEditListener(View.OnClickListener onEditListener) {
        mOnEditListener = onEditListener;
    }

    @Override
    protected void configureHolder(ViewHolder holder, FormItem item) {
        holder.value.setOnClickListener(mOnEditListener);
        holder.value.setTag(item);
        holder.title.setOnClickListener(mOnEditListener);
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
    }
}

