package com.topface.statistics.tests;

import com.topface.statistics.HitsQueue;
import com.topface.statistics.IAsyncStorage;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kirussell on 17.04.2014.
 *
 */
public class HitsQueueTest extends TestCase {

    private static final long MINUTE = 60000;
    private static final int DATA_SIZE = 100;

    public void testQueueDataPoll() {
        assertEquals(DATA_SIZE, getFilledQueue(DATA_SIZE).pollHits(DATA_SIZE).size());
    }

    public void testPollQueueDataLimits() {
        assertEquals(DATA_SIZE, getFilledQueue(DATA_SIZE).pollHits(DATA_SIZE).size());
        assertEquals(DATA_SIZE / 2, getFilledQueue(DATA_SIZE / 2).pollHits(DATA_SIZE).size());
        assertEquals(DATA_SIZE, getFilledQueue(DATA_SIZE * 2).pollHits(DATA_SIZE).size());
        assertEquals(DATA_SIZE, getFilledQueue(DATA_SIZE + 1).pollHits(DATA_SIZE).size());
    }

    public void testPollQueueExpireLimits() {
        assertEquals(0, getFilledQueue(DATA_SIZE, 0).pollHits(DATA_SIZE).size());
    }

    public void testStoreRestoreHits() {
        DummyStorage storage = new DummyStorage();
        final HitsQueue queue = getFilledQueue(
                DATA_SIZE,
                System.currentTimeMillis() + MINUTE,
                storage
        );
        queue.storeHitsQueue();
        assertEquals(DATA_SIZE, storage.testSize());
        assertEquals(queue.size(), 0);
        queue.restoreHitsQueue(new HitsQueue.IHitsRestoreListener() {
            @Override
            public void onHitsRestored() {
                assertEquals(queue.size(), DATA_SIZE);
            }
        });
    }

    // ===================== Helper Methods =====================
    private HitsQueue getFilledQueue(int dataSize) {
        return getFilledQueue(dataSize, System.currentTimeMillis() + MINUTE);
    }

    private HitsQueue getFilledQueue(int dataSize, long expireTime, IAsyncStorage storage) {
        HitsQueue queue = new HitsQueue(storage);
        fillQueue(queue, dataSize, expireTime);
        return queue;
    }

    private HitsQueue getFilledQueue(int dataSize, long expireTime) {
        return getFilledQueue(dataSize, expireTime, null);
    }

    private void fillQueue(HitsQueue queue, int dataSize, long expireTime) {
        for (int i = 0; i < dataSize; i++) {
            queue.addHit(expireTime, "data-" + i);
        }
    }

    private class DummyStorage implements IAsyncStorage {
        private Map<String, List<String>> map = new HashMap<>();

        @Override
        public void writeData(String key, List<String> data) {
            map.put(key, data);
        }

        @Override
        public void readData(IStorageReadListener listener, String... keys) {
            for (String key : keys){
                listener.onDataObtained(key, map.get(key));
            }
        }

        public int testSize() {
            int testSize = 0;
            for (List<String> lst : map.values()) {
                testSize += lst.size();
            }
            return testSize;
        }
    }
}
