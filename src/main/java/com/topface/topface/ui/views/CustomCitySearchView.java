package com.topface.topface.ui.views;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.ui.adapters.CitySearchAdapter;
import com.topface.topface.utils.Utils;

public class CustomCitySearchView extends EditText {
    private Context mContext;
    private int mListViewId = R.layout.filter_dialog_layout;
    private String mListViewTag = "loFilterList";
    private View mLayoutView;
    private ListView mListView;
    private onCityClickListener mOnCityClickListener;
    private CitySearchAdapter mAdapter;


    public CustomCitySearchView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CustomCitySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public CustomCitySearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null) {
                    if (s.length() > 2)
                        mAdapter.setSearchPhrase(s.toString());
                    else {
                        mAdapter.shortSearchPhrase();
                    }
                }
            }
        });
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDropDownView();
            }
        });
    }

    public void setListViewLayout(int id) {
        mListViewId = id;
    }

    public void setListViewTag(String tag) {
        mListViewTag = tag;
    }

    private View getLayoutView() {
        if (mLayoutView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mLayoutView = inflater.inflate(mListViewId, null);
        }
        return mLayoutView;
    }

    private ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) getLayoutView().findViewWithTag(mListViewTag);
        }
        return mListView;
    }

    private void showDropDownView() {
        mAdapter = new CitySearchAdapter(mContext, R.layout.spinner_text_layout, this.getText().toString());
        getListView().setAdapter(mAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(getLayoutView());
        final Dialog dialog = builder.create();
        dialog.show();
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = position - getListView().getHeaderViewsCount();
                if (pos >= 0) {
                    City city = ((CitySearchAdapter) getListView().getAdapter()).getCityByPosition(pos);
                    CustomCitySearchView.this.setText(city.getName());
                    dialog.dismiss();
                    Utils.hideSoftKeyboard(mContext, CustomCitySearchView.this);
                    if (mOnCityClickListener != null) {
                        mOnCityClickListener.onCityClick(city);
                    }
                }
            }
        });
    }

    public interface onCityClickListener {
        public void onCityClick(City city);
    }

    public void setOnCityClickListener(onCityClickListener listener) {
        mOnCityClickListener = listener;
    }

}