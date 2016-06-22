package com.topface.topface.utils.social;

import android.app.Activity;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.AuthTokenStateData;
import com.topface.topface.data.social.AppSocialAppsIds;
import com.topface.topface.state.AuthState;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.SessionConfig;

import org.json.JSONObject;

import java.util.Locale;

import javax.inject.Inject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkScope;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Class that starts Odnoklassniki authorization
 */
public class OkAuthorizer extends Authorizer {

    @Inject
    TopfaceAppState mAppState;
    @Inject
    AuthState mAuthState;

    public OkAuthorizer() {
        App.from(App.getContext()).inject(this);
    }

    public static String getOkId() {
        return App.getAppSocialAppsIds().okId;
    }

    public Odnoklassniki getOkAuthObj(AppSocialAppsIds ids) {
        if (!Odnoklassniki.hasInstance()) {
            return Odnoklassniki.createInstance(App.getContext(), ids.okId, ids.getOkPublicKey());
        }
        return Odnoklassniki.getInstance();
    }

    private void sendTokenIntent(@AuthTokenStateData.AuthTokenStatus int tokenStatus) {
        mAuthState.setData(new AuthTokenStateData(tokenStatus));
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
                        new CurrentUserRequest(getOkAuthObj(ids)).getObservable().subscribe(new Action1<OkUserData>() {
                            @Override
                            public void call(OkUserData okUserData) {
                                mAppState.setData(okUserData);
                                AuthToken authToken = AuthToken.getInstance();
                                authToken.temporarilySaveToken(
                                        AuthToken.SN_ODNOKLASSNIKI,
                                        okUserData.uid,
                                        okAccessData.getToken(),
                                        okAccessData.getSecretKey()
                                );
                                SessionConfig sessionConfig = App.getSessionConfig();
                                sessionConfig.setSocialAccountName(okUserData.name);
                                sessionConfig.saveConfig();
                                sendTokenIntent(AuthTokenStateData.TOKEN_READY);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                                sendTokenIntent(AuthTokenStateData.TOKEN_NOT_READY);
                            }
                        }, new Action0() {
                            @Override
                            public void call() {

                            }
                        });
                        sendTokenIntent(AuthTokenStateData.TOKEN_PREPARING);
                    } else {
                        Debug.log("Odnoklassniki auth data is empty");
                    }
                }
            }

            @Override
            public void onError(String error) {
                Debug.error("Odnoklassniki auth error");
                sendTokenIntent(AuthTokenStateData.TOKEN_NOT_READY);
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
