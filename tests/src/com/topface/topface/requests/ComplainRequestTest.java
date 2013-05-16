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
                    public void success(ApiResponse response) {
                        assertTrue(response.jsonResult.optBoolean("completed"));
                        stopTest("testComplainRequest");
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        assertFalse(response.jsonResult.optBoolean("completed",false));
                        stopTest("testComplainRequest");
                    }
                }).exec();
            }
        }, "testComplainRequest");
    }
}
