package com.topface.topface.social;

import com.topface.topface.App;
import com.topface.topface.Global;
import com.topface.topface.R;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;

/*
 * Класс активити для ввода логина и пароля в социальной сети
 * через WebView компонент
 */
public class SocialWebActivity extends Activity {
  // Data
  private WebView mWebView;
  private View mProgressBar;
  // Constants
  public static final int TYPE_VKONTAKTE = 0;
  public static final int TYPE_FACEBOOK  = 1;
  public static final int INTENT_SOCIAL_WEB = 101;
  public static final String TYPE = "social_network";
  //---------------------------------------------------------------------------
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_social_web);

    mProgressBar = findViewById(R.id.pgrsSocialWeb);
    
    mWebView = (WebView)findViewById(R.id.wvSocWebRegistr);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.setVerticalScrollbarOverlay(true);
    mWebView.setVerticalFadingEdgeEnabled(true);

    int type_network = getIntent().getIntExtra(TYPE,-1);
    
    if(type_network == TYPE_VKONTAKTE)
      mWebView.setWebViewClient(new VkAuthWebViewClient(SocialWebActivity.this, mWebView, mProgressBar, new WebHandler()));
    else if(type_network == TYPE_FACEBOOK)
      mWebView.setWebViewClient(new FbAuthWebViewClient(SocialWebActivity.this, mWebView, mProgressBar, new WebHandler()));
  }
  //---------------------------------------------------------------------------
  
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mProgressBar = null;
    CookieManager.getInstance().removeAllCookie();
    mWebView.destroy();
    mWebView = null;
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // class WebHandler
  //---------------------------------------------------------------------------
  private class WebHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      if(msg.what==AuthToken.AUTH_COMPLETE) {
        mProgressBar.setVisibility(View.VISIBLE);
        // отправка токена на TP сервер
        AuthToken.Token token   = (AuthToken.Token)msg.obj;
        AuthRequest authRequest = new AuthRequest(SocialWebActivity.this);
        authRequest.platform   = token.getSocialNet();
        authRequest.sid        = token.getUserId();
        authRequest.token      = token.getTokenKey();
        authRequest.locale     = Global.LOCALE;
        authRequest.clienttype = Global.CLIENT_TYPE;
        authRequest.callback(new ApiHandler() {
          @Override
          public void success(ApiResponse response) {
            // запись ssid
            Auth auth = Auth.parse(response);
            App.saveSSID(getApplicationContext(),auth.ssid);
            setResult(Activity.RESULT_OK);
            mProgressBar.setVisibility(View.GONE);
            finish();
          }
          @Override
          public void fail(int codeError,ApiResponse response) {
            mProgressBar.setVisibility(View.GONE);
            Debug.log(SocialWebActivity.this,"ssid is wrong");
            setResult(Activity.RESULT_CANCELED);
            finish();
          }
        }).exec();
      } else {
        // стирание ssid
        App.removeSSID(getApplicationContext());
        setResult(Activity.RESULT_CANCELED);
        finish();
      }
    }
  }// WebHandler
  //---------------------------------------------------------------------------
}// SocialWebActivity

