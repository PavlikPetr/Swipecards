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
        AUTH_BUTTONS_SETTINGS.put(AuthServiceButtons.SocServicesAuthButtons.VK_BUTTON, false);
        AUTH_BUTTONS_SETTINGS.put(AuthServiceButtons.SocServicesAuthButtons.OK_BUTTON, true);
        AUTH_BUTTONS_SETTINGS.put(AuthServiceButtons.SocServicesAuthButtons.FB_BUTTON, false);
        AUTH_BUTTONS_SETTINGS.put(AuthServiceButtons.SocServicesAuthButtons.TF_BUTTON, false);
    }

    public static boolean isVkButtonMainScreenLoginEnable() {
        return false;
    }

    public static boolean isOkButtonMainScreenLoginEnable() {
        return true;
    }

    public static boolean isFbButtonMainScreenLoginEnable() {
        return false;
    }
}
