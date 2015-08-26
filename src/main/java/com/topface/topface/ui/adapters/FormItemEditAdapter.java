package com.topface.topface.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

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

    public FormItemEditAdapter(Context context, FormItem formItem) {
        super(context);
        mFormItem = new FormItem(formItem);
        mFormInfo = new FormInfo(App.getContext(), CacheProfile.getProfile().sex, Profile.TYPE_OWN_PROFILE);
        mEntries = createEntries(mFormItem);
        mIds = createIds(mFormItem);
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
    protected int getItemLayoutRes() {
        return R.layout.edit_dialog_radiobutton;
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
            convertView = inflate(parent);
            Holder holder = new Holder();
            holder.item = (CheckedTextView) convertView.findViewById(R.id.editor_check);
            convertView.setTag(holder);
        }

        final Holder holder = (Holder) convertView.getTag();
        holder.item.setOnClickListener(null);

        holder.item.setText(item);
        holder.item.setChecked(TextUtils.equals(item, mFormItem.value));
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFormItem.dataId = mIds[position];
                mFormItem.value = item;
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    protected String[] createEntries(FormItem formItem) {
        return mFormInfo.getEntriesByTitleId(formItem.titleId);
    }

    protected int[] createIds(FormItem formItem) {
        return mFormInfo.getIdsByTitleId(formItem.titleId);
    }

    private static class Holder {
        CheckedTextView item;
    }
}
