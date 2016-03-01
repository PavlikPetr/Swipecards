package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;

import com.topface.topface.R;
import com.topface.topface.databinding.BuyButtonVer2Binding;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ppetr on 24.02.16.
 * Custom button for purchase screen version 1
 */
public class BuyButtonVer2 extends BuyButton<BuyButtonVer2.BuyButtonBuilder> {

    public static final int BUTTON_TYPE_BLUE = 0;
    public static final int BUTTON_TYPE_GREEN = 1;

    public static final int STICKER_TYPE_NONE = 0;
    public static final int STICKER_TYPE_POPULAR = 1;
    public static final int STICKER_TYPE_BEST_VALUE = 2;

    private BuyButtonVer2Binding mBinding;
    private BuyButtonVersion1Handler mBtnHandler;

    public BuyButtonVer2(Context context) {
        this(context, (BuyButtonBuilder) null);
    }

    public BuyButtonVer2(Context context, BuyButtonBuilder builder) {
        super(context, builder);
    }

    public BuyButtonVer2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BuyButtonVer2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private enum Sticker {
        NONE(STICKER_TYPE_NONE),
        POPULAR(STICKER_TYPE_POPULAR, R.string.buy_button_sticker_popular, R.drawable.sticker_popular_selector),
        BEST_VALUE(STICKER_TYPE_BEST_VALUE, R.string.buy_button_sticker_best_value, R.drawable.sticker_best_value_selector);

        private boolean mIsVisible;
        @DrawableRes
        private int mBackground;
        @StringRes
        private int mTitle;
        @StickerType
        private int mType;

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
        mBtnHandler.progressVisibility.set(View.VISIBLE);
        mBtnHandler.buttonTextVisibility.set(View.INVISIBLE);
        mBtnHandler.buttonVisibility.set(View.VISIBLE);
        mBinding.button.setEnabled(false);
    }

    @Override
    public void stopWaiting() {
        mBtnHandler.progressVisibility.set(View.GONE);
        mBtnHandler.buttonTextVisibility.set(View.VISIBLE);
        mBtnHandler.buttonVisibility.set(View.VISIBLE);
        mBinding.button.setEnabled(true);
    }

