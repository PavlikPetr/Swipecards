package com.topface.topface.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
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
    public static final String PAGE_UNKNOWK = "UNKNOWN_PAGE";
    public final static String PAGE_LIKES = "LIKE";
    public final static String PAGE_MUTUAL = "MUTUAL";
    public final static String PAGE_MESSAGES = "MESSAGES";
    public final static String PAGE_VISITORS = "VISITORS";
    public final static String PAGE_DIALOGS = "DIALOGS";
    public final static String PAGE_FANS = "FANS";
    public final static String PAGE_BOOKMARKS = "BOOKMARKS";
    public final static String PAGE_VIEWS = "VIEWS";
    public final static String PAGE_START = "START";
    public final static String PAGE_GAG = "GAG";
    public final static String[] PAGES = new String[]{
            PAGE_UNKNOWK,
            PAGE_LIKES,
            PAGE_MUTUAL,
            PAGE_MESSAGES,
            PAGE_VISITORS,
            PAGE_DIALOGS,
            PAGE_FANS,
            PAGE_BOOKMARKS,
            PAGE_VIEWS,
            PAGE_START,
            PAGE_GAG
    };

    public final static String GENERAL_MAIL_CONST = "mail";
    public final static String GENERAL_APNS_CONST = "apns";
    public final static String GENERAL_SEPARATOR = ":";

    /**
     * Идентификаторы для типов блоков (лидеры, баннеры, не показывать блоки)
     */
    public final static String FLOAT_TYPE_BANNER = "BANNER";
    public final static String FLOAT_TYPE_LEADERS = "LEADERS";
    public final static String FLOAT_TYPE_NONE = "NONE";
    public final static String[] FLOAT_TYPES = new String[]{
            FLOAT_TYPE_BANNER,
            FLOAT_TYPE_LEADERS,
            FLOAT_TYPE_NONE
    };

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
    public static final String BANNER_INNERACTIVE = "INNERACTIVE";
    public static final String BANNER_MOBCLIX = "MOBCLIX";
    public static final String BANNER_GAG = "GAG";
    public final static String[] BANNERS = new String[]{
            BANNER_TOPFACE,
            BANNER_ADFONIC,
            BANNER_ADMOB,
            BANNER_WAPSTART,
            BANNER_ADWIRED,
            BANNER_MADNET,
            BANNER_BEGUN,
            BANNER_MOPUB,
            BANNER_INNERACTIVE,
            BANNER_MOBCLIX,
            BANNER_GAG
    };

    /**
     * Идентификаторы для типов офферволлов
     */
    public static final String TAPJOY = "TAPJOY";
    public static final String SPONSORPAY = "SPONSORPAY";
    public static final String CLICKKY = "CLICKKY";
    public static final String RANDOM = "RANDOM";
    public final static String[] OFFERWALLS = new String[]{
            TAPJOY,
            SPONSORPAY,
            CLICKKY,
            RANDOM
    };
    public static final String PREMIUM_MESSAGES_POPUP_SHOW_TIME = "premium_messages_popup_show_time";

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
    public long popup_timeout;
    public boolean block_unconfirmed;
    public boolean block_chat_not_mutual;
    public Closing closing = new Closing();
    public PremiumMessages premium_messages;

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

                String pageName = getPageName(page);
                String floatType = page.optString("float");
                String bannerType = page.optString("banner");

                options.pages.put(pageName, new Page(pageName, floatType, bannerType));
            }
            options.offerwall = response.jsonResult.optString("offerwall");
            options.max_version = response.jsonResult.optString("max_version");
            options.block_unconfirmed = response.jsonResult.optBoolean("block_unconfirmed");
            options.block_chat_not_mutual = response.jsonResult.optBoolean("block_chat_not_mutual");

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
            options.popup_timeout = contacts_invite.optInt("show_popup_timeout") * 60 * 60 * 1000;

            if (response.jsonResult.has("premium_messages")) {
                options.premium_messages = new PremiumMessages(
                        response.jsonResult.optJSONObject("premium_messages")
                );
            } else {
                options.premium_messages = new PremiumMessages(false, 10, 1000);
            }

            if (response.jsonResult.has("links")) {
                JSONObject links = response.jsonResult.optJSONObject("links");
                if (links != null && links.has("paymentwall")) {
                    options.paymentwall = links.optString("paymentwall");
                }
            }

            JSONObject closings = response.jsonResult.optJSONObject("closing");
            if (options.closing == null) options.closing = new Closing();
            options.closing.enabledMutual = closings.optBoolean("enabled_mutual");
            options.closing.enableSympathies = closings.optBoolean("enabled_sympathies");
            options.closing.limitMutual = closings.optInt("limit_mutual");
            options.closing.limitSympathies = closings.optInt("limit_sympathies");
        } catch (Exception e) {
            Debug.error("Options parsing error", e);
        }

        CacheProfile.setOptions(options, response.jsonResult);
        return options;
    }

    private static String getPageName(JSONObject page) {
        String name = page.optString("name");
        if (PAGE_LIKES.equals(name)) {
            return PAGE_LIKES;
        } else if (PAGE_MUTUAL.equals(name)) {
            return PAGE_MUTUAL;
        } else if (PAGE_MESSAGES.equals(name)) {
            return PAGE_MESSAGES;
        } else if (PAGE_VISITORS.equals(name)) {
            return PAGE_VISITORS;
        } else if (PAGE_DIALOGS.equals(name)) {
            return PAGE_DIALOGS;
        } else if (PAGE_FANS.equals(name)) {
            return PAGE_FANS;
        } else if (PAGE_BOOKMARKS.equals(name)) {
            return PAGE_BOOKMARKS;
        } else if (PAGE_VIEWS.equals(name)) {
            return PAGE_VIEWS;
        } else if (PAGE_START.equals(name)) {
            return PAGE_START;
        } else if (PAGE_GAG.equals(name)) {
            return PAGE_GAG;
        } else {
            return PAGE_UNKNOWK + "(" + name + ")";
        }
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

        private static final String SEPARATOR = ";";

        public Page(String name, String floatType, String banner) {
            this.name = name;
            this.floatType = floatType;
            this.banner = banner;
        }

        @Override
        public String toString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(name).append(SEPARATOR)
                    .append(floatType).append(SEPARATOR)
                    .append(banner);
            return strBuilder.toString();
        }

        public static Page parseFromString(String str) {
            String[] params = str.split(SEPARATOR);
            if (params.length == 3) {
                return new Page(params[0],params[1],params[2]);
            } else {
                return null;
            }
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

    public static class PremiumMessages {
        public static final int DEFAULT_COUNT = 10;
        private static final int DEFAULT_TIMEOUT = 1000;
        /**
         * включен ли механизм для данного пользователя в булевых константах
         */
        private boolean mEnabled;
        /**
         * количесто отправляемых пользователю сообщений в штуках
         */
        private int mCount;
        /**
         * таймаут для отображения попапа покупки премиума в часах
         */
        private int mTimeout;

        public PremiumMessages(JSONObject premiumMessages) {
            if (premiumMessages != null) {
                mEnabled = premiumMessages.optBoolean("enabled");
                mCount = premiumMessages.optInt("count", DEFAULT_COUNT);
                mTimeout = premiumMessages.optInt("timeout", DEFAULT_TIMEOUT);
            }
        }

        public PremiumMessages(boolean enabled, int count, int timeout) {
            mEnabled = enabled;
            mCount = count;
            mTimeout = timeout;
        }

        public int getCount() {
            return mCount;
        }

        public boolean isNeedShow() {
            return mEnabled && (getLashShowTime() + mTimeout * 60 * 60 * 1000) < System.currentTimeMillis();
        }

        public void setPopupShowTime() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PreferenceManager.getDefaultSharedPreferences(App.getContext())
                            .edit()
                            .putLong(PREMIUM_MESSAGES_POPUP_SHOW_TIME, System.currentTimeMillis())
                            .commit();
                }
            }).run();
        }

        private long getLashShowTime() {
            return  PreferenceManager.getDefaultSharedPreferences(App.getContext())
                    .getLong(PREMIUM_MESSAGES_POPUP_SHOW_TIME, 0);
        }
    }
    public static class Closing {
        public static String DATA_FOR_CLOSING_RECEIVED_ACTION = "DATA_FOR_CLOSING_RECEIVED_ACTION";

        private static Ssid.ISsidUpdateListener listener;
        public boolean enableSympathies;
        public boolean enabledMutual;
        public int limitSympathies;
        public int limitMutual;

        public Closing() {
            if (listener == null) {
                listener = new Ssid.ISsidUpdateListener() {
                    @Override
                    public void onUpdate() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences pref = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putLong(Static.PREFERENCES_MUTUAL_CLOSING_LAST_TIME, 0);
                                editor.commit();
                            }
                        }).start();
                    }
                };
                Ssid.addUpdateListener(listener);
            }
        }

        public void onStopMutualClosings() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences pref = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    long currentTime = System.currentTimeMillis();
                    editor.putLong(Static.PREFERENCES_MUTUAL_CLOSING_LAST_TIME, currentTime);
                    editor.commit();
                }
            }).start();
        }

        public void onStopLikesClosings() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences pref = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    long currentTime = System.currentTimeMillis();
                    editor.putLong(Static.PREFERENCES_LIKES_CLOSING_LAST_TIME, currentTime);
                    editor.commit();
                }
            }).start();
        }

        public boolean isClosingsEnabled() {
            return (enabledMutual || enableSympathies) && !CacheProfile.premium;
        }

        public boolean isMutualClosingAvailable() {
            SharedPreferences pref =  App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
            long currentTime = System.currentTimeMillis();
            long lastCallTime = pref.getLong(Static.PREFERENCES_MUTUAL_CLOSING_LAST_TIME,0);
            return DateUtils.isOutside24Hours(lastCallTime, System.currentTimeMillis());
        }

        public boolean isLikesClosingAvailable() {
            SharedPreferences pref =  App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
            long lastCallTime = pref.getLong(Static.PREFERENCES_LIKES_CLOSING_LAST_TIME,0);
            return DateUtils.isOutside24Hours(lastCallTime, System.currentTimeMillis());
        }
    }
}
