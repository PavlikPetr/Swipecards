package com.topface.topface.data.social;

/**
 * Created by kirussell on 31/07/15.
 */
class VkSocialAppsIdsHolder {

    /**
     * {String} VK_ID - идентификатор приложения в vk
     */
    static final int VK_ID = 5015934;

    public static int checkAllowedVkId(String serverId) {
        //ignore server ids for Ifree
        return VK_ID;
    }
}
