package com.topface.topface.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.BuyButtonVer1Binding;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ppetr on 24.02.16.
 * Custom button for purchase screen version 0 (base)
 */
public class BuyButtonVer1 extends BuyButton<BuyButtonVer1.BuyButtonBuilder> {

    private static final int PADDING_FIVE = (int) App.getContext().getResources().getDimension(R.dimen.default_purchase_button_container_padding);

    private BuyButtonVer1Binding mBinding;
    private BuyButtonVersion1Handler mBtnHandler;
    private View.OnClickListener mButtonClickListener;

    public BuyButtonVer1(Context context) {
        this(context, (BuyButtonBuilder) null);
    }

    public BuyButtonVer1(Context context, BuyButtonBuilder builder) {
        super(context, builder);
    }

    public BuyButtonVer1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BuyButtonVer1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnClickListener(OnClickListener listener) {
        mButtonClickListener = listener;
    }

    public void setButtonCondition(boolean isDiscount, int showType) {
        if (isDiscount) {
            mBtnHandler.containerPaddingTop.set((int) getContext().getResources().getDimension(R.dimen.default_purchase_button_container_padding_top));
            mBtnHandler.containerPaddingLeft.set(PADDING_FIVE);
            mBtnHandler.containerPaddingRight.set(PADDING_FIVE);
            mBtnHandler.containerPaddingBottom.set(PADDING_FIVE);
        }
        setBuyButtonTextColor(showType, mBinding.itText);
        setBuyButtonBackground(isDiscount, showType);
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
        mBtnHandler.titleText.set(text);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBuyButtonBackground(boolean discount, int showType) {
        switch (showType) {
            case 1:
                mBtnHandler.containerBackgroundRes.set(discount ? R.drawable.btn_sale_blue_selector : R.drawable.btn_blue_selector);
                return;
            case 2:
                mBtnHandler.containerBackgroundRes.set(discount ? R.drawable.btn_sale_blue_disabled : R.drawable.btn_blue_shape_disabled);
                return;
            default:
                mBtnHandler.containerBackgroundRes.set(discount ? R.drawable.btn_sale_gray_selector : R.drawable.btn_gray_selector);
        }
    }

    @Override
    public void startWaiting() {
        mBtnHandler.titleVisibility.set(View.INVISIBLE);
        mBtnHandler.progressVisibility.set(View.VISIBLE);
        mBtnHandler.containerVisibility.set(View.VISIBLE);
        mBinding.itContainer.setEnabled(false);
    }

    @Override
    public void stopWaiting() {
        mBtnHandler.titleVisibility.set(View.VISIBLE);
        mBtnHandler.progressVisibility.set(View.GONE);
        mBtnHandler.containerVisibility.set(View.VISIBLE);
        mBinding.itContainer.setEnabled(true);
    }

    @Override
    @LayoutRes
    int getButtonLayout() {
        return R.layout.buy_button_ver_1;
    }

    @Override
    void getAttrs(Context context, AttributeSet attrs, int defStyle) {
    }

    @Override
    void build(@NotNull BuyButtonBuilder builder) {
        setButtonCondition(builder.mHasDiscount, builder.mShowType);
        setTitle(builder.mTitle);
        setOnClickListener(builder.mButtonClickListener);
    }

    @Override
    void initViews(View root) {
        mBinding = DataBindingUtil.bind(root);
        mBtnHandler = new BuyButtonVersion1Handler();
        mBinding.setHandler(mBtnHandler);
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

        public BuyButtonVer1 build(Context context) {
            return new BuyButtonVer1(context, this);
        }
    }

    public class BuyButtonVersion1Handler {

        public final ObservableInt containerVisibility = new ObservableInt(View.VISIBLE);
        public final ObservableInt containerBackgroundRes = new ObservableInt();
        public final ObservableInt containerPaddingTop = new ObservableInt(PADDING_FIVE);
        public final ObservableInt containerPaddingBottom = new ObservableInt(PADDING_FIVE);
        public final ObservableInt containerPaddingLeft = new ObservableInt(PADDING_FIVE);
        public final ObservableInt containerPaddingRight = new ObservableInt(PADDING_FIVE);

        public void onButtonClick(View view) {
            if (mButtonClickListener != null) {
                mButtonClickListener.onClick(view);
            }
        }

        public final ObservableInt titleVisibility = new ObservableInt();
        public final ObservableField<String> titleText = new ObservableField<>();

        public final ObservableInt progressVisibility = new ObservableInt(View.GONE);

    }
}
