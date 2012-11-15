package com.topface.topface.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.topface.topface.R;

public class RetryView extends LinearLayout {
    public static final String REFRESH_TEMPLATE = "{{refresh}} ";
    private IllustratedTextView mRetryBtn;
    private TextView mErrorMsg;
    private boolean showOnlyMessage;

    public RetryView(Context context) {
        super(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
        setLayoutParams(params);
    }

    public void init(LayoutInflater inflater) {
        inflater.inflate(R.layout.btn_retry, this);
        mRetryBtn = (IllustratedTextView) findViewById(R.id.retry);
        mRetryBtn.setText(REFRESH_TEMPLATE + mRetryBtn.getText());
        mErrorMsg = (TextView) findViewById(R.id.err_msg);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mRetryBtn.setOnClickListener(l);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(showOnlyMessage) mRetryBtn.setVisibility(View.GONE);
    }

    public void showOnlyMessage(boolean value) {
        showOnlyMessage = value;
    }

    public void setErrorMsg(String errorMsg) {
        mErrorMsg.setText(errorMsg);
    }

}
