package com.topface.topface.data;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.topface.DialogError;
import com.facebook.topface.Facebook;
import com.facebook.topface.FacebookError;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.VirusLikesRequest;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class VirusLike extends AbstractData {
    public static final String FIELD_SOCIAL_IDS = "social_ids";
    public static final String FIELD_REQUEST_TEXT = "text";
    public static final int MAX_USERS_FOR_REQUEST = 50;
    private ArrayList<Long> mSocialIdArray = null;
    private String mRequestText;

    public VirusLike(ApiResponse response) {
        parseResponse(response);
    }

    private void parseResponse(ApiResponse response) {
        if (response != null && response.jsonResult != null) {
            mSocialIdArray = parseIdsArray(response.jsonResult);
            mRequestText = response.jsonResult.optString(FIELD_REQUEST_TEXT);
        } else {
            mSocialIdArray = new ArrayList<>();
        }
    }

    private ArrayList<Long> parseIdsArray(JSONObject response) {

        ArrayList<Long> socialIdArray = new ArrayList<>();

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
     * @param from     контекст вызова, нужен для статистики
     * @param context  контекст
     * @param listener листенер диалога приглашений
     */
    public void sendFacebookRequest(final String from, final Context context, final VirusLikeDialogListener listener) {
        if (mSocialIdArray != null && mSocialIdArray.size() > 0) {
            Bundle params = new Bundle();
            //Заголовок приглашения
            params.putString("title", context.getString(R.string.virus_chat_likes_request_title));
            //Текст приглашения
            params.putString("message", getFacebookRequestText());
            //Магический переключатель стиля приглашений
            params.putString("new_style_message", "true");
            //Еще более магический параметр для реквеста
            setRequestDataParam(params);

            //ID друзей которым мы отправляем реквест
            final ArrayList<Long> socialIdForRequest = getSocialIdForRequest();
            params.putString("to", TextUtils.join(",", socialIdForRequest));
            //Показываем диалог прилашения
            Facebook facebook = AuthorizationManager.getFacebook();
            facebook.setAccessToken(AuthToken.getInstance().getTokenKey());
            facebook.dialog(context, "apprequests", params, new VirusLikeDialogListener(context) {

                @Override
                public void onComplete(Bundle values) {

                    EasyTracker.getTracker().sendEvent(
                            "VirusLikeRequest", "Complete",
                            from, (long) socialIdForRequest.size()
                    );

                    //Если есть еще друзья, которых можно пригласить, то отправляем запрос заново
                    if (mSocialIdArray != null && mSocialIdArray.size() > 0) {
                        sendFacebookRequest(from, context, listener);
                    } else {
                        //Когда все пользователи закончились, отправляем коллбэки
                        super.onComplete(values);
                        listener.onComplete(values);
                    }
                    //Отправляем на сервер id друзей, которым отправили реквесты
                    new VirusLikesRequest(socialIdForRequest, context).exec();
                }

                @Override
                public void onFacebookError(FacebookError e) {
                    super.onFacebookError(e);

                    EasyTracker.getTracker().sendEvent(
                            "VirusLikeRequest", "FacebookError_" + e.getErrorType(),
                            from, (long) socialIdForRequest.size()
                    );

                    listener.onFacebookError(e);
                }

                @Override
                public void onError(DialogError e) {
                    super.onError(e);

                    EasyTracker.getTracker().sendEvent(
                            "VirusLikeRequest", "DialogError",
                            from, 0L
                    );

                    listener.onError(e);
                }

                @Override
                public void onCancel() {
                    super.onCancel();

                    EasyTracker.getTracker().sendEvent(
                            "VirusLikeRequest", "Cancel",
                            from, 0L
                    );

                    listener.onCancel();
                }
            });

        } else {
            //Если пустой список пользователей, которых мы приглашаем, то просто считаем запрос завершенным
            listener.onComplete(null);
        }
    }

    /**
     * Возвращаем id друзей, которым отправляется реквест и после этого удаляем их из списка
     *
     * @return массив социальных id друзей для реквеста
     */
    private ArrayList<Long> getSocialIdForRequest() {
        //Получаем индекс первых 50 id из массива
        int maxArrayIndex = Math.min(MAX_USERS_FOR_REQUEST, mSocialIdArray.size());
        //Получаем первые 50
        ArrayList<Long> result = new ArrayList<>(
                mSocialIdArray.subList(0, maxArrayIndex)
        );
        //После этого пересоздаем массив уже без этих id
        mSocialIdArray = new ArrayList<>(
                mSocialIdArray.subList(maxArrayIndex, mSocialIdArray.size())
        );

        return result;
    }

    /**
     * Устанавливает поле data в FbDialog, это дополнительные данные, которые получит Topface при клике на ссылку
     *
     * @param params Bundle с параметрами диалога
     */
    private void setRequestDataParam(Bundle params) {
        AuthToken token = AuthToken.getInstance();
        params.putString("data", "{\"type\":\"invite\",\"page\":\"Dating\",\"ref\":\"likegift:" + token.getUserSocialId() + "\"}");
    }

    public static class VirusLikeDialogListener implements Facebook.DialogListener {
        private final Context mContext;

        public VirusLikeDialogListener(Context context) {
            mContext = context;
        }

        @Override
        public void onComplete(Bundle values) {
        }

        @Override
        public void onFacebookError(FacebookError e) {
            onError(null);
        }

        @Override
        public void onError(DialogError e) {
            Toast.makeText(
                    mContext,
                    R.string.virus_request_error,
                    Toast.LENGTH_SHORT
            ).show();
        }

        @Override
        public void onCancel() {
        }

    }
}
