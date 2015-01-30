package com.topface.topface.data;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.data.experiments.AutoOpenGallery;
import com.topface.topface.data.experiments.ForceOfferwallRedirect;
import com.topface.topface.data.experiments.InstantMessageFromSearch;
import com.topface.topface.data.experiments.InstantMessagesForNewbies;
import com.topface.topface.data.experiments.LikesWithThreeTabs;
import com.topface.topface.data.experiments.MessagesWithTabs;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.UserGetAppOptionsRequest;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.ClosingsController;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Опции приложения
 * <p/>
 * NOTICE: В данном типе данных используем значения по умолчанию
 */
@SuppressWarnings("UnusedDeclaration")
public class Options extends AbstractData {

    public final static String INNER_MAIL_CONST = "mail";
    public final static String INNER_APNS_CONST = "apns";
    public final static String INNER_SEPARATOR = ":";

    public static final String PREMIUM_MESSAGES_POPUP_SHOW_TIME = "premium_messages_popup_last_show";
    public static final String PREMIUM_VISITORS_POPUP_SHOW_TIME = "premium_visitors_popup_last_show";
    public static final String PREMIUM_ADMIRATION_POPUP_SHOW_TIME = "premium_admirations_popup_last_show";

    /**
     * Настройки для каждого типа страниц
     */
    private HashMap<String, PageInfo> pages = new HashMap<>();

    public boolean ratePopupEnabled = false;
    public long ratePopupTimeout = DateUtils.DAY_IN_MILLISECONDS;

    private String paymentwall;

    public String maxVersion = "2147483647";
    /**
     * Стоимость отправки "Восхищения"
     */
    public int priceAdmiration = 1;

    /**
     * Id фрагмента, который будет отображаться при старте приложения
     * По умолчанию откроем раздел "Знакомства", если сервер не переопределит его
     */
    public BaseFragment.FragmentId startPageFragmentId = BaseFragment.FragmentId.DATING;

    /**
     * Флаг отображения превью в диалогах
     */
    public boolean hidePreviewDialog;

    /**
     * Стоимость вставания в лидеры
     */
    public int priceLeader = 8;
    public int minLeadersPercent = 25; //Не уверен в этом, возможно стоит использовать другое дефолтное значение

    public String offerwall = OfferwallsManager.SPONSORPAY;

    public int premium_period;
    public int contacts_count = 10;
    public long popup_timeout;
    public boolean blockUnconfirmed;
    public boolean blockChatNotMutual;
    public Closing closing = new Closing();
    public BlockSympathy blockSympathy = new BlockSympathy();
    public BlockPeopleNearby blockPeople = new BlockPeopleNearby();
    public boolean isActivityAllowed = true; //Разрешено ли пользователю ставить лайки и совершать прочую активность
    /**
     * Настройки для промо-попапа 3-1
     */
    public PromoPopupEntity premiumMessages;
    /**
     * Настройки для промо-попапа 7-1
     */
    public PromoPopupEntity premiumVisitors;
    /**
     * Настройки для промо-попапа 8-1
     */
    public PromoPopupEntity premiumAdmirations;
    public GetJar getJar;
    public String fallbackTypeBanner = AdProvidersFactory.BANNER_ADMOB;
    public String gagTypeFullscreen = AdProvidersFactory.BANNER_NONE;
    public String helpUrl;

    public LinkedList<Tab> otherTabs = new LinkedList<>();
    public LinkedList<Tab> premiumTabs = new LinkedList<>();

    /**
     * Ключ эксперимента под который попадает данный пользователь (передаем его в GA)
     */
    public ExperimentTags experimentTags;

    public Bonus bonus = new Bonus();
    public Offerwalls offerwalls = new Offerwalls();
    public boolean forceCoinsSubscriptions;

    public boolean unlockAllForPremium;
    public int maxMessageSize;

    public ForceOfferwallRedirect forceOfferwallRedirect = new ForceOfferwallRedirect();

    public TopfaceOfferwallRedirect topfaceOfferwallRedirect = new TopfaceOfferwallRedirect();

    public InstantMessageFromSearch instantMessageFromSearch = new InstantMessageFromSearch();

    public FeedNativeAd feedNativeAd = new FeedNativeAd();

    public AutoOpenGallery autoOpenGallery = new AutoOpenGallery();

    public NotShown notShown = new NotShown();

