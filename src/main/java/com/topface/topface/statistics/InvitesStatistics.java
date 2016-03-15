package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;

/**
 * Created by ppetr on 14.03.16.
 * Send statistics from all Invite places
 */
public class InvitesStatistics {

    public static final String PLC_INVITE_POPUP = "plc_invite_popup"; //события InvitesPopup
    public static final String PLC_SMS_INVITE = "plc_sms_invite"; //события SmsInviteFragment
    public static final String PLC_VK_INVITES = "plc_vk_invites"; //события InviteVkFriendsActivity

    private static final String INVITE_BTN_CLICK = "invite_btn_click"; //событие клика кнопки "Пригласить"
    private static final String CATCH_SUCCESS_INVITE_RESPONSE = "catch_invite_response"; //событие получения успешного ответа сервера
    private static final String CATCH_FAILED_INVITE_RESPONSE = "catch_failed_invite_response"; //событие ошибки при запросе
    private static final String INVITES_SUCCESS_PREMIUM_RECEIVED = "invites_success_premium_received"; //событие получения премиума за отправку приглашений
    private static final String CLOSE_INVITE_SCREEN = "close_invite_screen";
    private static final String SLICE_PLC = "plc"; //место срабатывания события
    private static final String SLICE_SPC = "spc"; //принадлежность пользователя к вип/премиум
    private static final String SLICE_VAL = "val"; //значение (по ситуации)
    // срез spc принимает значение 0 (неВип) или 1 (Вип)
    private final static String USER_RECEIVE_PREMIUM = "1";
    private final static String USER_HAS_NOT_RECEIVE_PREMIUM = "0";


    private static void send(String command, Slices slices) {
        StatisticsTracker.getInstance()
                .sendEvent(command, 1, slices);
    }

    public static void sendInviteBtnClickAction(String place) {
        if (!place.isEmpty()) {
            Slices slices = new Slices();
            slices.put(SLICE_PLC, place);
            send(INVITE_BTN_CLICK, slices);
        }
    }

    public static void sendSuccessInviteResponseAction(String place) {
        sendSuccessInviteResponseAction(place, null, 1);
    }

    public static void sendSuccessInviteResponseAction(String place, Boolean isGetPremium, int invitesSend) {
        if (!place.isEmpty()) {
            Slices slices = new Slices();
            slices.put(SLICE_PLC, place);
            if (isGetPremium != null) {
                slices.put(SLICE_SPC, isGetPremium ? USER_RECEIVE_PREMIUM : USER_HAS_NOT_RECEIVE_PREMIUM);
            }
            slices.put(SLICE_VAL, String.valueOf(invitesSend));
            send(CATCH_SUCCESS_INVITE_RESPONSE, slices);
        }
    }

    public static void sendPremiumReceivedAction(String place, int period) {
        if (!place.isEmpty()) {
            Slices slices = new Slices();
            slices.put(SLICE_PLC, place);
            slices.put(SLICE_VAL, String.valueOf(period));
            send(INVITES_SUCCESS_PREMIUM_RECEIVED, slices);
        }
    }

    public static void sendFailedInviteResponseAction(String place, Integer errorCode) {
        if (!place.isEmpty()) {
            Slices slices = new Slices();
            slices.put(SLICE_PLC, place);
            if (errorCode != null) {
                slices.put(SLICE_VAL, String.valueOf(errorCode));
            }
            send(CATCH_FAILED_INVITE_RESPONSE, slices);
        }
    }

    public static void sendCloseScreenAction(String place) {
        if (!place.isEmpty()) {
            Slices slices = new Slices();
            slices.put(SLICE_PLC, place);
            send(CLOSE_INVITE_SCREEN, slices);
        }
    }
}
