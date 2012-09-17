package com.topface.topface.requests;

import android.test.InstrumentationTestCase;
import com.topface.topface.data.Visitor;
import com.topface.topface.data.Visitors;

import java.util.concurrent.CountDownLatch;

public class VisitorsRequestTest extends InstrumentationTestCase {

    public void testVisitorsRequestExec() throws Throwable {
        final CountDownLatch signal = new CountDownLatch(1);

        //Запускаем в UI потоке, для чистоты теста, т.к. мы выполняем запросы
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendVisitorsRequest(signal);
            }
        });

        signal.await();
    }

    private void sendVisitorsRequest(final CountDownLatch signal) {
        new VisitorsRequest(VisitorsRequest.DEFAULT_LIMIT, 0, true, getInstrumentation().getContext())
                .callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) throws NullPointerException {
                        Visitors visitors = Visitors.parse(response);
                        assertNotNull("Visitors result is null", visitors.visitors);
                        assertTrue("Visitors result is empty", visitors.visitors.size() > 0);
                        for (Visitor user : visitors.visitors) {
                            assertNotNull("Visitor is null", user);
                            assertTrue("Visitor id is incorrect", user.id > 0);
                            assertTrue("Visitor age is incorrect", user.age > 0);
                            assertTrue("Visitor created date is incorrect", user.time > 0);
                            assertNotNull("Visitor has't city", user.city);
                            assertTrue("Visitor city id is incorrect", user.city.id > 0);
                            assertTrue("Visitor has't big photo", user.avatar_big != null);
                            assertTrue("Visitor getSuitableLink error", user.avatar_small != null);
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
