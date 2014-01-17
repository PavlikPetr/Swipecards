package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.utils.social.AuthToken;

/**
 * Created by kirussell on 06.01.14.
 * Config for data related to User
 * Unique key for data based on AuthToken (social net user id),
 * so you need to call onAuthTokenReceived()
 * <p/>
 * use generateKey(String name) to create keys to put(key) and get(key) data
 */
public class UserConfig extends AbstractConfig {
    private static final String PROFILE_CONFIG_SETTINGS = "profile_config_settings";
    /**
     * Keys' names to generate user-based keys
     */
    public static final String DATA_PIN_CODE = "data_profile_pin_code";
    private static final String DATA_PROMO_POPUP = "data_promo_popup_";
    public static final String DATA_NOVICE_BUY_SYMPATHY = "novice_dating_buy_sympathy";
    public static final String DATA_NOVICE_BUY_SYMPATHY_DATE = "novice_dating_buy_symathy_date_tag";
    public static final String DATA_NOVICE_SYMPATHY = "novice_dating_sympathy";

    public UserConfig(Context context) {
        super(context);
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        // pincode value
        settingsMap.addStringField(generateKey(DATA_PIN_CODE), Static.EMPTY);
        // admirations promo popup last date of show
        settingsMap.addLongField(generatePromoPopupKey(Options.PromoPopupEntity.AIR_ADMIRATIONS), 0L);
        // messages promo popup last date of show
        settingsMap.addLongField(generatePromoPopupKey(Options.PromoPopupEntity.AIR_MESSAGES), 0L);
        // visitors promo popup last date of show
        settingsMap.addLongField(generatePromoPopupKey(Options.PromoPopupEntity.AIR_VISITORS), 0L);
        // flag show if "buy sympathies hint" is passed
        settingsMap.addBooleanField(generateKey(DATA_NOVICE_BUY_SYMPATHY), true);
        // data of first launch to show "buy sympathies hint" with some delay from first launch
        settingsMap.addLongField(generateKey(DATA_NOVICE_BUY_SYMPATHY_DATE), 0L);
        // flag show if "send sympathy hint" is passed
        settingsMap.addBooleanField(generateKey(DATA_NOVICE_SYMPATHY), true);
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
     * Use this method to create key which will be related to current user
     *
     * @param name data field name
     * @return new key which contains user id
     */
    private String generateKey(String name) {
        AuthToken token = AuthToken.getInstance();
        return token.getSocialNet() +
                Static.AMPERSAND + token.getUserSocialId() +
                Static.AMPERSAND + name;
    }

    /**
     * Use this method to create key for promopopup which will be related to current user
     *
     * @param popupType type of promo popup []
     * @return new key which contains user id
     */
    private String generatePromoPopupKey(int popupType) {
        return generateKey(DATA_PROMO_POPUP + popupType);
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
     * @param pinCode value
     * @return true on success
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean setPinCode(String pinCode) {
        return getSettingsMap().setField(generateKey(DATA_PIN_CODE), pinCode);
    }

    /**
     * Current user pincode
     * @return pincode value
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getPinCode() {
        return getSettingsMap().getStringField(generateKey(DATA_PIN_CODE));
    }

    // PromoPopups

    /**
     * Sets promo popup last date
     * @param popupType type of popup (((Options.PromoPopupEntity)someEntity).getPopupAirType())
     * @param lastTime date of launch
     * @return true on success
     */
    public boolean setPromoPopupLastTime(int popupType, long lastTime) {
        return getSettingsMap().setField(generatePromoPopupKey(popupType), lastTime);
    }

    /**
     * Last date of promo popup's launch
     * @param popupType type of popup (((Options.PromoPopupEntity)someEntity).getPopupAirType())
     * @return date
     */
    public long getPromoPopupLastTime(int popupType) {
        return getSettingsMap().getLongField(generatePromoPopupKey(popupType));
    }

    /**
     * Resets last date of promo popup's launch
     * @param popupType type of popup (((Options.PromoPopupEntity)someEntity).getPopupAirType())
     */
    public void resetPromoPopupData(int popupType) {
        resetAndSaveConfig(generatePromoPopupKey(popupType));
    }

    // Novice

    /**
     * "Send sympathy hint" for novice user
     * @return true if hint needs to be shown
     */
    public boolean getNoviceSympathy() {
        return getSettingsMap().getBooleanField(generateKey(DATA_NOVICE_SYMPATHY));
    }

    /**
     * Sets "send sympathy hint" flag for novice user
     * @param passed true if hint is passed
     * @return true on success
     */
    public boolean setNoviceSympathy(boolean passed) {
        return getSettingsMap().setField(generateKey(DATA_NOVICE_SYMPATHY), passed);
    }

    /**
     * "Buy sympathy hint" flag for novice user
     * @return true if hint need to be shown
     */
    public boolean getNoviceBuySympathy() {
        return getSettingsMap().getBooleanField(generateKey(DATA_NOVICE_BUY_SYMPATHY));
    }

    /**
     * Sets "buy sympathy hint" flag for novice user
     * @param passed true if hint is passed
     * @return true on success
     */
    public boolean setNoviceBuySympathy(boolean passed) {
        return getSettingsMap().setField(generateKey(DATA_NOVICE_BUY_SYMPATHY), passed);
    }

    /**
     * First trial to show "Buy sympathy hint" for delay hint purposes
     * @return time of first trial
     */
    public long getNoviceBuySympathyDate() {
        return getSettingsMap().getLongField(generateKey(DATA_NOVICE_BUY_SYMPATHY_DATE));
    }

    /**
     * Sets time of first trial to show "Buy sympathy hint" for delay hint purposes
     * @param lastTime time of show
     * @return true on success
     */
    public Boolean setNoviceBuySympathyDate(long lastTime) {
        return getSettingsMap().setField(generateKey(DATA_NOVICE_BUY_SYMPATHY_DATE), lastTime);
    }

}