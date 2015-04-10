package com.topface.topface.ui.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.topface.topface.R;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

/**
 * Adapter for text forms edit dialog
 */
public class TextFormEditAdapter extends AbstractEditAdapter<FormItem> {

    private static final int TEXT_FORMS_COUNT = 1;

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
        if (mFormItem.isValueValid()) {
            mIsSaved = true;
        }
    }

    @Override
    public int getCount() {
        return TEXT_FORMS_COUNT;
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
    @SuppressWarnings("unused")
    public View getView(int position, View convertView, ViewGroup parent) {
        String value = getItem(position);

        if (convertView == null) {
            convertView = inflate(parent);

            final Holder holder = new Holder();
            holder.text = (EditText) convertView.findViewById(R.id.edit_dialog_text);
            holder.text.setInputType(FormInfo.getInputType(mOriginalItem));
            holder.text.setImeOptions(EditorInfo.IME_ACTION_DONE);

            FormItem.TextLimitInterface textLimitInterface = mOriginalItem.getTextLimitInterface();
            FormItem.ValueLimitInterface valueLimitInterface = mOriginalItem.getValueLimitInterface();
            InputFilter[] fArray = new InputFilter[1];
            if (textLimitInterface != null) {
                fArray[0] = new InputFilter.LengthFilter(textLimitInterface.getLimit());
                holder.text.setFilters(fArray);
            } else if (valueLimitInterface != null) {
                fArray[0] = new InputFilter.LengthFilter(String.valueOf(valueLimitInterface.getMaxValue()).length()) {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        char[] v = new char[end - start];
                        TextUtils.getChars(source, start, end, v, 0);
                        for (char ch : v) {
                            if (!Character.isDigit(ch)) {
                                ch = 0;
                            }
                        }
                        return super.filter(new String(v), start, end, dest, dstart, dend);
                    }
                };
                holder.text.setFilters(fArray);
            }

            holder.textWatcher = new TextWatcher() {
                private OnDataChangeListener<FormItem> mChangeListener = getDataChangeListener();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mFormItem.value = s.toString();
                    if (mChangeListener != null) {
                        mChangeListener.onDataChanged(mFormItem);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

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