    public LikesWithThreeTabs likesWithThreeTabs = new LikesWithThreeTabs();

    public InstantMessagesForNewbies instantMessagesForNewbies = new InstantMessagesForNewbies();

    public MessagesWithTabs messagesWithTabs = new MessagesWithTabs();
    private Map<String, PageInfo> pagesInfo;

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
            // по умолчанию превью в диалогах всегда отображаем
            hidePreviewDialog = response.optBoolean("hidePreviewDialog", false);
            priceLeader = response.optInt("leaderPrice");
            minLeadersPercent = response.optInt("leaderPercent");
            // Pages initialization
            PageInfo[] pagesArr = JsonUtils.fromJson(response.optString("pages"), PageInfo[].class);
            for (PageInfo pageInfo : pagesArr) {
                pages.put(pageInfo.name, pageInfo);
            }
            offerwall = response.optString("offerwall");
            maxVersion = response.optString("maxVersion");
            blockUnconfirmed = response.optBoolean("blockUnconfirmed");
            blockChatNotMutual = response.optBoolean("blockChatNotMutual");

            JSONObject payments = response.optJSONObject("payments");

            if (payments != null) {
                JSONObject other = payments.optJSONObject("other");
                JSONObject premium = payments.optJSONObject("premium");
                fillTabs(other, otherTabs);
                fillTabs(premium, premiumTabs);
            }

            JSONObject contactsInvite = response.optJSONObject("inviteContacts");
            if (contactsInvite != null) {
                premium_period = contactsInvite.optInt("premiumPeriod");
                contacts_count = contactsInvite.optInt("contactsCount");
                popup_timeout = contactsInvite.optInt("showPopupTimeout") * 60 * 60 * 1000;
            }

            if (response.has("premiumMessages")) {
                premiumMessages = new PromoPopupEntity(
                        response.optJSONObject("premiumMessages"), PromoPopupEntity.AIR_MESSAGES
                );
            } else {
                premiumMessages = new PromoPopupEntity(false, 10, 1000, PromoPopupEntity.AIR_MESSAGES);
            }

            if (response.has("visitorsPopup")) {
                premiumVisitors = new PromoPopupEntity(
                        response.optJSONObject("visitorsPopup"), PromoPopupEntity.AIR_VISITORS
                );
            } else {
                premiumVisitors = new PromoPopupEntity(false, 10, 1000, PromoPopupEntity.AIR_VISITORS);
            }

            if (response.has("admirationPopup")) {
                premiumAdmirations = new PromoPopupEntity(
                        response.optJSONObject("admirationPopup"), PromoPopupEntity.AIR_ADMIRATIONS
                );
            } else {
                premiumAdmirations = new PromoPopupEntity(false, 10, 1000, PromoPopupEntity.AIR_ADMIRATIONS);
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
                closing.timeoutSympathies = closingsObj.optInt("timeoutSympathies", Closing.DEFAULT_LIKES_TIMEOUT) * DateUtils.MINUTE_IN_MILLISECONDS;
                closing.timeoutMutual = closingsObj.optInt("timeoutMutual", Closing.DEFAULT_MUTUALS_TIMEOUT) * DateUtils.MINUTE_IN_MILLISECONDS;
            }

            JSONObject ratePopupObject = response.optJSONObject("applicationRatePopup");
            if (ratePopupObject != null) {
                ratePopupEnabled = ratePopupObject.optBoolean("enabled");
                ratePopupTimeout = ratePopupObject.optInt("timeout") * DateUtils.HOUR_IN_MILLISECONDS;
            }

            JSONObject blockSympathyObj = response.optJSONObject("blockSympathy");
            if (blockSympathy == null) blockSympathy = new BlockSympathy();
            if (blockSympathyObj != null) {
                blockSympathy.enabled = blockSympathyObj.optBoolean("enabled");
                JSONObject settingsBlock = blockSympathyObj.optJSONObject("settings");
                if (settingsBlock != null) {
                    blockSympathy.text = settingsBlock.optString("text");
                    blockSympathy.buttonText = settingsBlock.optString("buttonText");
                    blockSympathy.textPremium = settingsBlock.optString("textPremium");
                    blockSympathy.buttonTextPremium = settingsBlock.optString("buttonTextPremium");
                    blockSympathy.showPhotos = settingsBlock.optBoolean("showPhotos");
                    blockSympathy.group = settingsBlock.optString("group");
                    blockSympathy.price = settingsBlock.optInt("price");
                }
            }

