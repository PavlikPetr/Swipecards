package com.topface.topface.data;


import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.data.experiments.ForceOfferwallRedirect;
import com.topface.topface.data.experiments.InstantMessagesForNewbies;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.UserGetAppOptionsRequest;
import com.topface.topface.ui.bonus.models.OfferwallsSettings;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.topface.topface.data.PurchasesTabData.PAYMENT_NINJA;
import static com.topface.topface.data.leftMenu.FragmentIdData.DATING;
import static com.topface.topface.data.leftMenu.FragmentIdData.UNDEFINED;

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
    public static final String UNKNOWN = "unknown";

    public static final String PREMIUM_MESSAGES_POPUP_SHOW_TIME = "premium_messages_popup_last_show";
    public static final String PREMIUM_VISITORS_POPUP_SHOW_TIME = "premium_visitors_popup_last_show";
    public static final String PREMIUM_ADMIRATION_POPUP_SHOW_TIME = "premium_admirations_popup_last_show";
    protected static final String INSTANT_MSG = "instantMessageFromSearch";

    private final static int TRIAL_VIP_MAX_SHOW_COUNT = 10;

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
     *
     */
    public boolean isAutoreplyAllow = true;
    /**
     * data for experiment of Trial VIP
     */
    public TrialVipExperiment trialVipExperiment = new TrialVipExperiment();

    /**
     * udate url (path to application on market)
     */
    public String updateUrl;

    /**
     * manage SmsInvite screen
     */
    public ForceSmsInviteRedirect forceSmsInviteRedirect = new ForceSmsInviteRedirect();

    /**
     * Id фрагмента, который будет отображаться при старте приложения
     * По умолчанию откроем раздел "Знакомства", если сервер не переопределит его
     */
    @FragmentIdData.FragmentId
    public int startPage = DATING;

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

    public Payments payments = new Payments();

    /**
     * Ключ эксперимента под который попадает данный пользователь (передаем его в GA)
     */
    public ExperimentTags experimentTags;
    public AppOfTheDay appOfTheDay;
    public Offerwalls offerwalls = new Offerwalls();
    public boolean forceCoinsSubscriptions;
    public boolean mutualPopupEnabled;
    public boolean showRefillBalanceInSideMenu;
    public boolean enableFacebookInvite;
    public boolean unlockAllForPremium;
    public int maxMessageSize = 10000;
    public ForceOfferwallRedirect forceOfferwallRedirect = new ForceOfferwallRedirect();
    transient public TopfaceOfferwallRedirect topfaceOfferwallRedirect = new TopfaceOfferwallRedirect();
    public InstantMessageFromSearch instantMessageFromSearch = new InstantMessageFromSearch();
    public FeedNativeAd feedNativeAd = new FeedNativeAd();
    public NotShown notShown = new NotShown();
    transient public InstantMessagesForNewbies instantMessagesForNewbies = new InstantMessagesForNewbies();
    public InterstitialInFeeds interstitial = new InterstitialInFeeds();

    /**
     * Набор разнообразных параметров срезов по пользователю, для статистики
     */
    public HashMap<String, Object> statisticsSlices = new HashMap<>();

    /**
     * массив пунктов левого меню от интеграторов
     */
    public ArrayList<LeftMenuIntegrationItems> leftMenuItems = new ArrayList<>();

    /**
     * настройки для оферволов на экране Бонус
     */
    public OfferwallsSettings offerwallsSettings = new OfferwallsSettings();

    /**
     * {Boolean} dialogRedesignEnabled - флаг определяющий показ нового экрана диалогов, настройки
     */
    @Deprecated
    private boolean dialogRedesignEnabled;

    /**
     * {Integer} dialogRedesign - версия дизайна лайков/сообщений
     * 0 - дефолт (старые диалоги)
     * 1 - новые диалоги (замена для флажка dialogRedesignEnabled, который останется для старых клиентов)
     * 2 - новый экран диалогов + убрать табы в симпатиях, оставить только одну, основную, страничку
     * 3 - новый экран диалогов + вернуть все табы в симпатиях
     */
    private int dialogRedesign;

    /**
     * {Boolean} peopleNearbyRedesignEnabled - флаг определяющий показ нового экрана "Люди рядом"
     */
    public boolean peopleNearbyRedesignEnabled;

    /**
     * {Boolean} datingRedesignEnabled - флаг определяющий показ нового экрана "Знакомства"
     */
    public Boolean datingRedesignEnabled = false;

    /**
     * {FBInviteSettings} - настройки для приглашения в приложение друзей из FB
     */
    public FBInviteSettings fbInviteSettings = new FBInviteSettings();

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
            JSONObject statisticsSlicesSource = response.optJSONObject("statisticsSlices");
            //noinspection unchecked
            statisticsSlices = statisticsSlicesSource != null ? JsonUtils.fromJson(statisticsSlicesSource.toString(), HashMap.class) : new HashMap<>();
            priceAdmiration = response.optInt("admirationPrice");
            isAutoreplyAllow = response.optBoolean("allowAutoreply", true);
            trialVipExperiment = JsonUtils.optFromJson(response.optString("experimentTrialVip"), TrialVipExperiment.class, new TrialVipExperiment());
            forceSmsInviteRedirect = JsonUtils.optFromJson(response.optString("forceSmsInviteRedirect"), ForceSmsInviteRedirect.class, new ForceSmsInviteRedirect());
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
            updateUrl = response.optString("updateUrl", App.getContext().getString(R.string.app_update_url));
            aboutApp = new AboutApp(aboutAppJson.optString("title"), aboutAppJson.optString("url"));
            maxVersion = response.optString("maxVersion");
            blockUnconfirmed = response.optBoolean("blockUnconfirmed");
            blockChatNotMutual = response.optBoolean("blockChatNotMutual");

            JSONObject payments = response.optJSONObject("payments");

            if (payments != null) {
                this.payments = JsonUtils.optFromJson(payments.toString(), Payments.class, new Payments());
            }
            this.payments.other.list.add(0, new PurchasesTabData("БАНКОВСКАЯ КАРТА", PAYMENT_NINJA));

            JSONObject contactsInvite = response.optJSONObject("inviteContacts");
            if (contactsInvite != null) {
                premium_period = contactsInvite.optInt("premiumPeriod");
                contacts_count = contactsInvite.optInt("contactsCount");
                popup_timeout = contactsInvite.optInt("showPopupTimeout") * 60 * 60 * 1000;
            }

            premiumMessages = response.has("premiumMessages")
                    ? new PromoPopupEntity(response.optJSONObject("premiumMessages"), PromoPopupEntity.AIR_MESSAGES)
                    : new PromoPopupEntity(false, 10, 1000, PromoPopupEntity.AIR_MESSAGES, "", 0);

            premiumVisitors = response.has("visitorsPopup")
                    ? new PromoPopupEntity(response.optJSONObject("visitorsPopup"), PromoPopupEntity.AIR_VISITORS)
                    : new PromoPopupEntity(false, 10, 1000, PromoPopupEntity.AIR_VISITORS, "", 0);

            premiumAdmirations = response.has("admirationPopup")
                    ? new PromoPopupEntity(response.optJSONObject("admirationPopup"), PromoPopupEntity.AIR_ADMIRATIONS)
                    : new PromoPopupEntity(false, 10, 1000, PromoPopupEntity.AIR_ADMIRATIONS, "", 0);

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
            App.isScruffyEnabled = scruffy;
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

            maxMessageSize = response.optInt("maxMessageSize", 10000);

            // experiments init
            forceOfferwallRedirect.init(response);
            topfaceOfferwallRedirect.init(response);
            datingRedesignEnabled = response.optBoolean("datingRedesignEnabled");

            instantMessageFromSearch = JsonUtils.optFromJson(response.optString(INSTANT_MSG),
                    InstantMessageFromSearch.class, new InstantMessageFromSearch());

            instantMessagesForNewbies.init(response);

            startPage = getStartPageFragmentId(response);

            JSONObject jsonNotShown = response.optJSONObject("notShown");
            if (jsonNotShown != null) {
                notShown.parseNotShownJSON(jsonNotShown);
            }
            feedNativeAd.parseFeedAdJSON(response.optJSONObject("feedNativeAd"));
            interstitial = JsonUtils.optFromJson(response.optString("interstitial"),
                    InterstitialInFeeds.class, interstitial);
            if (response.has("leftMenuItems")) {
                leftMenuItems = JsonUtils.fromJson(response.getJSONArray("leftMenuItems").toString(), new TypeToken<ArrayList<LeftMenuIntegrationItems>>() {
                });
            }
            JSONObject offerwallsSettingsJsonObject = response.optJSONObject("offerwallsSettings");
            if (offerwallsSettingsJsonObject != null) {
                offerwallsSettings = JsonUtils.fromJson(offerwallsSettingsJsonObject.toString(), OfferwallsSettings.class);
            }
            JSONObject appOfTheDayJsonObject = response.optJSONObject("appOfTheDay");
            if (appOfTheDayJsonObject != null) {
                appOfTheDay = JsonUtils.optFromJson(appOfTheDayJsonObject.toString(), AppOfTheDay.class, new AppOfTheDay());
            }

            showRefillBalanceInSideMenu = response.optBoolean("showRefillBalanceInSideMenu");
            dialogRedesignEnabled = response.optBoolean("dialogRedesignEnabled");
            dialogRedesign = response.optInt("dialogRedesign");
            peopleNearbyRedesignEnabled = response.optBoolean("peopleNearbyRedesignEnabled");
            enableFacebookInvite = response.optBoolean("enableFacebookInvite");
            mutualPopupEnabled = response.optBoolean("mutualPopupEnabled");
            JSONObject fbInvitesJsonObject = response.optJSONObject("fbInvite");
            if (fbInvitesJsonObject != null) {
                fbInviteSettings = JsonUtils.fromJson(fbInvitesJsonObject.toString(), FBInviteSettings.class);
            }

        } catch (Exception e) {
            // отображение максимально заметного тоста, чтобы на этапе тестирования любого функционала
            // не пропустить ошибку парсинга опций, т.к. это может приветси к денежным потерям проекта
            // вызызывается только для сборок debug & qa
            new Handler(App.getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG) {
                        Utils.showCustomToast(R.string.options_parsing_error);
                    }
                }
            });
            Debug.error("Options parsing error", e);
        }
        if (cacheToPreferences) {
            App.getAppComponent().appState().setData(this);
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

    public boolean getDialogRedesignEnabled() {
        return dialogRedesignEnabled;
    }

    public int getDialogDesignVersion() {
        return dialogRedesign;
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
            title = App.getContext().getString(R.string.settings_topface_url_title);
            url = App.getContext().getString(R.string.settings_topface_url);
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

        /**
         * визуализация попапа меняется в зависимости от версии
         */
        private int mPopupVersion;

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
        /**
         * id страницы, где показывать попап (ориентируемся на FragmentId)
         */
        private int mPageId;

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
                mPageId = FragmentIdData.getFragmentId(premiumMessages.optString("page"), UNDEFINED);
                mPopupVersion = premiumMessages.optInt("popupVersion", 0);
            }
        }

        public PromoPopupEntity(boolean enabled, int count, int timeout, int type, String page, int popupVersion) {
            mEnabled = enabled;
            mCount = count;
            mTimeout = timeout;
            airType = type;
            mPageId = FragmentIdData.getFragmentId(page, UNDEFINED);
            mPopupVersion = popupVersion;
        }

        public int getCount() {
            return mCount;
        }

        public int getPopupVersion() {
            return mPopupVersion;
        }

        public int getPageId() {
            return mPageId;
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
        String id = UNKNOWN;
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
        public String text = Utils.EMPTY;
        public String buttonText = Utils.EMPTY;
        public String textPremium;
        public String buttonTextPremium;
        public boolean showPhotos = true;
        public String group = UNKNOWN;
        public int price = 0;
    }

    public static class BlockPeopleNearby {
        public boolean enabled = false;
        public String text = Utils.EMPTY;
        public String buttonText = Utils.EMPTY;
        public String textPremium;
        public String buttonTextPremium;
        public int price = 0;
    }

    public static class TabsList {
        @SerializedName("tabs")
        public ArrayList<PurchasesTabData> list;

        public TabsList(ArrayList<PurchasesTabData> list) {
            this.list = list;
        }

        public TabsList() {
            list = new ArrayList<>();
        }
    }

    public static class Payments {

        public TabsList other = new TabsList();
        public TabsList premium = new TabsList();

        public Payments() {
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
        public String text = Utils.EMPTY;

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    @FragmentIdData.FragmentId
    private int getStartPageFragmentId(JSONObject response) {
        startPage = FragmentIdData.getFragmentId(response.optString("startPage"), startPage);
        return startPage;
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

    public class TrialVipExperiment {
        public boolean enabled = false;
        public String subscriptionSku = "com.topface.topface.sub.trial.vip.13";
        public int maxShowCount = TRIAL_VIP_MAX_SHOW_COUNT;
    }

    public int getMaxShowCountTrialVipPopup() {
        // пока серверн не добавит в объект поле "maxShowCount" оно будет возвращать 0 при парсинге,
        // поэтому ставим по умолчанию 10
        return trialVipExperiment.maxShowCount <= 0 ? TRIAL_VIP_MAX_SHOW_COUNT : trialVipExperiment.maxShowCount;
    }

    public class ForceSmsInviteRedirect {
        public boolean enabled = false;
    }

    public static class LeftMenuIntegrationItems {
        public String iconUrl = Utils.EMPTY;
        public String title = Utils.EMPTY;
        public String url = Utils.EMPTY;
        public boolean external = false;

        public LeftMenuIntegrationItems(String icon, String title, String url, boolean external) {
            iconUrl = icon;
            this.title = title;
            this.url = url;
            this.external = external;
        }
    }

    public static class AppOfTheDay {

        /**
         * Информация об элементе "Приложение дня" или null, если для этого пользователя нет подходящего элемента ~ #49836
         */

        public String title;
        /**
         * {String} title - Название
         */
        public String description;
        /**
         * {String} description - Описание
         */
        public String iconUrl;
        /**
         * {String} iconUrl - URL-адрес иконки
         */
        public String targetUrl;

        /**
         * {String} targetUrl - URL-адрес страницы для перехода
         */

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AppOfTheDay)) return false;
            AppOfTheDay that = (AppOfTheDay) o;
            if (title != null ? !title.equals(that.title) : that.title != null) return false;
            if (description != null ? !description.equals(that.description) : that.description != null)
                return false;
            if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null)
                return false;
            return targetUrl != null ? targetUrl.equals(that.targetUrl) : that.targetUrl == null;

        }

        @Override
        public int hashCode() {
            int result = title != null ? title.hashCode() : 0;
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
            result = 31 * result + (targetUrl != null ? targetUrl.hashCode() : 0);
            return result;
        }
    }

}
