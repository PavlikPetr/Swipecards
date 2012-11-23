package com.topface.topface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.topface.topface.ui.AuthActivity;

public class ReAuthReceiver extends BroadcastReceiver {

    public static final String REAUTH_INTENT = "com.topface.topface.action.AUTH";
    public static final String REAUTH_FROM_RECEIVER = "AuthFromReciever";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(REAUTH_INTENT)) {
            Intent authIntent = new Intent(context, AuthActivity.class);
            authIntent.putExtra(REAUTH_FROM_RECEIVER, true);
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            authIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(authIntent);
        }
    }

}
