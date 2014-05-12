package com.topface.statistics;

import java.util.List;

/**
 * Created by kirussell on 17.04.2014.
 * Interface for async writing/reading data from storage
 */
public interface IAsyncStorage {
    void writeData(String key, List<String> data);

    void readData(IStorageReadListener listener, String... keys);

    public static interface IStorageReadListener {
        void onDataObtained(String key, List<String> data);

        void onFinished();
    }
}