    @Override
    int getButtonLayout() {
        return R.layout.buy_button_ver_2;
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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BuyButtonVer2, defStyle, 0);
        setType(getTypeFromAttr(a.getInt(R.styleable.BuyButtonVer2_buy_button_ver_1_type, BUTTON_TYPE_BLUE)));
        setStickerType(getStickerTypeFromAttr(a.getInt(R.styleable.BuyButtonVer2_buy_button_ver_1_sticker_type, STICKER_TYPE_NONE)));
        setButtonTitle(a.getString(R.styleable.BuyButtonVer2_buy_button_ver_1_title));
        setDescription(a.getString(R.styleable.BuyButtonVer2_buy_button_ver_1_discount),
                a.getString(R.styleable.BuyButtonVer2_buy_button_ver_1_pricePerItem),
                a.getString(R.styleable.BuyButtonVer2_buy_button_ver_1_totalPrice));
        a.recycle();
    }

    /**
     * Set buy button title
     *
     * @param title buy button title value
     */
    public void setButtonTitle(String title) {
        mBtnHandler.buttonTextVisibility.set(View.VISIBLE);
        mBtnHandler.buttonText.set(title);
    }

    /**
     * Fill all descriptions view
     *
     * @param discount     products discount value
     * @param pricePerItem products price per period value
     * @param totalPrice   products price value
     */
    public void setDescription(String discount, String pricePerItem, String totalPrice) {
        boolean isDescriptionEnable = pricePerItem != null && totalPrice != null;
        mBtnHandler.buttonDescriptionVisibility.set(isDescriptionEnable ? View.VISIBLE : View.INVISIBLE);
        if (isDescriptionEnable) {
            mBtnHandler.discountVisibility.set(View.VISIBLE);
            mBtnHandler.discountText.set(discount);
            mBtnHandler.pricePerItemVisibility.set(View.VISIBLE);
            mBtnHandler.pricePerItemText.set(pricePerItem);
            mBtnHandler.totalPriceVisibility.set(View.VISIBLE);
            mBtnHandler.totalPriceText.set(totalPrice);
        }
    }

    /**
     * Choose button background color
     *
     * @param type One of {@link #BUTTON_TYPE_BLUE} or {@link #BUTTON_TYPE_GREEN}.
     */
    public void setType(@Type int type) {
        mBtnHandler.buttonVisibility.set(View.VISIBLE);
        mBtnHandler.buttonBackgroundRes.set(getButtonRes(type));
    }

    public void setOnClickListener(OnClickListener listener) {
        mBtnHandler.setClickLisktener(listener);
    }

    @Type
    private int getTypeFromAttr(int type) {
        switch (type) {
            case BUTTON_TYPE_GREEN:
                return BUTTON_TYPE_GREEN;
            case BUTTON_TYPE_BLUE:
            default:
                return BUTTON_TYPE_BLUE;

        }
    }

    @StickerType
    private int getStickerTypeFromAttr(int type) {
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
        mBtnHandler.stickerTextVisibility.set(sticker.isVisible() ? View.VISIBLE : View.GONE);
        if (sticker.isVisible()) {
            padding = (int) getContext().getResources().getDimension(R.dimen.sticker_width);
            mBtnHandler.stickerBackgroundRes.set(sticker.getBackground());
            mBtnHandler.stickerText.set(getContext().getString(sticker.getTitle()).toUpperCase());
        }
        mBtnHandler.buttonTextPaddingLeft.set(padding);
        mBtnHandler.buttonTextPaddingRight.set(padding);
    }

    @Retention(value = RetentionPolicy.SOURCE)
    @IntDef({BUTTON_TYPE_BLUE, BUTTON_TYPE_GREEN})
    public @interface Type {
    }

    @IntDef({STICKER_TYPE_NONE, STICKER_TYPE_POPULAR, STICKER_TYPE_BEST_VALUE})
    public @interface StickerType {
    }

    @Override
    void initViews(View root) {
        mBinding = DataBindingUtil.bind(root);
        mBtnHandler = new BuyButtonVersion1Handler();
        mBinding.setHandler(mBtnHandler);
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

        public BuyButtonVer2 build(Context context) {
            return new BuyButtonVer2(context, this);
        }
    }

    public static class BuyButtonVersion1Handler {

        private View.OnClickListener mButtonClickListener;

        public void setClickLisktener(View.OnClickListener listener) {
            mButtonClickListener = listener;
        }

        // Observable fields to control FrameLayout button
        public final ObservableInt buttonVisibility = new ObservableInt(View.VISIBLE);
        public final ObservableInt buttonBackgroundRes = new ObservableInt(R.drawable.btn_blue_selector);

        // Catch click on FrameLayout button
        @SuppressWarnings("unused")
        public void onButtonClick(View view) {
            if (mButtonClickListener != null) {
                mButtonClickListener.onClick(view);
            }
        }

        // Observable fields to control TextView buttonTitle
        public final ObservableInt buttonTextVisibility = new ObservableInt();
        public final ObservableField<String> buttonText = new ObservableField<>();
        public final ObservableInt buttonTextPaddingLeft = new ObservableInt(0);
        public final ObservableInt buttonTextPaddingRight = new ObservableInt(0);

        // Observable fields to control TextView buttonSticker
        public final ObservableInt stickerTextVisibility = new ObservableInt(View.GONE);
        public final ObservableInt stickerBackgroundRes = new ObservableInt(R.drawable.btn_blue_selector);
        public final ObservableField<String> stickerText = new ObservableField<>();

        // Observable field to control LinearLayout buttonDetails visibility
        public final ObservableInt buttonDescriptionVisibility = new ObservableInt(View.GONE);

        // Observable fields to control TextView discount
        public final ObservableInt discountVisibility = new ObservableInt(View.GONE);
        public final ObservableField<String> discountText = new ObservableField<>();

        // Observable fields to control TextView pricePerItem
        public final ObservableInt pricePerItemVisibility = new ObservableInt(View.GONE);
        public final ObservableField<String> pricePerItemText = new ObservableField<>();

        // Observable fields to control TextView totalPrice
        public final ObservableInt totalPriceVisibility = new ObservableInt(View.GONE);
        public final ObservableField<String> totalPriceText = new ObservableField<>();

        // Observable field to control ProgressBar marketWaiter visibility
        public final ObservableInt progressVisibility = new ObservableInt(View.GONE);
    }
}
