package com.topface.topface.ui.auth;

import com.topface.topface.utils.AuthServiceButtons;
import com.topface.topface.utils.social.FbAuthorizer;
import com.topface.topface.utils.social.OkAuthorizer;
import com.topface.topface.utils.social.VkAuthorizer;

import java.util.HashMap;

/**
 * Created by Петр on 04.04.2016.
 * set enabled value of social buttons
 */
public class AuthButtonsSettings {

        public static final HashMap<AuthServiceButtons.SocServicesAuthButtons, Boolean> AUTH_BUTTONS_SETTINGS;

    static {
        AUTH_BUTTONS_SETTINGS = new HashMap<>();
        AUTH_BUTTONS_SETTINGS.put(AuthServiceButtons.SocServicesAuthButtons.VK_BUTTON, true);
        AUTH_BUTTONS_SETTINGS.put(AuthServiceButtons.SocServicesAuthButtons.OK_BUTTON, true);
        AUTH_BUTTONS_SETTINGS.put(AuthServiceButtons.SocServicesAuthButtons.FB_BUTTON, true);
        AUTH_BUTTONS_SETTINGS.put(AuthServiceButtons.SocServicesAuthButtons.TF_BUTTON, true);
    }

    public static boolean isVkButtonMainScreenLoginEnable() {
        return VkAuthorizer.isMainScreenLoginEnable();
    }

    public static boolean isOkButtonMainScreenLoginEnable() {
        return OkAuthorizer.isMainScreenLoginEnable();
    }

    public static boolean isFbButtonMainScreenLoginEnable() {
        return FbAuthorizer.isMainScreenLoginEnable();
    }
}
