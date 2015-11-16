package com.topface.topface.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.billing.DeveloperPayload;
import com.topface.billing.OpenIabFragment;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onepf.oms.appstore.googleUtils.Purchase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Products extends AbstractData {
    public static final String PRICE = "{{price}}";
    public static final String PRICE_PER_ITEM = "{{price_per_item}}";
    public static final String USD = "USD";

    public enum ProductType {
        COINS("coins"),
        LIKES("likes"),
        PREMIUM("premium", true),
        LEADER("leader"),
        OTHERS("others"),
        COINS_SUBSCRIPTION("coinsSubscription", true),
        COINS_SUBSCRIPTION_MASKED("coinsSubscriptionMasked", true);

        private String mTypeName;
        private boolean mIsSubscription;

        ProductType(String typeName) {
            this(typeName, false);
        }

        ProductType(String typeName, boolean isSubscription) {
            mTypeName = typeName;
            mIsSubscription = isSubscription;
        }

        public String getName() {
            return mTypeName;
        }

        public boolean isSubscription() {
            return mIsSubscription;
        }
    }

    public boolean saleExists = false;
    public LinkedList<BuyButtonData> coins = new LinkedList<>();
    public LinkedList<BuyButtonData> likes = new LinkedList<>();
    public LinkedList<BuyButtonData> premium = new LinkedList<>();
    public LinkedList<BuyButtonData> others = new LinkedList<>();
    public LinkedList<BuyButtonData> coinsSubscriptions = new LinkedList<>();
    public LinkedList<BuyButtonData> coinsSubscriptionsMasked = new LinkedList<>();
    //Список всех подписок пользователя
    public ProductsInventory inventory;
    public ProductsInfo info;

    public Products() {

    }

    public Products(@NonNull IApiResponse data) {
        fillData(data.getJsonResult());
    }

    public Products(@Nullable JSONObject data) {
        if (data != null) {
            fillData(data);
        }
    }

    protected void fillData(JSONObject data) {
        fillProducts(data);
        updateCache(data);
    }

    protected void fillProducts(JSONObject data) {
        if (data == null) {
            Debug.error("Products data is empty");
            return;
        }
        try {
            fillProductsInfo(data.optJSONObject("info"));
            if (info != null) {
                fillSubscriptionsProductsArray(
                        coinsSubscriptions,
                        data.optJSONArray(ProductType.COINS_SUBSCRIPTION.getName()),
                        info.coinsSubscription.status.userSubscriptions
                );
                fillSubscriptionsProductsArray(
                        coinsSubscriptionsMasked,
                        data.optJSONArray(ProductType.COINS_SUBSCRIPTION_MASKED.getName()),
                        info.coinsSubscriptionMasked.status.userSubscriptions
                );

            }
            fillProductsArray(coins, data.optJSONArray(ProductType.COINS.getName()));
            fillProductsArray(likes, data.optJSONArray(ProductType.LIKES.getName()));
            fillProductsArray(premium, data.optJSONArray(ProductType.PREMIUM.getName()));
            fillProductsArray(others, data.optJSONArray(ProductType.OTHERS.getName()));
        } catch (Exception e) {
            Debug.error("Products parsing error", e);
        }
    }

    protected void updateCache(JSONObject data) {
        //Обновляем кэш
        CacheProfile.setMarketProducts(this, data);
    }

    protected void fillProductsInfo(JSONObject infoJson) {
        if (infoJson != null) {
            info = new ProductsInfo(infoJson);
        }
    }

    protected void fillSubscriptionsProductsArray(LinkedList<BuyButtonData> list, JSONArray coinsJSON,
                                                  List<String> userSubscriptions) {
        if (coinsJSON != null && list != null) {
            SubscriptionBuyButton buyButtonFromJSON;
            for (int i = 0; i < coinsJSON.length(); i++) {
                buyButtonFromJSON = createSubscriptionBuyButtonFromJSON(coinsJSON.optJSONObject(i), false);
                if (buyButtonFromJSON != null) {
                    if (userSubscriptions.contains(buyButtonFromJSON.id)) {
                        buyButtonFromJSON.activated = true;
                    }
                    list.add(buyButtonFromJSON);
                }
            }
        }
    }

    protected void fillProductsArray(LinkedList<BuyButtonData> list, JSONArray coinsJSON) {
        if (coinsJSON != null && list != null) {
            BuyButtonData buyButtonFromJSON;
            for (int i = 0; i < coinsJSON.length(); i++) {
                buyButtonFromJSON = createBuyButtonFromJSON(coinsJSON.optJSONObject(i));
                if (buyButtonFromJSON != null) {
                    list.add(buyButtonFromJSON);
                }
            }
        }
    }

    public BuyButtonData createBuyButtonFromJSON(JSONObject purchaseItem) {
        BuyButtonData button = null;
        if (purchaseItem != null) {
            if (purchaseItem.optInt("discount") > 0) {
                saleExists = true;
            }
            button = new BuyButtonData(purchaseItem);
        }
        return button;
    }

    public SubscriptionBuyButton createSubscriptionBuyButtonFromJSON(JSONObject purchaseItem, boolean activeSubscription) {
        SubscriptionBuyButton button = null;
        if (purchaseItem != null) {
            if (purchaseItem.optInt("discount") > 0) {
                saleExists = true;
            }
            button = new SubscriptionBuyButton(purchaseItem, activeSubscription);
        }
        return button;
    }

    /**
     * Creates view for buy actions. Button with hints
     *
     * @param context  current context
     * @param buyBtn   google play product object with configuration data
     * @param listener to process click
     * @return created view
     */
    public static View createBuyButtonLayout(Context context, BuyButtonData buyBtn,
                                             final BuyButtonClickListener listener) {
        String value;
        if (buyBtn.type == ProductType.COINS_SUBSCRIPTION && buyBtn.price == 0) {
            value = buyBtn.hint;
        } else {
            ProductsDetails productsDetails = CacheProfile.getMarketProductsDetails();
            Currency currency;
            NumberFormat currencyFormatter;
            currency = Currency.getInstance(USD);
            currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            currencyFormatter.setCurrency(currency);
            value = formatPrice(buyBtn.price / 100, currencyFormatter, buyBtn.titleTemplate, PRICE, PRICE_PER_ITEM);
            if (productsDetails != null && !TextUtils.isEmpty(buyBtn.totalTemplate)) {
                ProductsDetails.ProductDetail detail = productsDetails.getProductDetail(buyBtn.id);

                if (detail != null) {
                    double price = detail.price / ProductsDetails.MICRO_AMOUNT;
                    currency = Currency.getInstance(detail.currency);
                    currencyFormatter = detail.currency.equalsIgnoreCase(USD)
                            ? NumberFormat.getCurrencyInstance(Locale.US) : NumberFormat.getCurrencyInstance();
                    currencyFormatter.setCurrency(currency);
                    value = formatPrice(price, currencyFormatter, buyBtn.titleTemplate, PRICE, PRICE_PER_ITEM);
                } else {
                    value = formatPrice(buyBtn.price / 100, currencyFormatter, buyBtn.titleTemplate, PRICE, PRICE_PER_ITEM);
                }
            }
        }
        return createBuyButtonLayout(
                context, buyBtn.id, buyBtn.title, buyBtn.discount > 0,
                buyBtn.showType, value, listener
        );
    }

    public static String formatPrice(double price, NumberFormat currencyFormatter, String template, @NotNull String... replaceTemplateArray) {
        currencyFormatter.setMaximumFractionDigits(price % 1 != 0 ? 2 : 0);
        for (String replaceTemplate : replaceTemplateArray) {
            if (template.contains(replaceTemplate)) {
                return template.replace(replaceTemplate, currencyFormatter.format(price));
            }
        }
        return template.replace(replaceTemplateArray[0], currencyFormatter.format(price));
    }

    /**
     * Creates view for buy actions. Button with hints
     *
     * @param context  current context
     * @param id       unique good's id from google play in-app billing system
     * @param title    for button
     * @param discount true if button background has to be with sale badge
     * @param showType 0 - gray, 1 - blue button, 2 - disabled button
     * @param value    hint under button
     * @param listener to process click
     * @return created view
     */
    public static View createBuyButtonLayout(
            Context context, final String id, String title, boolean discount, int showType,
            String value, final BuyButtonClickListener listener
    ) {
        if (context == null || TextUtils.isEmpty(title)) return null;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_buying_btn, null);
        initBuyButtonViews(
                view, id, title, discount, value, listener, showType);
        return view;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setBuyButtonBackground(boolean discount, int showType, View view) {
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

    private static void setBuyButtonTextColor(int showType, TextView view) {
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

    private static void initBuyButtonViews(
            View view, final String id, String title, boolean discount,
            String value, final BuyButtonClickListener listener, int showType) {
        RelativeLayout container = (RelativeLayout) view.findViewById(R.id.itContainer);
        // button background
        if (discount) {
            int paddingFive = Utils.getPxFromDp(5);
            container.setPadding(paddingFive, paddingFive, Utils.getPxFromDp(56), paddingFive);
        }
        setBuyButtonBackground(discount, showType, container);
        container.requestLayout();
        // click listener
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(id);
            }
        });
        // title text
        TextView tvTitle = (TextView) view.findViewById(R.id.itText);
        setBuyButtonTextColor(showType, tvTitle);
        tvTitle.setText(TextUtils.isEmpty(value) ? title : value);
    }

    private static void setSelectorTextColor(int selector, TextView view) {
        try {
            XmlResourceParser xrp = App.getContext().getResources().getXml(selector);
            ColorStateList csl = ColorStateList.createFromXml(App.getContext().getResources(), xrp);
            view.setTextColor(csl);
        } catch (Exception e) {
            Debug.error(e.toString());
        }
    }

    public static View setBuyButton(LinearLayout root, final BuyButtonData buyBtn,
                                    Context context, final BuyButtonClickListener listener) {
        View view = createBuyButtonLayout(context, buyBtn, listener);
        if (view != null) {
            root.addView(view);
            view.setTag(buyBtn);
            return view;
        } else {
            return null;
        }
    }

    /**
     * Can check if this product id is in on of subscriptions list
     *
     * @param product product
     * @return true if productId refers to subscriptions
     */
    public boolean isSubscription(Purchase product) {
        String productId = product.getSku();
        if (productId.equals(OpenIabFragment.TEST_PURCHASED_PRODUCT_ID)) {
            productId = getSkuFromDeveloperPayload(product.getDeveloperPayload());
        }
        for (BuyButtonData subscription : coinsSubscriptions) {
            if (subscription.id.equals(productId)) {
                return true;
            }
        }
        for (BuyButtonData subscription : coinsSubscriptionsMasked) {
            if (subscription.id.equals(productId)) {
                return true;
            }
        }
        return false;
    }

    private String getSkuFromDeveloperPayload(String developerPayload) {
        DeveloperPayload payload = JsonUtils.fromJson(developerPayload, DeveloperPayload.class);
        return payload.sku;
    }

    public interface BuyButtonClickListener {
        void onClick(String id);
    }

    public static ProductType getProductTypeByName(String name) {
        if (name.equals(ProductType.COINS.getName())) {
            return ProductType.COINS;
        } else if (name.equals(ProductType.COINS_SUBSCRIPTION.getName())) {
            return ProductType.COINS_SUBSCRIPTION;
        } else if (name.equals(ProductType.COINS_SUBSCRIPTION_MASKED.getName())) {
            return ProductType.COINS_SUBSCRIPTION_MASKED;
        } else if (name.equals(ProductType.LIKES.getName())) {
            return ProductType.LIKES;
        } else if (name.equals(ProductType.PREMIUM.getName())) {
            return ProductType.PREMIUM;
        } else if (name.equals(ProductType.LEADER.getName())) {
            return ProductType.LEADER;
        } else {
            return ProductType.OTHERS;
        }
    }

    public static class SubscriptionBuyButton extends BuyButtonData {

        public boolean activated = false;

        public SubscriptionBuyButton(JSONObject json, boolean active) {
            super(json);
            activated = active;
        }
    }

    public class ProductsInfo {
        public CoinsSubscriptionInfo coinsSubscription;
        public CoinsSubscriptionInfo coinsSubscriptionMasked;

        public ProductsInfo(JSONObject infoJson) {
            coinsSubscription = new CoinsSubscriptionInfo(infoJson.optJSONObject(ProductType.COINS_SUBSCRIPTION.getName()));
            coinsSubscriptionMasked = new CoinsSubscriptionInfo(infoJson.optJSONObject(ProductType.COINS_SUBSCRIPTION_MASKED.getName()));
            //Парсим список всех подписок
            JSONArray inventoryArray = infoJson.optJSONArray("inventory");
            if (inventoryArray != null) {
                InventoryItem[] inventoryItems = JsonUtils.fromJson(
                        inventoryArray.toString(),
                        InventoryItem[].class
                );
                inventory = new ProductsInventory(inventoryItems);
            }

        }

        public class CoinsSubscriptionInfo {
            public List<MonthInfo> months = new ArrayList<>();
            public String text;
            public BuyButtonData hasSubscriptionButton;
            public BuyButtonData noSubscriptionButton;
            public StatusInfo status = new StatusInfo(null);

            public CoinsSubscriptionInfo(JSONObject json) {
                if (json != null) {
                    JSONArray arrMonths = json.optJSONArray("months");
                    if (arrMonths != null) {
                        for (int i = 0; i < arrMonths.length(); i++) {
                            months.add(new MonthInfo(arrMonths.optJSONObject(i)));
                        }
                    }
                    text = json.optString("text");
                    hasSubscriptionButton = new BuyButtonData(json.optJSONObject("hasSubscriptionButton"));
                    noSubscriptionButton = new BuyButtonData(json.optJSONObject("noSubscriptionButton"));
                    status = new StatusInfo(json.optJSONObject("status"));
                }
            }

            public class MonthInfo {
                public String title;
                public String amount;

                public MonthInfo(JSONObject json) {
                    if (json != null) {
                        title = json.optString("title");
                        amount = json.optString("amount");
                    }
                }
            }

            public class StatusInfo {
                public List<String> userSubscriptions = new ArrayList<>();

                public StatusInfo(JSONObject json) {
                    if (json != null) {
                        JSONArray arrSubscriptions = json.optJSONArray("products");
                        for (int i = 0; i < arrSubscriptions.length(); i++) {
                            userSubscriptions.add(arrSubscriptions.optString(i));
                        }
                    }
                }

                public boolean isActive() {
                    return !userSubscriptions.isEmpty();
                }
            }
        }
    }

    public class ProductsInventory extends ArrayList<InventoryItem> {
        public ProductsInventory(InventoryItem[] items) {
            super(Arrays.asList(items));
        }

        /**
         * Присутствует ли указанный id продукта (productId) в списке
         */
        public boolean containsSku(String sku) {
            for (InventoryItem product : this) {
                if (TextUtils.equals(product.productId, sku)) {
                    return true;
                }
            }
            return false;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private class InventoryItem {
        public String productId;
        public String status;
    }
}
