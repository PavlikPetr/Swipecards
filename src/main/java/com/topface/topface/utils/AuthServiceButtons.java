package com.topface.topface.utils;

import com.topface.topface.R;
import com.topface.topface.ui.auth.AuthButtonsSettings;

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
        return OTHER_AUTH_BUTTONS.get(buttonId);
    }

    private static boolean isEnabled(@NotNull SocServicesAuthButtons buttonId) {
        return AuthButtonsSettings.AUTH_BUTTONS_SETTINGS.get(buttonId);
    }

    public int getSmallButtonsIconRes() {
        return mIconButtonRes;
    }

    private interface AuthButtonMainScreenEnable {
        boolean isEnable();
    }

    public static HashMap<SocServicesAuthButtons, AuthServiceButtons> getOtherButtonsList() {
        return OTHER_AUTH_BUTTONS;
    }

    private static final HashMap<SocServicesAuthButtons, AuthServiceButtons> OTHER_AUTH_BUTTONS;

    static {
        OTHER_AUTH_BUTTONS = new HashMap<>();
        OTHER_AUTH_BUTTONS.put(SocServicesAuthButtons.VK_BUTTON, new AuthServiceButtons(R.drawable.ic_vk, new AuthButtonMainScreenEnable() {
            @Override
            public boolean isEnable() {
                return AuthButtonsSettings.isVkButtonMainScreenLoginEnable();
            }
        }));
        OTHER_AUTH_BUTTONS.put(SocServicesAuthButtons.OK_BUTTON, new AuthServiceButtons(R.drawable.ic_ok, new AuthButtonMainScreenEnable() {
            @Override
            public boolean isEnable() {
                return AuthButtonsSettings.isOkButtonMainScreenLoginEnable();
            }
        }));
        OTHER_AUTH_BUTTONS.put(SocServicesAuthButtons.FB_BUTTON, new AuthServiceButtons(R.drawable.ic_fb, new AuthButtonMainScreenEnable() {
            @Override
            public boolean isEnable() {
                return AuthButtonsSettings.isFbButtonMainScreenLoginEnable();
            }
        }));
    }

    public enum SocServicesAuthButtons {
        VK_BUTTON,
        OK_BUTTON,
        FB_BUTTON,
        TF_BUTTON;

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

        public boolean isEnabled() {
            return AuthServiceButtons.isEnabled(this);
        }
    }
}
