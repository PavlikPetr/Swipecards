package com.topface.topface.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.R;

public abstract class BaseMarketApiManager {
    public void setContext(Context context) {
        mContext = context;
    }

    private View.OnClickListener mClickListener;
    private View mView;
    private TextView mTitle;
    private Button mButton;
    private Context mContext;

    public abstract void onResume();

    public abstract View getView();

    public abstract String getMessage();

    public abstract int getResultCode();

    public abstract boolean isServicesAvailable();

    public TextView getTitle() {
        if (mTitle == null) {
            initView();
        }
        return mTitle;
    }

    public Button getButton() {
        if (mButton == null) {
            initView();
        }
        return mButton;
    }

    public View getCurrentView() {
        if (mView == null) {
            initView();
        }
        return mView;
    }

    private void initView() {
        if (mContext == null) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.services_manager_layout, null);
        mTitle = (TextView) mView.findViewById(R.id.loTitle);
        mButton = (Button) mView.findViewById(R.id.loButton);
        mTitle.setVisibility(View.VISIBLE);
        mButton.setVisibility(View.VISIBLE);
        if (mClickListener != null) {
            mButton.setOnClickListener(mClickListener);
        }
    }

    public void setClickListener(View.OnClickListener listener) {
        mClickListener = listener;
    }
}
