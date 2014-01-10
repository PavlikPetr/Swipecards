package com.topface.topface.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.Static;
import com.topface.topface.utils.AppConfig;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.TopfaceNotificationManager;

public class TestNotificationsReceiver extends BroadcastReceiver {

    public static final String ACTION_NOTIFY = "com.topface.topface.actions.NOTIFY";
    public static final String ACTION_TEST_NETWORK_ERRORS = "com.topface.topface.actions.TEST_NETWORK_ERRORS";
    public static final String ACTION_CANCEL_TEST_NETWORK_ERRORS = "com.topface.topface.actions.CANCEL_TEST_NETWORK_ERRORS";

    private static final String EXTRA_ACTION_PARAMETER = "extraParameter";

    @Override
    public void onReceive(Context context, Intent intent) {
        AppConfig config = App.getConfig();
        String action = intent.getAction();
        if (action == null) return;
        if (action.equals(ACTION_TEST_NETWORK_ERRORS)) {
            config.setApiUrl(Static.API_500_ERROR_URL, 0, "");
            Toast.makeText(context, "Now you will receive network errors", Toast.LENGTH_LONG).show();
        } else if (action.equals(ACTION_CANCEL_TEST_NETWORK_ERRORS)) {
            int notificationId = intent.getIntExtra(EXTRA_ACTION_PARAMETER, 0);
            TopfaceNotificationManager.getInstance(App.getContext())
                    .cancelNotification(notificationId);
            config.setTestNetwork(false);
            config.setApiUrl(Static.API_URL, Static.API_VERSION, null);
            config.saveConfig();
            Toast.makeText(context, "All requests will be OK. No more errors.", Toast.LENGTH_LONG).show();
        } else if (action.equals(ACTION_NOTIFY)) {
            Debug.log("TOPFACE_NOTIFICATION:" + intent.getStringExtra("text"));
            GCMUtils.showNotification(intent, context);
        }
    }

    public static PendingIntent createBroadcastPendingIntent(String action) {
        return createBroadcastPendingIntent(action, null);
    }

    public static PendingIntent createBroadcastPendingIntent(String action, Integer extraParameter) {
        Intent intent = new Intent(action);
        if (extraParameter != null) {
            intent.putExtra(EXTRA_ACTION_PARAMETER, extraParameter);
        }
        return PendingIntent.getBroadcast(App.getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
