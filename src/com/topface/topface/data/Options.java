package com.topface.topface.data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Novice;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Опции приложения
 * <p/>
 * NOTICE: В данном типе данных используем значения по умолчанию
 */
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


    public final static String GENERAL_MAIL_CONST = "true";
    public final static String GENERAL_APNS_CONST = "false";
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

    /**
     * Настройки для каждого типа страниц
     */
    public HashMap<String, Options.Page> pages = new HashMap<String, Options.Page>();
    public LinkedList<BuyButton> coins = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> likes = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> premium = new LinkedList<BuyButton>();
    public LinkedList<BuyButton> others = new LinkedList<BuyButton>();

    /**
     * Стоимость отправки "Восхищения"
     */
    public int price_highrate = 1;
    /**
     * Стоимость вставания в лидеры
     */
    public int price_leader = 6;


    public static Options parse(ApiResponse response) {
        Options options = new Options();

        try {
            options.price_highrate = response.jsonResult.optInt("price_highrate");
            options.price_leader = response.jsonResult.optInt("price_leader");
            // Pages initialization
            JSONArray pages = response.jsonResult.optJSONArray("pages");
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i);

                String pageName = page.optString("name");
                String floatType = page.optString("float");
                String bannerType = page.optString("banner");

                options.pages.put(pageName, new Page(pageName, floatType, bannerType));
            }

            JSONObject purchases = response.jsonResult.optJSONObject("purchases");
            if(purchases != null) {
                JSONArray coinsJSON = purchases.optJSONArray("coins");
                for (int i=0; i < coinsJSON.length(); i++) {
                    options.coins.add(createBuyButtonFromJSON(coinsJSON.optJSONObject(i)));
                }

                JSONArray likesJSON = purchases.optJSONArray("likes");
                for (int i=0; i < likesJSON.length(); i++) {
                    options.likes.add(createBuyButtonFromJSON(likesJSON.optJSONObject(i)));
                }

                JSONArray premiumJSON = purchases.optJSONArray("premium");
                for (int i=0; i < premiumJSON.length(); i++) {
                    options.premium.add(createBuyButtonFromJSON(premiumJSON.optJSONObject(i)));
                }

                JSONArray othersJSON = purchases.optJSONArray("others");
                for (int i=0; i < othersJSON.length(); i++) {
                    options.others.add(createBuyButtonFromJSON(othersJSON.optJSONObject(i)));
                }
            }


        } catch (Exception e) {
            Debug.log("Message.class", "Wrong response parsing: " + e);
        }

        CacheProfile.setOptions(options, response.jsonResult);
        return options;
    }

    public static BuyButton createBuyButtonFromJSON(JSONObject purchaseItem) {
        BuyButton buyCoinBtn = new BuyButton(purchaseItem.optString("id"),
                purchaseItem.optString("amount"),
                purchaseItem.optInt("price"),
                purchaseItem.optString("additional"),
                purchaseItem.optInt("showType"));
        return buyCoinBtn;
    }

    public static String generateKey(int type, boolean isMail) {
        return Integer.toString(type) + GENERAL_SEPARATOR + ((isMail) ? GENERAL_MAIL_CONST : GENERAL_APNS_CONST);
    }

    public static RelativeLayout setButton(LinearLayout root, final BuyButton curBtn, Context context, final BuyButtonClickListener l) {
        if(context != null && !curBtn.text.equals("")) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_buying_btn,root,false);
            RelativeLayout container = (RelativeLayout) view.findViewById(R.id.itContainer);
            container.setBackgroundResource(curBtn.type == 0 ? R.drawable.btn_vip_sale_selector : R.drawable.btn_vip_super_sale_selector);

            container.requestLayout();

            String color = curBtn.type == 0 ? "#B8B8B8" : "#FFFFFF";

            TextView title = (TextView) view.findViewById(R.id.itText);
            title.setText(curBtn.text);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(Color.parseColor(color));

            TextView value = (TextView) view.findViewById(R.id.itValue);


            double price = (double)curBtn.value/100.0;

            value.setText(Double.toString(price) + " " + context.getString(R.string.default_currency));
            value.setTypeface(Typeface.DEFAULT_BOLD);
            value.setTextColor(Color.parseColor(color));

            if(curBtn.economy != null) {
                TextView economy = (TextView) view.findViewById(R.id.itEconomy);
                economy.setText(curBtn.economy);
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
        public String text;
        public int value;
        public String economy;
        public int type;
        public static final String COINS_NAME = "coins";
        public static final String LIKES_NAME = "likes";

        public BuyButton(String id, String text, int value, String economy, int type) {
            this.id = id;
            this.text = text;
            this.value = value;
            this.economy = economy;
            this.type = type;
        }
    }

}
