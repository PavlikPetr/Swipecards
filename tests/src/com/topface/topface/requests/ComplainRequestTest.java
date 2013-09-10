package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.requests.handlers.ApiHandler;


public class ComplainRequestTest extends AbstractThreadTest {

    /**
     * Тестируем только API v1
     */

    public void testComplainRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                Context context = getInstrumentation().getContext();
                ComplainRequest request = new ComplainRequest(context, 43945394, ComplainRequest.ClassNames.PRIVATE_MSG, ComplainRequest.TypesNames.SPAM);
                request.callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        assertTrue(response.getJsonResult().optBoolean("completed"));
                        stopTest("testComplainRequest");
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        assertFalse(response.getJsonResult().optBoolean("completed", false));
                        stopTest("testComplainRequest");
                    }
                }).exec();
            }
        }, "testComplainRequest");
    }
}
