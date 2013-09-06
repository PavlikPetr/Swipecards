package com.topface.topface.requests;

import android.test.InstrumentationTestCase;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

abstract public class AbstractThreadTest extends InstrumentationTestCase {
    private HashMap<String, CountDownLatch> mSignal = new HashMap<String, CountDownLatch>();

    protected void runAsyncTest(Runnable test, String testName) throws Throwable {
        mSignal.put(testName, new CountDownLatch(1));

        //Запускаем в UI потоке, для чистоты теста, т.к. мы выполняем запросы
        runTestOnUiThread(test);

        mSignal.get(testName).await();
        mSignal.remove(testName);
    }

    protected void stopTest(String testName) {
        if (mSignal.containsKey(testName)) {
            mSignal.get(testName).countDown();
        }
    }
}
