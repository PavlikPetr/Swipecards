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
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class GooglePlayProducts extends AbstractData{

    public boolean saleExists = false;

    public LinkedList<BuyButton> coins = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> likes = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> premium = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> others = new LinkedList<BuyButton>();

    public static GooglePlayProducts parse(ApiResponse response) {
        GooglePlayProducts products = new GooglePlayProducts();
        JSONObject data = response.getJsonResult();
        try {
                JSONArray coinsJSON = data.optJSONArray("coins");
                if (coinsJSON != null) {
                    for (int i = 0; i < coinsJSON.length(); i++) {
                        products.coins.add(products.createBuyButtonFromJSON(coinsJSON.optJSONObject(i)));
                    }
                }

                JSONArray likesJSON = data.optJSONArray("likes");
                for (int i = 0; i < likesJSON.length(); i++) {
                    products.likes.add(products.createBuyButtonFromJSON(likesJSON.optJSONObject(i)));
                }

                JSONArray premiumJSON = data.optJSONArray("premium");
                if (premiumJSON != null) {
                    for (int i = 0; i < premiumJSON.length(); i++) {
                        products.premium.add(products.createBuyButtonFromJSON(premiumJSON.optJSONObject(i)));
                    }
                }

                JSONArray othersJSON = data.optJSONArray("others");
                if (othersJSON != null) {
                    for (int i = 0; i < othersJSON.length(); i++) {
                        products.others.add(products.createBuyButtonFromJSON(othersJSON.optJSONObject(i)));
                    }
                }
        } catch (Exception e) {
            Debug.error("Options parsing error", e);
        }
        CacheProfile.setGooglePlayProducts(products, response.getJsonResult());
        return products;
    }

    public BuyButton createBuyButtonFromJSON(JSONObject purchaseItem) {
        if (purchaseItem.optInt("discount") > 0) {
            saleExists = true;

        }
        return new BuyButton(
                purchaseItem.optString("id"),
                purchaseItem.optString("title"),
                purchaseItem.optInt("price"),
                purchaseItem.optString("hint"),
                purchaseItem.optInt("showType"),
                purchaseItem.optString("type"),
                purchaseItem.optInt("discount")
        );
    }

    public static RelativeLayout setButton(LinearLayout root, final BuyButton curBtn, Context context, final BuyButtonClickListener l) {
        if (context != null && !curBtn.title.equals("")) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_buying_btn, root, false);

            RelativeLayout container = (RelativeLayout) view.findViewById(R.id.itContainer);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) container.getLayoutParams();
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
        public static final String COINS_NAME = "coins";
        public static final String LIKES_NAME = "likes";

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
