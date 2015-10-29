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
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

/**
 * Class that starts Vkontakte authorization
 */
public class VkAuthorizer extends Authorizer {

    private static final String[] VK_SCOPE = new String[]{
            VKScope.NOTIFY,
            VKScope.PHOTOS,
            VKScope.FRIENDS,
            VKScope.OFFLINE,
            VKScope.GROUPS
    };

    public VkAuthorizer() {
        super();
    }

    public static int getVkId() {
        return App.getAppConfig().getStageChecked()
                ? Static.STAGE_AUTH_VK_ID
                : App.getAppSocialAppsIds().vkId;
    }

    @Override
    public void authorize(Activity activity) {
        VKSdk.login(activity, VK_SCOPE);
    }

    @Override
    public void logout() {
        VKSdk.customInitialize(App.getContext(), VkAuthorizer.getVkId(), null);
        VKSdk.logout();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKSdk.customInitialize(App.getContext(), VkAuthorizer.getVkId(), null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        {
            if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    Debug.log("VkAuthorizer: receive new token");
                    String tokenKey = res.accessToken;
                    String userId = res.userId;
                    int expiresIn = res.expiresIn;
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
                public void onError(VKError error) {
                    Debug.log("VkAuthorizer: captcha error");
                }
            })) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
