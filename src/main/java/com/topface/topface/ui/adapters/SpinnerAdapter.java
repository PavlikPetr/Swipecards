package com.topface.topface.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.topface.topface.R;

import java.util.ArrayList;

public class SpinnerAdapter extends ArrayAdapter<String> {
    private final static String DEFAULT_PREFIX_VALUE = null;
    private Context mContext;
    ArrayList<String> data = null;
    String prefix;

    public SpinnerAdapter(Activity context, int resource, ArrayList<String> data, String prefix) {
        super(context, resource, data);
        this.mContext = context;
        this.data = data;
        this.prefix = prefix;
    }

    public SpinnerAdapter(Activity context, int resource, ArrayList<String> data) {
        super(context, resource, data);
        this.mContext = context;
        this.data = data;
        this.prefix = DEFAULT_PREFIX_VALUE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.spinner_header_text_layout, parent, false);
        }
        TextView textView = (TextView) view;
        if (textView != null) {
            textView.setText(getItem(position));
        }
        return view;
    }

    @Override
    public String getItem(int position) {
        if (TextUtils.isEmpty(prefix)) {
            return super.getItem(position);
        } else {
            return String.format(prefix, super.getItem(position));
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.spinner_text_layout, parent, false);
        }
        String item = data.get(position);
        if (item != null) {
            TextView textView = (TextView) view;
            if (textView != null) {
                textView.setText(item);
            }
        }
        return view;
    }


}