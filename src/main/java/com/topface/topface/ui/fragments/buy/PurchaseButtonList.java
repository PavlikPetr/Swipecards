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
import com.topface.topface.ui.views.BuyButtonVer2;
import com.topface.topface.utils.CacheProfile;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ppetr on 26.02.16.
 * Create list of purchases buttons
 */
public class PurchaseButtonList {

    private ArrayList<ValidatedAndDeffProductPrice> mValidatedAndDeffProductPrices;
    private List<BuyButtonData> mAllButtonArray;
    private List<BuyButtonData> mNoProductsTrialList;
    private Boolean mIsAllNotTrialProductsValidated;

    private enum ViewsVersions {
        V1("v1"),
        V2("v2");

        private String mVersionName;

        ViewsVersions(String name) {
            mVersionName = name;
        }

        public String getVersionName() {
            return mVersionName;
        }
    }

    public ArrayList<View> getButtonsListView(String version, LinearLayout root, List<BuyButtonData> buttons,
                                              Context context, BuyButtonClickListener listener) {
        mAllButtonArray = buttons;
        getAllProductsPrices();
        ArrayList<View> allViews = new ArrayList<>();
        View tempView;
        for (BuyButtonData buyBtn : buttons) {
            tempView = getButtonView(getVersionByName(version), buyBtn, context, listener);
            if (tempView != null) {
                root.addView(tempView);
                tempView.setTag(buyBtn);
                allViews.add(tempView);
            }
        }
        return allViews;
    }

    @Nullable
    private View getButtonView(ViewsVersions viewsType, BuyButtonData buyBtn,
                               Context context, BuyButtonClickListener listener) {
        switch (viewsType) {
            case V2:
                return getViewV2(buyBtn, listener, context);
            case V1:
            default:
                return getViewV1(buyBtn, context, listener);
        }
    }

