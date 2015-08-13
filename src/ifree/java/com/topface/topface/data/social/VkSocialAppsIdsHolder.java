package com.topface.topface.data.social;

import java.util.ArrayList;

/**
 * Created by kirussell on 31/07/15.
 */
class VkSocialAppsIdsHolder {

    /**
     * {String} VK_ID - идентификатор приложения в vk
     */
    static final String VK_ID = "5015934";

    public static String checkAllowedVkId(String serverId) {
        //ignore server ids for Ifree
        return VK_ID;
    }
}
