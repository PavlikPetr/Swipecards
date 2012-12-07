package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;

public abstract class FeedTest<T extends FeedItem> extends AbstractThreadTest {
    private static final int LIMIT = 10;
    public Exception mAssertError;


    private void sendFeedRequest(final String testName) {
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
                stopTest(testName);
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                assertTrue("Request exec fail: " + codeError, false);
                stopTest(testName);
            }
        }).exec();
    }

    protected abstract void runAdditionalItemAsserts(T item);

    protected abstract FeedListData<T> getFeedList(ApiResponse response);

    abstract protected FeedRequest.FeedService getFeedType();

    protected void runFeedTest(final String testName) {
        try {
            runAsyncTest(new Runnable() {
                @Override
                public void run() {
                    sendFeedRequest(testName);
                }
            }, testName);
        } catch (Throwable throwable) {
            assertTrue("FeedTest fail", false);
        }
    }

    protected Context getContext() {
        return getInstrumentation().getContext();
    }
}
