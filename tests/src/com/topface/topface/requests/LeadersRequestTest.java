package com.topface.topface.requests;

import android.test.InstrumentationTestCase;
import com.topface.topface.Data;
import com.topface.topface.data.Leaders;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LeadersRequest;

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
                            assertNotNull("Leader has't city", user.city);
                            assertTrue("Leader city id is incorrect", user.city.id > 0);
                            assertNotNull("Leader photo is null", user.photo);
                            assertNotNull("Leader photos links is null", user.photo.links);
                            assertTrue("Leader has't photo", user.photo.links.size() > 0);
                            assertTrue("Leader has't original photo", user.photo.links.containsKey("original"));
                            assertTrue("Leader has't name", user.photo.links.size() > 0);
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
