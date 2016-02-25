package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ppetr on 24.02.16.
 * Custom button for purchase screen version 1
 */
public class BuyButtonVer1 extends BuyButton<BuyButtonVer1.BuyButtonBuilder> {

    public static final int BUTTON_TYPE_BLUE = 0;
    public static final int BUTTON_TYPE_GREEN = 1;

    public static final int STICKER_TYPE_NONE = 0;
    public static final int STICKER_TYPE_POPULAR = 1;
    public static final int STICKER_TYPE_BEST_VALUE = 2;

    private FrameLayout mButtonView;
    private TextView mButtonTitle;
    private TextView mButtonSticker;
    private LinearLayout mButtonDescription;
    private TextView mDescriptionDicscount;
    private TextView mDescriptionPricePerItem;
    private TextView mDescriptionTotalPrice;
    private ProgressBar mProgress;

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

    private enum Sticker {
        NONE(STICKER_TYPE_NONE),
        POPULAR(STICKER_TYPE_POPULAR, R.string.buy_button_sticker_popular, R.drawable.sticker_popular_selector),
        BEST_VALUE(STICKER_TYPE_BEST_VALUE, R.string.buy_button_sticker_best_value, R.drawable.sticker_best_value_selector);

        private boolean mIsVisible;
        private
        @DrawableRes
        int mBackground;
        private
        @StringRes
        int mTitle;
        private
        @StickerType
        int mType;

        Sticker(@StickerType int type, @StringRes int title, @DrawableRes int background) {
            mType = type;
            mIsVisible = true;
            mTitle = title;
            mBackground = background;
        }

        Sticker(@StickerType int type) {
            this(type, 0, 0);
            mIsVisible = false;
        }

        @DrawableRes
        public int getBackground() {
            return mBackground;
        }

        public boolean isVisible() {
            return mIsVisible;
        }

        @StringRes
        public int getTitle() {
            return mTitle;
        }

        @StickerType
        public int getType() {
            return mType;
        }
    }

    @DrawableRes
    private int getButtonRes(@Type int type) {
        switch (type) {
            case BUTTON_TYPE_GREEN:
                return R.drawable.btn_green_selector;
            case BUTTON_TYPE_BLUE:
            default:
                return R.drawable.btn_blue_selector;
        }
    }

    private Sticker getStickerByType(@StickerType int type) {
        Sticker sticker = Sticker.NONE;
        for (Sticker value : Sticker.values()) {
            if (value.getType() == type) {
                sticker = value;
            }
        }
        return sticker;
    }

    @Override
    public void startWaiting() {
        setViewVisibility(mProgress, View.VISIBLE);
        setViewVisibility(mButtonTitle, View.INVISIBLE);
        if (setViewVisibility(mButtonView, View.VISIBLE)) {
            mButtonView.setEnabled(false);
        }
    }

    @Override
    public void stopWaiting() {
        setViewVisibility(mProgress, View.GONE);
        setViewVisibility(mButtonTitle, View.VISIBLE);
        if (setViewVisibility(mButtonView, View.VISIBLE)) {
            mButtonView.setEnabled(true);
        }
    }

    @Override
    int getButtonLayout() {
        return R.layout.buy_button_ver_1;
    }

    @Override
    void build(@NotNull BuyButtonBuilder builder) {
        setButtonTitle(builder.mTitle);
        setType(builder.mButtonBgType);
        setStickerType(builder.mStickerType);
        setDescription(builder.mDiscount, builder.mPricePerItem, builder.mTotalPrice);
        setOnClickListener(builder.mButtonClickListener);
    }

