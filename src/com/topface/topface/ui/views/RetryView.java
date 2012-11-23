package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.topface.topface.R;

public class RetryView extends LinearLayout {
    public static final String REFRESH_TEMPLATE = "{{refresh}} ";
    private IllustratedTextView mBtn1;
    private IllustratedTextView mBtn2;
    private int textColor = Color.parseColor("#B8B8B8");
    private TextView mErrorMsg;
    private LinearLayout mButtonContainer;
    private boolean showOnlyMessage;

    public RetryView(Context context) {
        super(context);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
        setLayoutParams(params);
        setOrientation(VERTICAL);

        setTextViewSettings();
        addView(mErrorMsg);

        addView(initButtonContainer());
    }

    public void setErrorMsg(String errorMsg) {
        mErrorMsg.setText(errorMsg);
    }

    public void addButton(String title, OnClickListener listener) {
        if(mBtn1 == null) {
            mBtn1 = generateButton();
            mBtn1.setText(title);
            mBtn1.setOnClickListener(listener);
            mButtonContainer.addView(mBtn1);
        } else if(mBtn2 == null) {
            mBtn2 = generateButton();
            mBtn2.setText(title);
            mBtn2.setOnClickListener(listener);
            mButtonContainer.addView(mBtn2);
        }
    }

    private IllustratedTextView generateButton() {
        IllustratedTextView btn = new IllustratedTextView(getContext(),null);
        btn.setBackgroundResource(R.drawable.btn_retry_selector);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        lp.setMargins(10,5,10,5);
        btn.setLayoutParams(lp);
        btn.setPadding(15,0,15,0);
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        return btn;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(showOnlyMessage) {
            mButtonContainer.setVisibility(View.GONE);
        }
    }

    public void showOnlyMessage(boolean value) {
        showOnlyMessage = value;
    }

    private void setTextViewSettings() {
        mErrorMsg = new TextView(getContext());

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);
        mErrorMsg.setLayoutParams(layoutParams);

        mErrorMsg.setTextSize(16);
        mErrorMsg.setTextColor(textColor);
        mErrorMsg.setGravity(Gravity.CENTER);
    }

    private RelativeLayout initButtonContainer() {
        RelativeLayout rl = new RelativeLayout(getContext());
        rl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mButtonContainer = new LinearLayout(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mButtonContainer.setLayoutParams(params);
        mButtonContainer.setOrientation(HORIZONTAL);
        rl.addView(mButtonContainer);
        return rl;
    }

}
