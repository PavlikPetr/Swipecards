package com.topface.topface.requests;

import android.test.InstrumentationTestCase;
import com.topface.topface.data.FeedUserListData;
import com.topface.topface.data.Leader;
import com.topface.topface.data.Photo;

import java.util.concurrent.CountDownLatch;

/**
 * Тест
 */
public class LeadersRequestTest extends InstrumentationTestCase {

    public void testLeadersRequestExec() throws Throwable {
        final CountDownLatch signal = new CountDownLatch(1);

        //Запускаем в UI потоке, для чистоты теста, т.к. мы выполняем запросы
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendLeadersRequest(signal);
            }
        });

        signal.await();
    }

    private void sendLeadersRequest(final CountDownLatch signal) {
        new LeadersRequest(getInstrumentation().getContext())
                .callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) throws NullPointerException {
                        FeedUserListData<Leader> leaders = new FeedUserListData<Leader>(response.jsonResult, Leader.class);
                        assertNotNull("Leaders result is null", leaders);
                        assertTrue("Leaders result is empty", leaders.size() > 0);
                        for (Leader item : leaders) {
                            assertNotNull("Leader item is null", item);
                            assertTrue("Leader id is incorrect", item.id > 0);
                            assertNotNull("Leader has't city", item.city);
                            assertTrue("Leader city id is incorrect", item.city.id > 0);
                            assertNotNull("Leader photo is null", item.photo);
                            assertTrue("Leader has't original photo", item.photo.getSuitableLink(Photo.SIZE_ORIGINAL) != null);
                            assertTrue("Leader getSuitableLink error", item.photo.getSuitableLink(Photo.SIZE_128) != null);
                        }
                        signal.countDown();
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) throws NullPointerException {
                        assertTrue("Request exec fail: " + codeError, false);
                        signal.countDown();
                    }
                })
                .exec();
    }
}
