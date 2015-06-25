package com.topface.topface.unittests.appstate;

import android.os.Handler;

import com.topface.topface.state.AppState;
import com.topface.topface.state.CacheDataInterface;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CountDownLatch;

import rx.functions.Action1;

import static org.junit.Assert.assertTrue;

/**
 * Created by ppetr on 15.06.15.
 * testing AppState
 */
@RunWith(RobolectricTestRunner.class)
public class AppStateTest {

    private static final int THREAD_COUNT = 3;
    private static final int REPEAT_COUNT = 50;
    int counter;
    String receivedData;

    @Test
    public void testNullData() {
        AppState state = new AppState(null);
        counter = 0;
        state.getObservable(Object.class).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                counter++;
            }
        });
        assertTrue(counter == 0);
    }

    @Test
    public void testConcurrent() {
        final AppState state = new AppState(null);
        counter = 0;
        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        state.getObservable(String.class).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                counter++;

            }
        });

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long threadId = Thread.currentThread().getId();
                    for (int j = 0; j < REPEAT_COUNT; j++) {
                        state.setData("Test thread " + threadId + " iter " + j);
                    }
                    latch.countDown();
                }
            }).start();
        }
        try {
            latch.await();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    assertTrue(counter == THREAD_COUNT);
                }
            }, 5000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNotNullCacheData() {
        AppState state = new AppState(new CacheDataInterface() {
            @Override
            public <T> void saveDataToCache(T data) {

            }

            @Override
            public <T> T getDataFromCache(Class<T> classType) {
                if (String.class.equals(classType)) {
                    return (T) "Data from cache";
                }
                return null;
            }
        });
        counter = 0;
        state.getObservable(String.class).subscribe(new Action1<String>() {
            @Override
            public void call(String o) {
                counter++;
            }
        });
        assertTrue(counter == 1);
    }

    /**
     * testing BehaviorSubject
     * we must receive only one last data, when subscribe
     */
    @Test
    public void testBehaviorSubject() {
        String lastValue = "last_value";
        AppState state = new AppState(new CacheDataInterface() {
            @Override
            public <T> void saveDataToCache(T data) {

            }

            @Override
            public <T> T getDataFromCache(Class<T> classType) {
                if (String.class.equals(classType)) {
                    return (T) "Data from cache";
                }
                return null;
            }
        });
        state.setData("first Data");
        state.setData("second Data");
        state.setData(lastValue);
        counter = 0;
        state.getObservable(String.class).subscribe(new Action1<String>() {
            @Override
            public void call(String o) {
                counter++;
                receivedData = o;
            }
        });
        assertTrue(counter == 1 && receivedData.equals(lastValue));
    }

    /**
     * subscriber couldn't return NULL, checked it
     */
    @Test
    public void testFullyNullData() {
        counter = 0;
        final AppState state = new AppState(null);
        state.setData(null);
        state.getObservable(Object.class).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                counter++;
            }
        });
        state.setData(null);
        assertTrue(counter == 0);
    }
}
