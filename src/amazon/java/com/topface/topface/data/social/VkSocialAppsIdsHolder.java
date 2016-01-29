package com.topface.topface.data.social;

import java.util.ArrayList;

/**
 * Created by kirussell on 31/07/15.
 */
class VkSocialAppsIdsHolder {

    /**
     * {String} VK_ID - идентификатор приложения в vk
     */
    static final int VK_ID = 2664589;
    static final ArrayList<String> ALLOWED_VK_IDS = new ArrayList<>(1);

    static {
        ALLOWED_VK_IDS.add("2664589");
    }

    public static int checkAllowedVkId(String serverId) {
        int servIdValue = 0;
        try {
            servIdValue = Integer.parseInt(serverId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return servIdValue != 0 && VkSocialAppsIdsHolder.ALLOWED_VK_IDS.contains(serverId) ? servIdValue : VK_ID;
    }
}
