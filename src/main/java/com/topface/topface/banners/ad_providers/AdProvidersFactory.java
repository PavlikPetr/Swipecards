package com.topface.topface.banners.ad_providers;

/**
 * Created by kirussell on 12/01/15.
 * Factory through which you can obtain needed ad's provider
 */
public class AdProvidersFactory {

    /**
     * Идентификаторы типов баннеров
     */
    public static final String BANNER_TOPFACE = "TOPFACE";
    public static final String BANNER_ADMOB = "ADMOB";
    public static final String BANNER_ADMOB_MEDIATION = "ADMOB_MEDIATION";
    public static final String BANNER_ADMOB_FULLSCREEN_START_APP = "ADMOB_FULLSCREEN_EXP";
    public static final String BANNER_APPODEAL_FULLSCREEN = "APPODEAL_FULLSCREEN";
    public static final String BANNER_ADTOAPP_FULLSCREEN = "ADTOAPP_FULLSCREEN";
    public static final String BANNER_GAG = "GAG";
    public static final String BANNER_NONE = "NONE";
    public static final String BANNER_APPODEAL = "APPODEAL";
    public static final String BANNER_ADTOAPP = "ADTOAPP";
    public static final String[] BANNERS = new String[]{
            BANNER_TOPFACE,
            BANNER_ADMOB,
            BANNER_ADMOB_MEDIATION,
            BANNER_GAG,
            BANNER_NONE,
            BANNER_APPODEAL,
            BANNER_ADTOAPP
    };

    /**
     * Creates provider for given banner name
     *
     * @param banner name of banner system from server
     * @return appropriate provider
     */
    public IAdsProvider createProvider(String banner) {
        switch (banner) {
            case BANNER_ADMOB:
                return new AdMobProvider();
            case BANNER_TOPFACE:
                return new TopfaceBannerProvider();
            case BANNER_ADMOB_MEDIATION:
                return new AdMobMediationProvider();
            case BANNER_APPODEAL:
                return new AppodealProvider();
            case BANNER_ADTOAPP:
                return new AdToAppProvider();
            default:
                return null;
        }
    }
}
