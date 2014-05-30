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
    private static final String SESSION_CONFIG_SETTINGS = "session_config_settings";

    private static final String DATA_PROFILE = "data_profile_user_data";
    private static final String DATA_OPTIONS = "data_options";
    private static final String DATA_MARKET_PRODUCTS = "data_google_products";
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
        addField(settingsMap, DATA_PAYMENTWALL_PRODUCTS, Static.EMPTY);
        addField(settingsMap, DATA_PAYMENTWALL_MOBILE_PRODUCTS, Static.EMPTY);
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
        return setField(getSettingsMap(), DATA_MARKET_PRODUCTS, googleProductsResponseJson);
    }

    public boolean setPaymentWallProductsData(String pwProductsResponseJson, PaymentWallProducts.TYPE type) {
        return setField(getSettingsMap(), type == PaymentWallProducts.TYPE.MOBILE ? DATA_PAYMENTWALL_MOBILE_PRODUCTS :
                DATA_PAYMENTWALL_PRODUCTS, pwProductsResponseJson);
    }

    /**
     * Google products cached json response
     *
     * @return google plat products json
     */
    public String getProductsData() {
        return getStringField(getSettingsMap(), DATA_MARKET_PRODUCTS);
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
}
