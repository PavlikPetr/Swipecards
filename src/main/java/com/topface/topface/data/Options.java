package com.topface.topface.data;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.URLUtil;

import com.google.gson.annotations.SerializedName;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.data.experiments.ForceOfferwallRedirect;
import com.topface.topface.data.experiments.InstantMessagesForNewbies;
import com.topface.topface.data.experiments.SixCoinsSubscribeExperiment;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.UserGetAppOptionsRequest;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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

    public static final String OPTIONS_RECEIVED_ACTION = "com.topface.topface.options_received_action";

    public final static String INNER_MAIL_CONST = "mail";
    public final static String INNER_APNS_CONST = "apns";
    public final static String INNER_SEPARATOR = ":";

    public static final String PREMIUM_MESSAGES_POPUP_SHOW_TIME = "premium_messages_popup_last_show";
    public static final String PREMIUM_VISITORS_POPUP_SHOW_TIME = "premium_visitors_popup_last_show";
    public static final String PREMIUM_ADMIRATION_POPUP_SHOW_TIME = "premium_admirations_popup_last_show";
    protected static final String INSTANT_MSG = "instantMessageFromSearch";

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
     * Флаг непоказа восхищений
     */
    public boolean isHideAdmirations = false;

    /**
     * title и url для экрана "О программе"
     * по умолчанию отобразим "topface.com" с переходом на "http://topface.com", если сервер не пришлет другое значение
     */
    public AboutApp aboutApp = new AboutApp();

    /**
     * buttons add to leaders
     */
    public List<LeaderButton> buyLeaderButtons = new ArrayList<>();

    /**
     * Стоимость вставания в лидеры
     */
    public int priceLeader = 8;
    public int minLeadersPercent = 25; //Не уверен в этом, возможно стоит использовать другое дефолтное значение

    public String offerwall = OfferwallsManager.SPONSORPAY;

    public int premium_period;
    public int contacts_count = Integer.MAX_VALUE;
    public long popup_timeout;
    public boolean blockUnconfirmed;
    public boolean blockChatNotMutual;
    public Boolean scruffy = null;
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

    public TabsList otherTabs = new TabsList();
    public TabsList premiumTabs = new TabsList();

    /**
     * Ключ эксперимента под который попадает данный пользователь (передаем его в GA)
     */
    public ExperimentTags experimentTags;

    public Bonus bonus = new Bonus();
    public Offerwalls offerwalls = new Offerwalls();
    public boolean forceCoinsSubscriptions;

    public boolean unlockAllForPremium;
    public int maxMessageSize;
    public SixCoinsSubscribeExperiment sixCoinsSubscribeExperiment = new SixCoinsSubscribeExperiment();
    public ForceOfferwallRedirect forceOfferwallRedirect = new ForceOfferwallRedirect();
    public TopfaceOfferwallRedirect topfaceOfferwallRedirect = new TopfaceOfferwallRedirect();
    public InstantMessageFromSearch instantMessageFromSearch = new InstantMessageFromSearch();
    public FeedNativeAd feedNativeAd = new FeedNativeAd();
    public NotShown notShown = new NotShown();
    public InstantMessagesForNewbies instantMessagesForNewbies = new InstantMessagesForNewbies();
    public InterstitialInFeeds interstitial = new InterstitialInFeeds();

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
            fillLeaderButtons(response.optJSONObject("photofeed"));
            JSONObject aboutAppJson = response.optJSONObject("aboutApp");
            aboutApp = new AboutApp(aboutAppJson.optString("title"), aboutAppJson.optString("url"));
            offerwall = response.optString("offerwall");
            maxVersion = response.optString("maxVersion");
            blockUnconfirmed = response.optBoolean("blockUnconfirmed");
            blockChatNotMutual = response.optBoolean("blockChatNotMutual");

            JSONObject payments = response.optJSONObject("payments");

            if (payments != null) {
                JSONObject other = payments.optJSONObject("other");
                JSONObject premium = payments.optJSONObject("premium");
                otherTabs = JsonUtils.optFromJson(other.toString(), TabsList.class, new TabsList());
                premiumTabs = JsonUtils.optFromJson(premium.toString(), TabsList.class, new TabsList());
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
            isHideAdmirations = response.optBoolean("hideAdmirations", false);

            fallbackTypeBanner = response.optString("gag_type_banner", AdProvidersFactory.BANNER_ADMOB);
            gagTypeFullscreen = response.optString("gag_type_fullscreen", AdProvidersFactory.BANNER_NONE);
            scruffy = response.optBoolean("scruffy", false);
            JSONObject bonusObject = response.optJSONObject("bonus");
            if (bonusObject != null) {
                bonus.enabled = bonusObject.optBoolean("enabled");
                bonus.counter = bonusObject.optInt("counter");
                bonus.timestamp = bonusObject.optLong("counterTimestamp");
                bonus.integrationUrl = bonusObject.optString("integrationUrl");
                bonus.buttonText = bonusObject.optString("title", bonus.buttonText);
                String iconUrl = bonusObject.optString("iconUrl", bonus.buttonPicture);
                // проверяем валидность ссылки на картинку. Если ссылка не валидна, то подставим дефолт
                bonus.buttonPicture = URLUtil.isValidUrl(iconUrl) ? iconUrl : bonus.buttonPicture;
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
            sixCoinsSubscribeExperiment = JsonUtils.
                    optFromJson(new JSONObject().getJSONObject("unlockLikeList").toString(),
                            SixCoinsSubscribeExperiment.class, new SixCoinsSubscribeExperiment());
            topfaceOfferwallRedirect.init(response);

            instantMessageFromSearch = JsonUtils.optFromJson(response.optString(INSTANT_MSG),
                    InstantMessageFromSearch.class, new InstantMessageFromSearch());

            instantMessagesForNewbies.init(response);

            startPageFragmentId = getStartPageFragmentId(response);

            JSONObject jsonNotShown = response.optJSONObject("notShown");
            if (jsonNotShown != null) {
                notShown.parseNotShownJSON(jsonNotShown);
            }
            feedNativeAd.parseFeedAdJSON(response.optJSONObject("feedNativeAd"));
            interstitial = JsonUtils.optFromJson(response.optString("interstitial"),
                    InterstitialInFeeds.class, interstitial);

        } catch (Exception e) {
            Debug.error("Options parsing error", e);
        }

        if (response != null && cacheToPreferences) {
            CacheProfile.setOptions(this, response);
        } else {
            Debug.error(cacheToPreferences ? "Options from preferences" : "Options response is null");
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

    public void setPagesInfo(Map<String, PageInfo> pages) {
        this.pages = new HashMap<>(pages);
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

    public static class AboutApp {
        public String title;
        public String url;


        public AboutApp(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public AboutApp() {
            title = App.getContext().getString(R.string.settings_topface_url);
            url = App.getContext().getString(R.string.settings_topface_url_title);
        }
    }

    private void fillLeaderButtons(JSONObject photofeedObject) throws JSONException {
        String buttonsArrayKey = "items";
        if (photofeedObject == null || !photofeedObject.has(buttonsArrayKey)) {
            return;
        }
        JSONArray buttonsArray = photofeedObject.getJSONArray(buttonsArrayKey);
        if (buyLeaderButtons != null) {
            buyLeaderButtons.clear();
        } else {
            buyLeaderButtons = new ArrayList<>();
        }
        for (int i = 0; i < buttonsArray.length(); i++) {
            JSONObject buttonObj = buttonsArray.getJSONObject(i);
            if (buttonObj != null) {
                buyLeaderButtons.add(new LeaderButton(
                        buttonObj.optString("text"),
                        buttonObj.optInt("price"),
                        buttonObj.optInt("count")));
            }
        }
    }

    public static class LeaderButton {
        public String title;
        public int price;
        public int photoCount;

        public LeaderButton(String title, int price, int photoCount) {
            this.title = title;
            this.price = price;
            this.photoCount = photoCount;
        }
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
        public String buttonText = App.getContext().getString(R.string.general_bonus);// по умолчанию кнопка имеет название "Бонус"
        public String buttonPicture = null;// по умолчанию кнопка отображается с картинкой ic_bonus_1
    }

    public static class TabsList {
        @SerializedName("tabs")
        public LinkedList<Tab> list;

        public TabsList(LinkedList<Tab> list) {
            this.list = list;
        }

        public TabsList() {
            list = new LinkedList<>();
        }
    }

    public static class Tab {
        public static final String GPLAY = "google-play";
        public static final String AMAZON = "amazon";
        public static final String PWALL_MOBILE = "paymentwall-mobile";
        public static final String PWALL = "paymentwall-direct";
        public static final String BONUS = "bonus";

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
        }

        public String name;
        public String type;

        public Tab(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getUpperCaseName() {
            return name.toUpperCase(Locale.getDefault());
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

    public class InstantMessageFromSearch {
        public String text = Static.EMPTY;

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private BaseFragment.FragmentId getStartPageFragmentId(JSONObject response) {
        BaseFragment.FragmentId fragmentId = startPageFragmentId;
        try {
            fragmentId = BaseFragment.FragmentId.valueOf(response.optString("startPage"));
        } catch (IllegalArgumentException e) {
            Debug.error("Illegal value of startPage", e);
        }
        return fragmentId;
    }

    public boolean isScruffyEnabled() {
        return scruffy != null ? scruffy : false;
    }

    public class InterstitialInFeeds {
        public static final String FEED_NEWBIE = "NEWBIE";
        public static final String FEED = "NORMAL";

        public boolean enabled;
        public int count = 0;
        public long period = 0;
        public String adGroup = "";

        public boolean canShow() {
            UserConfig config = App.getUserConfig();
            if (config == null) {
                return enabled;
            } else {
                long diff = System.currentTimeMillis() - config.getInterstitialsInFeedFirstShow();
                if (diff > period * 1000) {
                    config.resetInterstitialInFeedsCounter();
                }
                return enabled || (count > 0 && (config.getInterstitialsInFeedCounter() < count));
            }
        }
    }
}
