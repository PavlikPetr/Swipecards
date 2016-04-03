package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.data.Options;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.notifications.MessageStack;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.OkUserData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kirussell on 06.01.14.
 * Config for data related to User
 * Unique key for data based on AuthToken (social net user id),
 * so you need to call onAuthTokenReceived()
 * <p>
 * use generateKey(String name) to create keys to put(key) and get(key) data
 */
public class UserConfig extends AbstractConfig {
    public static final double DEFAULT_USER_LATITUDE_LOCATION = Double.MAX_VALUE;
    public static final double DEFAULT_USER_LONGITUDE_LOCATION = Double.MAX_VALUE;
    private static final String LOCATION_PROVIDER = "dummyprovider";
    public final static int DEFAULT_SHOW_COUNT = 0;
    public static final int TOPFACE_OFFERWALL_REDIRECTION_FREQUENCY = 2;
    private static final int DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    public static final String LISTS_SEPARATOR = "&";
    public static final String PROFILE_CONFIG_SETTINGS = "profile_config_settings";
    /**
     * Keys' names to generate user-based keys
     */
    public static final String DATA_PIN_CODE = "data_profile_pin_code";
    public static final String DATA_PROMO_POPUP = "data_promo_popup_";
    public static final String DATA_NOVICE_BUY_SYMPATHY = "novice_dating_buy_sympathy";
    public static final String DATA_NOVICE_BUY_SYMPATHY_DATE = "novice_dating_buy_symathy_date_tag";
    public static final String DATA_NOVICE_SYMPATHY = "novice_dating_sympathy";
    public static final String DATA_BONUS_LAST_SHOW_TIME = "data_bonus_last_show_time";
    public static final String NOTIFICATIONS_MESSAGES_STACK = "notifications_messages_stack";
    public static final String NOTIFICATION_REST_MESSAGES = "notifications_rest_messages";

    public static final String DEFAULT_DATING_MESSAGE = "default_dating_message";
    public static final String SETTINGS_GCM_RINGTONE = "settings_c2dm_ringtone";
    public static final String SETTINGS_PRELOAD_PHOTO = "settings_preload_photo";
    public static final String SETTINGS_GCM_VIBRATION = "settings_c2dm_vibration";
    public static final String SETTINGS_GCM = "settings_c2dm";
    public static final String IS_AVATAR_AVAILABLE = "is_avatar_available";
    public static final String DEFAULT_SOUND = "DEFAULT_SOUND";
    public static final String SETTINGS_GCM_LED = "settings_gcm_led";
    public static final String SILENT = "silent";
    public static final String PURCHASED_SUBSCRIPTIONS = "purchased_subscriptions";
    public static final String DATING_LOCK_POPUP_TIME = "dating_lock_popup_time";
    public static final String TRIAL_VIP_POPUP_COUNTER = "trial_vip_popup_counter";
    public static final String TOPFACE_OFFERWALL_REDIRECT_COUNTER = "topface_offerwall_redirect_counter";
    public static final String REMAINED_DAILY_PUBNATIVE_SHOWS = "remained_feed_ad_shows";
    public static final String LAST_DAY_PUBNATIVE_SHOWN = "current_day_for_showing_feed_ad";
    public static final String SYMPATHY_SENT_ID_ARRAY = "sympathy_sent_id_array";
    private static final String APPSFLYER_FIRST_PAY = "appsflyer_first_purchase";
    private static final String IS_EMAIL_CONFIRM_SENT = "is_button_send_confirmation_clicked";
    private static final String INTERSTITIAL_IN_FEEDS_COUNTER = "interstitial_in_feed_counter";
    private static final String INTERSTITIAL_IN_FEEDS_FIRST_SHOW_TIME = "interstitial_in_feed_first_show_time";
    private static final String TEST_PAYMENT_ENABLED = "test_payment_enabled";
    public static final String INVITED_CONTACTS_FOR_SMS = "invite_contacts_for_sms";
    public static final String LAST_CATCHED_GEO_LATITUDE = "last_catched_geo_latitude";
    public static final String LAST_CATCHED_GEO_LONGITUDE = "last_catched_geo_longitude";
    public static final String LAST_CATCHED_GEO_PROVIDER = "last_catched_geo_provider";
    public static final String TRIAL_LAST_TIME = "trial_last_time";
    public static final String OK_USER_DATA = "ok_user_data";
    private String mUnique;

