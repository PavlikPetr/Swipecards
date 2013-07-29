package com.topface.topface;

@SuppressWarnings("UnusedDeclaration")
public class Static {
    // Constants

    public static final int API_VERSION = 6;
    public static final String PLATFORM = "Android";

    public static final String API_ALPHA_URL = "http://api.alpha.topface.com/";
    public static final String API_BETA_URL = "http://api.beta.topface.com/";
    public static final String API_GAMMA_URL = "http://api.gamma.topface.com/";
    public static final String API_DELTA_URL = "http://api.delta.topface.com/";
    public static final String API_URL = "https://api.topface.com/";
    public static final String API_500_ERROR_URL = "http://httpstat.us/500";

    /**
     * Ревизия серверной части, используется толькро при тестировании на бете
     */
    public static final String REV = "20121019010";
    public static final String AUTH_VK_ID = "2664589";
    //Это id нашего забаненого приложения
    //public static final String AUTH_FACEBOOK_ID = "161347997227885";
    /**
     * Это id приложения нужный для авторизации
     */
    public static final String AUTH_FACEBOOK_ID = "642883445728173";


    public static final int GIRL = 0;
    public static final int BOY = 1;
    public static final int MIN_AGE = 16;
    public static final int MAX_AGE = 99;
    public static final int HEADER_SHADOW_SHIFT = -1;
    public static final int RED_ALERT_APPEARANCE_TIME = 3000;

    // Preferences
    public static final String INTENT_REQUEST_KEY = "requestCode";
    public static final String PREFERENCES_TAG_GEO = "preferences_geo";
    public static final String PREFERENCES_TAG_SHARED = "preferences_general";

    public static final String PREFERENCES_NEED_EDIT = "need_edit";
    public static final String PREFERENCES_NEED_CHANGE_PASSWORD = "need_change_password";
    public static final String PREFERENCES_NEED_CITY_CONFIRM = "city_need_confirm";
    public static final String PREFERENCES_NOVICE_DATING_ENERGY = "novice_dating_energy";
    public static final String PREFERENCES_NOVICE_DATING_ENERGY_TO_SYMPATHIES = "novice_dating_energy_to_sympathies";
    public static final String PREFERENCES_NOVICE_DATING_BUY_SYMPATHY = "novice_dating_buy_sympathy";
    public static final String PREFERENCES_NOVICE_DATING_BUY_SYMPATHY_DATE = "novice_dating_buy_symathy_date_tag";
    public static final String PREFERENCES_NOVICE_DATING_SYMPATHY = "novice_dating_sympathy";
    public static final String PREFERENCES_NOVICE_MENU_FILL_PROFILE = "novice_menu_fill_profile";
    public static final String PREFERENCES_NOVICE_MENU_FILL_PROFILE_DATE = "novice_menu_fill_profile_date_tag";
    public static final String PREFERENCES_STOP_TIME = "preferences_stop_time";
    public static final String PREFERENCES_FULLSCREEN_URLS_SET = "fullscreen_urls_string";

    public static final String PREFERENCES_LAST_FULLSCREEN_TIME = "fullScreeenBanner_last_time";

    public static final String PREFERENCES_TAG_PROFILE = "preferences_profile";
    public static final String PREFERENCES_DELETED_ACCOUNTS_FB_IDS = "fb_tokens";
    public static final String PREFERENCES_DELETED_ACCOUNTS_VK_IDS = "vk_tokens";
    public static final String PREFERENCES_DELETED_ACCOUNTS_TF_IDS = "tf_tokens";

    public static final String LOGOUT_INTENT = "com.topface.topface.intent.LOGOUT";
    public static final boolean PAUSE_DOWNLOAD_ON_SCROLL = false;
    public static final boolean PAUSE_DOWNLOAD_ON_FLING = true;

    public static final String EMPTY = "";
    public static final String SLASH = "/";
    public static final String QUESTION = "?";
    public static final String EQUAL = "=";
    public static final String AMPERSAND = "&";
}
