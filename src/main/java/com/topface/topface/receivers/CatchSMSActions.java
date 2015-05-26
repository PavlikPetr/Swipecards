package com.topface.topface.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.topface.framework.JsonUtils;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MarkSMSInviteRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.fragments.SMSInviteFragment;

import static com.topface.topface.ui.fragments.SMSInviteFragment.PHONES_STATUSES.CAN_SEND_CONFIRMATION;
import static com.topface.topface.ui.fragments.SMSInviteFragment.PHONES_STATUSES.CONFIRMATION_WAS_SENT;
import static com.topface.topface.ui.fragments.SMSInviteFragment.PHONES_STATUSES.USER_REGISTERED;

/**
 * Created by ppetr on 27.04.15.
 * catch here status of sms send
 */
public class CatchSMSActions extends BroadcastReceiver {

    public static final String SMS_WAS_SEND = "com.topface.topface.SMS.SMS_WAS_SEND";
    public static final String INVITATIONS_SENT_COUNT = "invitations_sent_count";
    public static final String FRIENDS_REGISTERED_COUNT = "friends_registered_count";
    public static final String SMS_SENT_STATUS = "sms_sent_status";

    public static final String FILTER_SMS_SENT = "android.provider.Telephony.SMS_SEND";

    @Override
    public void onReceive(Context context, Intent intent) {

        String filter = intent.getAction();

        if (FILTER_SMS_SENT.equals(filter)) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    parseMessage(context, intent);
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

    private void parseMessage(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String text = bundle.getString(SMSInviteFragment.SMS_TEXT);
            String phone = bundle.getString(SMSInviteFragment.SMS_PHONE_NUMBER);
            int id = bundle.getInt(SMSInviteFragment.SMS_ID);
            final String phoneId = bundle.getString(SMSInviteFragment.SMS_PHONE_ID);
            Log.e("TOPFACE_TEST", "CatchSMSActions text " + text);
            Log.e("TOPFACE_TEST", "CatchSMSActions phone " + phone);
            Log.e("TOPFACE_TEST", "CatchSMSActions id " + id);
            Log.e("TOPFACE_TEST", "CatchSMSActions phone id = " + phoneId);
            new MarkSMSInviteRequest(context, id).callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    SMSInvitationCounters counters = JsonUtils.fromJson(response.toString(), SMSInvitationCounters.class);
                    Integer invitationCount = null;
                    Integer registeredCount = null;
                    if (null != counters) {
                        invitationCount = counters.sentCount;
                        registeredCount = counters.registeredCount;
                    }
                    sendBroadcast(context, CONFIRMATION_WAS_SENT.getPosition(), phoneId, invitationCount, registeredCount);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    int status;
                    switch (codeError) {
                        case ErrorCodes.CODE_SMS_INVITE_ALREADY_SENT:
                            status = CONFIRMATION_WAS_SENT.getPosition();
                            break;
                        case ErrorCodes.CODE_SMS_INVITE_ALREADY_REGISTERED:
                            status = USER_REGISTERED.getPosition();
                            break;
                        default:
                            status = CAN_SEND_CONFIRMATION.getPosition();
                            break;
                    }
                    sendBroadcast(context, status, phoneId);
                }
            }).exec();
        }
    }

    private Intent createIntent(int status, String phoneId, Integer invitationCount, Integer registeredCount) {
        Intent intent = new Intent(SMS_WAS_SEND);
        intent.putExtra(INVITATIONS_SENT_COUNT, invitationCount);
        intent.putExtra(FRIENDS_REGISTERED_COUNT, registeredCount);
        intent.putExtra(SMSInviteFragment.SMS_PHONE_ID, phoneId);
        intent.putExtra(SMS_SENT_STATUS, status);
        return intent;
    }

    private void sendBroadcast(Context context, int status, String phoneId) {
        sendBroadcast(context, status, phoneId, null, null);
    }

    private void sendBroadcast(Context context, int status, String phoneId, Integer invitationCount, Integer registeredCount) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(createIntent(status, phoneId, invitationCount, registeredCount));
    }

    private class SMSInvitationCounters {
        public int sentCount;
        public int registeredCount;
    }
}