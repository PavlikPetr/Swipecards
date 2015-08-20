package com.topface.topface.data.social;

import java.util.ArrayList;

class VkSocialAppsIdsHolder {

    /**
     * {String} VK_ID - идентификатор приложения в vk
     */
    static final String VK_ID = "5029543";
    static final ArrayList<String> ALLOWED_VK_IDS = new ArrayList<>(1);

    static {
        ALLOWED_VK_IDS.add("5029543");
    }

    public static String checkAllowedVkId(String serverId) {
        return VkSocialAppsIdsHolder.ALLOWED_VK_IDS.contains(serverId) ? serverId : VK_ID;
    }
}
