package com.topface.topface.requests.v6;

import android.content.Context;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Register;
import com.topface.topface.requests.*;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;

public class RegisterRequestTest extends AbstractThreadTest {

    public void testRegisterRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
            Context context = getInstrumentation().getContext();
            RegisterRequest request = new RegisterRequest(context,
                    "iovorobievw@gmail.com",
                    "asdasd",
                    "ilya",
                    123456,
                    1);
            request.callback(new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    Register reg = new Register(response);
                    assertNotSame(0,reg.getUserId());
                    stopTest("testRegisterRequest");
                }

                @Override
                public void fail(int codeError, ApiResponse response) {

                    assertTrue("Register error: " + codeError, false);
                    stopTest("testRegisterRequest");
                }
            }).exec();
            }
        }, "testRegisterRequest");
    }
}
