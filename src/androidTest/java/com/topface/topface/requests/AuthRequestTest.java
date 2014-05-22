package com.topface.topface.requests;

import android.content.Context;

import com.topface.framework.utils.Debug;
import com.topface.topface.Ssid;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.social.AuthToken;

public class AuthRequestTest extends AbstractThreadTest {

    public void testAuthRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                Context context = getInstrumentation().getTargetContext();
                AuthToken token = AuthToken.getInstance();
                Debug.log("Token: " + token.getTokenKey());
                AuthRequest authRequest = new AuthRequest(token.getTokenInfo(), context);
                authRequest.callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        Auth auth = new Auth(response);
                        assertNotNull("Ssid is null", auth.ssid);
                        Ssid.save(auth.ssid);
                        stopTest("testAuthRequest");
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        assertTrue("Auth error: " + codeError, false);
                        stopTest("testAuthRequest");
                    }
                }).exec();
            }
        }, "testAuthRequest");
    }
}
