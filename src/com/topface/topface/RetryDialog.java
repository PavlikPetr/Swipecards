package com.topface.topface;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created with IntelliJ IDEA.
 * User: Ilya Vorobiev
 * Date: 19.10.12
 * Time: 9:43
 * To change this template use File | Settings | File Templates.
 */
public class RetryDialog extends AlertDialog {
    private BroadcastReceiver mReciever;
    private Context mContext;

    public RetryDialog(Context context) {
        super(context);
        mContext = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReciever = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                RetryDialog.this.getButton(Dialog.BUTTON_POSITIVE).performClick();
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReciever, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReciever);
    }
}
