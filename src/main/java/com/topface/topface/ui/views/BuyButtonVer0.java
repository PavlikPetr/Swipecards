package com.topface.topface.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;

/**
 * Created by ppetr on 24.02.16.
 * Custom button for purchase screen version 0 (base)
 */
public class BuyButtonVer0 extends BuyButton {

    private RelativeLayout mCotainer;
    private TextView mText;
    private ProgressBar mProgress;

    public BuyButtonVer0(Context context) {
        this(context, (BuyButtonBuilder) null);
    }

    public BuyButtonVer0(Context context, BuyButtonBuilder builder) {
        super(context);
        init();
        if (builder != null) {
            setButtonCondition(builder.mHasDiscount, builder.mShowType);
            setTitle(builder.mTitle);
            setOnClickListener(builder.mButtonClickListener);
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        if (mCotainer != null) {
            mCotainer.setOnClickListener(listener);
        }
    }

    public void setButtonCondition(boolean isDiscount, int showType) {
        if (isDiscount && mCotainer != null) {
            int paddingFive = Utils.getPxFromDp(5);
            mCotainer.setPadding(paddingFive, paddingFive, Utils.getPxFromDp(56), paddingFive);
        }
        setBuyButtonTextColor(showType, mText);
        setBuyButtonBackground(isDiscount, showType, mCotainer);
    }

    private void setSelectorTextColor(int selector, TextView view) {
        try {
            XmlResourceParser xrp = App.getContext().getResources().getXml(selector);
            ColorStateList csl = ColorStateList.createFromXml(App.getContext().getResources(), xrp);
            view.setTextColor(csl);
        } catch (Exception e) {
            Debug.error(e.toString());
        }
    }

    private void setBuyButtonTextColor(int showType, TextView view) {
        if (view == null) {
            return;
        }
        switch (showType) {
            case 1:
                setSelectorTextColor(R.drawable.btn_blue_text_color_selector, view);
                break;
            case 2:
                view.setTextColor(App.getContext().getResources().getColor(R.color.button_blue_text_disable_color));
                break;
            default:
                setSelectorTextColor(R.drawable.btn_gray_text_color_selector, view);
                break;
        }
    }

    public void setTitle(String text) {
        if (mText != null) {
            mText.setText(text);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setBuyButtonBackground(boolean discount, int showType, View view) {
        if (view == null) {
            return;
        }
        int bgResource;
        switch (showType) {
            case 1:
                bgResource = discount ? R.drawable.btn_sale_blue_selector : R.drawable.btn_blue_selector;
                break;
            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bgResource = discount ? R.drawable.btn_sale_blue_disabled_only : R.drawable.btn_blue_disabled_only;
                } else {
                    bgResource = discount ? R.drawable.btn_sale_blue_disabled : R.drawable.btn_blue_shape_disabled;
                }
                break;
            default:
                bgResource = discount ? R.drawable.btn_sale_gray_selector : R.drawable.btn_gray_selector;
                break;
        }
        view.setBackgroundResource(bgResource);
    }

    public BuyButtonVer0(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        getAttrs(context, attrs, 0);
    }

    public BuyButtonVer0(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        getAttrs(context, attrs, defStyleAttr);
    }

    @Override
    public void startWaiting() {

    }

    @Override
    public void stopWaiting() {

    }

    private void init() {
        inflate(getContext(), R.layout.item_buying_btn, this);
        mCotainer = (RelativeLayout) findViewById(R.id.itContainer);
        mText = (TextView) findViewById(R.id.itText);
        mProgress = (ProgressBar) findViewById(R.id.marketWaiter);
    }

    private void getAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BuyButtonVer1, defStyle, 0);
        a.recycle();
    }

    public static class BuyButtonBuilder {
        private String mTitle;
        private boolean mHasDiscount;
        private int mShowType;
        private OnClickListener mButtonClickListener;

        public BuyButtonBuilder title(String title) {
            mTitle = title;
            return this;
        }

        public BuyButtonBuilder discount(boolean discount) {
            mHasDiscount = discount;
            return this;
        }

        public BuyButtonBuilder showType(int showType) {
            mShowType = showType;
            return this;
        }

        public BuyButtonBuilder onClick(OnClickListener listener) {
            mButtonClickListener = listener;
            return this;
        }

        public BuyButtonVer0 build(Context context) {
            return new BuyButtonVer0(context, this);
        }
    }

}
