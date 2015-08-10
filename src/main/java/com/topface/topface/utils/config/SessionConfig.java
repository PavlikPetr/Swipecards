package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.Static;
import com.topface.topface.data.PaymentWallProducts;

/**
 * Created by kirussell on 14.01.14.
 * Config for data in current session (from login[receive AuthToken] to logout[AuthorizationManager])
 */
public class SessionConfig extends AbstractConfig {
    public static final String SETTINGS_SOCIAL_ACCOUNT_NAME = "social_account_name";
    public static final String SETTINGS_SOCIAL_ACCOUNT_EMAIL = "social_account_email";
    private static final String SESSION_CONFIG_SETTINGS = "session_config_settings";

    private static final String DATA_PROFILE = "data_profile_user_data";
    private static final String DATA_OPTIONS = "data_options";
    private static final String DATA_MARKET_PRODUCTS = "data_google_products";
    private static final String DATA_MARKET_PRODUCTS_DETAILS = "data_google_products_details";
    private static final String DATA_PAYMENTWALL_PRODUCTS = "data_pw_products";
    private static final String DATA_PAYMENTWALL_MOBILE_PRODUCTS = "data_pw_mobile_products";

    public SessionConfig(Context context) {
        super(context);
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        // user profile
        addField(settingsMap, DATA_PROFILE, Static.EMPTY);
        // user options
        addField(settingsMap, DATA_OPTIONS, Static.EMPTY);
        // use market and paymentwall products
        addField(settingsMap, DATA_MARKET_PRODUCTS, Static.EMPTY);
        addField(settingsMap, DATA_MARKET_PRODUCTS_DETAILS, Static.EMPTY);
        addField(settingsMap, DATA_PAYMENTWALL_PRODUCTS, Static.EMPTY);
        addField(settingsMap, DATA_PAYMENTWALL_MOBILE_PRODUCTS, Static.EMPTY);
        //Social network account name
        addField(settingsMap, SETTINGS_SOCIAL_ACCOUNT_NAME, Static.EMPTY);
        //Social network account email
        addField(settingsMap, SETTINGS_SOCIAL_ACCOUNT_EMAIL, Static.EMPTY);
    }

    @Override
    protected SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(
                SESSION_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }

    // Profile

    /**
     * Set profile cache from profile request
     *
     * @param profileResponseJson profile json response
     * @return true on success
     */
    public boolean setProfileData(String profileResponseJson) {
        return setField(getSettingsMap(), DATA_PROFILE, profileResponseJson);
    }

    /**
     * Profile cached json response
     *
     * @return profile json response
     */
    public String getProfileData() {
        return getStringField(getSettingsMap(), DATA_PROFILE);
    }

    /**
     * Resets saved profile data
     */
    public void resetProfileData() {
        resetAndSaveConfig(DATA_PROFILE);
    }

    // Options

    /**
     * Sets options cache from options request
     *
     * @param optionsResponseJson options json response
     * @return true on success
     */
    public boolean setOptionsData(String optionsResponseJson) {
        return setField(getSettingsMap(), DATA_OPTIONS, optionsResponseJson);
    }

    /**
     * Options caches json response
     *
     * @return options json response
     */
    public String getOptionsData() {
        return getStringField(getSettingsMap(), DATA_OPTIONS);
    }

    /**
     * Resets saved options data
     */
    public void resetOptionsData() {
        resetAndSaveConfig(DATA_OPTIONS);
    }

    // Google Products

    /**
     * Sets google products cache from google play products request
     *
     * @param googleProductsResponseJson google play products json response
     * @return true on success
     */
    public boolean setMarketProductsData(String googleProductsResponseJson) {
        boolean res = setField(getSettingsMap(), DATA_MARKET_PRODUCTS, googleProductsResponseJson);
        this.saveConfig();
        return res;
    }

    /**
     * Sets google products dateils cache from openIab helper
     *
     * @param googleProductsDetailsJson google play products details json
     * @return true on success
     */
    public boolean setMarketProductsDetailsData(String googleProductsDetailsJson) {
        boolean res = setField(getSettingsMap(), DATA_MARKET_PRODUCTS_DETAILS, googleProductsDetailsJson);
        this.saveConfig();
        return res;
    }

    public boolean setPaymentWallProductsData(String pwProductsResponseJson, PaymentWallProducts.TYPE type) {
        boolean res = setField(getSettingsMap(), type == PaymentWallProducts.TYPE.MOBILE ? DATA_PAYMENTWALL_MOBILE_PRODUCTS :
                DATA_PAYMENTWALL_PRODUCTS, pwProductsResponseJson);
        this.saveConfig();
        return res;
    }

    /**
     * Google products cached json response
     *
     * @return google plat products json
     */
    public String getProductsData() {
        return getStringField(getSettingsMap(), DATA_MARKET_PRODUCTS);
    }

    /**
     * Google products details cached json response
     *
     * @return google products details json
     */
    public String getProductsDetailsData() {
        return getStringField(getSettingsMap(), DATA_MARKET_PRODUCTS_DETAILS);
    }

    public String getPaymentwallProductsData(PaymentWallProducts.TYPE type) {
        return getStringField(getSettingsMap(), type == PaymentWallProducts.TYPE.MOBILE ? DATA_PAYMENTWALL_MOBILE_PRODUCTS :
                DATA_PAYMENTWALL_PRODUCTS);
    }

    /**
     * Resets saved google play products data
     */
    public void resetGoogleProductsData() {
        resetAndSaveConfig(DATA_MARKET_PRODUCTS);
    }

    /**
     * Sets social account name
     *
     * @param name account name
     */
    public void setSocialAccountName(String name) {
        setField(getSettingsMap(), SETTINGS_SOCIAL_ACCOUNT_NAME, name);
    }

    /**
     * @return users's social account name
     */
    public String getSocialAccountName() {
        return getStringField(getSettingsMap(), SETTINGS_SOCIAL_ACCOUNT_NAME);
    }

    /**
     * Sets social account email
     *
     * @param email account email
     */
    public void setSocialAccountEmail(String email) {
        setField(getSettingsMap(), SETTINGS_SOCIAL_ACCOUNT_EMAIL, email);
    }

    /**
     * @return user's social account email
     */
    public String getSocialAccountEmail() {
        return getStringField(getSettingsMap(), SETTINGS_SOCIAL_ACCOUNT_EMAIL);
    }
}
