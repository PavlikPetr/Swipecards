package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;

public class AuthRequestTest extends AbstractThreadTest {

    /**
     * Тестируем только API v1
     */
    private static final int API_VERSION = 3;

    public void testAuthRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                Context context = getInstrumentation().getContext();
                AuthToken token = AuthToken.getInstance();
                Debug.log(token.toString());
                AuthRequest authRequest = new AuthRequest(token, context);
                authRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        Auth auth = new Auth(response);
                        assertNotNull("Ssid is null", auth.ssid);
                        assertEquals("Wrong API version", API_VERSION, Static.API_VERSION);
                        Ssid.save(auth.ssid);
                        stopTest("testAuthRequest");
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        assertTrue("Auth error: " + codeError, false);
                        stopTest("testAuthRequest");
                    }
                }).exec();
            }
        }, "testAuthRequest");
    }
}
