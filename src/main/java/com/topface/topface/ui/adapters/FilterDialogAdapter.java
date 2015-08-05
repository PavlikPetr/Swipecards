package com.topface.topface.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.topface.topface.R;

public class FilterDialogAdapter extends ArrayAdapter<String> {
    private static String DEFAULT_CURRENT_VALUE = "";
    private Context mContext;
    private String[] mData = null;
    private String mCurrentField;

    @SuppressWarnings("unused")
    public FilterDialogAdapter(Activity context, int resource, String[] data) {
        super(context, resource, data);
        this.mContext = context;
        this.mData = data;
        mCurrentField = DEFAULT_CURRENT_VALUE;
    }

    public FilterDialogAdapter(Activity context, int resource, String[] data, String currentValue) {
        super(context, resource, data);
        this.mContext = context;
        this.mData = data;
        this.mCurrentField = currentValue;
    }

    @Override
    public int getCount() {
        return mData.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.filter_edit_form_dialog_cell, parent, false);
        }
        String item = mData[position];
        if (item != null) {
            CheckedTextView textView = (CheckedTextView) view;
            if (textView != null) {
                if ((DEFAULT_CURRENT_VALUE.equals(mCurrentField) && position == 0) || item.equals(mCurrentField)) {
                    textView.setChecked(true);
                } else {
                    textView.setChecked(false);
                }
                textView.setText(item);
            }
        }
        return view;
    }
}
