package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.facebook.topface.AsyncFacebookRunner;
import com.facebook.topface.AsyncFacebookRunner.RequestListener;
import com.facebook.topface.DialogError;
import com.facebook.topface.Facebook;
import com.facebook.topface.Facebook.DialogListener;
import com.facebook.topface.FacebookError;
import com.topface.topface.Static;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.http.HttpUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * AuthorizationManager has to be attached to some Activity (setted on getInstance(...))
 * onActivityResult(...) put to the parent Activity.onActivityResult(...)
 * Use handler setted with setOnAuthorizationHandler() to receive authorization events
 * message.what = AUTHORIZATION_FAILED - authorization failed for some reasons
 * message.what = TOKEN_RECEIVED | message.obj = authToken - token received from SN
 * message.what = DIALOG_COMPLETED - dialog for login closed
 *
 * @author kirussell
 */

//TODO make some Strategy or State pattern for different social networks 
public class AuthorizationManager {

    public final static int AUTHORIZATION_FAILED = 0;
    public final static int TOKEN_RECEIVED = 1;
    public final static int DIALOG_COMPLETED = 2;
    public final static int AUTHORIZATION_CANCELLED = 3;

    // Constants
    private String[] FB_PERMISSIONS = {"user_photos", "publish_stream", "email", "publish_actions", "offline_access"};

    // Facebook
    private AsyncFacebookRunner mAsyncFacebookRunner;

    private Handler mHandler;
    private Activity mParentActivity;
    private Facebook mFacebook;

    public AuthorizationManager(Activity parent) {
        mParentActivity = parent;
        mHandler = null;
        mFacebook = getFacebook();
    }

    public static Facebook getFacebook() {
        return new Facebook(Static.AUTH_FACEBOOK_ID);
    }

    public static void extendAccessToken(Activity parentActivity) {
        getFacebook().extendAccessTokenIfNeeded(parentActivity.getApplicationContext(), null);
    }

    private void receiveToken(AuthToken authToken) {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(DIALOG_COMPLETED);
            Message msg = new Message();
            msg.what = TOKEN_RECEIVED;
            msg.obj = authToken;
            mHandler.sendMessage(msg);
        }
    }

    public void setOnAuthorizationHandler(Handler handler) {
        mHandler = handler;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == WebAuthActivity.INTENT_WEB_AUTH) {
                if (data != null) {
                    String token_key = data.getExtras().getString(WebAuthActivity.ACCESS_TOKEN);
                    String user_id = data.getExtras().getString(WebAuthActivity.USER_ID);
                    String expires_in = data.getExtras().getString(WebAuthActivity.EXPIRES_IN);
                    String user_name = data.getExtras().getString(WebAuthActivity.USER_NAME);
                    Settings.getInstance().setSocialAccountName(user_name);

                    AuthToken authToken = AuthToken.getInstance();
                    authToken.saveToken(AuthToken.SN_VKONTAKTE, user_id, token_key, expires_in);
                    receiveToken(authToken);
                }
            } else {
                mFacebook.authorizeCallback(requestCode, resultCode, data);
            }
        }
    }

    // vkontakte methods
    public void vkontakteAuth() {
        Intent intent = new Intent(mParentActivity.getApplicationContext(), WebAuthActivity.class);
        mParentActivity.startActivityForResult(intent, WebAuthActivity.INTENT_WEB_AUTH);
    }

    // mFacebook methods
    public void facebookAuth() {
        mAsyncFacebookRunner = new AsyncFacebookRunner(mFacebook);
        mFacebook.authorize(mParentActivity, FB_PERMISSIONS, mDialogListener);
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
                mHandler.sendEmptyMessage(AUTHORIZATION_CANCELLED);
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
                String user_email = jsonResult.getString("email");
                Settings.getInstance().setSocialAccountName(user_name);
                Settings.getInstance().setSocialAccountEmail(user_email);

                final AuthToken authToken = AuthToken.getInstance();
                authToken.saveToken(AuthToken.SN_FACEBOOK, user_id, mFacebook.getAccessToken(),
                        Long.toString(mFacebook.getAccessExpires()));
                receiveToken(authToken);
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

    public static void getAccountName(Handler handler) {
        AuthToken authToken = AuthToken.getInstance();

        if (authToken.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            getFbName(authToken.getUserId(), handler);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            getVkName(authToken.getTokenKey(), authToken.getUserId(), handler);
        }
    }

    private static final String VK_NAME_URL = "https://api.vk.com/method/getProfiles?uid=%s&access_token=%s";
    public static final int SUCCESS_GET_NAME = 0;
    public static final int FAILURE_GET_NAME = 1;

    public static void getVkName(final String token, final String user_id, final Handler handler) {
        (new Thread() {
            @Override
            public void run() {
                String responseRaw = HttpUtils.httpGetRequest(String.format(VK_NAME_URL, user_id, token));
                try {
                    String result = "";
                    JSONObject response = new JSONObject(responseRaw);
                    JSONArray responseArr = response.optJSONArray("response");
                    if (responseArr != null) {
                        if (responseArr.length() > 0) {
                            JSONObject profile = responseArr.getJSONObject(0);
                            result = profile.optString("first_name") + " " + profile.optString("last_name");
                        }
                        handler.sendMessage(Message.obtain(null, SUCCESS_GET_NAME, result));
                    } else {
                        handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
                    }
                } catch (JSONException e) {
                    Debug.log(AuthorizationManager.class, "can't get name in vk:" + e);
                    handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
                }
            }
        }).start();
    }

    public static void getFbName(final String user_id, final Handler handler) {
        new AsyncFacebookRunner(new Facebook(Static.AUTH_FACEBOOK_ID)).request("/" + user_id, new RequestListener() {

            @Override
            public void onComplete(String response, Object state) {
                try {
                    JSONObject jsonResult = new JSONObject(response);
                    String user_name = jsonResult.getString("name");
                    handler.sendMessage(Message.obtain(null, SUCCESS_GET_NAME, user_name));
                } catch (JSONException e) {
                    Debug.log("FB", "mRequestListener::onComplete:error");
                    handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
                }
            }

            @Override
            public void onMalformedURLException(MalformedURLException e, Object state) {
                Debug.log("FB", "mRequestListener::onMalformedURLException");
                handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
            }

            @Override
            public void onIOException(IOException e, Object state) {
                Debug.log("FB", "mRequestListener::onIOException");
                handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e, Object state) {
                Debug.log("FB", "mRequestListener::onFileNotFoundException");
                handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
                Debug.log("FB", "mRequestListener::onFacebookError:" + e + ":" + state);
                handler.sendMessage(Message.obtain(null, FAILURE_GET_NAME, ""));
            }
        });
    }

}