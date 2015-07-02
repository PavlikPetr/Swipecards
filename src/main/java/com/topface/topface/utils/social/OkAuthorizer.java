package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.AppSocialAppsIds;
import com.topface.topface.utils.config.SessionConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

/**
 * Class that starts Odnoklassniki authorization
 */
public class OkAuthorizer extends Authorizer {

    public static String getOkId() {
        return App.getAppSocialAppsIds().okId;
    }

    private final class GetCurrentUserTask extends AsyncTask<Void, Void, String> {

        private final Odnoklassniki odnoklassniki;
        private final String token;

        public GetCurrentUserTask(Odnoklassniki ok, String token) {
            odnoklassniki = ok;
            Debug.log("Odnoklassniki token: " + token);
            this.token = token;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return odnoklassniki.request("users.getCurrentUser", null, "get");
            } catch (IOException e) {
                Debug.error("Odnoklassniki doInBackground error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Debug.log("Odnoklassniki users.getCurrentUser result: " + s);
            Intent intent = new Intent(AUTH_TOKEN_READY_ACTION);
            if (s != null) {
                AuthToken authToken = AuthToken.getInstance();
                try {
                    JSONObject user = new JSONObject(s);
                    Field f = odnoklassniki.getClass().getDeclaredField("mRefreshToken");
                    f.setAccessible(true);
                    authToken.saveToken(
                            AuthToken.SN_ODNOKLASSNIKI,
                            user.optString("uid"),
                            token,
                            (String) f.get(odnoklassniki)
                    );
                    SessionConfig sessionConfig = App.getSessionConfig();
                    sessionConfig.setSocialAccountName(user.optString("name"));
                    sessionConfig.saveConfig();

                    intent.putExtra(TOKEN_STATUS, TOKEN_READY);
                } catch (Exception e) {
                    Debug.error("Odnoklassniki result parse error", e);
                    intent.putExtra(TOKEN_STATUS, TOKEN_NOT_READY);
                }
            } else {
                Debug.error("Odnoklassniki auth error. users.getCurrentUser returns null");
                intent.putExtra(TOKEN_STATUS, TOKEN_NOT_READY);
            }

            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Debug.error("Odnoklassniki auth cancelled");
        }
    }

    public OkAuthorizer(Activity activity) {
        super(activity);
        AppSocialAppsIds ids = App.getAppSocialAppsIds();
    }

    private Odnoklassniki getOkAuthObj(AppSocialAppsIds ids) {
        if (!Odnoklassniki.hasInstance()) {
            Odnoklassniki.createInstance(getActivity(), ids.okId, ids.getOkSecretKey(), ids.getOkPublicKey());
        }
        return Odnoklassniki.createInstance(getActivity(), ids.okId, ids.getOkSecretKey(), ids.getOkPublicKey());
    }

    @Override
    public void authorize() {
        final AppSocialAppsIds ids = App.getAppSocialAppsIds();
        getOkAuthObj(ids).setTokenRequestListener(new OkTokenRequestListener() {
            @Override
            public void onSuccess(String token) {
                Debug.log("Odnoklassniki auth success with token " + token);
                new GetCurrentUserTask(getOkAuthObj(ids), token).execute();
                Intent intent = new Intent(AUTH_TOKEN_READY_ACTION);
                intent.putExtra(TOKEN_STATUS, TOKEN_PREPARING);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }

            @Override
            public void onError() {
                Debug.error("Odnoklassniki auth error");
            }

            @Override
            public void onCancel() {
                Debug.error("Odnoklassniki auth cancel");
            }
        });
        getOkAuthObj(ids).requestAuthorization(getActivity(), false, OkScope.SET_STATUS, OkScope.PHOTO_CONTENT, OkScope.VALUABLE_ACCESS);
    }

    @Override
    public void logout() {
        getOkAuthObj(App.getAppSocialAppsIds()).clearTokens(getActivity());
    }
}
