package com.topface.topface.ui.adapters;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

/**
 * Adapter for form items edit dialog
 */
public class FormItemEditAdapter extends AbstractEditAdapter<FormItem> {

    private FormItem mFormItem;
    private FormInfo mFormInfo;
    private String[] mEntries;
    private int[] mIds;

    public FormItemEditAdapter(FormItem formItem) {
        mFormItem = new FormItem(formItem);
        mFormInfo = new FormInfo(App.getContext(), CacheProfile.sex, Profile.TYPE_OWN_PROFILE);
        mEntries = mFormInfo.getEntriesByTitleId(mFormItem.titleId);
        mIds = mFormInfo.getIdsByTitleId(mFormItem.titleId);
    }

    @Override
    public FormItem getData() {
        return mFormItem;
    }

    /**
     * This adapter saves data automatically
     */
    @Override
    public void saveData() {

    }

    @Override
    public int getCount() {
        return mEntries.length;
    }

    @Override
    public String getItem(int position) {
        return mEntries[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final String item = getItem(position);

        if (convertView == null) {
            convertView = inflate(R.layout.edit_dialog_radiobutton, parent);
            Holder holder = new Holder();
            holder.radioButton = (RadioButton) convertView.findViewById(R.id.editor_check);
            convertView.setTag(holder);
        }

        Holder holder = (Holder) convertView.getTag();
        holder.radioButton.setOnCheckedChangeListener(null);

        holder.radioButton.setText(item);
        holder.radioButton.setChecked(TextUtils.equals(item, mFormItem.value));
        holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFormItem.dataId = mIds[position];
                    mFormItem.value = item;
                }
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    private static class Holder {
        RadioButton radioButton;
    }
}
