package com.sonetica.topface.social;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.R;
import com.sonetica.topface.social.fb.FbAuthWebViewClient;
import com.sonetica.topface.social.vk.VkAuthWebViewClient;
import com.sonetica.topface.utils.Utils;
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
    super.onDestroy();
    Utils.log(this,"-onDestroy");
  }
  //---------------------------------------------------------------------------
  // class WebHandler
  //---------------------------------------------------------------------------
  private class WebHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      if(msg.arg1 == AuthToken.AUTH_COMPLETE) {
        AuthToken.Token token = (AuthToken.Token) msg.obj;
        if(token == null)
          finish();

        //Отправляем полученый токен на сервер
        JSONObject request     = new JSONObject();
        JSONObject requestData = new JSONObject();
        try {
          //request.put("fb", requestData);
          //request.put("vk", requestData);          
          request.put("vk", token.getSocialNet());
          requestData.put("token", token.getTokenKey());
          requestData.put("id",    token.getUserId());
          /*
          Requester.getRequester().sendPacket("user.updateProfile", request.toString(), 
            new Handler() {
              @Override
              public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setResult(Activity.RESULT_OK);
              }
            });
          */
          finish();
        } catch (JSONException e) {
          Utils.log(this,"Error get token " + e.getMessage());
          setResult(Activity.RESULT_CANCELED);
        } finally {
          finish();
        }
      } else {
        Utils.log(this,"SocialAuth error. Auth dismissed");
        setResult(Activity.RESULT_CANCELED);
        finish();
      }
    }
  }// WebHandler
  //---------------------------------------------------------------------------
}// SocialWebActivity