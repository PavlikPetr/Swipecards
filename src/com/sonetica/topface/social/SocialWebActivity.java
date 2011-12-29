package com.sonetica.topface.social;

import com.sonetica.topface.Global;
import com.sonetica.topface.R;
import com.sonetica.topface.net.AuthRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.social.fb.FbAuthWebViewClient;
import com.sonetica.topface.social.vk.VkAuthWebViewClient;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
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
  @Override
  protected void onDestroy() {
    mWebView.destroy();
    mWebView=null;
    mProgressBar=null;
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
        // отправка токена на TP сервер
        AuthToken.Token token   = (AuthToken.Token)msg.obj;
        AuthRequest authRequest = new AuthRequest();
        authRequest.platform = token.getSocialNet();
        authRequest.sid      = token.getUserId();
        authRequest.token    = token.getTokenKey();
        ConnectionService.sendRequest(authRequest,new Handler() {
          @Override
          public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Response resp = (Response)msg.obj;
            if(resp.code==0) {
              // запись ssid
              Global.saveSSID(SocialWebActivity.this,resp.getSSID());
              setResult(Activity.RESULT_OK);
              finish();
            } else {
              Debug.log(SocialWebActivity.this,"ssid is wrong");
              setResult(Activity.RESULT_CANCELED);
              finish();
            }
          }
        });
      } else {
        // стирание ssid
        Global.saveSSID(SocialWebActivity.this,"");
        setResult(Activity.RESULT_CANCELED);
        finish();
      }
    }
  }// WebHandler
  //---------------------------------------------------------------------------
}// SocialWebActivity

