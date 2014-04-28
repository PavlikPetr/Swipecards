package com.topface.statistics.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import android.text.TextUtils;
import com.topface.statistics.IAsyncStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kirussell on 22.04.2014.
 * Async storage based on SharedPreferences
 */
public class SharedPreferencesStorage implements IAsyncStorage {

    private static final String PREFS = "com.topface.statistics.android.QUEUE_DATA";
    private static final String DELIMITER = "|";

    private final SharedPreferences mPreferences;

    public SharedPreferencesStorage(Context context) {
        mPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public void writeData(final String key, final List<String> data) {
        startThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mPreferences) {
                    mPreferences.edit().putString(key, packData(data));
                }
            }
        });
    }

    @Override
    public void readData(final IStorageReadListener listener, final String... keys) {
        startThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mPreferences) {
                    for (String key : keys) {
                        listener.onDataObtained(key, unpackData(mPreferences.getString(key, "")));
                    }
                    listener.onFinished();
                }
            }
        });
    }

    private void startThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    private String packData(List<String> data) {
        return TextUtils.join(DELIMITER, data);
    }

    private List<String> unpackData(String data) {
        return new ArrayList<>(Arrays.asList(TextUtils.split(data, DELIMITER)));
    }
}
