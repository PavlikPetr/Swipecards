package com.topface.topface.banners;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kirussell on 12/01/15.
 * Information about page with type of showing block or/and banner type
 */
public class PageInfo {
    /**
     * Идентификаторы для типов блоков (лидеры, баннеры, не показывать блоки)
     */
    public final static String FLOAT_TYPE_NONE = "NONE";
    public final static String FLOAT_TYPE_BANNER = "BANNER";
    public final static String[] FLOAT_TYPES = new String[]{
            FLOAT_TYPE_BANNER,
            FLOAT_TYPE_NONE
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

    /**
     * Created by kirussell on 15/01/15.
     * Pages' identifiers with given String's names from server map
     */
    public static enum PageName {
        UNKNOWN_PAGE(),
        LIKE(),
        MUTUAL(),
        MESSAGES(),
        VISITORS(),
        DIALOGS(),
        FANS(),
        BOOKMARKS(),
        VIEWS(),
        START(),
        GAG(),
        LIKES_TABS(),
        MESSAGES_TABS();

        private final String mName;

        PageName() {
            mName = name();
        }

        @SuppressWarnings("unused")
        PageName(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }
}
