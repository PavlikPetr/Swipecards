package com.topface.topface.requests.v6;

import android.content.Context;
import com.topface.topface.data.Register;
import com.topface.topface.requests.*;

public class RegisterRequestTest extends AbstractThreadTest {

    public void testRegisterRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                Context context = getInstrumentation().getContext();
                RegisterRequest request = new RegisterRequest(context,
                        "iovorobiev@mail.ru",
                        "asdasd",
                        "ilya",
                        123456,
                        1);
                request.callback(new DataApiHandler<Register>() {

                    @Override
                    protected void success(Register reg, IApiResponse response) {
                        assertNotSame(0, reg.getUserId());
                        stopTest("testRegisterRequest");
                    }

                    @Override
                    protected Register parseResponse(ApiResponse response) {
                        return new Register(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {

                        assertTrue("Register error: " + codeError, false);
                        stopTest("testRegisterRequest");
                    }
                }).exec();
            }
        }, "testRegisterRequest");
    }
}
