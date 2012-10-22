package com.topface.topface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.topface.topface.ui.NavigationActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Ilya Vorobiev
 * Date: 18.10.12
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class RetryRequestReceiver extends BroadcastReceiver {
    public static final String RETRY_INTENT = "com.topface.topface.action.RETRY";
    public static final String RETRY_REQUEST_FROM_RECEIVER = "RetryRequestFromReciever";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(RETRY_INTENT)) {
            Intent retryIntent = new Intent(context, NavigationActivity.class);
            retryIntent.putExtra(RETRY_REQUEST_FROM_RECEIVER, true);
            context.startActivity(retryIntent);
        }
    }

}
