package com.sonetica.topface.social;

import com.sonetica.topface.App;
import com.sonetica.topface.R;
import com.sonetica.topface.net.Auth;
import com.sonetica.topface.net.Requester;
import com.sonetica.topface.social.fb.FbAuthWebViewClient;
import com.sonetica.topface.social.vk.VkAuthWebViewClient;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
    Utils.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // class WebHandler
  //---------------------------------------------------------------------------
  private class WebHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      if(msg.arg1==AuthToken.AUTH_COMPLETE) {
        AuthToken.Token token = (AuthToken.Token)msg.obj;
        
        if(token==null) {
          setResult(Activity.RESULT_CANCELED);
          finish();
        }
        
        Auth auth = new Auth();
        auth.platform = token.getSocialNet();
        auth.sid  = token.getUserId();
        auth.token    = token.getTokenKey();
        
        Requester.sendAuth(auth,new Handler() {
          @Override
          public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1==Requester.OK && msg.obj!=null) {
              // запись ssid
              SharedPreferences preferences = SocialWebActivity.this.getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
              SharedPreferences.Editor editor = preferences.edit();
              String ss1 = getString(R.string.ssid);
              String ss2 = (String)msg.obj;
              editor.putString(ss1,ss2);
              editor.commit();

              setResult(Activity.RESULT_OK);
              finish();
            } else {
              setResult(Activity.RESULT_CANCELED);
              finish();
            }
          }
        });
      } else {
        // стирание ssid
        SharedPreferences preferences = SocialWebActivity.this.getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.ssid),"");
        editor.commit();
        
        setResult(Activity.RESULT_CANCELED);
        finish();
      }
    }
  }// WebHandler
  //---------------------------------------------------------------------------
}// SocialWebActivity

