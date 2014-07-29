package com.topface.topface.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Products extends AbstractData {
    public static final String INTENT_UPDATE_PRODUCTS = "com.topface.topface.action.UPDATE_PRODUCTS";

    public static enum ProductType {
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
    public LinkedList<BuyButton> coins = new LinkedList<>();
    public LinkedList<BuyButton> likes = new LinkedList<>();
    public LinkedList<BuyButton> premium = new LinkedList<>();
    public LinkedList<BuyButton> others = new LinkedList<>();
    public LinkedList<BuyButton> coinsSubscriptions = new LinkedList<>();
    public LinkedList<BuyButton> coinsSubscriptionsMasked = new LinkedList<>();
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

    private void fillProducts(JSONObject data) {
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
                // skip sale flag if there is an active forceCoinsSubscription experiment
                if (CacheProfile.getOptions().forceCoinsSubscriptions) {
                    saleExists = false;
                }

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
        CacheProfile.setGooglePlayProducts(this, data);
    }

    protected void fillProductsInfo(JSONObject infoJson) {
        if (infoJson != null) {
            info = new ProductsInfo(infoJson);
        }
    }

    protected void fillSubscriptionsProductsArray(LinkedList<BuyButton> list, JSONArray coinsJSON,
                                                  List<String> userSubscriptions) {
        if (coinsJSON != null && list != null) {
            SubscriptionBuyButton buyButtonFromJSON;
            for (int i = 0; i < coinsJSON.length(); i++) {
                buyButtonFromJSON = createSubscriptionBuyButtonFromJSON(coinsJSON.optJSONObject(i), false);
                if (buyButtonFromJSON != null) {
                    if (userSubscriptions.contains(buyButtonFromJSON.id)) {
                        buyButtonFromJSON.price = 0;
                        buyButtonFromJSON.hint = App.getContext().getString(R.string.you_were_subscribed);
                        buyButtonFromJSON.activated = true;
                    }
                    list.add(buyButtonFromJSON);
                }
            }
        }
    }

    protected void fillProductsArray(LinkedList<BuyButton> list, JSONArray coinsJSON) {
        if (coinsJSON != null && list != null) {
            BuyButton buyButtonFromJSON;
            for (int i = 0; i < coinsJSON.length(); i++) {
                buyButtonFromJSON = createBuyButtonFromJSON(coinsJSON.optJSONObject(i));
                if (buyButtonFromJSON != null) {
                    list.add(buyButtonFromJSON);
                }
            }
        }
    }

    public BuyButton createBuyButtonFromJSON(JSONObject purchaseItem) {
        BuyButton button = null;
        if (purchaseItem != null) {
            if (purchaseItem.optInt("discount") > 0) {
                saleExists = true;
            }
            button = new BuyButton(purchaseItem);
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
    public static View createBuyButtonLayout(Context context, BuyButton buyBtn,
                                             final BuyButtonClickListener listener) {
        String value;
        String economy;
        if (buyBtn.type == ProductType.COINS_SUBSCRIPTION && buyBtn.price == 0) {
            value = buyBtn.hint;
            economy = null;
        } else {
            value = String.format(
                    App.getContext().getString(R.string.default_price_format),
                    ((float) buyBtn.price / 100)
            );
            economy = buyBtn.hint;
        }
        return createBuyButtonLayout(
                context, buyBtn.id, buyBtn.title, buyBtn.discount > 0,
                buyBtn.showType, economy, value, listener
        );
    }

    /**
     * Creates view for buy actions. Button with hints
     *
     * @param context  current context
     * @param id       unique good's id from google play in-app billing system
     * @param title    for button
     * @param discount true if button background has to be with sale badge
     * @param showType 0 - gray, 1 - blue button
     * @param economy  hint under button with highlighted background
     * @param value    hint under button
     * @param listener to process click
     * @return created view
     */
    public static View createBuyButtonLayout(
            Context context, final String id, String title, boolean discount, int showType,
            String economy, String value, final BuyButtonClickListener listener
    ) {
        if (context == null || TextUtils.isEmpty(title)) return null;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_buying_btn, null);
        initBuyButtonViews(
                view, id, title, discount, economy, value, listener,
                getBuyButtonTextColor(showType),
                getBuyButtonBackground(discount, showType)
        );
        return view;
    }

    private static int getBuyButtonTextColor(int showType) {
        Context context = App.getContext();
        return showType == 0 ?
                context.getResources().getColor(R.color.text_light_gray) :
                context.getResources().getColor(R.color.text_white);
    }

    private static int getBuyButtonBackground(boolean discount, int showType) {
        int bgResource;
        if (discount) {
            bgResource = R.drawable.btn_sale_selector;
        } else {
            bgResource = showType == 0 ?
                    R.drawable.btn_gray_selector :
                    R.drawable.btn_blue_selector;
        }
        return bgResource;
    }

    private static void initBuyButtonViews(
            View view, final String id, String title, boolean discount, String economy,
            String value, final BuyButtonClickListener listener, int color, int bgResource
    ) {
        RelativeLayout container = (RelativeLayout) view.findViewById(R.id.itContainer);
        // button background
        if (discount) {
            int paddingFive = Utils.getPxFromDp(5);
            container.setPadding(paddingFive, paddingFive, Utils.getPxFromDp(56), paddingFive);
        }
        container.setBackgroundResource(bgResource);
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
        tvTitle.setText(title);
        tvTitle.setTextColor(color);
        // value text
        TextView tvValue = (TextView) view.findViewById(R.id.itValue);
        tvValue.setText(value);
        tvValue.setTextColor(color);
        // economy text
        TextView tvEconomy = (TextView) view.findViewById(R.id.itEconomy);
        tvEconomy.setTextColor(color);
        if (!TextUtils.isEmpty(economy)) {
            tvEconomy.setText(economy);
        } else {
            tvEconomy.setVisibility(View.GONE);
        }
    }

    public static View setBuyButton(LinearLayout root, final BuyButton buyBtn,
                                    Context context, final BuyButtonClickListener listener) {
        View view = createBuyButtonLayout(context, buyBtn, listener);
        if (view != null) {
            root.addView(view);
            return view;
        } else {
            return null;
        }
    }

    /**
     * Creates and adds view for buy button which opens new screen action
     *
     * @param root     container for button
     * @param openBtn  data to configure button
     * @param context  current context
     * @param listener to process click
     * @return create view
     */
    public static View setOpenButton(LinearLayout root, final BuyButton openBtn,
                                     Context context, final BuyButtonClickListener listener) {
        View view = createBuyButtonLayout(context, null, openBtn.title, openBtn.discount > 0,
                openBtn.showType, null, openBtn.hint, listener);
        if (view != null) {
            root.addView(view);
            return view;
        } else {
            return null;
        }
    }

    /**
     * Takes created buy button and changes it's UI representation
     * based on new buy button configure data
     *
     * @param button     button view created before
     * @param newOpenBtn new buy button configure data
     * @param listener   to process click
     */
    public static void switchOpenButtonTexts(View button, final BuyButton newOpenBtn, BuyButtonClickListener listener) {
        boolean discount = newOpenBtn.discount > 0;
        initBuyButtonViews(
                button, null, newOpenBtn.title, discount, null, newOpenBtn.hint, listener,
                getBuyButtonTextColor(newOpenBtn.showType),
                getBuyButtonBackground(discount, newOpenBtn.showType)
        );
    }

    /**
     * Can check if this product id is in on of subscriptions list
     *
     * @param productId productId for product
     * @return true if productId refers to subscriptions
     */
    public boolean isSubscription(String productId) {
        for (BuyButton subscription : coinsSubscriptions) {
            if (subscription.id.equals(productId)) {
                return true;
            }
        }
        for (BuyButton subscription : coinsSubscriptionsMasked) {
            if (subscription.id.equals(productId)) {
                return true;
            }
        }
        return false;
    }

    public interface BuyButtonClickListener {
        public void onClick(String id);
    }

    private static ProductType getProductTypeByName(String name) {
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

    public static class BuyButton {
        public String id;
        public String title;
        public int price;
        protected int showType;
        public String hint;
        public ProductType type;
        public int discount;
        public String paymentwallLink;

        public BuyButton(JSONObject json) {
            if (json != null) {
                id = json.optString("id");
                title = json.optString("title");
                price = json.optInt("price");
                hint = json.optString("hint");
                showType = json.optInt("showType");
                type = getProductTypeByName(json.optString("type"));
                discount = json.optInt("discount");
                paymentwallLink = json.optString("url");
            }
        }
    }

    public static class SubscriptionBuyButton extends BuyButton {

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
            public BuyButton hasSubscriptionButton;
            public BuyButton noSubscriptionButton;
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
                    hasSubscriptionButton = new BuyButton(json.optJSONObject("hasSubscriptionButton"));
                    noSubscriptionButton = new BuyButton(json.optJSONObject("noSubscriptionButton"));
                    status = new StatusInfo(json.optJSONObject("status"));
                }
            }

            public BuyButton getSubscriptionButton() {
                return status.isActive() ? hasSubscriptionButton : noSubscriptionButton;
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
