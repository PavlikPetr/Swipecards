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

    private ClientStatisticSettings clientStatisticsSettings = new ClientStatisticSettings();
    /**
     * Session timeout in seconds
     */
    private int sessionTimeout;

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
            sessionTimeout = item.optInt("sessionTimeout", DEFAULT_SESSION_TIMEOUT);
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
}
