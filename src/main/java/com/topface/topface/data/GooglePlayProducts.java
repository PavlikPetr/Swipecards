package com.topface.topface.data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class GooglePlayProducts extends AbstractData {

    public static final String INTENT_UPDATE_PRODUCTS = "com.topface.topface.action.UPDATE_PRODUCTS";
    public boolean saleExists = false;

    public LinkedList<BuyButton> coins = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> likes = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> premium = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> others = new LinkedList<BuyButton>();

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
            button = new BuyButton(
                    purchaseItem.optString("id"),
                    purchaseItem.optString("title"),
                    purchaseItem.optInt("price"),
                    purchaseItem.optString("hint"),
                    purchaseItem.optInt("showType"),
                    purchaseItem.optString("type"),
                    purchaseItem.optInt("discount")
            );
        }

        return button;
    }

    public static RelativeLayout setButton(LinearLayout root, final BuyButton curBtn, Context context, final BuyButtonClickListener l) {
        if (context != null && !curBtn.title.equals("")) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_buying_btn, root, false);

            RelativeLayout container = (RelativeLayout) view.findViewById(R.id.itContainer);
            double density = context.getResources().getDisplayMetrics().density;

            int bgResource;
            if (curBtn.discount > 0) {
                bgResource = R.drawable.btn_sale_selector;
                container.setPadding((int) (5 * density), (int) (5 * density), (int) (56 * density), (int) (5 * density));
            } else {
                bgResource = curBtn.showType == 0 ?
                        R.drawable.btn_gray_selector :
                        R.drawable.btn_blue_selector;
            }
            container.setBackgroundResource(bgResource);


            container.requestLayout();

            String color = curBtn.showType == 0 ? "#B8B8B8" : "#FFFFFF";

            TextView title = (TextView) view.findViewById(R.id.itText);
            title.setText(curBtn.title);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(Color.parseColor(color));

            TextView value = (TextView) view.findViewById(R.id.itValue);

            value.setText(
                    String.format(
                            App.getContext().getString(R.string.default_price_format),
                            curBtn.price / 100f
                    )
            );
            value.setTextColor(Color.parseColor(color));
            TextView economy = (TextView) view.findViewById(R.id.itEconomy);
            economy.setTextColor(Color.parseColor(color));

            if (!TextUtils.isEmpty(curBtn.hint)) {
                economy.setText(curBtn.hint);
            } else {
                economy.setVisibility(View.GONE);
            }

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

        public BuyButton(String id, String title, int price, String hint, int showType, String type, int discount) {
            this.id = id;
            this.title = title;
            this.price = price;
            this.hint = hint;
            this.showType = showType;
            this.type = type;
            this.discount = discount;
        }
    }
}
