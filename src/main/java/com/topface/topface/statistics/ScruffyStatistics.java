package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.utils.Connectivity;
import com.topface.topface.utils.Utils;

/**
 * Created by kirussell on 27/02/15.
 * Statistics for scruffy work performance
 */
public class ScruffyStatistics {

    private static final String TF_SCRUFFY_CONNECT_SUCCESS = "scruffy_connect_success";
    private static final String TF_SCRUFFY_CONNECT_FAILURE = "scruffy_connect_failure";
    private static final String TF_SCRUFFY_REQUEST_SEND = "scruffy_request_send";
    private static final String TF_SCRUFFY_REQUEST_FAIL = "scruffy_request_fail";
    private static final String TF_SCRUFFY_RESPONSE_FAIL = "scruffy_response_fail";
    private static final String TF_SCRUFFY_RESPONSE_SUCCESS = "scruffy_response_success";
    private static final String TF_SCRUFFY_TRANSPORT_FALLBACK = "scruffy_transport_fallback";

    private static Slices slicesWithVal = new Slices();
    private static Slices slicesOnlyWithCon = new Slices();

    private static void send(String key) {
        StatisticsTracker.getInstance().sendEvent(
                key, 1,
                slicesOnlyWithCon
                        .putSlice(TfStatConsts.con, TfStatConsts.getConnType(Connectivity.getConnType(App.getContext())))
                        .putSlice(TfStatConsts.plc, Utils.getCarrierName())
        );
    }

    private static void send(String key, String val) {
        StatisticsTracker.getInstance().sendEvent(
                key, 1,
                slicesWithVal
                        .putSlice(TfStatConsts.val, val)
                        .putSlice(TfStatConsts.con, TfStatConsts.getConnType(Connectivity.getConnType(App.getContext())))
                        .putSlice(TfStatConsts.plc, Utils.getCarrierName())
        );
    }

    public static void sendScruffyConnectSuccess() {
        send(TF_SCRUFFY_CONNECT_SUCCESS);
    }

    public static void sendScruffyConnectFailure(String val) {
        send(TF_SCRUFFY_CONNECT_FAILURE, val);
    }

    public static void sendScruffyRequestSend() {
        send(TF_SCRUFFY_REQUEST_SEND);
    }

    public static void sendScruffyRequestFail(String val) {
        send(TF_SCRUFFY_REQUEST_FAIL, val);
    }

    public static void sendScruffyResponseSuccess() {
        send(TF_SCRUFFY_RESPONSE_SUCCESS);
    }

    public static void sendScruffyResponseFail(String val) {
        send(TF_SCRUFFY_RESPONSE_FAIL, val);
    }

    public static void sendScruffyTransportFallback() {
        send(TF_SCRUFFY_TRANSPORT_FALLBACK);
    }
}
