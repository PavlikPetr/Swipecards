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
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.ui.blocks.BannerBlock;
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

    public final static String INNER_MAIL_CONST = "mail";
    public final static String INNER_APNS_CONST = "apns";
    public final static String INNER_SEPARATOR = ":";

    public static final String PREMIUM_MESSAGES_POPUP_SHOW_TIME = "premium_messages_popup_last_show";
    public static final String PREMIUM_VISITORS_POPUP_SHOW_TIME = "premium_visitors_popup_last_show";
    public static final String PREMIUM_ADMIRATION_POPUP_SHOW_TIME = "premium_admirations_popup_last_show";
    /**
     * Настройки для каждого типа страниц
     */
    public HashMap<String, Page> pages = new HashMap<String, Page>();

    public String ratePopupType;
    private String paymentwall;

    public String maxVersion = "2147483647";
    /**
     * Стоимость отправки "Восхищения"
     */
    public int priceAdmiration = 1;
    /**
     * Стоимость вставания в лидеры
     */
    public int priceLeader = 8;
    public int minLeadersPercent = 25; //Не уверен в этом, возможно стоит использовать другое дефолтное значение

    public String offerwall;

    public int premium_period;
    public int contacts_count;
    public long popup_timeout;
    public boolean block_unconfirmed;
    public boolean block_chat_not_mutual;
    public Closing closing = new Closing();
    public PremiumAirEntity premium_messages;
    public PremiumAirEntity premium_visitors;
    public PremiumAirEntity premium_admirations;
    public GetJar getJar;
    public String gagTypeBanner = BannerBlock.BANNER_ADMOB;
    public String gagTypeFullscreen = BannerBlock.BANNER_NONE;

    public static Options parse(ApiResponse response) {
        Options options = new Options();

        try {
            options.priceAdmiration = response.jsonResult.optInt("admirationPrice");
            options.priceLeader = response.jsonResult.optInt("leaderPrice");
            options.minLeadersPercent = response.jsonResult.optInt("leaderPercent");
            // Pages initialization
            JSONArray pages = response.jsonResult.optJSONArray("pages");
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i);
                String pageName = getPageName(page, "name");
                options.pages.put(pageName,
                        new Page(
                                pageName,
                                page.optString("float"),
                                page.optString("banner")
                        )
                );
            }
            options.offerwall = response.jsonResult.optString("offerwall");
            options.maxVersion = response.jsonResult.optString("maxVersion");
            options.block_unconfirmed = response.jsonResult.optBoolean("blockUnconfirmed");
            options.block_chat_not_mutual = response.jsonResult.optBoolean("blockChatNotMutual");

            JSONObject contactsInvite = response.jsonResult.optJSONObject("inviteContacts");
            options.premium_period = contactsInvite.optInt("premiumPeriod");
            options.contacts_count = contactsInvite.optInt("contactsCount");
            options.popup_timeout = contactsInvite.optInt("showPopupTimeout") * 60 * 60 * 1000;

            if (response.jsonResult.has("premiumMessages")) {
                options.premium_messages = new PremiumAirEntity(
                        response.jsonResult.optJSONObject("premiumMessages"), PremiumAirEntity.AIR_MESSAGES
                );
            } else {
                options.premium_messages = new PremiumAirEntity(false, 10, 1000, PremiumAirEntity.AIR_MESSAGES);
            }

            if (response.jsonResult.has("visitors_popup")) {
                options.premium_visitors = new PremiumAirEntity(
                        response.jsonResult.optJSONObject("visitors_popup"), PremiumAirEntity.AIR_VISITORS
                );
            } else {
                options.premium_visitors = new PremiumAirEntity(false, 10, 1000, PremiumAirEntity.AIR_VISITORS);
            }

            if (response.jsonResult.has("admiration_popup")) {
                options.premium_admirations = new PremiumAirEntity(
                        response.jsonResult.optJSONObject("admiration_popup"), PremiumAirEntity.AIR_ADMIRATIONS
                );
            } else {
                options.premium_admirations = new PremiumAirEntity(false, 10, 1000, PremiumAirEntity.AIR_ADMIRATIONS);
            }

            if (response.jsonResult.has("links")) {
                JSONObject links = response.jsonResult.optJSONObject("links");
                if (links != null && links.has("paymentwall")) {
                    options.paymentwall = links.optString("paymentwall");
                }
            }

            JSONObject closings = response.jsonResult.optJSONObject("closing");
            if (options.closing == null) options.closing = new Closing();
            options.closing.enabledMutual = closings.optBoolean("enabledMutual");
            options.closing.enabledSympathies = closings.optBoolean("enabledSympathies");
            options.closing.limitMutual = closings.optInt("limitMutual");
            options.closing.limitSympathies = closings.optInt("limitSympathies");

            //TODO clarify parameter: timeout
            options.ratePopupType = response.jsonResult.optJSONObject("ratePopup").optString("type");

            JSONObject getJar = response.jsonResult.optJSONObject("getjar");
            options.getJar = new GetJar(getJar.optString("id"), getJar.optString("name"), getJar.optLong("price"));

            options.gagTypeBanner = response.jsonResult.optString("gag_type_banner", BannerBlock.BANNER_ADMOB);
            options.gagTypeFullscreen = response.jsonResult.optString("gag_type_fullscreen", BannerBlock.BANNER_NONE);
        } catch (Exception e) {
            Debug.error("Options parsing error", e);
        }

        CacheProfile.setOptions(options, response.getJsonResult());
        return options;
    }

    private static String getPageName(JSONObject page, String key) {
        String name = page.optString(key);
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

    public static String generateKey(int type, boolean isMail) {
        return Integer.toString(type) + INNER_SEPARATOR + ((isMail) ? INNER_MAIL_CONST : INNER_APNS_CONST);
    }

    public boolean containsBannerType(String bannerType) {
        for (Page page : pages.values()) {
            if (page.banner.equals(bannerType)) {
                return true;
            }
        }
        return false;
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
                return new Page(params[0], params[1], params[2]);
            } else {
                return null;
            }
        }
    }

    public String getPaymentwallLink() {
        return paymentwall;
    }

    public static class PremiumAirEntity {
        public static final int DEFAULT_COUNT = 10;
        private static final int DEFAULT_TIMEOUT = 1000;

        private int airType;
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

        public static final int AIR_NONE = 0;
        public static final int AIR_MESSAGES = 1;
        public static final int AIR_VISITORS = 2;
        public static final int AIR_ADMIRATIONS = 3;

        public PremiumAirEntity(JSONObject premiumMessages, int airType) {
            this.airType = airType;
            if (premiumMessages != null) {
                mEnabled = premiumMessages.optBoolean("enabled");
                mCount = premiumMessages.optInt("count", DEFAULT_COUNT);
                mTimeout = premiumMessages.optInt("timeout", DEFAULT_TIMEOUT);
            }
        }

        public PremiumAirEntity(boolean enabled, int count, int timeout, int type) {
            mEnabled = enabled;
            mCount = count;
            mTimeout = timeout;
            airType = type;
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
                            .putLong(getPrefsConstant(), System.currentTimeMillis())
                            .commit();
                }
            }).run();
        }

        public void clearPopupShowTime() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PreferenceManager.getDefaultSharedPreferences(App.getContext())
                            .edit()
                            .remove(getPrefsConstant())
                            .commit();
                }
            }).run();
        }

        public String getPrefsConstant() {
            switch (airType) {
                case AIR_MESSAGES:
                    return PREMIUM_MESSAGES_POPUP_SHOW_TIME;
                case AIR_VISITORS:
                    return PREMIUM_VISITORS_POPUP_SHOW_TIME;
                case AIR_ADMIRATIONS:
                    return PREMIUM_ADMIRATION_POPUP_SHOW_TIME;
            }

            return PREMIUM_MESSAGES_POPUP_SHOW_TIME;
        }

        private long getLashShowTime() {
            return  PreferenceManager.getDefaultSharedPreferences(App.getContext())
                    .getLong(getPrefsConstant(), 0);
        }
    }



    public static class Closing {
        public static String DATA_FOR_CLOSING_RECEIVED_ACTION = "DATA_FOR_CLOSING_RECEIVED_ACTION";

        private static Ssid.ISsidUpdateListener listener;
        public boolean enabledSympathies;
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
            return (enabledMutual || enabledSympathies) && !CacheProfile.premium;
        }

        public boolean isMutualClosingAvailable() {
            SharedPreferences pref = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
            long currentTime = System.currentTimeMillis();
            long lastCallTime = pref.getLong(Static.PREFERENCES_MUTUAL_CLOSING_LAST_TIME, 0);
            return DateUtils.isOutside24Hours(lastCallTime, System.currentTimeMillis());
        }

        public boolean isLikesClosingAvailable() {
            SharedPreferences pref = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
            long lastCallTime = pref.getLong(Static.PREFERENCES_LIKES_CLOSING_LAST_TIME, 0);
            return DateUtils.isOutside24Hours(lastCallTime, System.currentTimeMillis());
        }

        public void stopForPremium() {
            enabledMutual = false;
            enabledSympathies = false;
        }
    }

    public static class GetJar {
        String id = "unknown";
        String name = "coins";
        long price = Integer.MAX_VALUE;

        public GetJar(String id, String name, long price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public long getPrice() {
            return price;
        }
    }
}
