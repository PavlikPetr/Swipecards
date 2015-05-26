package com.topface.topface.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;

import com.topface.framework.JsonUtils;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MarkSMSInviteRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.fragments.SmsInviteFragment;

import static com.topface.topface.ui.fragments.SmsInviteFragment.PHONES_STATUSES.CAN_SEND_CONFIRMATION;
import static com.topface.topface.ui.fragments.SmsInviteFragment.PHONES_STATUSES.CONFIRMATION_WAS_SENT;
import static com.topface.topface.ui.fragments.SmsInviteFragment.PHONES_STATUSES.USER_REGISTERED;

/**
 * Created by ppetr on 27.04.15.
 * catch here status of sms send
 */
public class CatchSmsActions extends BroadcastReceiver {

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
            String text = bundle.getString(SmsInviteFragment.SMS_TEXT);
            String phone = bundle.getString(SmsInviteFragment.SMS_PHONE_NUMBER);
            int id = bundle.getInt(SmsInviteFragment.SMS_ID);
            final String phoneId = bundle.getString(SmsInviteFragment.SMS_PHONE_ID);
            new MarkSMSInviteRequest(context, id).callback(new DataApiHandler<SMSInvitationCounters>() {

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

                @Override
                protected void success(SMSInvitationCounters data, IApiResponse response) {
                    Integer invitationCount = null;
                    Integer registeredCount = null;
                    if (null != data) {
                        invitationCount = data.sentCount;
                        registeredCount = data.registeredCount;
                    }
                    sendBroadcast(context, CONFIRMATION_WAS_SENT.getPosition(), phoneId, invitationCount, registeredCount);
                }

                @Override
                protected SMSInvitationCounters parseResponse(ApiResponse response) {
                    return JsonUtils.fromJson(response.toString(), SMSInvitationCounters.class);
                }

            }).exec();
        }
    }

    private Intent createIntent(int status, String phoneId, Integer invitationCount, Integer registeredCount) {
        Intent intent = new Intent(SMS_WAS_SEND);
        intent.putExtra(INVITATIONS_SENT_COUNT, invitationCount);
        intent.putExtra(FRIENDS_REGISTERED_COUNT, registeredCount);
        intent.putExtra(SmsInviteFragment.SMS_PHONE_ID, phoneId);
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