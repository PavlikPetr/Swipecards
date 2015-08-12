package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AppsFlyerData;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.FbAuthorizer;
import com.topface.topface.utils.social.OkAuthorizer;
import com.topface.topface.utils.social.VkAuthorizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

public class AuthRequest extends PrimalAuthRequest {
    // Data
    public static final String SERVICE_NAME = "auth.login";
    public static final String FALLBACK_LOCALE = "en_US";
    /**
     * Временная зона девайса по умолчанию, отправляем каждый раз на сервер при авторизации
     */
    public static final String timezone = TimeZone.getDefault().getID();
    private String mSid; // id пользователя в социальной сети
    private String mToken; // токен авторизации в соц сети
    private String mPlatform; // код социальной сети
    private String mLogin;  // логин для нашей авторизации
    private String mPassword; // пароль для нашей авторизации
    private String mRefresh; // еще один токен для одноклассников
    private AppsFlyerData mAppsflyer; //ID пользователя в appsflyer

    private AuthRequest(Context context) {
        super(context);
        doNeedAlert(false);
        if (context != null) {
            try {
                mAppsflyer = new AppsFlyerData(context);
            } catch (Exception e) {
                Debug.error("AppsFlyer exception", e);
            }
        } else {
            Debug.log("AuthRequest: can't create appsflyer with null context.");
        }
    }

    public AuthRequest(AuthToken.TokenInfo authTokenInfo, Context context) {
        this(context);
        mPlatform = authTokenInfo.getSocialNet();
        if (TextUtils.equals(mPlatform, AuthToken.SN_TOPFACE)) {
            mLogin = authTokenInfo.getLogin();
            mPassword = authTokenInfo.getPassword();
        } else if (TextUtils.equals(mPlatform, AuthToken.SN_ODNOKLASSNIKI)) {
            mSid = authTokenInfo.getUserSocialId();
            mToken = authTokenInfo.getTokenKey();
            mRefresh = authTokenInfo.getExpiresIn();
        } else {
            mSid = authTokenInfo.getUserSocialId();
            mToken = authTokenInfo.getTokenKey();
        }
    }

    @Override
    public boolean containsAuth() {
        return true;
    }

    @Override
    protected String getClientLocale() {
        String locale;
        //На всякий случай проверяем возможность получить локаль
        try {
            locale = App.getContext().getResources().getString(R.string.app_locale);
        } catch (Exception e) {
            locale = FALLBACK_LOCALE;
        }

        return locale;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = super.getRequestData();
        data.put("sid", mSid)
                .put("platform", mPlatform)
                .put("login", mLogin)
                .put("password", mPassword)
                .put("refresh", mRefresh)
                .put("timezone", timezone)
                .put("token", mToken);
        if (mAppsflyer != null) {
            data.put("appsflyer", mAppsflyer.toJsonWithConversions(App.getConversionHolder()));
        }
        switch (mPlatform) {
            case AuthToken.SN_ODNOKLASSNIKI:
                data.put("socialAppId", OkAuthorizer.getOkId());
                break;
            case AuthToken.SN_FACEBOOK:
                data.put("socialAppId", FbAuthorizer.getFbId());
                break;
            case AuthToken.SN_VKONTAKTE:
                data.put("socialAppId", VkAuthorizer.getVkId());
                break;
        }
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public boolean isNeedAuth() {
        return false;
    }

    public boolean isValidRequest() {
        return !(TextUtils.isEmpty(mPlatform)
                && TextUtils.isEmpty(mToken)
                && TextUtils.isEmpty(mSid));
    }

    @Override
    public void exec() {
        if (!isValidRequest()) {
            handleFail(ErrorCodes.UNVERIFIED_TOKEN, "Key params are empty");
            return;
        } else {
            if (TextUtils.equals(mPlatform, AuthToken.SN_TOPFACE)) {
                if (TextUtils.isEmpty(mLogin) || TextUtils.isEmpty(mPassword)) {
                    handleFail(ErrorCodes.UNVERIFIED_TOKEN, "Key params are empty");
                    return;
                }
            } else if (TextUtils.isEmpty(mSid) || TextUtils.isEmpty(mToken)) {
                handleFail(ErrorCodes.UNVERIFIED_TOKEN, "Key params are empty");
                return;
            }
        }
        super.exec();
    }
}