    public UserConfig(String uniqueKey, Context context) {
        super(context);
        mUnique = uniqueKey;
    }

    private void setUnique(String mUnique) {
        this.mUnique = mUnique;
    }

    public String getUnique() {
        return mUnique;
    }

    public void updateConfig(String unique) {
        setUnique(unique);
        initData();
        saveConfig();
    }

    @Override
    protected void addField(SettingsMap settingsMap, String key, Object defaultValue) {
        super.addField(settingsMap, key, defaultValue);
    }

    @Override
    protected SettingsMap getSettingsMap() {
        return super.getSettingsMap();
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        // pincode value
        addField(settingsMap, DATA_PIN_CODE, Utils.EMPTY);
        // admirations promo popup last date of show
        addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_ADMIRATIONS), 0L);
        // messages promo popup last date of show
        addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_MESSAGES), 0L);
        // visitors promo popup last date of show
        addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_VISITORS), 0L);
        // flag show if "buy sympathies hint" is passed
        addField(settingsMap, DATA_NOVICE_BUY_SYMPATHY, true);
        // data of first launch to show "buy sympathies hint" with some delay from first launch
        addField(settingsMap, DATA_NOVICE_BUY_SYMPATHY_DATE, 0L);
        // flag show if "send sympathy hint" is passed
        addField(settingsMap, DATA_NOVICE_SYMPATHY, true);
        // список сообщений для сгруппированных нотификаций (сейчас группируются только сообщения)
        addField(settingsMap, NOTIFICATIONS_MESSAGES_STACK, Utils.EMPTY);
        // количество нотификаций, которые пишем в поле "еще %d сообщений"
        addField(settingsMap, NOTIFICATION_REST_MESSAGES, 0);
        // время последнего сброса счетчика вкладки бонусов
        addField(settingsMap, DATA_BONUS_LAST_SHOW_TIME, 0L);
        // default text for instant message on dating screen
        addField(settingsMap, DEFAULT_DATING_MESSAGE, Utils.EMPTY);
        // push notification melody
        addField(settingsMap, SETTINGS_GCM_RINGTONE, DEFAULT_SOUND);
        // preload photo default type WiFi and 3G
        addField(settingsMap, SETTINGS_PRELOAD_PHOTO, PreloadPhotoSelectorTypes.WIFI_3G.getId());
        // is vibration for notification enabled
        addField(settingsMap, SETTINGS_GCM_VIBRATION, true);
        // is led blinking for notification enabled
        addField(settingsMap, SETTINGS_GCM_LED, true);
        // is push notification enabled or not
        addField(settingsMap, SETTINGS_GCM, true);
        // purchased subscriptions which don't need verification
        addField(settingsMap, PURCHASED_SUBSCRIPTIONS, "");
        // время последнего показа попапа блокировки знакомств
        addField(settingsMap, DATING_LOCK_POPUP_TIME, 0L);
        // счётчик перехода на экран офервола топфейс
        addField(settingsMap, TOPFACE_OFFERWALL_REDIRECT_COUNTER, 0);
        // оставшееся количество показов нативный реклымы pubnative для текущих суток
        addField(settingsMap, REMAINED_DAILY_PUBNATIVE_SHOWS, 4);
        // Время начала текущих суток для учёта количества показов рекламы pubnative
        // Обновляется автоматически при попытке получить оставшиеся показы pubnative
        addField(settingsMap, LAST_DAY_PUBNATIVE_SHOWN, 0L);
        // Массив id пользователей из фотоленты, которым были отправлены симпатии
        addField(settingsMap, SYMPATHY_SENT_ID_ARRAY, "");
        // validate user avatar
        addField(settingsMap, IS_AVATAR_AVAILABLE, false);
        //Флаг первой покупки
        addField(settingsMap, APPSFLYER_FIRST_PAY, false);
        // is button send confirmation clicked by current user
        addField(settingsMap, IS_EMAIL_CONFIRM_SENT, false);
        // interstitials' shows counter in feeds
        addField(settingsMap, INTERSTITIAL_IN_FEEDS_COUNTER, 0);
        // interstitials' first show time
        addField(settingsMap, INTERSTITIAL_IN_FEEDS_FIRST_SHOW_TIME, 0L);
        // test payment mark
        addField(settingsMap, TEST_PAYMENT_ENABLED, false);
        // отправленные контакты для отправки смс
        addField(settingsMap, INVITED_CONTACTS_FOR_SMS, "");
        // счетчик показа попапа триального VIP
        addField(settingsMap, TRIAL_VIP_POPUP_COUNTER, DEFAULT_SHOW_COUNT);
        // последнее сохраненное местоположение пользователя
        addField(settingsMap, LAST_CATCHED_GEO_LATITUDE, DEFAULT_USER_LATITUDE_LOCATION);
        // время последнего показа попапа триала
        addField(settingsMap, TRIAL_LAST_TIME, 0L);
        addField(settingsMap, LAST_CATCHED_GEO_LONGITUDE, DEFAULT_USER_LONGITUDE_LOCATION);
        addField(settingsMap, LAST_CATCHED_GEO_PROVIDER, LOCATION_PROVIDER);
        // save OK user data
        addField(settingsMap, OK_USER_DATA, Utils.EMPTY);
    }

    @Override
    protected boolean canInitData() {
        return !AuthToken.getInstance().isEmpty();
    }

    @Override
    protected SharedPreferences getPreferences() {
        if (mUnique == null) {
            mUnique = AuthToken.getInstance().getUserTokenUniqueId();
        }
        return getContext().getSharedPreferences(
                PROFILE_CONFIG_SETTINGS + Utils.AMPERSAND + mUnique,
                Context.MODE_PRIVATE
        );
    }

    public boolean getTestPaymentFlag() {
        return getBooleanField(getSettingsMap(), TEST_PAYMENT_ENABLED);
    }

    public boolean setTestPaymentFlag(boolean testPaymentMark) {
        return setField(getSettingsMap(), TEST_PAYMENT_ENABLED, testPaymentMark);
    }

    /**
     * Return first purchase flag
     *
     * @return true if first purchase has already been made
     */
    public boolean getFirstPayFlag() {
        return getBooleanField(getSettingsMap(), APPSFLYER_FIRST_PAY);
    }

    /**
     * Set first purchase flag
     *
     * @param firstPurchaseMark first purchase flag
     * @return true on success
     */
    public boolean setFirstPayFlag(boolean firstPurchaseMark) {
        return setField(getSettingsMap(), APPSFLYER_FIRST_PAY, firstPurchaseMark);
    }

    /**
     * Use this method to create key for promopopup which will be related to current user
     *
     * @param popupType type of promo popup []
     * @return new key which contains user id
     */
    private String getPromoPopupKey(int popupType) {
        return DATA_PROMO_POPUP + popupType;
    }

    /**
     * Need to be called to generate keys based on AuthToken and create default SettingsMap.
     * canInitData() does not give to fill settings map with any values
     * while there is no AuthToken obtained
     */
    public void onAuthTokenReceived() {
        initData();
    }

    // Pincode

    /**
     * Sets current user pincode value
     *
     * @param pinCode value
     * @return true on success
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean setPinCode(String pinCode) {
        return setField(getSettingsMap(), DATA_PIN_CODE, pinCode);
    }

    /**
     * Current user pincode
     *
     * @return pincode value
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getPinCode() {
        return getStringField(getSettingsMap(), DATA_PIN_CODE);
    }

    /**
     * save the number of impressions trial vip popup
     *
     * @param count times trial vip popup showing
     */
    public void setTrialVipPopupCounter(int count) {
        setField(getSettingsMap(), TRIAL_VIP_POPUP_COUNTER, count);
    }

    /**
     * get the number of impressions trial vip popup
     *
     * @return count
     */
    public int getTrialVipCounter() {
        return getIntegerField(getSettingsMap(), TRIAL_VIP_POPUP_COUNTER);
    }


    public void setDatingLockPopupRedirect(long lastTime) {
        setField(getSettingsMap(), DATING_LOCK_POPUP_TIME, lastTime);
    }

    public long getDatingLockPopupRedirect() {
        return getLongField(getSettingsMap(), DATING_LOCK_POPUP_TIME);
    }

    public void setTrialLastTime(long lastTime) {
        setField(getSettingsMap(), TRIAL_LAST_TIME, lastTime);
    }

    public long getTrialLastTime() {
        return getLongField(getSettingsMap(), TRIAL_LAST_TIME);
    }

    // =======================PromoPopups=======================

    /**
     * Sets promo popup last date
     *
     * @param popupType type of popup (((Options.PromoPopupEntity)someEntity).getPopupAirType())
     * @param lastTime  date of launch
     * @return true on success
     */
    public boolean setPromoPopupLastTime(int popupType, long lastTime) {
        return setField(getSettingsMap(), getPromoPopupKey(popupType), lastTime);
    }

    /**
     * Last date of promo popup's launch
     *
     * @param popupType type of popup (((Options.PromoPopupEntity)someEntity).getPopupAirType())
     * @return date
     */
    public long getPromoPopupLastTime(int popupType) {
        return getLongField(getSettingsMap(), getPromoPopupKey(popupType));
    }

    /**
     * Resets last date of promo popup's launch
     *
     * @param popupType type of popup (((Options.PromoPopupEntity)someEntity).getPopupAirType())
     */
    public void resetPromoPopupData(int popupType) {
        resetAndSaveConfig(getPromoPopupKey(popupType));
    }

    // =======================PreloadPhotoType=======================

    public boolean setPreloadPhotoType(int type) {
        return setField(getSettingsMap(), SETTINGS_PRELOAD_PHOTO, type);
    }

    public PreloadPhotoSelectorTypes getPreloadPhotoType() {
        return PreloadPhotoSelectorTypes.values()[getIntegerField(getSettingsMap(), SETTINGS_PRELOAD_PHOTO)];
    }

    // =======================Novice=======================

    /**
     * "Send sympathy hint" for novice user
     *
     * @return true if hint needs to be shown
     */
    public boolean getNoviceSympathy() {
        return getBooleanField(getSettingsMap(), DATA_NOVICE_SYMPATHY);
    }

    /**
     * Sets "send sympathy hint" flag for novice user
     *
     * @param needShow true if hint needs to be shown
     * @return true on success
     */
    public boolean setNoviceSympathy(boolean needShow) {
        return setField(getSettingsMap(), DATA_NOVICE_SYMPATHY, needShow);
    }

    /**
     * "Buy sympathy hint" flag for novice user
     *
     * @return true if hint need to be shown
     */
    public boolean getNoviceBuySympathy() {
        return getBooleanField(getSettingsMap(), DATA_NOVICE_BUY_SYMPATHY);
    }

    /**
     * Sets "buy sympathy hint" flag for novice user
     *
     * @param needShow true if hint need to be shown
     * @return true on success
     */
    public boolean setNoviceBuySympathy(boolean needShow) {
        return setField(getSettingsMap(), DATA_NOVICE_BUY_SYMPATHY, needShow);
    }

    /**
     * First trial to show "Buy sympathy hint" for delay hint purposes
     *
     * @return time of first trial
     */
    public long getNoviceBuySympathyDate() {
        return getLongField(getSettingsMap(), DATA_NOVICE_BUY_SYMPATHY_DATE);
    }

    /**
     * Sets time of first trial to show "Buy sympathy hint" for delay hint purposes
     *
     * @param lastTime time of show
     * @return true on success
     */
    public Boolean setNoviceBuySympathyDate(long lastTime) {
        return setField(getSettingsMap(), DATA_NOVICE_BUY_SYMPATHY_DATE, lastTime);
    }

    /**
     * Sets last time of click on bonus menu item
     *
     * @param lastShowTime timestamp of last visit time from server
     */
    public void setBonusCounterLastShowTime(long lastShowTime) {
        setField(getSettingsMap(), DATA_BONUS_LAST_SHOW_TIME, lastShowTime);
    }

    /**
     * Last time of click on bonus menu item
     *
     * @return timestamp
     */
    public long getBonusCounterLastShowTime() {
        return getLongField(getSettingsMap(), DATA_BONUS_LAST_SHOW_TIME);
    }

    public MessageStack getNotificationMessagesStack() {
        MessageStack messageStack = new MessageStack();
        messageStack.fromJSON(getStringField(getSettingsMap(), NOTIFICATIONS_MESSAGES_STACK), MessageStack.Message.class.getName());
        messageStack.setRestMessages(getIntegerField(getSettingsMap(), NOTIFICATION_REST_MESSAGES));
        return messageStack;
    }

    public boolean setNotificationMessagesStack(MessageStack messages) {
        setField(getSettingsMap(), NOTIFICATION_REST_MESSAGES, messages.getRestMessages());
        return setField(getSettingsMap(), NOTIFICATIONS_MESSAGES_STACK, messages.toJson());
    }

    public void resetNotificationMessagesStack() {
        resetAndSaveConfig(NOTIFICATIONS_MESSAGES_STACK);
        resetAndSaveConfig(NOTIFICATION_REST_MESSAGES);
    }


    /**
     * @return Default text for dating screen message
     */
    public String getDatingMessage() {
        return getStringField(getSettingsMap(), DEFAULT_DATING_MESSAGE);
    }

    /**
     * Sets new default text for dating screen message and it's locale
     */
    public void setDatingMessage(String message) {
        SettingsMap settingsMap = getSettingsMap();
        setField(settingsMap, DEFAULT_DATING_MESSAGE, message);
    }

    /**
     * @return push notification melody name
     */
    public Uri getGCMRingtone() {
        if (getStringField(getSettingsMap(), SETTINGS_GCM_RINGTONE).equals(SILENT)) {
            return null;
        }
        String ringtone = getStringField(getSettingsMap(), SETTINGS_GCM_RINGTONE);
        return ringtone.equals(DEFAULT_SOUND) ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(ringtone);
    }

    /**
     * Sets push notification melody name
     */
    public void setGCMRingtone(String ringtoneName) {
        setField(getSettingsMap(), SETTINGS_GCM_RINGTONE, ringtoneName);
    }

    /**
     * @return true if vibration for push notification enabled
     */
    public Boolean isVibrationEnabled() {
        return getBooleanField(getSettingsMap(), SETTINGS_GCM_VIBRATION);
    }

    /**
     * Sets vibration for push notification enabled or not
     */
    public void setGCMVibrationEnabled(boolean enabled) {
        setField(getSettingsMap(), SETTINGS_GCM_VIBRATION, enabled);
    }

    /**
     * @return true if led blinking for push notification enabled
     */
    public boolean isLEDEnabled() {
        return getBooleanField(getSettingsMap(), SETTINGS_GCM_LED);
    }

    /**
     * Sets led blinking for push notification enabled or not
     */
    public void setLEDEnabled(boolean enabled) {
        setField(getSettingsMap(), SETTINGS_GCM_LED, enabled);
    }

    /**
     * @return true if user set avatar at empty album
     */
    public boolean isUserAvatarAvailable() {
        return getBooleanField(getSettingsMap(), IS_AVATAR_AVAILABLE);
    }

    /**
     * Sets avatar available state for popup "Set photo"
     */
    public void setUserAvatarAvailable(boolean enabled) {
        setField(getSettingsMap(), IS_AVATAR_AVAILABLE, enabled);
    }

    /**
     * @return true if user clicked button send confirmationF
     */
    public boolean isButtonSendConfirmationClicked() {
        return getBooleanField(getSettingsMap(), IS_EMAIL_CONFIRM_SENT);
    }

    /**
     * Set state of button email confirmation
     */
    public void saveButtonSendConfirmationPressed(boolean state) {
        setField(getSettingsMap(), IS_EMAIL_CONFIRM_SENT, state);
    }

    /**
     * Adds contact id to list of invited contacts to send sms
     *
     * @param id contact id
     */
    public void addInvitedContactBySms(String id) {
        String rawIds = getStringField(getSettingsMap(), INVITED_CONTACTS_FOR_SMS);
        if (TextUtils.isEmpty(rawIds)) {
            setField(getSettingsMap(), INVITED_CONTACTS_FOR_SMS, rawIds.concat(id));
        } else {
            setField(getSettingsMap(), INVITED_CONTACTS_FOR_SMS, rawIds.
                    concat(LISTS_SEPARATOR).concat(id));
        }
    }

    /**
     * List of invites contacts' ids to send sms
     *
     * @return list of contacts' ids
     */
    public Set<String> getInvitedContactsBySms() {
        String rawIds = getStringField(getSettingsMap(), INVITED_CONTACTS_FOR_SMS);
        return new HashSet<>(Arrays.asList(rawIds.split(LISTS_SEPARATOR)));
    }

    /**
     * @return true if push notification enabled
     */
    public boolean isNotificationEnabled() {
        return getBooleanField(getSettingsMap(), SETTINGS_GCM);
    }

    /**
     * Sets push notification enabled or not
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setNotificationEnabled(boolean enabled) {
        setField(getSettingsMap(), SETTINGS_GCM, enabled);
    }

    /**
     * @return List of purchased subscriptions
     */
    public List<String> getPurchasedSubscriptions() {
        String rawSubs = getStringField(getSettingsMap(), PURCHASED_SUBSCRIPTIONS);
        return Arrays.asList(rawSubs.split(LISTS_SEPARATOR));
    }

    /**
     * Add subscription order id to purchased subscriptions
     */
    public void addPurchasedSubscription(String subscriptionId) {
        String rawSubs = getStringField(getSettingsMap(), PURCHASED_SUBSCRIPTIONS);
        if (rawSubs.isEmpty()) {
            setField(getSettingsMap(), PURCHASED_SUBSCRIPTIONS, rawSubs.concat(subscriptionId));
        } else {
            setField(getSettingsMap(), PURCHASED_SUBSCRIPTIONS, rawSubs.
                    concat(LISTS_SEPARATOR).concat(subscriptionId));
        }
    }

    /**
     * Set new topface offerwall redirection counter value
     */
    public void incrementTopfaceOfferwallRedirectCounter() {
        int counter = getTopfaceOfferwallRedirectCounter();
        if (counter < TOPFACE_OFFERWALL_REDIRECTION_FREQUENCY) {
            counter++;
        } else {
            counter = 0;
        }
        setField(getSettingsMap(), TOPFACE_OFFERWALL_REDIRECT_COUNTER, counter);
    }

    /**
     * @return current topface offerwall redirection counter value
     */
    public int getTopfaceOfferwallRedirectCounter() {
        return getIntegerField(getSettingsMap(), TOPFACE_OFFERWALL_REDIRECT_COUNTER);
    }

    public int getRemainedPubnativeShows() {
        long lastDay = getLongField(getSettingsMap(), LAST_DAY_PUBNATIVE_SHOWN);
        long now = Calendar.getInstance().getTimeInMillis();
        if (now - lastDay > DAY_IN_MILLIS) {
            setField(getSettingsMap(), LAST_DAY_PUBNATIVE_SHOWN, now - now % (DAY_IN_MILLIS));
            setField(getSettingsMap(), REMAINED_DAILY_PUBNATIVE_SHOWS, CacheProfile.getOptions().feedNativeAd.dailyShows);
        }
        return getIntegerField(getSettingsMap(), REMAINED_DAILY_PUBNATIVE_SHOWS);
    }

    public void decreaseRemainedPubnativeShows() {
        int remainedShows = getIntegerField(getSettingsMap(), REMAINED_DAILY_PUBNATIVE_SHOWS);
        if (remainedShows > 0) {
            setField(getSettingsMap(), REMAINED_DAILY_PUBNATIVE_SHOWS, remainedShows - 1);
        }
    }

    /**
     * @return List of sympathy sent from photoblog
     */
    public List<Integer> getSympathySentArray() {
        String rawSubs = getStringField(getSettingsMap(), SYMPATHY_SENT_ID_ARRAY);
        List<Integer> res = new ArrayList<>();
        if (TextUtils.isEmpty(rawSubs)) {
            return res;
        }
        for (String item : rawSubs.split(LISTS_SEPARATOR)) {
            int id = -1;
            try {
                id = Integer.parseInt(item);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            res.add(id);
        }

        return res;
    }

    /**
     * Set list of sympathy sent from photoblog
     */
    public void setSympathySentArray(List<Integer> array) {
        String res = "";
        for (int i = 0; i < array.size(); i++) {
            res = res + Integer.toString(array.get(i));
            if (i < array.size() - 1) {
                res = res + LISTS_SEPARATOR;
            }
        }
        setField(getSettingsMap(), SYMPATHY_SENT_ID_ARRAY, res);
    }

    public int incrementInterstitialInFeedsCounter() {
        int counter = getIntegerField(getSettingsMap(), INTERSTITIAL_IN_FEEDS_COUNTER);
        setField(getSettingsMap(), INTERSTITIAL_IN_FEEDS_COUNTER, ++counter);
        this.saveConfig();
        return counter;
    }

    public void resetInterstitialInFeedsCounter() {
        setField(getSettingsMap(), INTERSTITIAL_IN_FEEDS_COUNTER, 0);
        this.saveConfig();
    }

    public int getInterstitialsInFeedCounter() {
        return getIntegerField(getSettingsMap(), INTERSTITIAL_IN_FEEDS_COUNTER);
    }

    public void setInterstitialsInFeedFirstShow(long timestamp) {
        setField(getSettingsMap(), INTERSTITIAL_IN_FEEDS_FIRST_SHOW_TIME, timestamp);
        this.saveConfig();
    }

    public long getInterstitialsInFeedFirstShow() {
        return getLongField(getSettingsMap(), INTERSTITIAL_IN_FEEDS_FIRST_SHOW_TIME);
    }
    // =====================================================

    /**
     * Save last catched user location
     *
     * @param location user geo position
     */
    public void setUserGeoLocation(@NotNull Location location) {
        setField(getSettingsMap(), LAST_CATCHED_GEO_LATITUDE, location.getLatitude());
        setField(getSettingsMap(), LAST_CATCHED_GEO_LONGITUDE, location.getLongitude());
        setField(getSettingsMap(), LAST_CATCHED_GEO_PROVIDER, location.getProvider());
        this.saveConfig();
    }

    /**
     * Return last saved user location
     *
     * @return last location
     */
    public Location getUserGeoLocation() {
        Location location = new Location(getStringField(getSettingsMap(), LAST_CATCHED_GEO_PROVIDER));
        location.setLatitude(getDoubleField(getSettingsMap(), LAST_CATCHED_GEO_LATITUDE));
        location.setLongitude(getDoubleField(getSettingsMap(), LAST_CATCHED_GEO_LONGITUDE));
        return location;
    }

    /**
     * @return OK user data
     */
    public OkUserData getOkUserData() {
        String okUserDataString = getStringField(getSettingsMap(), OK_USER_DATA);
        if (!TextUtils.isEmpty(okUserDataString)) {
            return JsonUtils.fromJson(okUserDataString, OkUserData.class);
        }
        return null;
    }

    /**
     * Set user OK data
     *
     * @param data current user data from OK account
     * @return state of setting data
     */
    public boolean setOkUserData(OkUserData data) {
        return setField(getSettingsMap(), OK_USER_DATA, JsonUtils.toJson(data));
    }
}
