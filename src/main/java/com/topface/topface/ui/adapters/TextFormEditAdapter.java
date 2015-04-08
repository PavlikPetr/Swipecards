package com.topface.topface.ui.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.topface.topface.R;
import com.topface.topface.utils.FormItem;

/**
 * Adapter for text forms edit dialog
 */
public class TextFormEditAdapter extends AbstractEditAdapter<FormItem> {

    private FormItem mFormItem;
    private FormItem mOriginalItem;
    private boolean mIsSaved;

    public TextFormEditAdapter(Context context, FormItem formItem) {
        super(context);
        mOriginalItem = formItem;
        mFormItem = new FormItem(formItem);
    }

    @Override
    public FormItem getData() {
        return mIsSaved ? mFormItem : mOriginalItem;
    }

    @Override
    public void saveData() {
        mIsSaved = true;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public String getItem(int position) {
        return mFormItem.value;
    }

    @Override
    public long getItemId(int position) {
        return mFormItem.titleId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String value = getItem(position);

        if (convertView == null) {
            convertView = inflate(parent);

            Holder holder = new Holder();
            holder.text = (EditText) convertView.findViewById(R.id.city_search);
            holder.textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mFormItem.value = s.toString();
                }
            };
            convertView.setTag(holder);
        }

        Holder holder = (Holder) convertView.getTag();

        holder.text.removeTextChangedListener(holder.textWatcher);
        holder.text.setText(value);
        holder.text.setSelection(value.length());
        holder.text.addTextChangedListener(holder.textWatcher);

        return convertView;
    }

    private static class Holder {
        EditText text;
        TextWatcher textWatcher;
    }

    protected int getItemLayoutRes() {
        return R.layout.edit_dialog_text;
    }
}
