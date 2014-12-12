package com.topface.topface.ui.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.topface.topface.R;

import java.util.ArrayList;

public class SpinnerAgeAdapter extends ArrayAdapter<String> {
    private final static int DEFAULT_TEXT_COLOR = Color.parseColor("#333333");
    private final static String DEFAULT_PREFIX_VALUE = null;
    private Activity context;
    ArrayList<String> data = null;
    String prefix;
    int textColor;

    public SpinnerAgeAdapter(Activity context, int resource, ArrayList<String> data, int textColor, String prefix) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
        this.prefix = prefix;
        this.textColor = textColor;
    }

    public SpinnerAgeAdapter(Activity context, int resource, ArrayList<String> data, int textColor) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
        this.prefix = DEFAULT_PREFIX_VALUE;
        this.textColor = textColor;
    }

    public SpinnerAgeAdapter(Activity context, int resource, ArrayList<String> data, String prefix) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
        this.prefix = prefix;
        this.textColor = DEFAULT_TEXT_COLOR;
    }

    public SpinnerAgeAdapter(Activity context, int resource, ArrayList<String> data) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
        this.prefix = DEFAULT_PREFIX_VALUE;
        this.textColor = DEFAULT_TEXT_COLOR;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
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
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.spinner_text_layout, parent, false);
        }
        String item = data.get(position);
        if (item != null) {
            TextView textView = (TextView) view;
            if (textView != null)
                textView.setPadding(getPxFromDp(8), getPxFromDp(15), 0, getPxFromDp(15));
            textView.setTextColor(textColor);
            textView.setText(item);
        }
        return view;
    }

    private int getPxFromDp(int dp) {
        double density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }
}