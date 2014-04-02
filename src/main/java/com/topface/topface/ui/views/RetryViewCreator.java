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

public class RetryViewCreator {
    public static final String REFRESH_TEMPLATE = "{{refresh}} ";
    private static int buttonsOrientation = LinearLayout.HORIZONTAL;

    private View mRetryView;
    private InnerButton mBtn1;

    private RetryViewCreator(View retryView, InnerButton btn1) {
        mRetryView = retryView;
        mBtn1 = btn1;
    }

    private static RetryViewCreator createRetryView(Context context, String text, InnerButton btn1, InnerButton btn2, Integer backgroundColor) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup retryView = (ViewGroup) mInflater.inflate(R.layout.layout_retryview, null);
        if (backgroundColor != null) {
            retryView.setBackgroundColor(backgroundColor);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
        retryView.setLayoutParams(params);

        if (text != null) {
            ((TextView) retryView.findViewById(R.id.tvMessage)).setText(text);
        } else {
            retryView.findViewById(R.id.tvMessage).setVisibility(View.GONE);
        }

        LinearLayout buttonsContainer = (LinearLayout) retryView.findViewById(R.id.loButtonsContainer);
        buttonsContainer.setOrientation(buttonsOrientation);
        if (btn1 != null) buttonsContainer.addView(btn1.createButtonView(retryView));
        if (btn2 != null) buttonsContainer.addView(btn2.createButtonView(retryView));
        return new RetryViewCreator(retryView, btn1);
    }

    private static RetryViewCreator createRetryView(Context context, String text, InnerButton btn, Integer backgroundColor) {
        return createRetryView(context, text, btn, null, backgroundColor);
    }

    public static RetryViewCreator createDefaultRetryView(Context context, View.OnClickListener listener) {
        return createDefaultRetryView(context, listener, null);
    }

    public static RetryViewCreator createDefaultRetryView(Context context, View.OnClickListener listener, Integer backgroundColor) {
        return createRetryView(
                context,
                context.getString(R.string.general_data_error),
                new InnerButton(InnerButton.Type.GRAY, REFRESH_TEMPLATE + context.getString(R.string.general_dialog_retry), listener),
                backgroundColor
        );
    }

    public static RetryViewCreator createDefaultRetryView(Context context, String text, View.OnClickListener listener,
                                                          String textBtn2, View.OnClickListener listener2, int orientation) {
        return createDefaultRetryView(context, text, listener, textBtn2, listener2, orientation, null);
    }

    public static RetryViewCreator createDefaultRetryView(Context context, String text, View.OnClickListener listener,
                                                          String textBtn2, View.OnClickListener listener2, int orientation, Integer backgroundColor) {
        buttonsOrientation = orientation;
        return createRetryView(
                context,
                text,
                new InnerButton(InnerButton.Type.GRAY, REFRESH_TEMPLATE + context.getString(R.string.general_dialog_retry), listener),
                new InnerButton(InnerButton.Type.GRAY, textBtn2, listener2),
                backgroundColor
        );
    }

    public void setText(String text) {
        if (mRetryView != null) {
            ((TextView) mRetryView.findViewById(R.id.tvMessage)).setText(text);
        }
    }

    public void setListener(View.OnClickListener listener) {
        if (mBtn1 != null) mBtn1.setListener(listener);
    }

    public View getView() {
        return mRetryView;
    }

    @SuppressWarnings("MagicConstant")
    public void setVisibility(int visibility) {
        if (mRetryView != null) mRetryView.setVisibility(visibility);
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

    public void showRetryButton(boolean showRetry) {
        if (mRetryView != null && mBtn1 != null) {
            mBtn1.setVisibility(showRetry ? View.VISIBLE : View.GONE);
        }
    }

    public boolean isVisible() {
        return mRetryView != null && mRetryView.getVisibility() == View.VISIBLE;
    }

    private static final class InnerButton {

        private IllustratedTextView mButtonView;
        private Type mType = Type.GRAY;
        private String mText;
        private View.OnClickListener mListener;

        private InnerButton(Type type, String text, View.OnClickListener listener) {
            mType = type;
            mText = text;
            mListener = listener;
        }

        public void setText(String btnText) {
            mButtonView.setText(btnText);
        }

        private View createButtonView(ViewGroup parentView) {
            if (mButtonView == null) {
                switch (mType) {
                    case BLUE:
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
            if (mButtonView != null) mButtonView.setOnClickListener(listener);
        }

        public void setVisibility(int visibility) {
            if (mButtonView != null) {
                mButtonView.setVisibility(visibility);
            }
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

        public enum Type {GRAY, BLUE}
    }
}
