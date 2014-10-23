package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.ui.fragments.BaseAuthFragment;

/**
 * Abstract class for starting different types of authorization
 */
public abstract class Authorizer {

    public static final String AUTHORIZATION_TAG = "com.topface.topface.authorization";
    public final static int AUTHORIZATION_FAILED = 0;
    public final static int TOKEN_RECEIVED = 1;
    public final static int DIALOG_COMPLETED = 2;
    public final static int AUTHORIZATION_CANCELLED = 3;
    private Activity mActivity;
    private OnTokenReceivedListener mOnTokenReceivedListener;

    public Authorizer(Activity activity) {
        mActivity = activity;
    }

    public abstract void authorize();

    public void onCreate(Bundle savedInstanceState) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void onResume() {
    }

    public void onDestroy() {
    }

    protected void receiveToken() {
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(AUTHORIZATION_TAG).putExtra(BaseAuthFragment.MSG_AUTH_KEY, TOKEN_RECEIVED));
    }

    public Activity getActivity() {
        return mActivity;
    }

    public abstract void logout();

    public boolean refreshToken() {
        return true;
    }

    public OnTokenReceivedListener getOnTokenReceivedListener() {
        return mOnTokenReceivedListener;
    }

    //Если пользователь авторизуется через ок и нажимает кнопку назад, когда открылся браузер
    //контроль переходит к нашему приложению и вызывается onResume. Если мы скрываем кнопки и показываем лоадер
    //до того, как получили токен одноклассников, получается, что лоадер будет вечно крутиться и кнопки никогда не появятся.
    //Этот лисенер специально для того, чтобы скрывать кнопки только тогда, когда нам уже придет ответ от одноклассников.
    public interface OnTokenReceivedListener {
        public void onTokenReceived();

        public void onTokenReceiveFailed();
    }
}
