package com.topface.topface.data;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GooglePlayProducts extends AbstractData {
    public static final String INTENT_UPDATE_PRODUCTS = "com.topface.topface.action.UPDATE_PRODUCTS";

    public static enum ButtonType {
        COINS("coins"),
        LIKES("likes"),
        PREMIUM("premium"),
        LEADER("leader"),
        OTHERS("others"),
        COINS_SUBSCRIPTION("coinsSubscription");

        private String mTypeName;

        ButtonType(String typeName) {
            mTypeName = typeName;
        }

        public String getName() {
            return mTypeName;
        }
    }

    public boolean saleExists = false;
    public LinkedList<BuyButton> coins = new LinkedList<>();
    public LinkedList<BuyButton> likes = new LinkedList<>();
    public LinkedList<BuyButton> premium = new LinkedList<>();
    public LinkedList<BuyButton> others = new LinkedList<>();
    public LinkedList<BuyButton> coinsSubscriptions = new LinkedList<>();
    public ProductsInfo productsInfo;

    public GooglePlayProducts(@NotNull IApiResponse data) {
        fillData(data.getJsonResult());
    }

    public GooglePlayProducts(@Nullable JSONObject data) {
        if (data != null) {
            fillData(data);
        }
    }

    protected void fillData(JSONObject data) {
        try {
            fillProductsInfo(data.optJSONObject("info"));
            fillProductsArray(coinsSubscriptions, data.optJSONArray("coinsSubscription"));
            fillProductsArray(coins, data.optJSONArray("coins"));
            fillProductsArray(likes, data.optJSONArray("likes"));
            fillProductsArray(premium, data.optJSONArray("premium"));
            fillProductsArray(others, data.optJSONArray("others"));
        } catch (Exception e) {
            Debug.error("GooglePlayProducts parsing error", e);
        }
        //Обновляем кэш
        CacheProfile.setGooglePlayProducts(this, data);
    }

    private void fillProductsInfo(JSONObject infoJson) throws JSONException {
        if (infoJson != null) {
            productsInfo = new ProductsInfo(infoJson);
        }
    }

    private void fillProductsArray(LinkedList<BuyButton> list, JSONArray coinsJSON) {
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
        if (buyBtn.type == ButtonType.COINS_SUBSCRIPTION && buyBtn.price == 0) {
            value = buyBtn.hint;
            economy = null;
        } else {
            value = String.format(
                    App.getContext().getString(R.string.default_price_format),
                    buyBtn.price / 100f
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

    public interface BuyButtonClickListener {
        public void onClick(String id);
    }

    private static ButtonType getButtonTypeByName(String name) {
        if (name.equals(ButtonType.COINS.getName())) {
            return ButtonType.COINS;
        } else if (name.equals(ButtonType.COINS_SUBSCRIPTION.getName())) {
            return ButtonType.COINS_SUBSCRIPTION;
        } else if (name.equals(ButtonType.LIKES.getName())) {
            return ButtonType.LIKES;
        } else if (name.equals(ButtonType.PREMIUM.getName())) {
            return ButtonType.PREMIUM;
        } else if (name.equals(ButtonType.LEADER.getName())) {
            return ButtonType.LEADER;
        } else {
            return ButtonType.OTHERS;
        }
    }

    public static class BuyButton {
        public String id;
        public String title;
        public int price;
        protected int showType;
        public String hint;
        public ButtonType type;
        public int discount;

        public BuyButton(JSONObject json) {
            id = json.optString("id");
            title = json.optString("title");
            price = json.optInt("price");
            hint = json.optString("hint");
            showType = json.optInt("showType");
            type = getButtonTypeByName(json.optString("type"));
            discount = json.optInt("discount");
        }
    }

    public class ProductsInfo {
        public CoinsSubscriptionInfo coinsSubscriptionInfo;

        public ProductsInfo(JSONObject infoJson) throws JSONException {
            coinsSubscriptionInfo = new CoinsSubscriptionInfo(infoJson.optJSONObject("coinsSubscription"));
        }

        public class CoinsSubscriptionInfo {
            public List<MonthInfo> months = new ArrayList<>();
            public String text;
            public BuyButton hasSubscriptionButton;
            public BuyButton noSubscriptionButton;
            public StatusInfo status;

            public CoinsSubscriptionInfo(JSONObject json) throws JSONException {
                JSONArray arrMonths = json.optJSONArray("months");
                for (int i = 0; i < arrMonths.length(); i++) {
                    months.add(new MonthInfo(arrMonths.getJSONObject(i)));
                }
                text = json.optString("text");
                hasSubscriptionButton = new BuyButton(json.optJSONObject("hasSubscriptionButton"));
                noSubscriptionButton = new BuyButton(json.optJSONObject("noSubscriptionButton"));
                status = new StatusInfo(json.optJSONObject("status"));
            }

            public BuyButton getSubscriptionButton() {
                return status.active ? hasSubscriptionButton : noSubscriptionButton;
            }

            public class MonthInfo {
                public String title;
                public String amount;

                public MonthInfo(JSONObject json) {
                    title = json.optString("title");
                    amount = json.optString("amount");
                }
            }

            public class StatusInfo {
                public boolean active;
                public long until;

                public StatusInfo(JSONObject json) {
                    active = json.optBoolean("active");
                    until = json.optLong("until");
                }
            }
        }
    }
}
