package com.topface.topface.social;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import org.json.JSONException;
import org.json.JSONObject;
import com.facebook.android.*;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.*;
import com.topface.topface.App;
import com.topface.topface.Global;
import com.topface.topface.R;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.ui.dashboard.DashboardActivity;
import com.topface.topface.utils.Debug;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Класс активити выбора социальной сети для аутентификации
 */
public class SocialActivity extends Activity implements View.OnClickListener {
  // Data
  private Facebook mFacebook;
  private AsyncFacebookRunner mAsyncFacebookRunner;
  // Constants
  private static final String APP_ID = "161347997227885";
  private static final String[] FB_PERMISSIONS = {"user_photos","publish_stream,email","user_birthday","friends_online_presence","user_about_me"};
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_social);
    
    mFacebook = new Facebook(APP_ID);
    mAsyncFacebookRunner = new AsyncFacebookRunner(mFacebook);
    
    /*// clear web cache
    mWebView.clearCache(true);
    mWebView.clearFormData();
    mWebView.clearView();
    mWebView.clearHistory();
    WebSettings webSettings = mWebView.getSettings();
    webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    webSettings.setAppCacheEnabled(false);
    webSettings.setAppCacheMaxSize(0);
    webSettings.setDatabaseEnabled(false);
    webSettings.setSavePassword(false);
    webSettings.setSaveFormData(false);
    deleteDatabase("webview.db");
    deleteDatabase("webviewCache.db");
    CookieManager.getInstance().removeAllCookie();
    */
    
    // VKontakte Button
    ((Button)findViewById(R.id.btnSocialVk)).setOnClickListener(this);
    
    // Facebook Button
    ((Button)findViewById(R.id.btnSocialFb)).setOnClickListener(this);

  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mFacebook.authorizeCallback(requestCode, resultCode, data);
    if(requestCode != SocialWebActivity.INTENT_SOCIAL_WEB)
      return;
    if(resultCode == Activity.RESULT_OK) {
      startActivity(new Intent(this,DashboardActivity.class));
      finish();
    }
  }
  //---------------------------------------------------------------------------
  private void auth(AuthToken.Token token){
    AuthRequest authRequest = new AuthRequest(getApplicationContext());
    authRequest.platform   = token.getSocialNet();
    authRequest.sid        = token.getUserId();
    authRequest.token      = token.getTokenKey();
    authRequest.locale     = Global.LOCALE;
    authRequest.clienttype = Global.CLIENT_TYPE;
    authRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Auth auth = Auth.parse(response);
        App.saveSSID(getApplicationContext(),auth.ssid);
        startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
        finish();
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        //
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View button) {
    Intent intent = new Intent(SocialActivity.this, SocialWebActivity.class);
    switch(button.getId()) {
      case R.id.btnSocialVk:
        intent.putExtra(SocialWebActivity.TYPE,SocialWebActivity.TYPE_VKONTAKTE);
        startActivityForResult(intent,SocialWebActivity.INTENT_SOCIAL_WEB);
        break;
      case R.id.btnSocialFb: {
        //intent.putExtra(SocialWebActivity.TYPE,SocialWebActivity.TYPE_FACEBOOK);
        if(!mFacebook.isSessionValid()) 
          mFacebook.authorize(this, FB_PERMISSIONS, mDialogListener);
      } break;
    }
    //startActivityForResult(intent,SocialWebActivity.INTENT_SOCIAL_WEB);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private DialogListener mDialogListener = new DialogListener(){
    @Override
    public void onComplete(Bundle values) {
      mAsyncFacebookRunner.request("/me",mRequestListener);
    }
    @Override
    public void onFacebookError(FacebookError e) {}
    @Override
    public void onError(DialogError e) {}
    @Override
    public void onCancel() {}};
  //---------------------------------------------------------------------------
  private RequestListener mRequestListener = new RequestListener() {
    @Override
    public void onMalformedURLException(MalformedURLException e,Object state) {}
    @Override
    public void onIOException(IOException e,Object state) {}
    @Override
    public void onFileNotFoundException(FileNotFoundException e,Object state) {}
    @Override
    public void onFacebookError(FacebookError e,Object state) {}
    @Override
    public void onComplete(String response,Object state) {
      try {
        JSONObject jsonResult = new JSONObject(response);
        String user_id = jsonResult.getString("id");
        AuthToken authToken = new AuthToken(getApplicationContext());
        final AuthToken.Token token = authToken.setToken(AuthToken.SN_FACEBOOK,user_id,mFacebook.getAccessToken(),""+mFacebook.getAccessExpires());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            SocialActivity.this.auth(token); 
          }
        });
      } catch(JSONException e) {
        e.printStackTrace();
      }
    }
  };
  //---------------------------------------------------------------------------
}
