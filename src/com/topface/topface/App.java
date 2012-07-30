package com.topface.topface;

import android.content.Context;
import com.topface.topface.utils.Debug;
import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

//@ReportsCrashes(formKey = "dERGQ3d6dTNjbHhlTGhfdkpfWF9EbVE6MQ")
public class App extends Application {
    private static Context mContext;
    public static final String TAG = "Topface";
    public static final boolean DEBUG = true;

    @Override
    public void onCreate() {
        //ACRA.init(this);
        super.onCreate();
        mContext = getApplicationContext();
        Data.init(getApplicationContext());
        Recycle.init(getApplicationContext());
    }

    public static Context getContext() {
        return mContext;
    }
}