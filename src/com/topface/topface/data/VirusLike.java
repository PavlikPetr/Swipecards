package com.topface.topface.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.facebook.topface.Facebook;
import com.topface.topface.App;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.utils.CacheProfile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class VirusLike extends AbstractData {
    public static final String FIELD_SOCIAL_IDS = "social_ids";
    public static final String FIELD_REQUEST_TEXT = "text";
    public static final String FIELD_LIKES = "likes";
    private ArrayList<Long> mSocialIdArray = null;
    private String mRequestText;

    public VirusLike(ApiResponse response) {
        parseResponse(response);
    }

    private void parseResponse(ApiResponse response) {
        if (response != null && response.jsonResult != null) {
            mSocialIdArray = parseIdsArray(response.jsonResult);
            mRequestText = response.jsonResult.optString(FIELD_REQUEST_TEXT);
            //При парсинге устанавливаем новое количество лайков
            CacheProfile.likes = response.jsonResult.optInt(FIELD_LIKES, CacheProfile.likes);
            //И обновляем UI
            LocalBroadcastManager.getInstance(App.getContext())
                    .sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));

        } else {
            mSocialIdArray = new ArrayList<Long>();
        }
    }

    public VirusLike(ArrayList<Long> ids) {
        mSocialIdArray = ids;
    }

    private ArrayList<Long> parseIdsArray(JSONObject response) {

        ArrayList<Long> socialIdArray = new ArrayList<Long>();

        JSONArray json = response.optJSONArray(FIELD_SOCIAL_IDS);
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

    public String getFacebookRequestText() {
        return TextUtils.isEmpty(mRequestText) ? "" : mRequestText;
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
            params.putString("message", getFacebookRequestText());
            Data.facebook.dialog(context, "apprequests", params, listener);
        }
    }
}
