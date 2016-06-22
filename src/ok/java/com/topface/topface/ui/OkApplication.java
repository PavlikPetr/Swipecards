package com.topface.topface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.OkStatsData;
import com.topface.topface.data.OkStatsResponseData;
import com.topface.topface.utils.OkUtils;
import com.topface.topface.utils.ReportStatsRequest;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.OkAuthorizer;

import java.util.Calendar;

import rx.functions.Action1;

import static com.topface.topface.data.OkStatsData.LAUNCH;
import static com.topface.topface.utils.social.Authorizer.AUTH_TOKEN_READY_ACTION;
import static com.topface.topface.utils.social.Authorizer.TOKEN_NOT_READY;
import static com.topface.topface.utils.social.Authorizer.TOKEN_READY;
import static com.topface.topface.utils.social.Authorizer.TOKEN_STATUS;

/**
 * Created by ppavlik on 18.05.16.
 * Child of main application, just for OK flavour
 */
public class OkApplication extends App {

    private static final String APP_START_OK_STATS = "app.start";

    private BroadcastReceiver mTokenReadyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int tokenStatus = intent.getIntExtra(TOKEN_STATUS, TOKEN_NOT_READY);
            switch (tokenStatus) {
                case TOKEN_READY:
                    OkUtils.sendAppStartRequest();
                    sendAppStartOkStats();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mTokenReadyReceiver,
                new IntentFilter(AUTH_TOKEN_READY_ACTION));
        super.onCreate();
        OkUtils.sendAppStartRequest();
        if (!AuthToken.getInstance().isEmpty()) {
            sendAppStartOkStats();
        }
    }

    private void sendAppStartOkStats() {
        new ReportStatsRequest(new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds()),
                new OkStatsData.OkStatsObjectData(APP_START_OK_STATS, Calendar.getInstance().getTimeInMillis(), LAUNCH, "true"))
                .getObservable()
                .subscribe(new Action1<OkStatsResponseData>() {
                    @Override
                    public void call(OkStatsResponseData okStatsResponseData) {
                        if (okStatsResponseData != null) {
                            if (okStatsResponseData.getErrors().size() == 0) {
                                Debug.log("OkStatistics", "All {" + okStatsResponseData.getProcessed() + "} reports send successfull");
                            } else {
                                Debug.log("OkStatistics", (okStatsResponseData.getProcessed() == 0 ? "No one reports send successfull" : okStatsResponseData.getProcessed() + " reports send successfull")
                                        + "\n" + JsonUtils.toJson(okStatsResponseData.getErrors()));
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Debug.error("OkStatistics", throwable);
                    }
                });
    }
}