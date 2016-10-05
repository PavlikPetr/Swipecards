package com.topface.topface.ui.fragments.buy;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.topface.topface.App;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.Products;
import com.topface.topface.data.ProductsDetails;
import com.topface.topface.ui.views.BuyButtonVer1;
import com.topface.topface.utils.CacheProfile;

import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by ppetr on 26.02.16.
 * Create list of purchases buttons
 */
public class PurchaseButtonList {

    private ArrayList<ValidatedAndDeffProductPrice> mValidatedAndDeffProductPrices;
    private List<BuyButtonData> mAllButtonArray;


    public ArrayList<View> getButtonsListView(LinearLayout root, List<BuyButtonData> buttons,
                                              Context context, BuyButtonClickListener listener) {
        mAllButtonArray = buttons;
        getAllProductsPrices();
        ArrayList<View> allViews = new ArrayList<>();
        View tempView;
        for (BuyButtonData buyBtn : buttons) {
            tempView = getButtonView(buyBtn, context, listener);
            if (tempView != null) {
                root.addView(tempView);
                tempView.setTag(buyBtn);
                allViews.add(tempView);
            }
        }
        return allViews;
    }

    @Nullable
    private View getButtonView(BuyButtonData buyBtn, Context context, BuyButtonClickListener listener) {
                return getViewV1(buyBtn, context, listener);
    }

    private ValidatedAndDeffProductPrice getCurrentProductPrices(BuyButtonData buyBtn) {
        ValidatedAndDeffProductPrice currentProductPrice = null;
        if (mValidatedAndDeffProductPrices != null) {
            for (ValidatedAndDeffProductPrice price : mValidatedAndDeffProductPrices) {
                if (price.getId().equals(buyBtn.id)) {
                    currentProductPrice = price;
                }
            }
        }
        if (currentProductPrice == null) {
            currentProductPrice = getProductPrices(buyBtn);
        }
        return currentProductPrice;
    }

    @Nullable
    private View getViewV1(final BuyButtonData buyBtn,
                           Context context, final BuyButtonClickListener listener) {
        String value;
        if (buyBtn.type == Products.ProductType.COINS_SUBSCRIPTION && buyBtn.price == 0) {
            value = buyBtn.hint;
        } else {
            ValidatedAndDeffProductPrice validatedAndDeffProductPrice = getCurrentProductPrices(buyBtn);
            value = validatedAndDeffProductPrice.getValidatedPrice() != null ?
                    formatPrice(validatedAndDeffProductPrice.getValidatedPrice().getPrice(), validatedAndDeffProductPrice.getValidatedPrice().getCurrencyFormat(), buyBtn) :
                    formatPrice(validatedAndDeffProductPrice.getDeffPrice().getPrice(), validatedAndDeffProductPrice.getDeffPrice().getCurrencyFormat(), buyBtn);
        }
        if (context == null) {
            return null;
        }
        return new BuyButtonVer1.BuyButtonBuilder().discount(buyBtn.discount > 0).showType(buyBtn.showType).title(TextUtils.isEmpty(value) ? buyBtn.title : value).onClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(buyBtn.id, buyBtn);
                }
            }
        }).build(context);
    }

    private String formatPrice(double price, NumberFormat currencyFormatter, BuyButtonData buyBtn) {
        price = getPriceByTemplate(price, buyBtn);
        for (String replaceTemplate : Products.PRICE_TEMPLATES) {
            if (buyBtn.titleTemplate.contains(replaceTemplate)) {
                return buyBtn.titleTemplate.replace(replaceTemplate, getFormatedPrice(price, currencyFormatter));
            }
        }
        return buyBtn.title;
    }

    private double getPriceByTemplate(double price, BuyButtonData buyBtn) {
        if (buyBtn.titleTemplate.contains((Products.PRICE_PER_ITEM))) {
            return price / buyBtn.amount;
        } else {
            return price;
        }
    }

 private ValidatedAndDeffProductPrice getProductPrices(BuyButtonData buyBtn) {
        ProductsDetails productsDetails = CacheProfile.getMarketProductsDetails();
        Currency currency;
        NumberFormat currencyFormatter;
        currency = Currency.getInstance(Products.USD);
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormatter.setCurrency(currency);
        double price = (double) buyBtn.price / 100;
        ProductPriceData deffPrice = new ProductPriceData(currencyFormatter, price);
        ProductPriceData validatedPrice = null;
        if (productsDetails != null && !TextUtils.isEmpty(buyBtn.totalTemplate)) {
            ProductsDetails.ProductDetail detail = productsDetails.getProductDetail(buyBtn.id);
            if (detail != null && detail.currency != null) {
                price = detail.price / ProductsDetails.MICRO_AMOUNT;
                currency = Currency.getInstance(detail.currency);
                currencyFormatter = detail.currency.equalsIgnoreCase(Products.USD)
                        ? NumberFormat.getCurrencyInstance(Locale.US) : NumberFormat.getCurrencyInstance(new Locale(App.getLocaleConfig().getApplicationLocale()));
                currencyFormatter.setCurrency(currency);
                validatedPrice = new ProductPriceData(currencyFormatter, price);
            }
        }
        buyBtn.currency = currency;
        return new ValidatedAndDeffProductPrice(buyBtn.id, deffPrice, validatedPrice);
    }

    private String getFormatedPrice(double price, NumberFormat currencyFormatter) {
        currencyFormatter.setMaximumFractionDigits(price % 1 != 0 ? 2 : 0);
        return currencyFormatter.format(price);
    }

    private ArrayList<ValidatedAndDeffProductPrice> getAllProductsPrices() {
        if (mValidatedAndDeffProductPrices == null) {
            mValidatedAndDeffProductPrices = getAllProductsPrices(mAllButtonArray);
        }
        return mValidatedAndDeffProductPrices;
    }

    private ArrayList<ValidatedAndDeffProductPrice> getAllProductsPrices(List<BuyButtonData> buttons) {
        ArrayList<ValidatedAndDeffProductPrice> prices = new ArrayList<>();
        if (buttons != null) {
            for (BuyButtonData buyBtn : buttons) {
                prices.add(getProductPrices(buyBtn));
            }
        }
        return prices;
    }

    private class ValidatedAndDeffProductPrice {
        private String mProductId;
        private ProductPriceData mDeffPrice;
        private ProductPriceData mValidatedPrice;

        public ValidatedAndDeffProductPrice(String productId, ProductPriceData deffPrice, ProductPriceData validatedPrice) {
            mProductId = productId;
            mDeffPrice = deffPrice;
            mValidatedPrice = validatedPrice;
        }

        public String getId() {
            return mProductId;
        }

        public ProductPriceData getDeffPrice() {
            return mDeffPrice;
        }

        public ProductPriceData getValidatedPrice() {
            return mValidatedPrice;
        }
    }

    private class ProductPriceData {
        private NumberFormat mCurrencyFormat;
        private double mPrice;


        public ProductPriceData(NumberFormat currencyFormat, double price) {
            mCurrencyFormat = currencyFormat;
            mPrice = price;
        }

        public NumberFormat getCurrencyFormat() {
            return mCurrencyFormat;
        }

        public double getPrice() {
            return mPrice;
        }

    }

    public interface BuyButtonClickListener {
        void onClick(String id, BuyButtonData btnData);
    }
}