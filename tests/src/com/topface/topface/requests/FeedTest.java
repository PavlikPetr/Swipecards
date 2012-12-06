package com.topface.topface.requests;

import android.test.InstrumentationTestCase;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;

import java.util.concurrent.CountDownLatch;

public abstract class FeedTest<T extends FeedItem> extends InstrumentationTestCase {
    private static final int LIMIT = 10;
    private CountDownLatch mSignal;
    public Exception mAssertError;

    protected void runAsyncTest(Runnable test) throws Throwable {
        mSignal = new CountDownLatch(1);

        //Запускаем в UI потоке, для чистоты теста, т.к. мы выполняем запросы
        runTestOnUiThread(test);

        mSignal.await();
    }

    private void sendFeedRequest() {
        FeedRequest request = new FeedRequest(getFeedType(), getInstrumentation().getContext());
        request.limit = LIMIT;
        request.unread = false;
        request.callback(new ApiHandler() {

            @Override
            public void success(ApiResponse response) throws NullPointerException {
                final FeedListData<T> feedList = getFeedList(response);
                assertNotNull("Feed list is null", feedList);
                assertNotNull("Feed list items is null", feedList.items);
                //В данный момент не используем счетчики, и они соответсвенно не разбираются
                //assertNotNull("Feed counters is null", feedList.counters);
                assertTrue("Feed list is empty", feedList.items.size() > 0);
                for (T item : feedList.items) {
                    assertNotNull("Feed item user is null", item.user);
                    assertTrue("Feed item id is wrong", item.id > 0);
                    //assertTrue("Feed item id created date is wrong", item.created > 0);
                    runAdditionalItemAsserts(item);
                }
                onTestFinish();
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                assertTrue("Request exec fail: " + codeError, false);
                onTestFinish();
            }
        }).exec();
    }

    protected abstract void runAdditionalItemAsserts(T item);

    protected abstract FeedListData<T> getFeedList(ApiResponse response);

    protected void onTestFinish() {
        mSignal.countDown();
    }

    abstract protected FeedRequest.FeedService getFeedType();

    protected void runFeedTest() {
        try {
            runAsyncTest(new Runnable() {
                @Override
                public void run() {
                    sendFeedRequest();
                }
            });
        } catch (Throwable throwable) {
            assertTrue("FeedTest fail", false);
        }
    }
}
