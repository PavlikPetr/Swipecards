package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import com.topface.framework.utils.config.AbstractUniqueConfig;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.utils.notifications.MessageStack;
import com.topface.topface.utils.social.AuthToken;

/**
 * Created by kirussell on 06.01.14.
 * Config for data related to User
 * Unique key for data based on AuthToken (social net user id),
 * so you need to call onAuthTokenReceived()
 * <p/>
 * use generateKey(String name) to create keys to put(key) and get(key) data
 */
public class UserConfig extends AbstractUniqueConfig {
    private static final String PROFILE_CONFIG_SETTINGS = "profile_config_settings";
    /**
     * Keys' names to generate user-based keys
     */
    public static final String DATA_PIN_CODE = "data_profile_pin_code";
    private static final String DATA_PROMO_POPUP = "data_promo_popup_";
    public static final String DATA_NOVICE_BUY_SYMPATHY = "novice_dating_buy_sympathy";
    public static final String DATA_NOVICE_BUY_SYMPATHY_DATE = "novice_dating_buy_symathy_date_tag";
    public static final String DATA_NOVICE_SYMPATHY = "novice_dating_sympathy";
    private static final String DATA_LIKE_CLOSING_LAST_TIME = "data_closings_likes_last_date";
    private static final String DATA_MUTUAL_CLOSING_LAST_TIME = "data_closings_mutual_last_date";
    private static final String DATA_BONUS_LAST_SHOW_TIME = "data_bonus_last_show_time";
    public static final String NOTIFICATIONS_MESSAGES_STACK = "notifications_messages_stack";
    public static final String NOTIFICATION_REST_MESSAGES = "notifications_rest_messages";

    private static final String DEFAULT_DATING_MESSAGE = "default_dating_message";
    public static final String SETTINGS_GCM_RINGTONE = "settings_c2dm_ringtone";
    public static final String SETTINGS_GCM_VIBRATION = "settings_c2dm_vibration";
    public static final String SETTINGS_GCM = "settings_c2dm";
    public static final String DEFAULT_SOUND = "DEFAULT_SOUND";
    public static final String SETTINGS_GCM_LED = "settings_gcm_led";
    public static final String SILENT = "silent";

    public UserConfig(Context context) {
        super(context);
    }

    @Override
    protected String generateUniqueKey(String name) {
        AuthToken token = AuthToken.getInstance();
        return token.getSocialNet() +
                Static.AMPERSAND + token.getUserTokenUniqueId() +
                Static.AMPERSAND + name;
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        // pincode value
        addField(settingsMap, DATA_PIN_CODE, Static.EMPTY);
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
        // date of last likes closings processing
        addField(settingsMap, DATA_LIKE_CLOSING_LAST_TIME, 0L);
        // date of last mutual closings processing
        addField(settingsMap, DATA_MUTUAL_CLOSING_LAST_TIME, 0L);
        // список сообщений для сгруппированных нотификаций (сейчас группируются только сообщения)
        addField(settingsMap, NOTIFICATIONS_MESSAGES_STACK, Static.EMPTY);
        // количество нотификаций, которые пишем в поле "еще %d сообщений"
        addField(settingsMap, NOTIFICATION_REST_MESSAGES, 0);
        // время последнего сброса счетчика вкладки бонусов
        addField(settingsMap, DATA_BONUS_LAST_SHOW_TIME, 0L);
        // default text for instant message on dating screen
        addField(settingsMap, DEFAULT_DATING_MESSAGE, Static.EMPTY);
        // push notification melody
        addField(settingsMap, SETTINGS_GCM_RINGTONE, DEFAULT_SOUND);
        // is vibration for notification enabled
        addField(settingsMap, SETTINGS_GCM_VIBRATION, true);
        // is led blinking for notification enabled
        addField(settingsMap, SETTINGS_GCM_LED, true);
        // is push notification enabled or not
        addField(settingsMap, SETTINGS_GCM, true);
    }

    @Override
    protected boolean canInitData() {
        return !AuthToken.getInstance().isEmpty();
    }

    @Override
    protected SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(
                PROFILE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
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
    public boolean setPinCode(String pinCode) {
        return setField(getSettingsMap(), DATA_PIN_CODE, pinCode);
    }

    /**
     * Current user pincode
     *
     * @return pincode value
     */
    public String getPinCode() {
        return getStringField(getSettingsMap(), DATA_PIN_CODE);
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

    // =======================Closings=======================

    /**
     * Sets date of last processing of likes closings
     *
     * @param lastTime date in unix time
     * @return true on success
     */
    public boolean setLikesClosingsLastTime(long lastTime) {
        return setField(getSettingsMap(), DATA_LIKE_CLOSING_LAST_TIME, lastTime);
    }

    /**
     * Date of last processing of likes closings
     *
     * @return date in unix time
     */
    public long getLikesClosingsLastTime() {
        return getLongField(getSettingsMap(), DATA_LIKE_CLOSING_LAST_TIME);
    }

    /**
     * Sets date of last processing of mutual closings
     *
     * @param lastTime date in unix time
     * @return true on success
     */
    public boolean setMutualClosingsLastTime(long lastTime) {
        return setField(getSettingsMap(), DATA_MUTUAL_CLOSING_LAST_TIME, lastTime);
    }

    /**
     * Date of last processing of mutual closings
     *
     * @return date in unix time
     */
    public long getMutualClosingsLastTime() {
        return getLongField(getSettingsMap(), DATA_MUTUAL_CLOSING_LAST_TIME);
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
    public String getDefaultDatingMessage() {
        return getStringField(getSettingsMap(), DEFAULT_DATING_MESSAGE);
    }

    /**
     * Sets new default text for dating screen message
     *
     * @param message
     */
    public void setDefaultDatingMessage(String message) {
        setField(getSettingsMap(), DEFAULT_DATING_MESSAGE, message);
    }

    /**
     * @return push notification melody name
     */
    public Uri getRingtone() {
        if (getStringField(getSettingsMap(), SETTINGS_GCM_RINGTONE).equals(SILENT)) {
            return null;
        }
        String ringtone = getStringField(getSettingsMap(), SETTINGS_GCM_RINGTONE);
        return ringtone.equals(DEFAULT_SOUND) ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(ringtone);
    }

    /**
     * Sets push notification melody name
     *
     * @param ringtoneName
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
     *
     * @param enabled
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
     *
     * @param enabled
     */
    public void setLEDEnabled(boolean enabled) {
        setField(getSettingsMap(), SETTINGS_GCM_LED, enabled);
    }

    /**
     * @return true if push notification enabled
     */
    public boolean isNotificationEnabled() {
        return getBooleanField(getSettingsMap(), SETTINGS_GCM);
    }

    /**
     * Sets push notification enabled or not
     *
     * @param enabled
     */
    public void setNotificationEnabled(boolean enabled) {
        setField(getSettingsMap(), SETTINGS_GCM, enabled);
    }



    // =====================================================
}