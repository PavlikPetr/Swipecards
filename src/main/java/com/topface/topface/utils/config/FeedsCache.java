package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.Static;
import com.topface.topface.utils.social.AuthToken;

import static com.topface.topface.utils.config.FeedsCache.FEEDS_TYPE.DATA_ADMIRATION_FEEDS;
import static com.topface.topface.utils.config.FeedsCache.FEEDS_TYPE.DATA_BOOKMARKS_FEEDS;
import static com.topface.topface.utils.config.FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS;
import static com.topface.topface.utils.config.FeedsCache.FEEDS_TYPE.DATA_FANS_FEEDS;
import static com.topface.topface.utils.config.FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS;
import static com.topface.topface.utils.config.FeedsCache.FEEDS_TYPE.DATA_MUTUALS_FEEDS;
import static com.topface.topface.utils.config.FeedsCache.FEEDS_TYPE.DATA_VISITORS_FEEDS;

public class FeedsCache extends AbstractConfig {

    public static final int FEEDS_CACHE_VERSION = 1;

    public static final String FEEDS_CACHE = "feeds_cache";

    public enum FEEDS_TYPE {
        UNKNOWN_TYPE(""),
        DATA_DIALOGS_FEEDS("data_dialogs_feeds"),
        DATA_BOOKMARKS_FEEDS("data_bookmarks_feeds"),
        DATA_LIKES_FEEDS("data_likes_feeds"),
        DATA_MUTUALS_FEEDS("data_mutuals_feeds"),
        DATA_ADMIRATION_FEEDS("data_admiration_feeds"),
        DATA_VISITORS_FEEDS("data_visitors_feeds"),
        DATA_FANS_FEEDS("data_fans_feeds");

        private String mText;

        FEEDS_TYPE(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
    }

    private static final String DATA_FEEDS_CACHE_VERSION = "data_feeds_cache_version";

    private String mUnique;

    public FeedsCache(String uniqueKey, Context context) {
        super(context);
        mUnique = uniqueKey;
    }

    private void setUnique(String mUnique) {
        this.mUnique = mUnique;
    }

    public String getUnique() {
        return mUnique;
    }

    public void updateConfig(String unique) {
        setUnique(unique);
        initData();
        saveConfig();
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        addField(settingsMap, DATA_DIALOGS_FEEDS.getText(), "");
        addField(settingsMap, DATA_BOOKMARKS_FEEDS.getText(), "");
        addField(settingsMap, DATA_LIKES_FEEDS.getText(), "");
        addField(settingsMap, DATA_MUTUALS_FEEDS.getText(), "");
        addField(settingsMap, DATA_VISITORS_FEEDS.getText(), "");
        addField(settingsMap, DATA_FANS_FEEDS.getText(), "");
        addField(settingsMap, DATA_ADMIRATION_FEEDS.getText(), "");
    }

    @Override
    protected SharedPreferences getPreferences() {
        if (mUnique == null) {
            mUnique = AuthToken.getInstance().getUserTokenUniqueId();
        }
        return getContext().getSharedPreferences(
                FEEDS_CACHE + Static.AMPERSAND + mUnique,
                Context.MODE_PRIVATE
        );
    }

    /**
     * Saves current version of config to trace relevance
     */
    public void saveConfigAdditional(SharedPreferences.Editor editor) {
        editor.putInt(DATA_FEEDS_CACHE_VERSION, FEEDS_CACHE_VERSION);
    }

    /**
     * Save feeds json to prefference
     *
     * @param value json of feeds array
     * @param type  feeds type
     */
    public FeedsCache setFeedToCache(String value, FEEDS_TYPE type) {
        SettingsMap settingsMap = getSettingsMap();
        setField(settingsMap, type.getText(), value);
        return this;
    }

    /**
     * Get json of feeds by type
     *
     * @param type feeds type
     * @return json of feeds array
     */
    public String getFeedFromCache(FEEDS_TYPE type) {
        return getStringField(getSettingsMap(), type.getText());
    }
}
