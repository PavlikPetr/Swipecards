package com.topface.topface.utils.debug;

import com.topface.topface.BuildConfig;

/**
 * Created by kirussell on 30/07/15.
 */
public class HockeySettings {

    static final String RELEASE_APP_ID = "817b00ae731c4a663272b4c4e53e4b61";
    static final String QA_APP_ID = "9bf289e229284c0cba8d47eba5da9618";
    static final String DEBUG_APP_ID = "176f942f1bdedecb45696cd6a6a58aac";

    public static String getAppId() {
        return BuildConfig.DEBUG ? QA_APP_ID : RELEASE_APP_ID;
    }
}