    @Override
    void getAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BuyButtonVer1, defStyle, 0);
        setType(getTypeFromAttr(a.getInt(R.styleable.BuyButtonVer1_buy_button_ver_1_type, BUTTON_TYPE_BLUE)));
        setStickerType(getStickerTypeFromAttr(a.getInt(R.styleable.BuyButtonVer1_buy_button_ver_1_sticker_type, STICKER_TYPE_NONE)));
        setButtonTitle(a.getString(R.styleable.BuyButtonVer1_buy_button_ver_1_title));
        setDescription(a.getString(R.styleable.BuyButtonVer1_buy_button_ver_1_discount),
                a.getString(R.styleable.BuyButtonVer1_buy_button_ver_1_pricePerItem),
                a.getString(R.styleable.BuyButtonVer1_buy_button_ver_1_totalPrice));
        a.recycle();
    }

    /**
     * Set buy button title
     *
     * @param title buy button title value
     */
    public void setButtonTitle(String title) {
        setText(mButtonTitle, title);
    }

    /**
     * Fill all descriptions view
     *
     * @param discount     products discount value
     * @param pricePerItem products price per period value
     * @param totalPrice   products price value
     */
    public void setDescription(String discount, String pricePerItem, String totalPrice) {
        boolean isDescriptionEnable = discount != null && pricePerItem != null && totalPrice != null;
        if (setViewVisibility(mButtonDescription, isDescriptionEnable ? View.VISIBLE : View.INVISIBLE)) {
            if (isDescriptionEnable) {
                setText(mDescriptionDicscount, discount);
                setText(mDescriptionPricePerItem, pricePerItem);
                setText(mDescriptionTotalPrice, totalPrice);
            }
        }
    }

    /**
     * Choose button background color
     *
     * @param type One of {@link #BUTTON_TYPE_BLUE} or {@link #BUTTON_TYPE_GREEN}.
     */
    public void setType(@Type int type) {
        if (setViewVisibility(mButtonView, View.VISIBLE)) {
            mButtonView.setBackgroundResource(getButtonRes(type));
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        if (mButtonView != null) {
            mButtonView.setOnClickListener(listener);
        }
    }

    private
    @Type
    int getTypeFromAttr(int type) {
        switch (type) {
            case BUTTON_TYPE_GREEN:
                return BUTTON_TYPE_GREEN;
            case BUTTON_TYPE_BLUE:
            default:
                return BUTTON_TYPE_BLUE;

        }
    }

    private
    @StickerType
    int getStickerTypeFromAttr(int type) {
        switch (type) {
            case STICKER_TYPE_POPULAR:
                return STICKER_TYPE_POPULAR;
            case STICKER_TYPE_BEST_VALUE:
                return STICKER_TYPE_BEST_VALUE;
            default:
                return STICKER_TYPE_NONE;

        }
    }

    /**
     * Set sticker over the product button
     *
     * @param type One of {@link #STICKER_TYPE_NONE}, {@link #STICKER_TYPE_POPULAR} or {@link #STICKER_TYPE_BEST_VALUE}.
     */
    public void setStickerType(@StickerType int type) {
        Sticker sticker = getStickerByType(type);
        int padding = 0;
        if (setViewVisibility(mButtonSticker, sticker.isVisible() ? View.VISIBLE : View.GONE)) {
            if (sticker.isVisible()) {
                padding = (int) getContext().getResources().getDimension(R.dimen.sticker_width);
                mButtonSticker.setBackgroundResource(sticker.getBackground());
                mButtonSticker.setText(getContext().getString(sticker.getTitle()).toUpperCase());
            }
        }
        if (mButtonTitle != null) {
            mButtonTitle.setPadding(padding, 0, padding, 0);
        }
    }

    @IntDef({BUTTON_TYPE_BLUE, BUTTON_TYPE_GREEN})
    public @interface Type {
    }

    @IntDef({STICKER_TYPE_NONE, STICKER_TYPE_POPULAR, STICKER_TYPE_BEST_VALUE})
    public @interface StickerType {
    }

    @Override
    protected void initViews() {
        super.initViews();
        mButtonView = (FrameLayout) findViewById(R.id.button);
        mButtonTitle = (TextView) findViewById(R.id.buttonTitle);
        mButtonSticker = (TextView) findViewById(R.id.buttonSticker);
        mButtonDescription = (LinearLayout) findViewById(R.id.buttonDetails);
        mDescriptionDicscount = (TextView) findViewById(R.id.discount);
        mDescriptionPricePerItem = (TextView) findViewById(R.id.pricePerItem);
        mDescriptionTotalPrice = (TextView) findViewById(R.id.totalPrice);
        mProgress = (ProgressBar) findViewById(R.id.marketWaiter);
    }

    public static class BuyButtonBuilder {
        private int mButtonBgType = BUTTON_TYPE_BLUE;
        private int mStickerType = STICKER_TYPE_NONE;
        private String mTitle;
        private String mDiscount;
        private String mPricePerItem;
        private String mTotalPrice;
        private OnClickListener mButtonClickListener;

        public BuyButtonBuilder title(String title) {
            mTitle = title;
            return this;
        }

        public BuyButtonBuilder discount(String discount) {
            mDiscount = discount;
            return this;
        }

        public BuyButtonBuilder pricePerItem(String pricePerItem) {
            mPricePerItem = pricePerItem;
            return this;
        }

        public BuyButtonBuilder totalPrice(String totalPrice) {
            mTotalPrice = totalPrice;
            return this;
        }

        public BuyButtonBuilder type(@Type int type) {
            mButtonBgType = type;
            return this;
        }

        public BuyButtonBuilder stickerType(@StickerType int type) {
            mStickerType = type;
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

}
