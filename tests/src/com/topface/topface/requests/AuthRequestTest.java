package com.topface.topface.requests;

import android.test.InstrumentationTestCase;
import com.topface.topface.Data;
import com.topface.topface.data.Auth;
import com.topface.topface.utils.AuthToken;
import com.topface.topface.utils.Debug;

import java.util.concurrent.CountDownLatch;

public class AuthRequestTest extends InstrumentationTestCase {

    public void testAuthRequest() throws Throwable {
        final CountDownLatch signal = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                AuthToken token = new AuthToken(getInstrumentation().getContext());
                Debug.log(token.toString());
                AuthRequest authRequest = new AuthRequest(getInstrumentation().getContext());
                authRequest.platform = token.getSocialNet();
                authRequest.sid = token.getUserId();
                authRequest.token = token.getTokenKey();
                authRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) throws NullPointerException {
                        Auth auth = Auth.parse(response);
                        assertNotNull("SSID is null", auth.ssid);
                        Data.SSID = auth.ssid;
                        //TODO: Check API version
                        //assertEquals(API_VERSION, auth.api_version);
                        signal.countDown();
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) throws NullPointerException {
                        assertTrue("Auth error: " + codeError, false);
                        signal.countDown();
                    }
                }).exec();
            }
        });

        signal.await();
    }
}
