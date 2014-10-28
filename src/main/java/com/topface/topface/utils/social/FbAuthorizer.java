package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.topface.AsyncFacebookRunner;
import com.facebook.topface.DialogError;
import com.facebook.topface.Facebook;
import com.facebook.topface.FacebookError;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.utils.config.SessionConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Class that starts Facebook authorization
 */
public class FbAuthorizer extends Authorizer {

    private String[] FB_PERMISSIONS = {"user_photos", "email", "offline_access"};

    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncFacebookRunner;

    private Facebook.DialogListener mDialogListener = new Facebook.DialogListener() {
        @Override
        public void onComplete(Bundle values) {
            Debug.log("FB", "mDialogListener::onComplete");
            mAsyncFacebookRunner.request("/me", mRequestListener);
        }

        @Override
        public void onFacebookError(FacebookError e) {
            Debug.log("FB", "mDialogListener::onFacebookError:" + e.getMessage());
        }

        @Override
        public void onError(DialogError e) {
            Debug.log("FB", "mDialogListener::onError");
        }

        @Override
        public void onCancel() {
            Debug.log("FB", "mDialogListener::onCancel");

        }
    };

    private AsyncFacebookRunner.RequestListener mRequestListener = new AsyncFacebookRunner.RequestListener() {
        @Override
        public void onComplete(String response, Object state) {
            try {
                Debug.log("FB", "mRequestListener::onComplete");
                JSONObject jsonResult = new JSONObject(response);
                AuthToken.getInstance().saveToken(
                        AuthToken.SN_FACEBOOK,
                        jsonResult.getString("id"),
                        mFacebook.getAccessToken(),
                        Long.toString(mFacebook.getAccessExpires())
                );
                SessionConfig sessionConfig = App.getSessionConfig();
                sessionConfig.setSocialAccountName(jsonResult.optString("name", ""));
                sessionConfig.setSocialAccountEmail(jsonResult.optString("email", ""));
                sessionConfig.saveConfig();
            } catch (JSONException e) {
                Debug.error("FB login mRequestListener::onComplete:error", e);
            }
        }

        @Override
        public void onMalformedURLException(MalformedURLException e, Object state) {
            Debug.error("FB mRequestListener::onMalformedURLException", e);
        }

        @Override
        public void onIOException(IOException e, Object state) {
            Debug.error("FB mRequestListener::onIOException", e);
        }

        @Override
        public void onFileNotFoundException(FileNotFoundException e, Object state) {
            Debug.error("FB mRequestListener::onFileNotFoundException", e);
        }

        @Override
        public void onFacebookError(FacebookError e, Object state) {
            Debug.error("FB mRequestListener::onFacebookError", e);
        }
    };

    public FbAuthorizer(Activity activity) {
        super(activity);
        mFacebook = new Facebook(App.getAppConfig().getAuthFbApi());
    }

    @Override
    public void authorize() {
        mAsyncFacebookRunner = new AsyncFacebookRunner(mFacebook);
        mFacebook.authorize(getActivity(), FB_PERMISSIONS, mDialogListener);
    }

    @Override
    public void logout() {
        new AsyncTask() {
            @Override
            protected java.lang.Object doInBackground(java.lang.Object... params) {
                try {
                    mFacebook.logout(App.getContext());
                } catch (Exception e) {
                    Debug.error(e);
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    @Override
    public boolean refreshToken() {
        return mFacebook.extendAccessTokenIfNeeded(getActivity(), null);
    }
}
