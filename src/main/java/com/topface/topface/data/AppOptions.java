package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.statistics.android.StatisticsConfiguration;
import com.topface.topface.App;
import com.topface.topface.utils.Connectivity;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONObject;

/**
 * Application options
 */
public class AppOptions extends AbstractData {

    private static final int DEFAULT_SESSION_TIMEOUT = 1200;
    private static final int DEFAULT_MAX_PARTIAL_REQUEST_COUNT = 5;

    private ClientStatisticSettings clientStatisticsSettings = new ClientStatisticSettings();
    private Conditions conditions = new Conditions();
    /**
     * Session timeout in seconds
     */
    private int sessionTimeout;
    private int maxPartialRequestsCount;
    private boolean scruffy;

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

    public int getUserStatusMaxLength() {
        return conditions.userStatusMaxLength;
    }

    public boolean isScruffyEnabled() {
        return scruffy;
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
        int userStatusMaxLength = 1024;
        int userWeightMin = 1;
        int userWeightMax = 999;
        int userHeightMin = 1;
        int userHeightMax = 999;

        Conditions() {
        }

        Conditions(JSONObject json) {
            userStatusMaxLength = json.optInt("userStatusMaxLength");
            userWeightMin = json.optInt("userWeightMin");
            userWeightMax = json.optInt("userWeightMax");
            userHeightMin = json.optInt("userHeightMin");
            userHeightMax = json.optInt("userHeightMax");
        }
    }
}
