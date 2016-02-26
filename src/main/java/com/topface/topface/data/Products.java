package com.topface.topface.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.topface.billing.DeveloperPayload;
import com.topface.billing.OpenIabFragment;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.CacheProfile;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Products extends AbstractData {
    public static final String DISCOUNT = "{{discount}}";
    public static final String PRICE = "{{price}}";
    public static final String PRICE_PER_ITEM = "{{price_per_item}}";
    public static String[] PRICE_TEMPLATES = {PRICE, PRICE_PER_ITEM};
    public static final String EUR = "EUR";
    public static final String RUB = "RUB";
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

    public class ViewsType {
        public String buyVip;
    }

    public class ProductsInfo {
        public CoinsSubscriptionInfo coinsSubscription;
        public CoinsSubscriptionInfo coinsSubscriptionMasked;
        public ViewsType views;

        public ProductsInfo(JSONObject infoJson) {
            coinsSubscription = new CoinsSubscriptionInfo(infoJson.optJSONObject(ProductType.COINS_SUBSCRIPTION.getName()));
            coinsSubscriptionMasked = new CoinsSubscriptionInfo(infoJson.optJSONObject(ProductType.COINS_SUBSCRIPTION_MASKED.getName()));
            if (infoJson.has("views")) {
                views = JsonUtils.fromJson(infoJson.optJSONObject("views").toString(), ViewsType.class);
            }
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
