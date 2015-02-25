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
    public static final String BANNER_ADMAN = "ADMAN";
    public static final String BANNER_MONEY_TAP = "MONEY_TAPP";
    public static final String MAD_NET = "MAD_NET";
    public static final String BANNER_ADWIRED = "ADWIRED";
    public static final String BANNER_GAG = "GAG";
    public static final String BANNER_NONE = "NONE";
    public static final String[] BANNERS = new String[]{
            BANNER_TOPFACE,
            BANNER_ADMOB,
            BANNER_ADMOB_MEDIATION,
            BANNER_ADMAN,
            BANNER_MONEY_TAP,
            MAD_NET,
            BANNER_ADWIRED,
            BANNER_GAG,
            BANNER_NONE
    };

    /**
     * Creates provider for given banner name
     *
     * @param banner name of banner system from server
     * @return appropriate provider
     */
    public IAdsProvider createProvider(String banner) {
//        switch (banner) {
//            case BANNER_ADMOB:
//                return new AdMobProvider();
//            case BANNER_ADWIRED:
//                return new AdWiredProvider();
//            case BANNER_TOPFACE:
//                return new TopfaceBannerProvider();
//            case BANNER_ADMOB_MEDIATION:
//                return new AdMobMediationProvider();
//            case BANNER_ADMAN:
//                return new AdmanProvider();
//            case BANNER_MONEY_TAPP:
//                return new MoneyTappProvider();
//            default:
//                return null;
//        }
        return new AdMobMediationProvider();
    }
}
