package com.topface.topface.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.topface.topface.R;

public class FilterDialogAdapter extends ArrayAdapter<String> {
    private static String DEFAULT_CURRENT_VALUE = "";
    private Activity context;
    String[] data = null;
    String currentValue;


    public FilterDialogAdapter(Activity context, int resource, String[] data) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
        currentValue = DEFAULT_CURRENT_VALUE;
    }

    public FilterDialogAdapter(Activity context, int resource, String[] data, String currentValue) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
        this.currentValue = currentValue;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.filter_edit_form_dialog_cell, parent, false);
        }
        String item = data[position];
        if (item != null) {
            CheckedTextView textView = (CheckedTextView) view;
            if (textView != null)
                if ((DEFAULT_CURRENT_VALUE.equals(currentValue) && position == 0) || item.equals(currentValue)) {
                    textView.setChecked(true);
                } else {
                    textView.setChecked(false);
                }
            textView.setText(item);
        }
        return view;
    }
}