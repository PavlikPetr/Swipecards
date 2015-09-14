package com.topface.topface.data.social;

import android.content.pm.PackageManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;

/**
 * Created by kirussell on 31/07/15.
 */
class VkSocialAppsIdsHolder {

    static final String VK_ID = getVkId();

    private static String getVkId()  {
        String res = "5015934";
        try {
            res = String.valueOf(App.getContext().getPackageManager()
                    .getApplicationInfo(App.getContext().getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("vk_id"));
        } catch (PackageManager.NameNotFoundException e) {
            Debug.log(e.toString());
        }
        return res;
    }

    public static String checkAllowedVkId(String serverId) {
        //ignore server ids for Ifree
        return VK_ID;
    }
}
