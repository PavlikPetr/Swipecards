package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.R;
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
    private Context mContext;

    public ProfileFormListAdapter(Context context) {
        super(context);
        mContext = context;
        mSavingText = context.getString(R.string.saving_in_progress);
        mSavingColor = context.getResources().getColor(R.color.text_color_gray_transparent);
        mMainValueColor = context.getResources().getColor(R.color.text_color_gray);
    }

    @Override
    protected LinkedList<FormItem> prepareForm(String status, LinkedList<FormItem> forms) {
        final Profile profile = App.from(mContext).getProfile();
        FormInfo formInfo = new FormInfo(App.getContext(), profile.sex, Profile.TYPE_OWN_PROFILE);
        forms.clear();
        if (profile.forms != null) {
            // fake forms for profile main data
            FormItem statusItem = new FormItem(R.string.edit_status, CacheProfile.getStatus(mContext), FormItem.STATUS) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.setStatus(mContext, formItem.value);
                }
            };
            statusItem.setTextLimitInterface(new FormItem.DefaultTextLimiter(App.getAppOptions().getUserStatusMaxLength()));
            forms.add(statusItem);

            FormItem nameItem = new FormItem(R.string.edit_name, profile.firstName, FormItem.NAME) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    profile.firstName = formItem.value;
                }
            };
            nameItem.setTextLimitInterface(new FormItem.DefaultTextLimiter() {
                @Override
                public boolean isVisible() {
                    return false;
                }
            });
            nameItem.setCanBeEmpty(false);
            forms.add(nameItem);

            String sex = App.getContext().getString(profile.sex == Profile.BOY ? R.string.boy : R.string.girl);
            forms.add(new FormItem(R.string.general_sex, sex, FormItem.SEX) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    profile.sex = formItem.dataId;
                }
            });

            FormItem ageItem = new FormItem(R.string.edit_age, String.valueOf(profile.age), FormItem.AGE) {
                @Override
                public void copy(FormItem formItem) {

                    super.copy(formItem);
                    profile.age = TextUtils.isEmpty(formItem.value) ? 0 : Integer.valueOf(formItem.value);
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

                @Override
                public boolean isEmptyValueAvailable() {
                    return false;
                }
            });
            forms.add(ageItem);

            forms.add(new FormItem(R.string.general_city, JsonUtils.toJson(profile.city), FormItem.CITY) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    profile.city = JsonUtils.fromJson(formItem.value, City.class);
                }
            });

            // real forms
            for (FormItem item : profile.forms) {
                if (!(item.isOnlyForWomen() && profile.sex == Profile.BOY)) {
                    formInfo.fillFormItem(item);
                    forms.add(item);
                }
            }
        }

        return forms;
    }

    public static FormItem getAgeItem(final Profile profile) {
        FormItem ageItem = new FormItem(R.string.edit_age, String.valueOf(profile.age), FormItem.AGE) {
            @Override
            public void copy(FormItem formItem) {
                super.copy(formItem);
                profile.age = TextUtils.isEmpty(formItem.value) ? 0 : Integer.valueOf(formItem.value);
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

            @Override
            public boolean isEmptyValueAvailable() {
                return false;
            }
        });
        return ageItem;
    }

    @Override
    protected void configureHolder(ViewHolder holder, FormItem item) {
        holder.value.setTag(item);
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

