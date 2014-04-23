package com.topface.topface.data;

import android.net.ConnectivityManager;
import com.topface.statistics.android.StatisticsConfiguration;
import com.topface.topface.App;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.HttpUtils;
import org.json.JSONObject;

/**
 * Created by kirussell on 23.04.2014.
 */
public class AppOptions extends AbstractData {

    private ClientStatisticSettings clientStatisticsSettings = new ClientStatisticSettings();

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
            App.getAppConfig().setAppOptions(item.toString());
        } catch (Exception e) {
            Debug.error("AppOptions.class : Wrong response parsing", e);
        }
    }

    public StatisticsConfiguration getStatisticsConfiguration(int connectivityType) {
        return getStatisticsConfiguration(true, connectivityType);
    }

    public StatisticsConfiguration getStatisticsConfiguration(boolean hasConnection, int connectivityType) {
        boolean wifi = connectivityType == ConnectivityManager.TYPE_WIFI;
        return new StatisticsConfiguration(
                hasConnection && clientStatisticsSettings.enabled,
                wifi ? clientStatisticsSettings.maxSizeWifi : clientStatisticsSettings.maxSizeCell,
                wifi ? clientStatisticsSettings.timeoutWifi : clientStatisticsSettings.timeoutCell,
                HttpUtils.getUserAgent()
        );
    }

    private class ClientStatisticSettings {
        boolean enabled = false;
        long timeoutWifi = 60000;
        long timeoutCell = 150000;
        int maxSizeWifi = 200;
        int maxSizeCell = 200;

        ClientStatisticSettings() {
        }

        ClientStatisticSettings(JSONObject json) {
            enabled = json.optBoolean("enabled");
            timeoutWifi = json.optLong("timeoutWifi") * 1000;
            timeoutCell = json.optLong("timeoutCell") * 1000;
            maxSizeWifi = json.optInt("maxSizeWifi");
            maxSizeCell = json.optInt("maxSizeCell");
        }
    }
}
