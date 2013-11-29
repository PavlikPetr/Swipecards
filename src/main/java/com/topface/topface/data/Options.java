package com.topface.topface.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.topface.topface.App;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.blocks.BannerBlock;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.PopupManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

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
    public final static String BANNER_ADMOB = "ADMOB";
    public static final String BANNER_ADWIRED = "ADWIRED";
    public static final String BANNER_MOPUB = "MOPUB";
    public static final String BANNER_IVENGO = "IVENGO";
    public static final String BANNER_ADCAMP = "ADCAMP";
    public static final String BANNER_LIFESTREET = "LIFESTREET";
    public static final String BANNER_ADLAB = "ADLAB";
    public static final String BANNER_INNERACTIVE = "INNERACTIVE";
    public static final String BANNER_GAG = "GAG";
    public static final String BANNER_NONE = "NONE";
    public final static String[] BANNERS = new String[]{
            BANNER_TOPFACE,
            BANNER_ADMOB,
            BANNER_ADWIRED,
            BANNER_MOPUB,
            BANNER_IVENGO,
            BANNER_ADCAMP,
            BANNER_LIFESTREET,
            BANNER_ADLAB,
            BANNER_INNERACTIVE,
            BANNER_GAG,
            BANNER_NONE
    };

    /**
     * Идентификаторы для типов офферволлов
     */
    public static final String TAPJOY = "TAPJOY";
    public static final String SPONSORPAY = "SPONSORPAY";
    public static final String CLICKKY = "CLICKKY";
    public static final String RANDOM = "RANDOM";
    public static final String GETJAR = "GETJAR";
    public final static String[] OFFERWALLS = new String[]{
            TAPJOY,
            SPONSORPAY,
            CLICKKY,
            GETJAR,
            RANDOM
    };
    public static final String PREMIUM_MESSAGES_POPUP_SHOW_TIME = "premium_messages_popup_last_show";
    public static final String PREMIUM_VISITORS_POPUP_SHOW_TIME = "premium_visitors_popup_last_show";
    public static final String PREMIUM_ADMIRATION_POPUP_SHOW_TIME = "premium_admirations_popup_last_show";
    /**
     * Настройки для каждого типа страниц
     */
    public HashMap<String, Page> pages = new HashMap<String, Page>();

    public String ratePopupType = PopupManager.OFF_RATE_TYPE;
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

    public String offerwall = SPONSORPAY;

    public int premium_period;
    public int contacts_count;
    public long popup_timeout;
    public boolean block_unconfirmed;
    public boolean block_chat_not_mutual;
    public Closing closing = new Closing();
    public BlockSympathy blockSympathy = new BlockSympathy();
    public PremiumAirEntity premium_messages;
    public PremiumAirEntity premium_visitors;
    public PremiumAirEntity premium_admirations;
    public GetJar getJar;
    public String gagTypeBanner = BannerBlock.BANNER_ADMOB;
    public String gagTypeFullscreen = BannerBlock.BANNER_NONE;
    public String helpUrl;

    public Options(IApiResponse data) {
        this(data.getJsonResult());
    }

    public Options(JSONObject data) {
        this(data, true);
    }

    public Options(JSONObject data, boolean cacheToPreferences) {
        if (data != null) {
            fillData(data, cacheToPreferences);
        }
    }

    protected void fillData(JSONObject response, boolean cacheToPreferences) {
        try {
            priceAdmiration = response.optInt("admirationPrice");
            priceLeader = response.optInt("leaderPrice");
            minLeadersPercent = response.optInt("leaderPercent");
            // Pages initialization
            JSONArray pagesJson = response.optJSONArray("pages");
            for (int i = 0; i < pagesJson.length(); i++) {
                JSONObject page = pagesJson.getJSONObject(i);
                String pageName = getPageName(page, "name");
                pages.put(pageName,
                        new Page(
                                pageName,
                                page.optString("float"),
                                page.optString("banner")
                        )
                );
            }
            offerwall = response.optString("offerwall");
            maxVersion = response.optString("maxVersion");
            block_unconfirmed = response.optBoolean("blockUnconfirmed");
            block_chat_not_mutual = response.optBoolean("blockChatNotMutual");

            JSONObject contactsInvite = response.optJSONObject("inviteContacts");
            premium_period = contactsInvite.optInt("premiumPeriod");
            contacts_count = contactsInvite.optInt("contactsCount");
            popup_timeout = contactsInvite.optInt("showPopupTimeout") * 60 * 60 * 1000;

            if (response.has("premiumMessages")) {
                premium_messages = new PremiumAirEntity(
                        response.optJSONObject("premiumMessages"), PremiumAirEntity.AIR_MESSAGES
                );
            } else {
                premium_messages = new PremiumAirEntity(false, 10, 1000, PremiumAirEntity.AIR_MESSAGES);
            }

            if (response.has("visitors_popup")) {
                premium_visitors = new PremiumAirEntity(
                        response.optJSONObject("visitors_popup"), PremiumAirEntity.AIR_VISITORS
                );
            } else {
                premium_visitors = new PremiumAirEntity(false, 10, 1000, PremiumAirEntity.AIR_VISITORS);
            }

            if (response.has("admiration_popup")) {
                premium_admirations = new PremiumAirEntity(
                        response.optJSONObject("admiration_popup"), PremiumAirEntity.AIR_ADMIRATIONS
                );
            } else {
                premium_admirations = new PremiumAirEntity(false, 10, 1000, PremiumAirEntity.AIR_ADMIRATIONS);
            }

            if (response.has("links")) {
                JSONObject links = response.optJSONObject("links");
                if (links != null && links.has("paymentwall")) {
                    paymentwall = links.optString("paymentwall");
                }
            }

            JSONObject closingsObj = response.optJSONObject("closing");
            if (closing == null) closing = new Closing();
            if (closingsObj != null) {
                closing.enabledMutual = closingsObj.optBoolean("enabledMutual");
                closing.enabledSympathies = closingsObj.optBoolean("enabledSympathies");
                closing.limitMutual = closingsObj.optInt("limitMutual");
                closing.limitSympathies = closingsObj.optInt("limitSympathies");
            }

            JSONObject blockSympathyObj = response.optJSONObject("blockSympathy");
            if (blockSympathy == null) blockSympathy = new BlockSympathy();
            if (blockSympathyObj != null) {
                blockSympathy.enabled = blockSympathyObj.optBoolean("enabled");
                JSONObject settingsBlock = blockSympathyObj.optJSONObject("settings");
                if (settingsBlock != null) {
                    blockSympathy.text = settingsBlock.optString("text");
                    blockSympathy.buttonText = settingsBlock.optString("buttonText");
                    blockSympathy.showPhotos = settingsBlock.optBoolean("showPhotos");
                    blockSympathy.group = settingsBlock.optString("group");
                    blockSympathy.price = settingsBlock.optInt("price");
                }
            }

            ratePopupType = response.optJSONObject("ratePopup").optString("type");

            JSONObject getJarJson = response.optJSONObject("getjar");
            getJar = new GetJar(getJarJson.optString("id"), getJarJson.optString("name"), getJarJson.optLong("price"));

            gagTypeBanner = response.optString("gag_type_banner", BannerBlock.BANNER_ADMOB);
            gagTypeFullscreen = response.optString("gag_type_fullscreen", BannerBlock.BANNER_NONE);

            helpUrl = response.optString("helpUrl");
        } catch (Exception e) {
            Debug.error("Options parsing error", e);
        }

        if (response != null && cacheToPreferences) {
            CacheProfile.setOptions(this, response);
        } else {
            Debug.error("Options response is null");
        }

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

    public PremiumAirEntity getPremiumEntityByType(int type) {
        switch (type) {
            case PremiumAirEntity.AIR_ADMIRATIONS:
                return premium_admirations;
            case PremiumAirEntity.AIR_VISITORS:
                return premium_visitors;
            case PremiumAirEntity.AIR_MESSAGES:
                return premium_messages;
        }
        return null;
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
            return new StringBuilder(name).append(SEPARATOR)
                    .append(floatType).append(SEPARATOR)
                    .append(banner).toString();
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
            new BackgroundThread() {
                @Override
                public void execute() {
                    PreferenceManager.getDefaultSharedPreferences(App.getContext())
                            .edit()
                            .putLong(getPrefsConstant(), System.currentTimeMillis())
                            .commit();
                }
            };
        }

        public void clearPopupShowTime() {
            new BackgroundThread() {
                @Override
                public void execute() {
                    PreferenceManager.getDefaultSharedPreferences(App.getContext())
                            .edit()
                            .remove(getPrefsConstant())
                            .commit();
                }
            };
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
            return PreferenceManager.getDefaultSharedPreferences(App.getContext())
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
                        new BackgroundThread() {
                            @Override
                            public void execute() {
                                SharedPreferences pref = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putLong(Static.PREFERENCES_MUTUAL_CLOSING_LAST_TIME, 0);
                                editor.commit();
                            }
                        };
                    }
                };
                Ssid.addUpdateListener(listener);
            }
        }

        public void onStopMutualClosings() {
            new BackgroundThread() {
                @Override
                public void execute() {
                    SharedPreferences pref = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    long currentTime = System.currentTimeMillis();
                    editor.putLong(Static.PREFERENCES_MUTUAL_CLOSING_LAST_TIME, currentTime);
                    editor.commit();
                }
            };
        }

        public void onStopLikesClosings() {
            new BackgroundThread() {
                @Override
                public void execute() {
                    SharedPreferences pref = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    long currentTime = System.currentTimeMillis();
                    editor.putLong(Static.PREFERENCES_LIKES_CLOSING_LAST_TIME, currentTime);
                    editor.commit();
                }
            };
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

    public static class BlockSympathy {
        public boolean enabled = false;
        public String text = Static.EMPTY;
        public String buttonText = Static.EMPTY;
        public boolean showPhotos = true;
        public String group;
        public int price = 0;

        public BlockSympathy() {
        }
    }
}
