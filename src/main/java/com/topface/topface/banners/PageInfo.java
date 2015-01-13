package com.topface.topface.banners;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kirussell on 12/01/15.
 * Information about page with type of showing block or/and banner type
 */
public class PageInfo {
    /**
     * Идентификаторы страниц
     */
    public static final String PAGE_UNKNOWK = "UNKNOWN_PAGE";
    public final static String PAGE_LIKES = "LIKE";
    public final static String PAGE_MUTUAL = "MUTUAL";
    public final static String PAGE_MESSAGES = "MESSAGES";
    public final static String PAGE_VISITORS = "VISITORS";
    public final static String PAGE_DIALOGS = "DIALOGS";
    public final static String PAGE_FANS = "FANS";
    public final static String PAGE_BOOKMARKS = "BOOKMARKS";
    public final static String PAGE_VIEWS = "VIEWS";
    public final static String PAGE_START = "START";
    public final static String PAGE_GAG = "GAG";
    public final static String PAGE_TABBED_LIKES = "LIKES_TABS";
    public final static String PAGE_TABBED_MESSAGES = "MESSAGES_TABS";
    public final static String[] PAGES = new String[]{
            PAGE_UNKNOWK,
            PAGE_LIKES,
            PAGE_MUTUAL,
            PAGE_MESSAGES,
            PAGE_VISITORS,
            PAGE_DIALOGS,
            PAGE_FANS,
            PAGE_BOOKMARKS,
            PAGE_VIEWS,
            PAGE_START,
            PAGE_TABBED_LIKES,
            PAGE_TABBED_MESSAGES,
            PAGE_GAG
    };
    private static final String SEPARATOR = ";";

    public String name;
    @SerializedName("float")
    public String floatType;
    private String banner;

    public PageInfo(String name, String floatType, String banner) {
        this.name = name;
        this.floatType = floatType;
        this.banner = banner;
    }

    public String getBanner() {
        return banner;
    }

    @Override
    public String toString() {
        return name + SEPARATOR + floatType + SEPARATOR + banner;
    }

    public static PageInfo parseFromString(String str) {
        String[] params = str.split(SEPARATOR);
        if (params.length == 3) {
            return new PageInfo(params[0], params[1], params[2]);
        } else {
            return null;
        }
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }
}
