package com.topface.topface.data;

import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.statistics.android.StatisticsConfiguration;
import com.topface.topface.App;
import com.topface.topface.utils.Connectivity;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Application options
 */
public class AppOptions extends AbstractData {

    private static final int DEFAULT_SESSION_TIMEOUT = 1200;
    private static final int DEFAULT_MAX_PARTIAL_REQUEST_COUNT = 5;

    private ClientStatisticSettings clientStatisticsSettings = new ClientStatisticSettings();
    private Conditions conditions = new Conditions();

    /**
     * минимальные размеры фотографии, которая может быть загружена
     */
    private MinPhotoSize minPhotoSize = new MinPhotoSize();

    /**
     * Session timeout in seconds
     */
    private int sessionTimeout;
    private int maxPartialRequestsCount;
    private Boolean scruffy = null;
    public Invites invites = new Invites();

    public AppOptions(JSONObject data) {
        if (data != null) {
            fillData(data);
        }
    }

    private void fillData(JSONObject item) {
        try {
            JSONObject clientStatisticsJson = item.optJSONObject("clientStatisticSettings");
            if (clientStatisticsJson != null) {
                clientStatisticsSettings = new ClientStatisticSettings(clientStatisticsJson);
            }
            maxPartialRequestsCount = item.optInt("maxPartialRequestsCount", DEFAULT_MAX_PARTIAL_REQUEST_COUNT);
            sessionTimeout = item.optInt("sessionTimeout", DEFAULT_SESSION_TIMEOUT);
            scruffy = item.optBoolean("scruffy", false);
            minPhotoSize = JsonUtils.fromJson(item.optString("minPhotoSize"), MinPhotoSize.class);
            invites = JsonUtils.optFromJson(item.optString("invites"), Invites.class, new Invites());
            JSONObject conditionsJson = item.optJSONObject("conditions");
            if (conditionsJson != null) {
                conditions = new Conditions(conditionsJson);
            }
            App.getAppConfig().setAppOptions(item.toString());
        } catch (Exception e) {
            Debug.error("AppOptions.class : Wrong response parsing", e);
        }
    }

    public StatisticsConfiguration getStatisticsConfiguration(Connectivity.Conn connectivityType) {
        return getStatisticsConfiguration(true, connectivityType);
    }

    public StatisticsConfiguration getStatisticsConfiguration(boolean hasConnection, Connectivity.Conn connectivityType) {
        boolean wifi = connectivityType == Connectivity.Conn.WIFI;
        return new StatisticsConfiguration(
                hasConnection && clientStatisticsSettings.enabled,
                hasConnection && clientStatisticsSettings.connectionStatisticsEnabled,
                wifi ? clientStatisticsSettings.maxSizeWifi : clientStatisticsSettings.maxSizeCell,
                wifi ? clientStatisticsSettings.timeoutWifi : clientStatisticsSettings.timeoutCell,
                HttpUtils.getUserAgent());
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getMaxPartialRequestsCount() {
        return maxPartialRequestsCount;
    }

    public Conditions getConditions() {
        return conditions;
    }

    public int getUserStringSettingMaxLength() {
        return conditions.userStringSettingMaxLength;
    }

    public MinPhotoSize getMinPhotoSize() {
        if (minPhotoSize != null) {
            return minPhotoSize;
        } else {
            return new MinPhotoSize();
        }
    }

    public int getUserWeightMin() {
        return conditions.userWeightMin;
    }

    public int getUserWeightMax() {
        return conditions.userWeightMax;
    }

    public int getUserHeightMin() {
        return conditions.userHeightMin;
    }

    public int getUserHeightMax() {
        return conditions.userHeightMax;
    }

    public int getUserAboutMeMaxLength() {
        return conditions.userAboutMeMaxLength;
    }

    public int getUserAgeMin() {
        return conditions.userAgeMin;
    }

    public int getUserAgeMax() {
        return conditions.userAgeMax;
    }

    public int getUserStatusMaxLength() {
        return  conditions.userStatusMaxLength;
    }

    public boolean isScruffyEnabled() {
        return scruffy != null ? scruffy : false;
    }

    private class ClientStatisticSettings {
        boolean enabled = false;
        boolean connectionStatisticsEnabled = false;
        long timeoutWifi = 60000;
        long timeoutCell = 150000;
        int maxSizeWifi = 200;
        int maxSizeCell = 200;

        ClientStatisticSettings() {
        }

        ClientStatisticSettings(JSONObject json) {
            enabled = json.optBoolean("enabled");
            connectionStatisticsEnabled = json.optBoolean("connectionStatisticsEnabled");
            timeoutWifi = json.optLong("timeoutWifi") * 1000;
            timeoutCell = json.optLong("timeoutCell") * 1000;
            maxSizeWifi = json.optInt("maxSizeWifi");
            maxSizeCell = json.optInt("maxSizeCell");
        }
    }

    private class Conditions {
        int userStringSettingMaxLength = 1024;
        int userAboutMeMaxLength = 1024;
        int userStatusMaxLength = 1024;
        int userWeightMin = 40;
        int userWeightMax = 160;
        int userHeightMin = 150;
        int userHeightMax = 220;
        int userAgeMin = 16;
        int userAgeMax = 99;

        Conditions() {
        }

        Conditions(JSONObject json) {
            userStringSettingMaxLength = json.optInt("userStringSettingMaxLength", 1024);
            userAboutMeMaxLength = json.optInt("userAboutMeMaxLength", 1024);
            userStatusMaxLength = json.optInt("userStatusMaxLength", 1024);
            userWeightMin = json.optInt("userWeightMin", 40);
            userWeightMax = json.optInt("userWeightMax", 160);
            userHeightMin = json.optInt("userHeightMin", 150);
            userHeightMax = json.optInt("userHeightMax", 220);
            userAgeMin = json.optInt("userAgeMin", 16);
            userAgeMax = json.optInt("userAgeMax", 99);
            DatingFilter.MIN_AGE = userAgeMin;
            DatingFilter.MAX_AGE = userAgeMax;
        }
    }

    public class MinPhotoSize {
        public int height = 150;
        public int width = 200;

        MinPhotoSize() {
        }
    }

    public class Invites {
        private ArrayList<String> facebookInvites;

        public boolean isLinkValid(String link) {
            if (!TextUtils.isEmpty(link) && !facebookInvites.isEmpty()) {
                for (String template: facebookInvites) {
                    if (link.matches(getCleanTemplate(template))) return true;
                }
            }
            return false;
        }

        private String getCleanTemplate(String template) {
            // можно было бы занести в константы {{tf_uid_hash}} но что-то у меня сомнения в надежности
            // данная строка настраивается руками в админке, мало ли опечатаются
            String mask = template.substring(template.indexOf("{{"), template.lastIndexOf("}}") + 2);
            return template.replace(mask, ".*");
        }

        /**
         * Construct empty invites if not found in options response
         */
        Invites() {
            facebookInvites = new ArrayList<>();
        }
    }
}