            JSONObject blockPeopleJson = response.optJSONObject("blockPeopleNearby");
            blockPeople = blockPeople == null ?
                    new BlockPeopleNearby() : blockPeople;
            if (blockPeopleJson != null) {
                blockPeople.enabled = blockPeopleJson.optBoolean("enabled");
                blockPeople.text = blockPeopleJson.optString("text");
                blockPeople.buttonText = blockPeopleJson.optString("buttonText");
                blockPeople.textPremium = blockPeopleJson.optString("textPremium");
                blockPeople.buttonTextPremium = blockPeopleJson.optString("buttonTextPremium");
                blockPeople.price = blockPeopleJson.optInt("price");
            }

            JSONObject getJarJson = response.optJSONObject("getjar");
            if (getJarJson != null) {
                getJar = new GetJar(getJarJson.optString("id"), getJarJson.optString("name"), getJarJson.optLong("price"));
            }

            fallbackTypeBanner = response.optString("gag_type_banner", AdProvidersFactory.BANNER_ADMOB);
            gagTypeFullscreen = response.optString("gag_type_fullscreen", AdProvidersFactory.BANNER_NONE);
            JSONObject bonusObject = response.optJSONObject("bonus");
            if (bonusObject != null) {
                bonus.enabled = bonusObject.optBoolean("enabled");
                bonus.counter = bonusObject.optInt("counter");
                bonus.timestamp = bonusObject.optLong("counterTimestamp");
                bonus.integrationUrl = bonusObject.optString("integrationUrl");
            }
            // offerwalls for
            JSONObject jsonOfferwalls = response.optJSONObject("offerwalls");
            if (jsonOfferwalls != null) {
                offerwalls.mainText = jsonOfferwalls.optString("mainText");
                offerwalls.extraText = jsonOfferwalls.optString("extraText", null);
                fillOffers(offerwalls.mainOffers, jsonOfferwalls.optJSONArray("mainOffers"));
                fillOffers(offerwalls.extraOffers, jsonOfferwalls.optJSONArray("extraOffers"));
            }

            isActivityAllowed = response.optBoolean("isActivityAllowed", true);

            helpUrl = response.optString("helpUrl");
            JSONObject tagsObject = response.optJSONObject("experimentTags");
            if (tagsObject != null && tagsObject.length() > 0) {
                experimentTags = new ExperimentTags(tagsObject);
            }

            forceCoinsSubscriptions = response.optBoolean("forceCoinsSubscriptions");
            unlockAllForPremium = response.optBoolean("unlockAllForPremium");

            maxMessageSize = response.optInt("maxMessageSize");

            // experiments init
            forceOfferwallRedirect.init(response);

            topfaceOfferwallRedirect.init(response);

            instantMessageFromSearch.init(response);

            autoOpenGallery.init(response);

            likesWithThreeTabs.init(response);

            instantMessagesForNewbies.init(response);

            messagesWithTabs.init(response);

            startPageFragmentId = getStartPageFragmentId(response);

            JSONObject jsonNotShown = response.optJSONObject("notShown");
            if (jsonNotShown != null) {
                notShown.parseNotShownJSON(jsonNotShown);
            }

