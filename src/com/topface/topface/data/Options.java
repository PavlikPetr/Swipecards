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

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Опции приложения
 * <p/>
 * NOTICE: В данном типе данных используем значения по умолчанию
 */
@SuppressWarnings("UnusedDeclaration")
public class Options extends AbstractData {

    /**
     * Идентификаторы страниц
     */
    public final static String PAGE_LIKES = "LIKE";
    public final static String PAGE_MUTUAL = "MUTUAL";
    public final static String PAGE_MESSAGES = "MESSAGES";
    public final static String PAGE_TOP = "TOP";
    public final static String PAGE_VISITORS = "VISITORS";
    public final static String PAGE_DIALOGS = "DIALOGS";
    public final static String PAGE_FANS = "FANS";
    public final static String PAGE_BOOKMARKS = "BOOKMARKS";
    public final static String PAGE_VIEWS = "VIEWS";
    public final static String PAGE_START = "START";

    public final static String GENERAL_MAIL_CONST = "mail";
    public final static String GENERAL_APNS_CONST = "apns";
    public final static String GENERAL_SEPARATOR = ":";

    /**
     * Идентификаторы для типов блоков (лидеры, баннеры, не показывать блоки)
     */
    public final static String FLOAT_TYPE_BANNER = "BANNER";
    public final static String FLOAT_TYPE_LEADERS = "LEADERS";
    public final static String FLOAT_TYPE_NONE = "NONE";

    /**
     * Идентификаторы типов баннеров
     */
    public final static String BANNER_TOPFACE = "TOPFACE";
    public final static String BANNER_ADFONIC = "ADFONIC";
    public final static String BANNER_ADMOB = "ADMOB";
    public final static String BANNER_WAPSTART = "WAPSTART";
    public static final String BANNER_ADWIRED = "ADWIRED";
    public final static String BANNER_MADNET = "MADNET";
    public static final String BANNER_BEGUN = "BEGUN";
    public static final String BANNER_MOPUB = "MOPUB";
    public static final String BANNER_GAG = "GAG";

    /**
     * Идентификаторы для типов офферволлов
     */
    public static final String TAPJOY = "TAPJOY";
    public static final String SPONSORPAY = "SPONSORPAY";
    public static final String CLICKKY = "CLICKKY";
    public static final String RANDOM = "RANDOM";

    /**
     * Настройки для каждого типа страниц
     */
    public HashMap<String, Options.Page> pages = new HashMap<String, Options.Page>();
    public LinkedList<BuyButton> coins = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> likes = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> premium = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> others = new LinkedList<BuyButton>();
    private String paymentwall;

    public String max_version = "2147483647"; //Integer.MAX_VALUE);

    /**
     * Стоимость отправки "Восхищения"
     */
    public int price_highrate = 1;
    /**
     * Стоимость вставания в лидеры
     */
    public int price_leader = 6;

    public int minLeadersPercent = 25; //Не уверен в этом, возможно стоит использовать другое дефолтное значение

    public String offerwall;
    public boolean saleExists = false;

    public int premium_period;
    public int contacts_count;

    public static Options parse(ApiResponse response) {
        Options options = new Options();

        try {
            options.saleExists = false;
            options.price_highrate = response.jsonResult.optInt("price_highrate");
            options.price_leader = response.jsonResult.optInt("price_leader");
            options.minLeadersPercent = response.jsonResult.optInt("leader_percent");
            // Pages initialization
            JSONArray pages = response.jsonResult.optJSONArray("pages");
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i);

                String pageName = page.optString("name");
                String floatType = page.optString("float");
                String bannerType = page.optString("banner");

                options.pages.put(pageName, new Page(pageName, floatType, bannerType));
            }
            options.offerwall = response.jsonResult.optString("offerwall");
            options.max_version = response.jsonResult.optString("max_version");

            JSONObject purchases = response.jsonResult.optJSONObject("purchases");
            if (purchases != null) {
                JSONArray coinsJSON = purchases.optJSONArray("coins");
                if (coinsJSON != null) {
                    for (int i = 0; i < coinsJSON.length(); i++) {
                        options.coins.add(options.createBuyButtonFromJSON(coinsJSON.optJSONObject(i)));
                    }
                }

                JSONArray likesJSON = purchases.optJSONArray("likes");
                for (int i = 0; i < likesJSON.length(); i++) {
                    options.likes.add(options.createBuyButtonFromJSON(likesJSON.optJSONObject(i)));
                }

                JSONArray premiumJSON = purchases.optJSONArray("premium");
                if (premiumJSON != null) {
                    for (int i = 0; i < premiumJSON.length(); i++) {
                        options.premium.add(options.createBuyButtonFromJSON(premiumJSON.optJSONObject(i)));
                    }
                }

                JSONArray othersJSON = purchases.optJSONArray("others");
                if (othersJSON != null) {
                    for (int i = 0; i < othersJSON.length(); i++) {
                        options.others.add(options.createBuyButtonFromJSON(othersJSON.optJSONObject(i)));
                    }
                }
            }

            JSONObject contacts_invite = response.jsonResult.optJSONObject("contacts_invite");
            options.premium_period = contacts_invite.optInt("premium_period");
            options.contacts_count = contacts_invite.optInt("contacts_count");

            if (response.jsonResult.has("links")) {
                JSONObject links = response.jsonResult.optJSONObject("links");
                if (links != null && links.has("paymentwall")) {
                    options.paymentwall = links.optString("paymentwall");
                }
            }


        } catch (Exception e) {
            Debug.error("Options parsing error", e);
        }

        CacheProfile.setOptions(options, response.jsonResult);
        return options;
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

    public static String generateKey(int type, boolean isMail) {
        return Integer.toString(type) + GENERAL_SEPARATOR + ((isMail) ? GENERAL_MAIL_CONST : GENERAL_APNS_CONST);
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

    public static class Page {
        public String name;
        public String floatType;
        public String banner;

        public Page(String name, String floatType, String banner) {
            this.name = name;
            this.floatType = floatType;
            this.banner = banner;
        }
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

    public String getPaymentwallLink() {
        return paymentwall;
    }

}
