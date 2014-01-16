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
        settingsMap.addStringField(generateKey(DATA_PIN_CODE), Static.EMPTY);
        settingsMap.addLongField(generatePromoPopupKey(Options.PromoPopupEntity.AIR_ADMIRATIONS), 0L);
        settingsMap.addLongField(generatePromoPopupKey(Options.PromoPopupEntity.AIR_MESSAGES), 0L);
        settingsMap.addLongField(generatePromoPopupKey(Options.PromoPopupEntity.AIR_VISITORS), 0L);
        settingsMap.addBooleanField(generateKey(DATA_NOVICE_BUY_SYMPATHY), true);
        settingsMap.addLongField(generateKey(DATA_NOVICE_BUY_SYMPATHY_DATE), 0L);
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

    @SuppressWarnings("UnusedDeclaration")
    public boolean setPinCode(String pinCode) {
        return getSettingsMap().setField(generateKey(DATA_PIN_CODE), pinCode);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getPinCode() {
        return getSettingsMap().getStringField(generateKey(DATA_PIN_CODE));
    }

    // PromoPopups

    public boolean setPromoPopupLastTime(int popupType, long lastTime) {
        return getSettingsMap().setField(generatePromoPopupKey(popupType), lastTime);
    }

    public long getPromoPopupLastTime(int popupType) {
        return getSettingsMap().getLongField(generatePromoPopupKey(popupType));
    }

    public void resetPromoPopupData(int popupType) {
        resetAndSaveConfig(generatePromoPopupKey(popupType));
    }

    // Novice

    public boolean getNoviceSympathy() {
        return getSettingsMap().getBooleanField(generateKey(DATA_NOVICE_SYMPATHY));
    }

    public boolean setNoviceSympathy(boolean passed) {
        return getSettingsMap().setField(DATA_NOVICE_SYMPATHY, passed);
    }

    public boolean getNoviceBuySympathy() {
        return getSettingsMap().getBooleanField(generateKey(DATA_NOVICE_BUY_SYMPATHY));
    }

    public boolean setNoviceBuySympathy(boolean passed) {
        return getSettingsMap().setField(DATA_NOVICE_BUY_SYMPATHY, passed);
    }

    public long getNoviceBuySympathyDate() {
        return getSettingsMap().getLongField(generateKey(DATA_NOVICE_BUY_SYMPATHY_DATE));
    }

    public Boolean setNoviceBuySympathyDate(long lastTime) {
        return getSettingsMap().setField(DATA_NOVICE_BUY_SYMPATHY_DATE, lastTime);
    }

}