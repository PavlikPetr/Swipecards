package com.topface.topface.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;
import com.topface.topface.Data;
import com.topface.topface.ui.WebAuthActivity;

/**
 * AuthorizationManager has to be attached to some Activity (setted on getInstance(...))
 * onActivityResult(...) put to the parent Activity.onActivityResult(...)
 * Use handler setted with setOnAuthorizationHandler() to receive authorization events
 * message.what = AUTHORIZATION_FAILED - authorization failed for some reasons
 * message.what = TOKEN_RECEIVED | message.obj = authToken - token received from SN 
 * message.what = DIALOG_COMPLETED - dialog for login closed
 * @author kirussell
 *
 */

//TODO make some Strategy or State pattern for different social networks 
public class AuthorizationManager {

	private static AuthorizationManager mInstance;

	public final static int AUTHORIZATION_FAILED = 0;
	public final static int TOKEN_RECEIVED = 1;
	public final static int DIALOG_COMPLETED = 2;

	// Constants
	private String[] FB_PERMISSIONS = { "user_photos", "publish_stream", "email", "publish_actions", "offline_access" };

	// Facebook
	private AsyncFacebookRunner mAsyncFacebookRunner;

	private static Handler mHandler;
	private static Activity mParentActivity;

	private AuthorizationManager() { }

	public static AuthorizationManager getInstance(Activity parent) {
		mParentActivity = parent;
		mHandler = null;
		if (mInstance == null)
			mInstance = new AuthorizationManager();
		return mInstance;
	}

	// common methods
	public void reAuthorize() {
		AuthToken authToken = new AuthToken(mParentActivity.getApplicationContext());

		if (authToken.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
			facebookAuth();
		} else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
			vkontakteAuth();
		}
	}

	public void extendAccessToken() {
		Data.facebook.extendAccessTokenIfNeeded(mParentActivity.getApplicationContext(), null);
	}

	private void receiveToken(AuthToken authToken) {
		if (mHandler != null) {			
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

					AuthToken authToken = new AuthToken(mParentActivity.getApplicationContext());
					authToken.saveToken(AuthToken.SN_VKONTAKTE, user_id, token_key, expires_in);
					receiveToken(authToken);
				}
			} else {
				Data.facebook.authorizeCallback(requestCode, resultCode, data);
			}
		}
	}

	// vkontakte methods
	public void vkontakteAuth() {
		Intent intent = new Intent(mParentActivity.getApplicationContext(), WebAuthActivity.class);
		mParentActivity.startActivityForResult(intent, WebAuthActivity.INTENT_WEB_AUTH);
	}

	// facebook methods
	public void facebookAuth() {
//		AuthToken authToken = new AuthToken(mParentActivity.getApplicationContext());
//				        
//        if(authToken.getTokenKey() != null) {
//            Data.facebook.setAccessToken(authToken.getTokenKey());
//        }
//        long expires = Long.parseLong(authToken.getExpires()); 
//        if(expires != 0) {
//        	Data.facebook.setAccessExpires(expires);
//        }
//        
//        if(!Data.facebook.isSessionValid()) {
			mAsyncFacebookRunner = new AsyncFacebookRunner(Data.facebook);		
			Data.facebook.authorize(mParentActivity, FB_PERMISSIONS, mDialogListener);
//        }
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
				final AuthToken authToken = new AuthToken(mParentActivity.getApplicationContext());
				authToken.saveToken(AuthToken.SN_FACEBOOK, user_id, Data.facebook.getAccessToken(),
						Long.toString(Data.facebook.getAccessExpires()));
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
}