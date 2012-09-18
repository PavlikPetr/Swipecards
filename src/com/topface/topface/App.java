package com.topface.topface;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

//@ReportsCrashes(formKey = "dE85SXowSDhBcXZvMXAtUEtPMTg4X2c6MQ")
public class App extends Application {
    // Constants
    public static final String TAG = "Topface";
    public static boolean DEBUG = false;
    private static Context mContext;
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static Intent mConnectionIntent;
    private static ConnectionChangeReceiver mConnectionReceiver;
//    private static PluralResources mPluralResources;

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
        DEBUG = Utils.isDebugMode(this);
        mContext = getApplicationContext();
        // C2DM
        C2DMUtils.init(getContext());
        Debug.log("App", "+onCreate");
        Data.init(getApplicationContext());
        Recycle.init(getApplicationContext());
        //Начинаем слушать подключение к интернету
        if (mConnectionIntent == null) {
            mConnectionReceiver = new ConnectionChangeReceiver(mContext);
            mConnectionIntent = registerReceiver(mConnectionReceiver, new IntentFilter(CONNECTIVITY_CHANGE_ACTION));
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //Прекращаем слушать подключение к интернету
        if (mConnectionIntent != null && mConnectionReceiver != null) {
            unregisterReceiver(mConnectionReceiver);
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean isOnline() {
        return mConnectionReceiver.isConnected();
    }

}

