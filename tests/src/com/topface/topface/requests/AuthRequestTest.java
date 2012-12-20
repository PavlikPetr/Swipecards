package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
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
                AuthToken token = new AuthToken(getInstrumentation().getContext());
                Debug.log(token.toString());
                AuthRequest authRequest = new AuthRequest(getInstrumentation().getContext());
                authRequest.platform = token.getSocialNet();
                authRequest.sid = token.getUserId();
                authRequest.token = token.getTokenKey();
                authRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        Auth auth = Auth.parse(response);
                        assertNotNull("SSID is null", auth.ssid);
                        assertEquals("Wrong API version", API_VERSION, Static.API_VERSION);
                        Data.SSID = auth.ssid;
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