    private View getViewV2(final BuyButtonData buyBtn, final BuyButtonClickListener listener, Context context) {
        String discount = null;
        String totalPrice = null;
        String pricePerItem = null;
        BuyButtonVer2.BuyButtonBuilder builder = new BuyButtonVer2.BuyButtonBuilder().title(buyBtn.title).type(BuyButtonVer2.BUTTON_TYPE_BLUE).stickerType(BuyButtonVer2.STICKER_TYPE_NONE).onClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(buyBtn.id, buyBtn);
            }
        });
        int pos = getIndex(getNoTrialProductsList(), buyBtn);
        if (buyBtn.trialPeriodInDays == 0) {
            // catch the second not trial product
            if (pos == 1) {
                builder.stickerType(BuyButtonVer2.STICKER_TYPE_POPULAR);
            }
            // catch the last not trial product
            if (pos == getNoTrialProductsList().size() - 1) {
                builder.stickerType(BuyButtonVer2.STICKER_TYPE_BEST_VALUE);
                builder.type(BuyButtonVer2.BUTTON_TYPE_GREEN);
            }
            totalPrice = getTotalPrice(buyBtn);
            discount = getDiscount(buyBtn);
            pricePerItem = getPricePerItem(buyBtn);
        }
        return builder.discount(discount).totalPrice(totalPrice).pricePerItem(pricePerItem).build(context);
    }

    private String getTotalPrice(BuyButtonData buyBtn) {
        if (buyBtn.totalTemplate.isEmpty()) {
            return null;
        }
        String formatedPrice = getCurrentButtonFormatedPrice(buyBtn);
        return buyBtn.totalTemplate.replace(Products.PRICE, formatedPrice);
    }

    private String getDiscount(BuyButtonData buyBtn) {
        if (buyBtn.discountTemplate.isEmpty()) {
            return null;
        }
        BuyButtonData firstNotTrialProduct = getNoTrialProductsList().get(0);
        ValidatedAndDeffProductPrice firstNtTrialProductPrices = getCurrentProductPrices(firstNotTrialProduct);
        ValidatedAndDeffProductPrice currentProductPrices = getCurrentProductPrices(buyBtn);
        double firstNotTrialProductPrice = isAllNotTrialProductsValidated() ? firstNtTrialProductPrices.getValidatedPrice().getPrice() : firstNtTrialProductPrices.getDeffPrice().getPrice();
        double currentProductPrice = isAllNotTrialProductsValidated() ? currentProductPrices.getValidatedPrice().getPrice() : currentProductPrices.getDeffPrice().getPrice();
        firstNotTrialProductPrice /= firstNotTrialProduct.periodInDays;
        currentProductPrice /= buyBtn.periodInDays;
        double free = (firstNotTrialProductPrice - currentProductPrice) * 100 / firstNotTrialProductPrice;
        return (int) free > 0 ? buyBtn.discountTemplate.replace(Products.DISCOUNT, String.valueOf((int) free)) : null;
    }

    private String getPricePerItem(BuyButtonData buyBtn) {
        if (buyBtn.pricePerItemTemplate.isEmpty()) {
            return null;
        }
        ValidatedAndDeffProductPrice currentProductPrices = getCurrentProductPrices(buyBtn);
        double pricePerItem = isAllNotTrialProductsValidated() ? currentProductPrices.getValidatedPrice().getPrice() / buyBtn.amount : currentProductPrices.getDeffPrice().getPrice() / buyBtn.amount;
        NumberFormat currentFormat = isAllNotTrialProductsValidated() ? currentProductPrices.getValidatedPrice().getCurrencyFormat() : currentProductPrices.getDeffPrice().getCurrencyFormat();
        return buyBtn.pricePerItemTemplate.replace(Products.PRICE_PER_ITEM, getFormatedPrice(pricePerItem, currentFormat));
    }

    private String getCurrentButtonFormatedPrice(BuyButtonData buyBtn) {
        ValidatedAndDeffProductPrice prices = getCurrentProductPrices(buyBtn);
        return isAllNotTrialProductsValidated() ? prices.getValidatedPrice().getFormatedPrice() : prices.getDeffPrice().getFormatedPrice();
    }

    private LinkedList<BuyButtonData> discardTrialProducts(List<BuyButtonData> products) {
        LinkedList<BuyButtonData> noTrialList = new LinkedList<>();
        for (BuyButtonData data : products) {
            if (data.trialPeriodInDays == 0) {
                noTrialList.add(data);
            }
        }
        return noTrialList;
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

    private int getIndex(List<BuyButtonData> products, BuyButtonData buyBtn) {
        for (int i = 0; i < products.size(); i++) {
            if (buyBtn.id.equals(products.get(i).id)) {
                return i;
            }
        }
        return -1;
    }

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
        if (context == null) return null;
        return new BuyButtonVer1.BuyButtonBuilder().discount(buyBtn.discount > 0).showType(buyBtn.showType).title(TextUtils.isEmpty(value) ? buyBtn.title : value).onClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(buyBtn.id, buyBtn);
            }
        }).build(context);
    }

    private String formatPrice(double price, NumberFormat currencyFormatter, BuyButtonData buyBtn) {
        price = getPriceByTemplate(price, buyBtn);
        for (String replaceTemplate : Products.PRICE_TEMPLATES) {
            if (buyBtn.titleTemplate.contains(replaceTemplate)) {
                return buyBtn.titleTemplate.replace(replaceTemplate, currencyFormatter.format(price));
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

    private ViewsVersions getVersionByName(String name) {
        for (ViewsVersions version : ViewsVersions.values()) {
            if (version.getVersionName().equals(name)) {
                return version;
            }
        }
        return ViewsVersions.V1;
    }

    public static JSONArray getSupportedViews() {
        JSONArray array = new JSONArray();
        for (ViewsVersions version : ViewsVersions.values()) {
            array.put(version.getVersionName());
        }
        return array;
    }

    private ValidatedAndDeffProductPrice getProductPrices(BuyButtonData buyBtn) {
        ProductsDetails productsDetails = CacheProfile.getMarketProductsDetails();
        Currency currency;
        NumberFormat currencyFormatter;
        currency = Currency.getInstance(Products.USD);
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormatter.setCurrency(currency);
        double price = (double) buyBtn.price / 100;
        ProductPriceData deffPrice = new ProductPriceData(currencyFormatter, price, getFormatedPrice(price, currencyFormatter));
        ProductPriceData validatedPrice = null;
        if (productsDetails != null && !TextUtils.isEmpty(buyBtn.totalTemplate)) {
            ProductsDetails.ProductDetail detail = productsDetails.getProductDetail(buyBtn.id);

            if (detail != null && detail.currency != null) {
                price = detail.price / ProductsDetails.MICRO_AMOUNT;
                currency = Currency.getInstance(detail.currency);
                currencyFormatter = detail.currency.equalsIgnoreCase(Products.USD)
                        ? NumberFormat.getCurrencyInstance(Locale.US) : NumberFormat.getCurrencyInstance(new Locale(App.getLocaleConfig().getApplicationLocale()));
                currencyFormatter.setCurrency(currency);
                validatedPrice = new ProductPriceData(currencyFormatter, price, getFormatedPrice(price, currencyFormatter));
            }
        }
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

    private List<BuyButtonData> getNoTrialProductsList() {
        if (mNoProductsTrialList == null) {
            mNoProductsTrialList = discardTrialProducts(mAllButtonArray);
        }
        return mNoProductsTrialList;
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

    private ArrayList<ValidatedAndDeffProductPrice> getAllNotTrialProductsPrices() {
        if (getNoTrialProductsList().size() == mAllButtonArray.size()) {
            return getAllProductsPrices();
        }
        ArrayList<ValidatedAndDeffProductPrice> prices = new ArrayList<>();
        for (BuyButtonData buyBtn : getNoTrialProductsList()) {
            prices.add(getCurrentProductPrices(buyBtn));
        }
        return prices;
    }

    private boolean isAllNotTrialProductsValidated() {
        if (mIsAllNotTrialProductsValidated == null) {
            mIsAllNotTrialProductsValidated = checkAllNotTrialProducts();
        }
        return mIsAllNotTrialProductsValidated;
    }

    private boolean checkAllNotTrialProducts() {
        for (ValidatedAndDeffProductPrice prices : getAllNotTrialProductsPrices()) {
            if (prices.getValidatedPrice() == null) {
                return false;
            }
        }
        return true;
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
        private String mFormatPrice;

        public ProductPriceData(NumberFormat currencyFormat, double price, String formatPrice) {
            mCurrencyFormat = currencyFormat;
            mPrice = price;
            mFormatPrice = formatPrice;
        }

        public NumberFormat getCurrencyFormat() {
            return mCurrencyFormat;
        }

        public double getPrice() {
            return mPrice;
        }

        public String getFormatedPrice() {
            return mFormatPrice;
        }
    }

    public interface BuyButtonClickListener {
        void onClick(String id, BuyButtonData btnData);
    }
}