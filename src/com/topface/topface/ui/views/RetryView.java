package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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
    private IllustratedTextView mBtnBlue;
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

    public RetryView(Context context, int id) {
        super(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, id);
        setLayoutParams(params);
        setOrientation(VERTICAL);

        setTextViewSettings();
        addView(mErrorMsg);

        addView(initButtonContainer());
    }

    public void setErrorMsg(final String errorMsg) {
        mErrorMsg.post(new Runnable() {
            @Override
            public void run() {
                mErrorMsg.setText(errorMsg);
            }
        });
    }

    public void addButton(String title, OnClickListener listener) {
        if (mBtn1 == null) {
            mBtn1 = generateButton();
            mBtn1.setText(title);
            mBtn1.setOnClickListener(listener);
            mButtonContainer.addView(mBtn1);
        } else if (mBtn2 == null) {
            mBtn2 = generateButton();
            mBtn2.setText(title);
            mBtn2.setOnClickListener(listener);
            mButtonContainer.addView(mBtn2);
        }
    }

    public void setListenerToBtn(OnClickListener l) {
        if(mBtn1 != null) {
            mBtn1.setOnClickListener(null);
            mBtn1.setOnClickListener(l);
        }
    }

    public void addBlueButton(String title, OnClickListener listener) {
        if (mBtnBlue == null) {
            mBtnBlue = generateBlueButton();
            mBtnBlue.setText(title);
            mBtnBlue.setOnClickListener(listener);
            mButtonContainer.addView(mBtnBlue);
        }
    }

    public IllustratedTextView getBtn1() {
        return mBtn1;
    }

    public IllustratedTextView getBtn2() {
        return mBtn2;
    }

    private IllustratedTextView generateButton() {
        IllustratedTextView btn = new IllustratedTextView(getContext(), null);
        btn.setBackgroundResource(R.drawable.btn_retry_selector);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 5, 10, 5);
        btn.setLayoutParams(lp);
        btn.setPadding(15, 0, 15, 0);
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        return btn;
    }

    private IllustratedTextView generateBlueButton() {
        IllustratedTextView btn = new IllustratedTextView(getContext(), null);
        btn.setBackgroundResource(R.drawable.btn_vip_super_sale_selector);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 5, 10, 5);
        btn.setLayoutParams(lp);
        btn.setPadding(15, 0, 15, 0);
        btn.setTextAppearance(getContext(), R.style.VipItemTitleWhiteText);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);

        return btn;
    }

    public void setTextToButton1(String text) {
        mBtn1.setText(text);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (showOnlyMessage) {
            mButtonContainer.setVisibility(View.GONE);
        }
    }

    public void showOnlyMessage(boolean value) {
        showOnlyMessage = value;
        if (showOnlyMessage) {
            mButtonContainer.setVisibility(View.GONE);
        } else {
            mButtonContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setTextViewSettings() {
        mErrorMsg = new TextView(getContext());

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mButtonContainer.setLayoutParams(params);
        mButtonContainer.setOrientation(HORIZONTAL);
        rl.addView(mButtonContainer);
        return rl;
    }

}
