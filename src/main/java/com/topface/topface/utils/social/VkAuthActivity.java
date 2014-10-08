package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.Static;
import com.topface.topface.utils.http.HttpUtils;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class VkAuthActivity extends Activity {

    // Data
    private VKAccessToken mToken;
    private String[] VK_SCOPE = new String[]{"notify", "photos", "offline"};
    private Intent mResult = new Intent();
    // Constants
    private static final String VK_NAME_URL = "https://api.vk.com/method/getProfiles?uid=%s&access_token=%s";
    public static final int INTENT_VK_AUTH = 101;
    public final static String ACCESS_TOKEN = "access_token";
    public final static String USER_ID = "user_id";
    public final static String EXPIRES_IN = "expires_in";
    public final static String USER_NAME = "user_name";

    private VKSdkListener vkSdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError vkError) {

        }

        @Override
        public void onTokenExpired(VKAccessToken vkAccessToken) {

        }

        @Override
        public void onAccessDenied(VKError vkError) {

        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            super.onReceiveNewToken(newToken);
            mToken = newToken;
            getVkName(mToken.accessToken, mToken.userId, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    mResult.putExtra(USER_NAME, (String) msg.obj);
                    return true;
                }
            }));
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            super.onAcceptUserToken(token);
            mToken = token;
        }

        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            super.onRenewAccessToken(token);
            mToken = token;
        }
    };

    public static void getVkName(final String token, final String user_id, final Handler handler) {
        new BackgroundThread() {
            @Override
            public void execute() {
                String responseRaw = HttpUtils.httpGetRequest(String.format(Locale.ENGLISH, VK_NAME_URL, user_id, token));
                try {
                    String result = "";
                    JSONObject response = new JSONObject(responseRaw);
                    JSONArray responseArr = response.optJSONArray("response");
                    if (responseArr != null) {
                        if (responseArr.length() > 0) {
                            JSONObject profile = responseArr.getJSONObject(0);
                            result = profile.optString("first_name") + " " + profile.optString("last_name");
                        }
                        handler.sendMessage(Message.obtain(null, AuthorizationManager.SUCCESS_GET_NAME, result));
                    } else {
                        handler.sendMessage(Message.obtain(null, AuthorizationManager.FAILURE_GET_NAME, ""));
                    }
                } catch (Exception e) {
                    Debug.error("AuthorizationManager can't get name in vk", e);
                    handler.sendMessage(Message.obtain(null, AuthorizationManager.FAILURE_GET_NAME, ""));
                }
            }
        };
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKUIHelper.onCreate(this);
        Debug.log(this, "+onCreate");
        VKSdk.initialize(vkSdkListener, Static.AUTH_VK_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
        if (mToken == null) {
            VKSdk.authorize(VK_SCOPE);
        } else {
            mResult.putExtra(ACCESS_TOKEN, mToken.accessToken);
            mResult.putExtra(USER_ID, mToken.userId);
            mResult.putExtra(EXPIRES_IN, mToken.expiresIn);
            setResult(Activity.RESULT_OK, mResult);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

}
