package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

import java.util.LinkedList;

public class ProfileFormListAdapter extends AbstractFormListAdapter {

    private String mSavingText;
    private int mSavingColor;
    private int mMainValueColor;
    private View.OnClickListener mOnEditListener;

    public ProfileFormListAdapter(Context context) {
        super(context);
        mSavingText = context.getString(R.string.saving_in_progress);
        mSavingColor = context.getResources().getColor(R.color.text_color_gray_transparent);
        mMainValueColor = context.getResources().getColor(R.color.text_color_gray);
    }

    @Override
    protected LinkedList<FormItem> prepareForm(String status, LinkedList<FormItem> forms) {
        FormInfo formInfo = new FormInfo(App.getContext(), CacheProfile.sex, Profile.TYPE_OWN_PROFILE);
        forms.clear();
        if (CacheProfile.forms != null) {
            // fake forms for profile main data
            FormItem statusItem = new FormItem(R.string.edit_status, CacheProfile.getStatus(), FormItem.STATUS) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.setStatus(formItem.value);
                }
            };
            statusItem.setTextLimitInterface(new FormItem.DefaultTextLimiter(App.getAppOptions().getUserStatusMaxLength()));
            forms.add(statusItem);

            FormItem nameItem = new FormItem(R.string.edit_name, CacheProfile.first_name, FormItem.NAME) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.first_name = formItem.value;
                }
            };
            nameItem.setTextLimitInterface(new FormItem.DefaultTextLimiter(){
                @Override
                public boolean isVisible() {
                    return false;
                }
            });
            nameItem.setCanBeEmpty(false);
            forms.add(nameItem);

            String sex = App.getContext().getString(CacheProfile.sex == Static.BOY ? R.string.boy : R.string.girl);
            forms.add(new FormItem(R.string.general_sex, sex, FormItem.SEX) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.sex = formItem.dataId;
                }
            });

            FormItem ageItem = new FormItem(R.string.edit_age, String.valueOf(CacheProfile.age), FormItem.AGE) {
                @Override
                public void copy(FormItem formItem) {

                    super.copy(formItem);
                    CacheProfile.age = TextUtils.isEmpty(formItem.value) ? 0 : Integer.valueOf(formItem.value);
                }
            };
            ageItem.setValueLimitInterface(new FormItem.ValueLimitInterface() {
                @Override
                public int getMinValue() {
                    return App.getAppOptions().getUserAgeMin();
                }

                @Override
                public int getMaxValue() {
                    return App.getAppOptions().getUserAgeMax();
                }
            });
            forms.add(ageItem);

            forms.add(new FormItem(R.string.general_city, JsonUtils.toJson(CacheProfile.city), FormItem.CITY) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.city = JsonUtils.fromJson(formItem.value, City.class);
                }
            });

            // real forms
            for (FormItem item : CacheProfile.forms) {
                if (!(item.isOnlyForWomen() && CacheProfile.sex == Static.BOY)) {
                    formInfo.fillFormItem(item);
                    forms.add(item);
                }
            }
        }

        return forms;
    }

    public static FormItem getAgeItem() {
        FormItem ageItem = new FormItem(R.string.edit_age, String.valueOf(CacheProfile.age), FormItem.AGE) {
            @Override
            public void copy(FormItem formItem) {
                super.copy(formItem);
                CacheProfile.age = TextUtils.isEmpty(formItem.value) ? 0 : Integer.valueOf(formItem.value);
            }
        };
        ageItem.setValueLimitInterface(new FormItem.ValueLimitInterface() {
            @Override
            public int getMinValue() {
                return App.getAppOptions().getUserAgeMin();
            }

            @Override
            public int getMaxValue() {
                return App.getAppOptions().getUserAgeMax();
            }
        });
        return ageItem;
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

