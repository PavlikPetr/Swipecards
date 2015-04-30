package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by ppetr on 27.04.15.
 * catch here status of sms send
 */
public class CatchSMSActions extends BroadcastReceiver {
    public static final String FILTER_SMS_SENT = "android.provider.Telephony.SMS_SEND";

    @Override
    public void onReceive(Context context, Intent intent) {

        String filter = intent.getAction();

        if (FILTER_SMS_SENT.equals(filter)) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    parseMessage(intent);
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    break;
            }
        }
    }

    private void parseMessage(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String text = bundle.getString(SMSInviteFragment.SMS_TEXT);
            String phone = bundle.getString(SMSInviteFragment.PHONE_NUMBER);
            Log.e("TOPFACETEST", "text " + text);
            Log.e("TOPFACETEST", "phone " + phone);
        }
    }
}