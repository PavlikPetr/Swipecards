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

    public static RelativeLayout setButton(LinearLayout root, final BuyButton curBtn, Context context, final BuyButtonClickListener l) {
        if (context != null && !curBtn.title.equals("")) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_buying_btn, root, false);
            RelativeLayout container = (RelativeLayout) view.findViewById(R.id.itContainer);
            // button background
            int bgResource;
            if (curBtn.discount > 0) {
                bgResource = R.drawable.btn_sale_selector;
                int paddingFive = Utils.getPxFromDp(5);
                container.setPadding(paddingFive, paddingFive, Utils.getPxFromDp(56), paddingFive);
            } else {
                bgResource = curBtn.showType == 0 ?
                        R.drawable.btn_gray_selector :
                        R.drawable.btn_blue_selector;
            }
            container.setBackgroundResource(bgResource);
            container.requestLayout();
            // title text
            int color = curBtn.showType == 0 ?
                    context.getResources().getColor(R.color.text_light_gray) :
                    context.getResources().getColor(R.color.text_white);
            TextView title = (TextView) view.findViewById(R.id.itText);
            title.setText(curBtn.title);
            title.setTextColor(color);
            // value text
            TextView value = (TextView) view.findViewById(R.id.itValue);
            value.setText(getValueText(curBtn));
            value.setTextColor(color);
            // economy text
            TextView economy = (TextView) view.findViewById(R.id.itEconomy);
            economy.setTextColor(color);
            setEconomyTextView(curBtn, economy);
            // click listener
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    l.onClick(curBtn.id);
                }
            });
            root.addView(view);
            return container;
        } else {
            return null;
        }
    }

    private static void setEconomyTextView(BuyButton curBtn, TextView economy) {
        if (!TextUtils.isEmpty(curBtn.hint)) {
            economy.setText(curBtn.hint);
        } else {
            economy.setVisibility(View.GONE);
        }
    }

    private static String getValueText(BuyButton curBtn) {
        String result = String.format(
                App.getContext().getString(R.string.default_price_format),
                curBtn.price / 100f
        );
        return result;
    }

    public interface BuyButtonClickListener {
        public void onClick(String id);
    }

    public static class BuyButton {
        public String id;
        public String title;
        public int price;
        private int showType;
        public String hint;
        public String type;
        public int discount;

        public BuyButton(JSONObject json) {
            id = json.optString("id");
            title = json.optString("title");
            price = json.optInt("price");
            hint = json.optString("hint");
            showType = json.optInt("showType");
            type = json.optString("type");
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
                public boolean until;

                public StatusInfo(JSONObject json) {
                    active = json.optBoolean("active");
                    until = json.optBoolean("until");
                }
            }
        }
    }
}
