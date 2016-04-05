package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.social.AppSocialAppsIds;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.SessionConfig;

import org.json.JSONObject;

import java.util.Locale;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkScope;

/**
 * Class that starts Odnoklassniki authorization
 */
public class OkAuthorizer extends Authorizer {

    public static String getOkId() {
        return App.getAppSocialAppsIds().okId;
    }

    public Odnoklassniki getOkAuthObj(AppSocialAppsIds ids) {
        if (!Odnoklassniki.hasInstance()) {
            Odnoklassniki.createInstance(App.getContext(), ids.okId, ids.getOkPublicKey());
        }
        return Odnoklassniki.getInstance();
    }

    private void sendTokenIntent(int tokenStatus) {
        Intent intent = new Intent(AUTH_TOKEN_READY_ACTION);
        intent.putExtra(TOKEN_STATUS, tokenStatus);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    @Override
    public void authorize(Activity activity) {
        final AppSocialAppsIds ids = App.getAppSocialAppsIds();
        getOkAuthObj(ids).requestAuthorization(new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                if (json != null) {
                    final OkAccessData okAccessData = JsonUtils.fromJson(json.toString(), OkAccessData.class);
                    if (!okAccessData.isEmpty()) {
                        Debug.log("Odnoklassniki auth success with token " + okAccessData.getToken());
                        new CurrentUser(getOkAuthObj(ids)).setUserListener(new CurrentUser.OkRequestListener() {
                            @Override
                            public void onSuccess(OkUserData data) {
                                AuthToken authToken = AuthToken.getInstance();

                                authToken.saveToken(
                                        AuthToken.SN_ODNOKLASSNIKI,
                                        data.uid,
                                        okAccessData.getToken(),
                                        okAccessData.getSecretKey()
                                );
                                SessionConfig sessionConfig = App.getSessionConfig();
                                sessionConfig.setSocialAccountName(data.name);
                                sessionConfig.saveConfig();
                                sendTokenIntent(TOKEN_READY);
                            }

                            @Override
                            public void onFail() {
                                sendTokenIntent(TOKEN_NOT_READY);
                            }
                        }).exec();
                        sendTokenIntent(TOKEN_PREPARING);
                    } else {
                        Debug.log("Odnoklassniki auth data is empty");
                    }
                }
            }

            @Override
            public void onError(String error) {
                Debug.error("Odnoklassniki auth error");
            }
        }, "okauth://ok125879808", OkAuthType.ANY, OkScope.SET_STATUS, OkScope.PHOTO_CONTENT, OkScope.VALUABLE_ACCESS);
    }

    @Override
    public void logout() {
        getOkAuthObj(App.getAppSocialAppsIds()).clearTokens();
    }

    public static boolean isMainScreenLoginEnable() {
        return new Locale(App.getLocaleConfig().getApplicationLocale()).getLanguage().equals(Utils.getRussianLocale().getLanguage());
    }
}