            feedNativeAd.parseFeedAdJSON(response.optJSONObject("feedNativeAd"));


        } catch (Exception e) {
            Debug.error("Options parsing error", e);
        }

        if (response != null && cacheToPreferences) {
            CacheProfile.setOptions(this, response);
        } else {
            Debug.error(cacheToPreferences ? "Options from preferences" : "Options response is null");
        }

    }

    private void fillTabs(JSONObject other, LinkedList<Tab> tabs) {
        JSONArray tabsArray = other.optJSONArray("tabs");
        for (int i = 0; i < tabsArray.length(); i++) {
            JSONObject tabObject = tabsArray.optJSONObject(i);
            tabs.add(new Tab(tabObject.optString("name"), tabObject.optString("type")));
        }
    }

    private void fillOffers(List<Offerwalls.Offer> list, JSONArray offersArrObj) throws JSONException {
        if (offersArrObj == null) return;
        for (int i = 0; i < offersArrObj.length(); i++) {
            JSONObject offerObj = offersArrObj.getJSONObject(i);
            if (offerObj != null) {
                Offerwalls.Offer offer = new Offerwalls.Offer();
                offer.text = offerObj.optString("text");
                offer.action = offerObj.optString("action");
                offer.type = offerObj.optInt("type");
                list.add(offer);
            }
        }
    }

    public PromoPopupEntity getPremiumEntityByType(int type) {
        switch (type) {
            case PromoPopupEntity.AIR_ADMIRATIONS:
                return premiumAdmirations;
            case PromoPopupEntity.AIR_VISITORS:
                return premiumVisitors;
            case PromoPopupEntity.AIR_MESSAGES:
                return premiumMessages;
        }
        return null;
    }

    public static String generateKey(int type, boolean isMail) {
        return Integer.toString(type) + INNER_SEPARATOR + ((isMail) ? INNER_MAIL_CONST : INNER_APNS_CONST);
    }

    public Map<String, PageInfo> getPagesInfo() {
        return pages;
    }

    public void setPagesInfo(Map<String, PageInfo> pagesInfo) {
        this.pagesInfo = new HashMap<>(pagesInfo);
    }

    public static void sendUpdateOptionsBroadcast() {
        LocalBroadcastManager.getInstance(App.getContext())
                .sendBroadcast(new Intent(UserGetAppOptionsRequest.OPTIONS_UPDATE_ACTION));
    }

    public String getPaymentwallLink() {
        return paymentwall;
    }

    public boolean containsBannerType(String bannerType) {
        for (PageInfo page : pages.values()) {
            if (page.getBanner().equals(bannerType)) {
                return true;
            }
        }
        return false;
    }


    public static class PromoPopupEntity {
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

        public PromoPopupEntity(JSONObject premiumMessages, int airType) {
            this.airType = airType;
            if (premiumMessages != null) {
                mEnabled = premiumMessages.optBoolean("enabled");
                mCount = premiumMessages.optInt("count", DEFAULT_COUNT);
                mTimeout = premiumMessages.optInt("timeout", DEFAULT_TIMEOUT);
            }
        }

        public PromoPopupEntity(boolean enabled, int count, int timeout, int type) {
            mEnabled = enabled;
            mCount = count;
            mTimeout = timeout;
            airType = type;
        }

        public int getCount() {
            return mCount;
        }

        public boolean isNeedShow() {
            return mEnabled && (getLastShowTime() + mTimeout * 60 * 60 * 1000) < System.currentTimeMillis();
        }

        public void setPopupShowTime() {
            UserConfig config = App.getUserConfig();
            config.setPromoPopupLastTime(getPopupAirType(), System.currentTimeMillis());
            config.saveConfig();
        }

        public void clearPopupShowTime() {
            App.getUserConfig().resetPromoPopupData(getPopupAirType());
        }

        public int getPopupAirType() {
            return airType;
        }

        private long getLastShowTime() {
            return App.getUserConfig().getPromoPopupLastTime(getPopupAirType());
        }
    }


    public static class Closing {
        private static final int DEFAULT_LIKES_TIMEOUT = 24 * 60;
        private static final int DEFAULT_MUTUALS_TIMEOUT = 10;

        public static final String DATA_FOR_CLOSING_RECEIVED_ACTION = "closings_received_action";
        private static Ssid.ISsidUpdateListener listener;
        public boolean enabledSympathies;
        public boolean enabledMutual;
        public int limitSympathies;
        public int limitMutual;
        public long timeoutSympathies;
        public long timeoutMutual;

        public void onStopMutualClosings() {
            UserConfig config = App.getUserConfig();
            config.setMutualClosingsLastTime(System.currentTimeMillis());
            config.saveConfig();
        }

        public void onStopLikesClosings() {
            UserConfig config = App.getUserConfig();
            config.setLikesClosingsLastTime(System.currentTimeMillis());
            config.saveConfig();
        }

        public boolean isClosingsEnabled() {
            return (isLikesAvailable() || isMutualAvailable());
        }

        public boolean isMutualAvailable() {
            long diff = Math.abs(System.currentTimeMillis() - App.getUserConfig().getMutualClosingsLastTime());
            Debug.log(ClosingsController.TAG, "time in sec from last mutuals show = " + diff / 1000);
            return enabledMutual && diff > timeoutMutual && CacheProfile.unread_mutual > 0;
        }

        public boolean isLikesAvailable() {
            long diff = Math.abs(System.currentTimeMillis() - App.getUserConfig().getLikesClosingsLastTime());
            Debug.log(ClosingsController.TAG, "time in sec from last likes show = " + diff / 1000);
            return enabledSympathies && diff > timeoutSympathies && CacheProfile.unread_likes > 0;
        }
    }

    public static class GetJar {
        String id = Static.UNKNOWN;
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
        public String textPremium;
        public String buttonTextPremium;
        public boolean showPhotos = true;
        public String group = Static.UNKNOWN;
        public int price = 0;
    }

    public static class BlockPeopleNearby {
        public boolean enabled = false;
        public String text = Static.EMPTY;
        public String buttonText = Static.EMPTY;
        public String textPremium;
        public String buttonTextPremium;
        public int price = 0;
    }

    public static class Bonus {
        public boolean enabled;
        public int counter;
        public long timestamp;
        public String integrationUrl;
    }

    public static class Tab {
        public static final String GPLAY = "google-play";
        public static final String AMAZON = "amazon";
        public static final String PWALL_MOBILE = "paymentwall-mobile";
        public static final String PWALL = "paymentwall-direct";
        public static final String BONUS = "bonus";
        public static final String FORTUMO = "fortumo";

        /**
         * !!! IMPORTANT !!!
         * markets stores all available markets. Used to delete missing tabs on older client versions.
         * Add all new purchase tabs to markets.
         */
        public static Set<String> markets = new HashSet<>();

        static {
            markets.add(GPLAY);
            markets.add(AMAZON);
            markets.add(PWALL_MOBILE);
            markets.add(PWALL);
            markets.add(BONUS);
            markets.add(FORTUMO);
        }

        public String name;
        public String type;

        public Tab(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    public static class Offerwalls {
        public String mainText;
        public String extraText;
        public List<Offer> mainOffers = new ArrayList<>();
        public List<Offer> extraOffers = new ArrayList<>();

        public static class Offer {
            public static final int TYPE_MAIN = 1;
            public static final int TYPE_EXTRA = 0;
            public String text;
            public String action;
            public int type;
        }

        public boolean hasOffers() {
            return !mainOffers.isEmpty() && !extraOffers.isEmpty();
        }
    }

    public static class NotShown {
        public boolean enabledDatingLockPopup = false;
        public long datingLockPopupTimeout = DateUtils.DAY_IN_SECONDS;
        public String title;
        public String text;

        public void parseNotShownJSON(JSONObject jsonNotShown) {
            if (jsonNotShown != null) {
                enabledDatingLockPopup = jsonNotShown.optBoolean("enabled");
                datingLockPopupTimeout = jsonNotShown.optLong("timeout");
                title = jsonNotShown.optString("title");
                text = jsonNotShown.optString("text");
            }
        }
    }

    public static class FeedNativeAd {
        public boolean enabled;
        public String type;
        public int dailyShows;
        public int positionMax;
        public int positionMin;
        private Random random = new Random(System.currentTimeMillis());

        public void parseFeedAdJSON(JSONObject jsonFeedAd) {
            if (jsonFeedAd != null) {
                enabled = jsonFeedAd.optBoolean("enabled");
                type = jsonFeedAd.optString("type");
                dailyShows = jsonFeedAd.optInt("dailyShows");
                positionMin = jsonFeedAd.optInt("positionMin");
                positionMax = jsonFeedAd.optInt("positionMax");
            } else {
                enabled = false;
            }
        }

        public int getPosition() {
            return random.nextInt(positionMax - positionMin + 1) + positionMin;
        }
    }

    private BaseFragment.FragmentId getStartPageFragmentId(JSONObject response) {
        BaseFragment.FragmentId fragmentId = startPageFragmentId;
        try {
            fragmentId = BaseFragment.FragmentId.valueOf(response.optString("startPage"));
        } catch (IllegalArgumentException e) {
            Debug.error("Illegal value of startPage", e);
        }
        if (messagesWithTabs.isEnabled()) {
            switch (fragmentId) {
                case FANS:
                case DIALOGS:
                    fragmentId = BaseFragment.FragmentId.TABBED_DIALOGS;
                    break;
                case MUTUAL:
                case ADMIRATIONS:
                case LIKES:
                    fragmentId = BaseFragment.FragmentId.TABBED_LIKES;
                    break;
            }
        }
        return fragmentId;
    }
}
