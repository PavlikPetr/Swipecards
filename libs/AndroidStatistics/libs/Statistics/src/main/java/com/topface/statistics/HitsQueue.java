package com.topface.statistics;

import java.util.*;

/**
 * Created by kirussell on 09.04.14.
 * Not almost a queue, but temporary controller of hits' data
 * - stores data in memory
 * - writes not dispatched data to given storage and retrieves it
 * Note: if data storage not presented, not dispatched data will be lost
 */
public class HitsQueue {

    private static final String DELIMITER = "&";
    private static final String IMPORTANT_HITS_KEY = "important_hits";
    private static final String COMMON_HITS_KEY = "common_hits";
    private LinkedList<Entry<Long, String>> mCommonHits = new LinkedList<>();
    private LinkedList<String> mImportantHits = new LinkedList<>();
    private IAsyncStorage mStorage;

    public HitsQueue(IAsyncStorage storage) {
        mStorage = storage;
    }

    private static String[] splitString(String str, String delim) {
        StringTokenizer stringtokenizer = new StringTokenizer(str, delim);
        String arr[] = new String[stringtokenizer.countTokens()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = stringtokenizer.nextToken();
        }
        return arr;
    }

    public HitsQueue setStorage(IAsyncStorage storage) {
        mStorage = storage;
        return this;
    }

    /**
     * Adds hit to head of queue
     *
     * @param expireTime timestamp when data will expire
     * @param hitData    data to add
     */
    public void addHit(long expireTime, String hitData) {
        mCommonHits.addFirst(new Entry<>(expireTime, hitData));
    }

    /**
     * Most valuable hits. Guaranteed that they will be sent in first place
     *
     * @param hitData data to add
     */
    public void addImportantHit(String hitData) {
        mImportantHits.addFirst(hitData);
    }

    public int size() {
        return mCommonHits.size() + mImportantHits.size();
    }

    /**
     * Gets and removes hits' data from queue. Polls important hits first.
     *
     * @param dataSize max size for data to get
     * @return hits data
     */
    public List<String> pollHits(int dataSize) {
        int unloadedData = 0;
        List<String> result = new ArrayList<>();
        // first takes important hits
        if (!mImportantHits.isEmpty()) {
            for (Iterator<String> iter = mImportantHits.iterator(); iter.hasNext(); ) {
                if (unloadedData < dataSize) {
                    result.add(iter.next());
                    unloadedData++;
                    iter.remove();
                } else {
                    return result;
                }
            }
        }
        // then other hits
        if (!mCommonHits.isEmpty()) {
            long currentTimestamp = System.currentTimeMillis();
            for (Iterator<Entry<Long, String>> iter = mCommonHits.iterator(); iter.hasNext(); ) {
                Entry<Long, String> entry = iter.next();
                if (currentTimestamp < entry.time) {
                    if (unloadedData < dataSize) {
                        result.add(entry.data);
                        unloadedData++;
                        iter.remove();
                    } else {
                        return result;
                    }
                } else {
                    mCommonHits.clear();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Restores data from given storage
     */
    public boolean restoreHitsQueue(final IHitsRestoreListener listener) {
        if (mStorage != null) {
            mStorage.readData(new IAsyncStorage.IStorageReadListener() {
                @Override
                public void onDataObtained(String key, List<String> data) {
                    switch (key) {
                        case IMPORTANT_HITS_KEY:
                            mImportantHits.addAll(data);
                            break;
                        case COMMON_HITS_KEY:
                            Entry<Long, String> entry;
                            for (String item : data) {
                                entry = parseEntry(item);
                                if (entry != null) {
                                    mCommonHits.add(entry);
                                }
                            }
                            break;
                    }
                }

                @Override
                public void onFinished() {
                    listener.onHitsRestored();
                }
            }, IMPORTANT_HITS_KEY, COMMON_HITS_KEY);
            return true;
        }
        return false;
    }

    /**
     * Writes data to given storage
     */
    public boolean storeHitsQueue() {
        if (mStorage != null) {
            mStorage.writeData(IMPORTANT_HITS_KEY, mImportantHits);
            mImportantHits.clear();
            long currentTimeStamp = System.currentTimeMillis();
            List<String> writableData = new ArrayList<>();
            for (Entry<Long, String> entry : mCommonHits) {
                if (entry.time > currentTimeStamp) {
                    writableData.add(entry.toString());
                }
            }
            mStorage.writeData(COMMON_HITS_KEY, writableData);
            mCommonHits.clear();
            return true;
        }
        return false;
    }

    private Entry<Long, String> parseEntry(String str) {
        String[] arr = splitString(str, DELIMITER);
        if (arr.length == 2) {
            return new Entry<>(Long.parseLong(arr[0]), arr[1]);
        }
        return null;
    }

    public static interface IHitsRestoreListener {
        void onHitsRestored();
    }

    private class Entry<T, D> {
        T time;
        D data;

        public Entry(T time, D data) {
            this.time = time;
            this.data = data;
        }

        @Override
        public String toString() {
            return time.toString() + DELIMITER + data.toString();
        }
    }
}