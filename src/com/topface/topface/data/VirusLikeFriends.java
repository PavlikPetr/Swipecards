package com.topface.topface.data;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.facebook.topface.Facebook;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class VirusLikeFriends extends AbstractData {
    public static final String SOCIAL_IDS = "social_ids";
    private ArrayList<Long> mSocialIdArray = null;

    public VirusLikeFriends(ApiResponse response) {
        if (response != null && response.jsonResult != null) {
            mSocialIdArray = parseIdsArray(response.jsonResult);
        } else {
            mSocialIdArray = new ArrayList<Long>();
        }
    }

    public VirusLikeFriends(ArrayList<Long> ids) {
        mSocialIdArray = ids;
    }

    private ArrayList<Long> parseIdsArray(JSONObject response) {

        ArrayList<Long> socialIdArray = new ArrayList<Long>();

        JSONArray json = response.optJSONArray(SOCIAL_IDS);
        if (json != null && json.length() > 0) {
            int jsonLength = json.length();
            for (int i = 0; i < jsonLength; i++) {
                Long id = json.optLong(i);
                if (id > 0) {
                    socialIdArray.add(id);
                }
            }
        }

        return socialIdArray;
    }

    public ArrayList<Long> getIds() {
        return mSocialIdArray;
    }

    /**
     * Отправляет приглашение в Topface через Facebook
     *
     * @param context  контекст
     * @param listener листенер диалога приглашений
     */
    public void sendFacebookRequest(Context context, Facebook.DialogListener listener) {
        if (mSocialIdArray.size() > 0) {
            Bundle params = new Bundle();
            params.putString("title", context.getString(R.string.chat_likes_request_title));
            params.putString("to", TextUtils.join(",", mSocialIdArray));
            params.putString("message", context.getString(R.string.chat_likes_request_message));
            Data.facebook.dialog(context, "apprequests", params, listener);
        }
    }
}
