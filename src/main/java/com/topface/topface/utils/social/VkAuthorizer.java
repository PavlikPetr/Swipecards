package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.SessionConfig;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Class that starts Vkontakte authorization
 */
public class VkAuthorizer extends Authorizer {

    public static final int STAGE_AUTH_VK_ID = 4854621;

    private static VKSdk mVkSdk;

    private static final String[] VK_SCOPE = new String[]{
            VKScope.NOTIFY,
            VKScope.PHOTOS,
            VKScope.FRIENDS,
            VKScope.OFFLINE,
            VKScope.GROUPS,
            VKScope.EMAIL
    };

    public VkAuthorizer() {
        super();
    }

    public static int getVkId() {
        return App.getAppConfig().getStageChecked()
                ? STAGE_AUTH_VK_ID
                : App.getAppSocialAppsIds().vkId;
    }

    @Override
    public void authorize(Activity activity) {
        VKSdk.login(activity, VK_SCOPE);
    }

    @Override
    public void logout() {
        VkAuthorizer.initVkSdk();
        VKSdk.logout();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VkAuthorizer.initVkSdk();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        {
            if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    if (res == null) {
                        Debug.log("VkAuthorizer: received token == null");
                        onError(new VKError(VKError.VK_API_ERROR));
                        return;
                    }
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

    public static boolean isMainScreenLoginEnable() {
        return new Locale(App.getLocaleConfig().getApplicationLocale()).getLanguage().equals(Utils.getRussianLocale().getLanguage());
    }

    public static void dropCurrentAppId() {
        Field field = getCurrentAppIdField();
        if (field != null) {
            field.setAccessible(true);
            try {
                field.setInt(mVkSdk, 0);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getCurrentAppId() {
        Field field = getCurrentAppIdField();
        int currentAppId = 0;
        if (field != null) {
            field.setAccessible(true);
            try {
                currentAppId = field.getInt(mVkSdk);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return currentAppId;
    }

    public static Field getCurrentAppIdField() {
        if (mVkSdk == null) {
            return null;
        }
        Field field = null;
        try {
            field = VKSdk.class.getDeclaredField("sCurrentAppId");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return field;
    }


    public static void initVkSdk() {
        int appId = getVkId();
        int currentAppId = getCurrentAppId();
        if (currentAppId != 0 && appId != currentAppId) {
            dropCurrentAppId();
        }
        mVkSdk = VKSdk.customInitialize(App.getContext(), appId, null);
    }
}
