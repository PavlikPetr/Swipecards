package com.topface.topface.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.topface.topface.R;

public class RetryView extends  LinearLayout{
    private Button mRetryBtn;
    private TextView mErrorMsg;

    public RetryView(Context context) {
        super(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT,1);
        setLayoutParams(params);
    }

    public void init(LayoutInflater inflater) {
        inflater.inflate(R.layout.retry_btn,this);
        mRetryBtn = (Button)findViewById(R.id.retry);
        mErrorMsg = (TextView)findViewById(R.id.err_msg);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mRetryBtn.setOnClickListener(l);
    }

    public void setErrorMsg(String errorMsg) {
        mErrorMsg.setText(errorMsg);
    }
}
