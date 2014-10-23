package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.BaseAuthFragment;
import com.topface.topface.utils.config.SessionConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

/**
 * Created by saharuk on 20.10.14.
 */
public class OkAuthorizer extends Authorizer {

    private Odnoklassniki mOkAuthObject;

    private final class GetCurrentUserTask extends AsyncTask<Void, Void, String> {

        private final Odnoklassniki odnoklassniki;
        private final String token;

        public GetCurrentUserTask(Odnoklassniki ok, String token, OnTokenReceivedListener listener) {
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
            if (s != null) {
                final AuthToken authToken = AuthToken.getInstance();
                try {
                    JSONObject user = new JSONObject(s);
                    Field f = odnoklassniki.getClass().getDeclaredField("mRefreshToken");
                    f.setAccessible(true);
                    authToken.saveToken(AuthToken.SN_ODNOKLASSNIKI, user.optString("uid"), token, (String) f.get(odnoklassniki));
                    SessionConfig sessionConfig = App.getSessionConfig();
                    sessionConfig.setSocialAccountName(user.optString("name"));
                    sessionConfig.saveConfig();
                    receiveToken();
                } catch (Exception e) {
//                    mHandler.sendEmptyMessage(Authorizer.AUTHORIZATION_FAILED);
                    Debug.error("Odnoklassniki result parse error", e);
                }
            } else {
                Debug.error("Odnoklassniki auth error. users.getCurrentUser returns null");
//                mHandler.sendEmptyMessage(Authorizer.AUTHORIZATION_FAILED);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Debug.error("Odnoklassniki auth cancelled");
//            mHandler.sendEmptyMessage(Authorizer.AUTHORIZATION_FAILED);
        }
    }

    public OkAuthorizer(Activity activity) {
        super(activity);
        mOkAuthObject = Odnoklassniki.createInstance(getActivity(), Static.AUTH_OK_ID, Static.OK_SECRET_KEY, Static.OK_PUBLIC_KEY);
    }

    @Override
    public void authorize() {
        mOkAuthObject.setTokenRequestListener(new OkTokenRequestListener() {
            @Override
            public void onSuccess(String token) {
                Debug.log("Odnoklassniki auth success with token " + token);
                new GetCurrentUserTask(mOkAuthObject, token, getOnTokenReceivedListener()).execute();
            }

            @Override
            public void onError() {
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(Authorizer.AUTHORIZATION_TAG).putExtra(
                                BaseAuthFragment.MSG_AUTH_KEY,
                                Authorizer.AUTHORIZATION_FAILED
                        )
                );
                Debug.error("Odnoklassniki auth error");
            }

            @Override
            public void onCancel() {
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(Authorizer.AUTHORIZATION_TAG).putExtra(
                                BaseAuthFragment.MSG_AUTH_KEY,
                                Authorizer.AUTHORIZATION_CANCELLED
                        )
                );
                Debug.error("Odnoklassniki auth cancel");

            }
        });

        mOkAuthObject.requestAuthorization(getActivity(), false, OkScope.SET_STATUS, OkScope.PHOTO_CONTENT, OkScope.VALUABLE_ACCESS);
    }

    @Override
    public void logout() {
        mOkAuthObject.clearTokens(getActivity());
    }
}
