package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.utils.config.SessionConfig;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;

/**
 * Class that starts Vkontakte authorization
 */
public class VkAuthorizer extends Authorizer {

    private String[] VK_SCOPE = new String[]{"notify", "photos", "offline"};

    private VKSdkListener vkSdkListener = new VKSdkListener() {

        @Override
        public void onCaptchaError(VKError vkError) {
            Debug.log("VkAuthorizer: captcha error");
        }

        @Override
        public void onTokenExpired(VKAccessToken vkAccessToken) {
            Debug.log("VkAuthorizer: token expired");
        }

        @Override
        public void onAccessDenied(VKError vkError) {
            Debug.log("VkAuthorizer: access denied");
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            Debug.log("VkAuthorizer: receive new token");
            super.onReceiveNewToken(newToken);
            String tokenKey = newToken.accessToken;
            String userId = newToken.userId;
            int expiresIn = newToken.expiresIn;
            AuthToken authToken = AuthToken.getInstance();
            authToken.saveToken(AuthToken.SN_VKONTAKTE, userId, tokenKey, String.valueOf(expiresIn));
            AuthToken.getVkName(userId, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    SessionConfig sessionConfig = App.getSessionConfig();
                    sessionConfig.setSocialAccountName((String) msg.obj);
                    sessionConfig.saveConfig();
                    return true;
                }
            }));
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Debug.log("VkAuthorizer: accept user token");
            super.onAcceptUserToken(token);
        }

        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            Debug.log("VkAuthorizer: renew access token");
            super.onRenewAccessToken(token);
        }
    };

    public VkAuthorizer(Activity activity) {
        super(activity);
    }

    @Override
    public void authorize() {
        VKSdk.authorize(VK_SCOPE);
    }

    @Override
    public void logout() {
        VKUIHelper.onCreate(getActivity());
        VKSdk.initialize(vkSdkListener, Static.AUTH_VK_ID);
        VKSdk.logout();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKUIHelper.onCreate(getActivity());
        VKSdk.initialize(vkSdkListener, Static.AUTH_VK_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        VKUIHelper.onResume(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(getActivity());
    }
}
