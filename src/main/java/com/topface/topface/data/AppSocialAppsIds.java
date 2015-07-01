package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Application options
 */
public class AppSocialAppsIds extends AbstractData {

    /**
     * {String} fbId - идентификатор приложения в fb
     */
    public String fbId = "879159665497091"; //642883445728173
    private static final ArrayList<String>  ALLOWED_FB_IDS = new ArrayList<>(2);
    static {
        ALLOWED_FB_IDS.add("879159665497091");
        ALLOWED_FB_IDS.add("642883445728173");
    }

    /**
     * {String} okId - идентификатор приложения в ok
     */
    public String okId = "125879808";
    private static final ArrayList<String>  ALLOWED_OK_IDS = new ArrayList<>(2);
    private static final HashMap<String, String> OK_KEYS = new HashMap<>(4);
    static {
        ALLOWED_OK_IDS.add("125879808");
        OK_KEYS.put("secret125879808", "D54CFE092BA6F1FEA2C21BD3");
        OK_KEYS.put("public125879808", "CBAHMBIIABABABABA");
        ALLOWED_OK_IDS.add("192696576");
        OK_KEYS.put("secret192696576", "A9872F0F0DA2EA47EB876D85");
        OK_KEYS.put("public192696576", "CBACFNHMABABABABA");
    }

    /**
     * {String} vkId - идентификатор приложения в vk
     */
    public String vkId = "2664589";
    private static final ArrayList<String>  ALLOWED_VK_IDS = new ArrayList<>(1);
    static {
        ALLOWED_VK_IDS.add("2664589");
    }

    public AppSocialAppsIds(JSONObject data) {
        if (data != null) {
            fillData(data);
        }
    }

    private void fillData(JSONObject item) {
        try {
            String serverId = Utils.optString(item, "fbId");
            fbId = ALLOWED_FB_IDS.contains(serverId) ? serverId : fbId;
            serverId = Utils.optString(item, "okId");
            okId = ALLOWED_OK_IDS.contains(serverId) ? serverId : okId;
            serverId = Utils.optString(item, "vkId");
            vkId = ALLOWED_VK_IDS.contains(serverId) ? serverId : vkId;
            // caching
            App.getAppConfig().saveAppSocialAppsIds(this);
        } catch (Exception e) {
            Debug.error("AppSocialAppsIds.class : Wrong response parsing", e);
        }
    }

    public String getOkSecretKey() {
        return OK_KEYS.get("secret"+okId);
    }

    public String getOkPublicKey() {
        return OK_KEYS.get("public"+okId);
    }
}
