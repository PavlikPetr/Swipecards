package com.topface.topface.utils.gcmutils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.statistics.NotificationStatistics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {

    public static AtomicBoolean isOnMessageReceived = new AtomicBoolean(false);

    @Override
    public void onMessageReceived(String from, Bundle data) {
        isOnMessageReceived.set(true);
        if (data != null) {
            Debug.log("GCM: Try show\n" + data.keySet());
            // send update broadcast in any case
            //Сообщаем о том что есть новое уведомление и нужно обновить список
            Intent broadcastNotificationIntent = new Intent(GCMUtils.GCM_NOTIFICATION);
            String user = data.getString("user");
            int type = GCMUtils.getType(data);
            NotificationStatistics.sendReceived(type, GCMUtils.getLabel(data));

            if (user != null) {
                String userId = getUserId(user);
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(App.getContext());

                broadcastNotificationIntent.putExtra(GCMUtils.GCM_TYPE, type);
                broadcastNotificationIntent.putExtra(GCMUtils.USER_ID_EXTRA, userId);
                localBroadcastManager.sendBroadcast(broadcastNotificationIntent);
                Intent updateIntent = null;
                switch (type) {
                    case GCMUtils.GCM_TYPE_MESSAGE:
                    case GCMUtils.GCM_TYPE_DIALOGS:
                    case GCMUtils.GCM_TYPE_GIFT:
                        updateIntent = new Intent(GCMUtils.GCM_DIALOGS_UPDATE);
                        break;
                    case GCMUtils.GCM_TYPE_MUTUAL:
                        updateIntent = new Intent(GCMUtils.GCM_MUTUAL_UPDATE);
                        break;
                    case GCMUtils.GCM_TYPE_LIKE:
                        updateIntent = new Intent(GCMUtils.GCM_LIKE_UPDATE);
                        break;
                    case GCMUtils.GCM_TYPE_GUESTS:
                        updateIntent = new Intent(GCMUtils.GCM_GUESTS_UPDATE);
                        break;
                    case GCMUtils.GCM_TYPE_PEOPLE_NEARBY:
                        updateIntent = new Intent(GCMUtils.GCM_PEOPLE_NEARBY_UPDATE);
                        break;
                }
                if (updateIntent != null) {
                    localBroadcastManager.sendBroadcast(updateIntent);
                }
            }
            // try to show notification
            GCMUtils.showNotificationIfNeed(data, App.getContext(), App.get().getOptions().updateUrl);
        }
    }

    private String getUserId(String user) {
        String id = "";
        try {
            JSONObject userJSON = new JSONObject(user);
            id = userJSON.optString("id");
        } catch (JSONException e) {
            Debug.error(e);
        }

        return id;
    }

}
