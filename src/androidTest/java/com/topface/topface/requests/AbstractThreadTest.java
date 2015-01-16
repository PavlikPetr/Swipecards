package com.topface.topface.requests;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.topface.framework.utils.Debug;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

abstract public class AbstractThreadTest extends InstrumentationTestCase {
    private HashMap<String, CountDownLatch> mSignal = new HashMap<>();

    protected void runAsyncTest(Runnable test, String testName) throws Throwable {
        mSignal.put(testName, new CountDownLatch(1));
        Debug.debug("Test", "running " + testName);
        //Запускаем в UI потоке, для чистоты теста, т.к. мы выполняем запросы
        runTestOnUiThread(test);

        mSignal.get(testName).await();
        mSignal.remove(testName);
        Debug.debug("Test", "ends " + testName);
    }

    protected void stopTest(String testName) {
        if (mSignal.containsKey(testName)) {
            mSignal.get(testName).countDown();
        }
    }

    protected Context getContext() {
        return getInstrumentation().getTargetContext();
    }
}
