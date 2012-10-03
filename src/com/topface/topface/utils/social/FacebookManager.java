package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.topface.topface.Data;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public class FacebookManager implements SocialManager {
    private String[] FB_PERMISSIONS = {"user_photos", "publish_stream", "email", "publish_actions", "offline_access"};

    private AsyncFacebookRunner mAsyncFacebookRunner;
    private static Handler mHandler;

    @Override
    public void authorize(Activity activity) {
        mAsyncFacebookRunner = new AsyncFacebookRunner(Data.facebook);
        Data.facebook.authorize(activity, FB_PERMISSIONS, mDialogListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Data.facebook.authorizeCallback(requestCode, resultCode, data);
    }

    private DialogListener mDialogListener = new DialogListener() {
        @Override
        public void onComplete(Bundle values) {
            Debug.log("FB", "mDialogListener::onComplete");
            mAsyncFacebookRunner.request("/me", mRequestListener);
            if (mHandler != null) {
                mHandler.sendEmptyMessage(DIALOG_COMPLETED);
            }
        }

        @Override
        public void onFacebookError(FacebookError e) {
            Debug.log("FB", "mDialogListener::onFacebookError:" + e.getMessage());
            if (mHandler != null) {
                mHandler.sendEmptyMessage(AUTHORIZATION_FAILED);
            }
        }

        @Override
        public void onError(DialogError e) {
            Debug.log("FB", "mDialogListener::onError");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(AUTHORIZATION_FAILED);
            }
        }

        @Override
        public void onCancel() {
            Debug.log("FB", "mDialogListener::onCancel");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(AUTHORIZATION_FAILED);
            }
        }
    };

    private RequestListener mRequestListener = new RequestListener() {
        @Override
        public void onComplete(String response, Object state) {
            try {
                Debug.log("FB", "mRequestListener::onComplete");
                JSONObject jsonResult = new JSONObject(response);
                String user_id = jsonResult.getString("id");
                String user_name = jsonResult.getString("name");
                Settings.getInstance().setSocialAccountName(user_name);

                //TODO
//				final AuthToken authToken = new AuthToken(mParentActivity.getApplicationContext());
//				authToken.saveToken(AuthToken.SN_FACEBOOK, user_id, Data.facebook.getAccessToken(),
//						Long.toString(Data.facebook.getAccessExpires()));
//				receiveToken(authToken);
            } catch (JSONException e) {
                Debug.log("FB", "mRequestListener::onComplete:error");
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(AUTHORIZATION_FAILED);
                }
            }
        }

        @Override
        public void onMalformedURLException(MalformedURLException e, Object state) {
            Debug.log("FB", "mRequestListener::onMalformedURLException");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(AUTHORIZATION_FAILED);
            }
        }

        @Override
        public void onIOException(IOException e, Object state) {
            Debug.log("FB", "mRequestListener::onIOException");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(AUTHORIZATION_FAILED);
            }
        }

        @Override
        public void onFileNotFoundException(FileNotFoundException e, Object state) {
            Debug.log("FB", "mRequestListener::onFileNotFoundException");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(AUTHORIZATION_FAILED);
            }
        }

        @Override
        public void onFacebookError(FacebookError e, Object state) {
            Debug.log("FB", "mRequestListener::onFacebookError:" + e + ":" + state);
            if (mHandler != null) {
                mHandler.sendEmptyMessage(AUTHORIZATION_FAILED);
            }
        }
    };
}
