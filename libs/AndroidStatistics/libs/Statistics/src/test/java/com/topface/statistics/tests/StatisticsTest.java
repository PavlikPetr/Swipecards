package com.topface.statistics.tests;

import com.topface.statistics.Hit;
import com.topface.statistics.IAsyncStorage;
import com.topface.statistics.IDataDispatcher;
import com.topface.statistics.IHitDataBuilder;
import com.topface.statistics.ILogger;
import com.topface.statistics.Statistics;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kirussell on 21.04.2014.
 *
 */
public class StatisticsTest extends TestCase {

    private static final int DISPATCH_DELAY = 1000;

    private IHitDataBuilder builder = new DummyHitDataBuilder();
    private static final Map<String, String> testSlices = new HashMap<>();
    private static final Hit testHit = new Hit("data", 1, testSlices);

    static {
        testSlices.put("slice", "unique");
    }

    public void testStatisticsSendHit() throws InterruptedException {
        TestDispatcher dispatcher = new TestDispatcher();
        Statistics statistics = new Statistics(dispatcher, null)
                .setMaxDispatchExpireDelay(DISPATCH_DELAY)
                .setDataBuilder(builder);
        // one hit sended
        statistics.setMaxHitsDispatch(1);
        statistics.sendHit(testHit);
        assertEquals(dispatcher.lastDispatchedData.size(), 1);
        assertEquals(dispatcher.lastDispatchedData.get(0), builder.build(testHit));
        dispatcher.clear();
        // check: there is no send before time delay
        statistics.setMaxHitsDispatch(2);
        statistics.sendHit(testHit);
        assertEquals(dispatcher.lastDispatchedData.size(), 0);
        Thread.sleep(DISPATCH_DELAY / 2);
        statistics.sendHit(testHit);
        assertEquals(dispatcher.lastDispatchedData.size(), 1);
    }

    public void testStatisticsOnStart() {
        TestDispatcher dispatcher = new TestDispatcher();
        TestStorage storage = new TestStorage();
        List<String> testData = new ArrayList<>();
        testData.add(builder.build(testHit));
        storage.writeData(null, testData);
        Statistics statistics = new Statistics(dispatcher, storage).setDataBuilder(builder);
        statistics.onStart();
        assertEquals(dispatcher.lastDispatchedData.size(), 1);
        assertEquals(dispatcher.lastDispatchedData.get(0), builder.build(testHit));
    }

    public void testStatisticsOnStop() {
        TestDispatcher dispatcher = new TestDispatcher();
        TestStorage storage = new TestStorage();
        Statistics statistics = new Statistics(dispatcher, storage).setDataBuilder(builder);
        // fist hit will be sent cause of timer limitation
        statistics.sendHit(testHit);
        dispatcher.clear();
        // others will be in queue
        String injectedData = "testDataInjected";
        statistics.sendHit(testHit.addSlice("marker", injectedData));
        statistics.sendHit(testHit);
        statistics.setMaxHitsDispatch(1);
        statistics.onStop();
        assertEquals(dispatcher.lastDispatchedData.size(), 1);
        assertEquals(dispatcher.lastDispatchedData.get(0), builder.build(testHit));
        assertEquals(storage.savedData.size(), 1);
        assertEquals(storage.savedData.get(0).contains(injectedData), true);
    }

    private class TestDispatcher implements IDataDispatcher {
        List<String> lastDispatchedData = new ArrayList<>();

        @Override
        public void dispatchData(List<String> data) {
            lastDispatchedData = data;
        }

        @Override
        public IDataDispatcher setDataBuilder(IHitDataBuilder builder) {
            return this;
        }

        @Override
        public IDataDispatcher setLogger(ILogger logger) {
            return this;
        }

        public void clear() {
            lastDispatchedData.clear();
        }
    }

    private class TestStorage implements IAsyncStorage {
        List<String> savedData = new ArrayList<>();

        @Override
        public void writeData(String key, List<String> data) {
            savedData.addAll(data);
        }

        @Override
        public void readData(IStorageReadListener listener, String... keys) {
            List<String> result = new ArrayList<>();
            result.addAll(savedData);
            savedData.clear();
            listener.onDataObtained(null, result);
            listener.onFinished();
        }
    }
}
