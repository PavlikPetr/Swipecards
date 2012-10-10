package com.topface.topface.requests;

import android.test.InstrumentationTestCase;
import com.topface.topface.data.Leaders;
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
                        Leaders leaders = Leaders.parse(response);
                        assertNotNull("Leaders result is null", leaders.leaders);
                        assertTrue("Leaders result is empty", leaders.leaders.size() > 0);
                        for (Leaders.LeaderUser user : leaders.leaders) {
                            assertNotNull("Leader user is null", user);
                            assertTrue("Leader id is incorrect", user.user_id > 0);
                            assertNotNull("Leader has't city", user.city);
                            assertTrue("Leader city id is incorrect", user.city.id > 0);
                            assertNotNull("Leader photo is null", user.photo);
                            assertTrue("Leader has't original photo", user.photo.getSuitableLink(Photo.SIZE_ORIGINAL) != null);
                            assertTrue("Leader getSuitableLink error", user.photo.getSuitableLink(Photo.SIZE_128) != null);
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
