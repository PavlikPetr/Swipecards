package com.topface.topface.utils;

import com.topface.topface.R;
import com.topface.topface.utils.social.FbAuthorizer;
import com.topface.topface.utils.social.OkAuthorizer;
import com.topface.topface.utils.social.VkAuthorizer;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Created by Петр on 19.01.2016.
 * Settings for all social service authorization buttons (VK,FB,OK)
 */
public class AuthServiceButtons {
    private int mIconButtonRes;
    public AuthButtonMainScreenEnable isMainScreenLoginEnable;

    public AuthServiceButtons(int iconRes, AuthButtonMainScreenEnable isEnable) {
        mIconButtonRes = iconRes;
        isMainScreenLoginEnable = isEnable;
    }

    public static AuthServiceButtons getAuthButtonSettingById(@NotNull SocServicesAuthButtons buttonId) {
        return AUTH_BUTTONS.get(buttonId);
    }

    public int getSmallButtonsIconRes() {
        return mIconButtonRes;
    }

    private interface AuthButtonMainScreenEnable {
        boolean isEnable();
    }

    private static final HashMap<SocServicesAuthButtons, AuthServiceButtons> AUTH_BUTTONS;

    static {
        AUTH_BUTTONS = new HashMap<>();
        AUTH_BUTTONS.put(SocServicesAuthButtons.VK_BUTTON, new AuthServiceButtons(R.drawable.ic_vk, new AuthButtonMainScreenEnable() {
            @Override
            public boolean isEnable() {
                return VkAuthorizer.isMainScreenLoginEnable();
            }
        }));
        AUTH_BUTTONS.put(SocServicesAuthButtons.OK_BUTTON, new AuthServiceButtons(R.drawable.ic_ok, new AuthButtonMainScreenEnable() {
            @Override
            public boolean isEnable() {
                return OkAuthorizer.isMainScreenLoginEnable();
            }
        }));
        AUTH_BUTTONS.put(SocServicesAuthButtons.FB_BUTTON, new AuthServiceButtons(R.drawable.ic_fb, new AuthButtonMainScreenEnable() {
            @Override
            public boolean isEnable() {
                return FbAuthorizer.isMainScreenLoginEnable();
            }
        }));
    }

    public enum SocServicesAuthButtons {
        VK_BUTTON,
        OK_BUTTON,
        FB_BUTTON;

        private AuthServiceButtons getAuthButtonSettings() {
            return AuthServiceButtons.getAuthButtonSettingById(this);
        }

        public int getSmallButtonsIconRes() {
            AuthServiceButtons settings = getAuthButtonSettings();
            return settings != null ? settings.getSmallButtonsIconRes() : 0;
        }

        public boolean isMainScreenLoginEnable() {
            AuthServiceButtons settings = getAuthButtonSettings();
            return settings == null || settings.isMainScreenLoginEnable.isEnable();
        }
    }
}
