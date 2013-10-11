package com.topface.topface.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.IllustratedTextView.IllustratedTextView;
import com.topface.topface.R;
import com.topface.topface.ui.gridlayout.GridLayout;

public class RetryViewCreator {
    public static final String REFRESH_TEMPLATE = "{{refresh}} ";
    private static int buttonsOrientation = LinearLayout.HORIZONTAL;

    private View mRetryView;
    private InnerButton mBtn1;
    private InnerButton mBtn2;

    private RetryViewCreator(View retryView, InnerButton btn1, InnerButton btn2) {
        mRetryView = retryView;
        mBtn1 = btn1;
        mBtn2 = btn2;
    }

    public void setText(String text) {
        if (mRetryView != null) {
            ((TextView)mRetryView.findViewById(R.id.tvMessage)).setText(text);
        }
    }

    public void setListener(View.OnClickListener listener) {
        if (mBtn1 != null) mBtn1.setListener(listener);
    }

    public void setListener(View.OnClickListener listener1, View.OnClickListener listener2) {
        if (mBtn1 != null) mBtn1.setListener(listener1);
        if (mBtn2 != null) mBtn2.setListener(listener2);
    }

    public View getView() {
        return mRetryView;
    }

    public void setVisibility(int visibility) {
        if(mRetryView != null) mRetryView.setVisibility(visibility);
    }

    public void setButtonsOrientation(int buttonsOrientation) {
        this.buttonsOrientation = buttonsOrientation;
    }

    public static RetryViewCreator createRetryView(Context context, String text, InnerButton btn1, InnerButton btn2) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup retryView = (ViewGroup) mInflater.inflate(R.layout.layout_retryview,null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
        retryView.setLayoutParams(params);

        if (text != null) {
            ((TextView)retryView.findViewById(R.id.tvMessage)).setText(text);
        } else {
            retryView.findViewById(R.id.tvMessage).setVisibility(View.GONE);
        }

        LinearLayout buttonsContainer = (LinearLayout)retryView.findViewById(R.id.loButtonsContainer);
        buttonsContainer.setOrientation(buttonsOrientation);
        if (btn1 != null) buttonsContainer.addView(btn1.createButtonView(retryView));
        if (btn2 != null) buttonsContainer.addView(btn2.createButtonView(retryView));
        return new RetryViewCreator(retryView,btn1,btn2);
    }

    public static RetryViewCreator createRetryView(Context context, String text, InnerButton btn) {
        return createRetryView(context, text, btn, null);
    }

    public static RetryViewCreator createDefaultRetryView(Context context, View.OnClickListener listener) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(REFRESH_TEMPLATE).append(context.getString(R.string.general_dialog_retry));
        return createRetryView(
                context,
                context.getString(R.string.general_data_error),
                new InnerButton(InnerButton.Type.GRAY, strBuilder.toString(), listener)
        );
    }

    public static RetryViewCreator createDefaultRetryView(Context context, String text, View.OnClickListener listener,
                                                             String textBtn2, View.OnClickListener listener2, int orientation) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(REFRESH_TEMPLATE).append(context.getString(R.string.general_dialog_retry));
        buttonsOrientation = orientation;
        return createRetryView(
                context,
                text,
                new InnerButton(InnerButton.Type.GRAY, strBuilder.toString(), listener),
                new InnerButton(InnerButton.Type.GRAY,textBtn2,listener2)
        );
    }

    public static RetryViewCreator createBlueButtonRetryView(Context context, String text, String buttonText, View.OnClickListener listener) {
        return createRetryView(
                context,
                text,
                new InnerButton(InnerButton.Type.BLUE, buttonText, listener)
        );
    }

    public static RetryViewCreator createDefaultWithBlueRetryView(Context context, String text, View.OnClickListener listener,
                                                             String textBtn2, View.OnClickListener listener2) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(REFRESH_TEMPLATE).append(context.getString(R.string.general_dialog_retry));
        return createRetryView(
                context,
                text,
                new InnerButton(InnerButton.Type.GRAY, strBuilder.toString(), listener),
                new InnerButton(InnerButton.Type.GRAY,textBtn2,listener2)
        );
    }

    public void performClick() {
        mBtn1.performClick();
    }

    public void setButtonText(String btnText) {
        mBtn1.setText(btnText);
    }

    public void showOnlyMessage(boolean isVisible) {
        if (mRetryView != null) {
            mRetryView.findViewById(R.id.tvMessage).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    private static final class InnerButton {

        public void setText(String btnText) {
            mButtonView.setText(btnText);
        }

        public enum Type {GRAY,BLUE}

        private IllustratedTextView mButtonView;
        private Type mType = Type.GRAY;
        private String mText;
        private View.OnClickListener mListener;

        private InnerButton(Type type, String text, View.OnClickListener listener) {
            mType = type;
            mText = text;
            mListener = listener;
        }

        private View createButtonView(ViewGroup parentView) {
            if (mButtonView == null) {
                switch (mType) {
                    case BLUE :
                        mButtonView = generateBlueButton(parentView);
                        mButtonView.setText(mText);
                        mButtonView.setOnClickListener(mListener);
                        break;
                    case GRAY:
                    default:
                        mButtonView = generateGrayButton(parentView);
                        mButtonView.setText(mText);
                        mButtonView.setOnClickListener(mListener);
                        break;
                }
            }
            return mButtonView;
        }

        public void setListener(View.OnClickListener listener) {
            if(mButtonView != null) mButtonView.setOnClickListener(mListener);
        }

        public void performClick() {
            if (mButtonView != null) mButtonView.performClick();
        }

        private IllustratedTextView generateGrayButton(ViewGroup parent) {
            IllustratedTextView btn = (IllustratedTextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.retry_btn, null);
            btn.ICON_ALIGN = TfImageSpan.ALIGN_BASELINE;
            return btn;
        }

        private IllustratedTextView generateBlueButton(ViewGroup parent) {
            return (IllustratedTextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.retry_btn_blue, null);
        }
    }
}
