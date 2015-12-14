package com.topface.topface.statistics;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.utils.CacheProfile;

import java.util.HashMap;

/**
 * Statistics for take-photo-screen
 * Created by mbautin on 10.12.15.
 */
public class TakePhotoStatistics {
    private static final String MOBILE_TF_TAKE_PHOTO = "mobile_tf_take_photo";
    public static final String PLC_DATING_LIKE = "dating_like";
    public static final String PLC_DATING_SEND = "dating_send";
    public static final String PLC_DATING_CHAT = "dating_cat";
    public static final String PLC_OWN_PROFILE_ON_RESUME = "own_profile_on_resume";
    public static final String PLC_OWN_PROFILE_AVATAR_CLICK = "own_profile_on_avatar_click";
    public static final String PLC_ADD_TO_LEADER = "add_to_leader";
    public static final String PLC_AFTER_REGISTRATION_ACTION = "after_registration_action";

    private static final String ACTION_CAMERA = "camera";
    private static final String ACTION_GALLERY = "gallery";
    private static final String ACTION_CANCEL = "cancel";

    private static final String SLICE_PLC = "plc";
    private static final String SLICE_VAL = "val";

    private static Slices getOriginalSlices() {
        Slices slices = new Slices();
        HashMap<String, Object> serverSlices = CacheProfile.getOptions().statisticsSlices;
        Debug.log("from max ", "pognali! ");
        for(String key: serverSlices.keySet()) {
            slices.put(key, JsonUtils.toJson(serverSlices.get(key)));
        }
        Debug.log("from max ", slices.toString());
        return slices;
    }

    public static void sendCameraAction(String plc) {
        Slices slices = getOriginalSlices();
        slices.put(SLICE_PLC, plc);
        slices.put(SLICE_VAL, ACTION_CAMERA);
        StatisticsTracker.getInstance().sendEvent(MOBILE_TF_TAKE_PHOTO,
                1,
                slices);
    }
    public static void sendGalleryAction(String plc) {
        Slices slices = getOriginalSlices();
        slices.put(SLICE_PLC, plc);
        slices.put(SLICE_VAL, ACTION_GALLERY);
        StatisticsTracker.getInstance().sendEvent(MOBILE_TF_TAKE_PHOTO,
                1,
                slices);
    }
    public static void sendCancelAction(String plc) {
        Slices slices = getOriginalSlices();
        slices.put(SLICE_PLC, plc);
        slices.put(SLICE_VAL, ACTION_CANCEL);
        StatisticsTracker.getInstance().sendEvent(MOBILE_TF_TAKE_PHOTO,
                1,
                slices);
    }
}
