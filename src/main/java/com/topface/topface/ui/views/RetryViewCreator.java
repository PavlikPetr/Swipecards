package com.topface.topface.ui.views;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.IllustratedTextView.IllustratedTextView;
import com.topface.topface.R;

import java.util.ArrayList;
import java.util.List;

public class RetryViewCreator {
    public static final String REFRESH_TEMPLATE = "{{refresh}} ";
    private static int buttonsOrientation = LinearLayout.HORIZONTAL;

    private View mRetryView;
    private InnerButton[] mBtns;

    public static class Builder {

        private Context mContext;
        private String mMessage;
        private List<String> mTitles = new ArrayList<>();
        private List<View.OnClickListener> mListeners = new ArrayList<>();
        private int mOrientation = buttonsOrientation;
        private Integer mBackgroungColor;
        private Integer mMessageFontColor;
        private Boolean mNoShadow = false;

        public Builder(Context context, View.OnClickListener listener) {
            mContext = context;
            mListeners.add(listener);
            mTitles.add(mContext.getString(R.string.general_dialog_retry));
            mMessage = context.getString(R.string.general_data_error);
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Builder button(String title, View.OnClickListener listener) {
            mTitles.add(title);
            mListeners.add(listener);
            return this;
        }

        public Builder orientation(int orientation) {
            mOrientation = orientation;
            return this;
        }

        public Builder backgroundColor(Integer backgroundColor) {
            mBackgroungColor = backgroundColor;
            return this;
        }

        public Builder messageFontColor(@ColorRes int messageFontColor) {
            mMessageFontColor = mContext.getResources().getColor(messageFontColor);
            return this;
        }

        public Builder noShadow() {
            mNoShadow = true;
            return this;
        }

        public RetryViewCreator build() {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup retryView = (ViewGroup) mInflater.inflate(R.layout.layout_retryview, null);
            if (mBackgroungColor != null) {
                retryView.setBackgroundColor(mBackgroungColor);
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
            retryView.setLayoutParams(params);

            TextView message = (TextView) retryView.findViewById(R.id.tvMessage);
            if (mMessage != null) {
                message.setText(mMessage);
            } else {
                message.setVisibility(View.GONE);
            }
            if (mMessageFontColor != null) {
                message.setTextColor(mMessageFontColor);
            }
            if (mNoShadow) {
                message.setShadowLayer(0, 0, 0, 0);
            }

            LinearLayout buttonsContainer = (LinearLayout) retryView.findViewById(R.id.loButtonsContainer);
            //noinspection ResourceType
            buttonsContainer.setOrientation(mOrientation);

            List<InnerButton> btns = new ArrayList<>(mTitles.size());

            btns.add(new InnerButton(InnerButton.Type.GRAY, mTitles.get(0), mListeners.isEmpty() ? null : mListeners.get(0)));
            for (int i = 1; i < mTitles.size(); i++) {
                btns.add(new InnerButton(InnerButton.Type.GRAY, mTitles.get(i), mListeners.size() > i ? mListeners.get(i) : null));
            }
            for (InnerButton btn : btns) {
                if (btn != null) buttonsContainer.addView(btn.createButtonView(retryView));
            }

            return new RetryViewCreator(retryView, btns.toArray(new InnerButton[btns.size()]));
        }

    }

    private RetryViewCreator(View retryView, InnerButton[] btns) {
        mRetryView = retryView;
        mBtns = btns;
    }

    public void setText(String text) {
        if (mRetryView != null) {
            ((TextView) mRetryView.findViewById(R.id.tvMessage)).setText(text);
        }
    }

    public void setListener(View.OnClickListener listener) {
        if (mBtns != null && mBtns.length > 0) mBtns[0].setListener(listener);
    }

    public View getView() {
        return mRetryView;
    }

    @SuppressWarnings("MagicConstant")
    public void setVisibility(int visibility) {
        if (mRetryView != null) mRetryView.setVisibility(visibility);
    }

    public void performClick() {
        if (mBtns != null && mBtns.length > 0) mBtns[0].performClick();
    }

    public void setButtonText(String btnText) {
        if (mBtns != null && mBtns.length > 0) mBtns[0].setText(btnText);
    }

    @SuppressWarnings("unused")
    public void showOnlyMessage(boolean isVisible) {
        if (mRetryView != null) {
            mRetryView.findViewById(R.id.tvMessage).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void showRetryButton(boolean showRetry) {
        if (mRetryView != null && mBtns != null && mBtns.length > 0) {
            mBtns[0].setVisibility(showRetry ? View.VISIBLE : View.GONE);
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
