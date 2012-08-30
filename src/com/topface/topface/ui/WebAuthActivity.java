package com.topface.topface.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.apps.analytics.easytracking.TrackedActivity;
import org.apache.http.client.utils.URLEncodedUtils;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.utils.AuthToken;
import com.topface.topface.utils.Debug;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebAuthActivity extends TrackedActivity {
  // Data
  private WebView mWebView;
  private View mProgressBar;
  private String VK_PERMISSIONS = "notify,photos,offline";
  // RegExp
  private Pattern mRegExpToken  = Pattern.compile("blank.html#(.*access_token=.+)$");
  private Pattern mRegExpError  = Pattern.compile("blank.html#(.*error=.+)$");
  private Pattern mRegExpLogout = Pattern.compile("(.*act=logout.+)$");
  // Constants
  public static final int INTENT_WEB_AUTH = 101;
  //---------------------------------------------------------------------------
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Debug.log(this,"+onCreate");
    setContentView(R.layout.ac_web_auth);

    // Progress
    mProgressBar = findViewById(R.id.prsWebLoading);
    
    // WebView
    mWebView = (WebView)findViewById(R.id.wvWebFrame);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.setVerticalScrollbarOverlay(true);
    mWebView.setVerticalFadingEdgeEnabled(true);
    mWebView.setWebViewClient(new VkAuthClient(getApplicationContext(), mWebView, mProgressBar, new WebHandler()));
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mWebView.destroy();
    mWebView = null;
    mProgressBar = null;
    CookieManager.getInstance().removeAllCookie();
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // class WebHandler
  //---------------------------------------------------------------------------
  private class WebHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      if(msg.what == AuthToken.AUTH_COMPLETE) {
        AuthToken token = (AuthToken)msg.obj;
        if(token==null) {
          WebAuthActivity.this.finish();
          return;
        }
        
        mProgressBar.setVisibility(View.VISIBLE);
        
        AuthRequest authRequest = new AuthRequest(getApplication());
        authRequest.platform = token.getSocialNet();
        authRequest.sid      = token.getUserId();
        authRequest.token    = token.getTokenKey();
        authRequest.callback(new ApiHandler() {
          @Override
          public void success(ApiResponse response) {
            Debug.log(WebAuthActivity.this,"web auth ssid is ok");
            Auth auth = Auth.parse(response);
            Data.saveSSID(WebAuthActivity.this,auth.ssid);
            post(new Runnable() {
              @Override
              public void run() {
                setResult(Activity.RESULT_OK);
                finish();
              }
            });
          }
          @Override
          public void fail(int codeError,ApiResponse response) {
            Debug.log(WebAuthActivity.this,"web auth ssid is wrong:"+codeError);
            post(new Runnable() {
              @Override
              public void run() {
                setResult(Activity.RESULT_CANCELED);
                finish();
              }
            });
          }
        }).exec();
      } else {
        Debug.log(WebAuthActivity.this,"web auth token is wrong");
        Data.removeSSID(getApplicationContext());
        setResult(Activity.RESULT_CANCELED);
        finish();
      }
      mProgressBar.setVisibility(View.GONE);
    }
  }
  //---------------------------------------------------------------------------
  // VkAuthClient
  //---------------------------------------------------------------------------
  public class VkAuthClient extends WebViewClient {
    // Data
    private Handler mHandler;
    private String mUrl = "http://api.vkontakte.ru/oauth/authorize?client_id=" + Static.AUTH_VKONTAKTE_ID + "&scope=" + VK_PERMISSIONS + "&redirect_uri=http://api.vkontakte.ru/blank.html&display=touch&response_type=token";
    //---------------------------------------------------------------------------
    public VkAuthClient(Context context, WebView webView, View progressIndicator, Handler handler) {
      super();
      mHandler = handler;
      webView.loadUrl(mUrl);
    }
    //---------------------------------------------------------------------------
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
      
      mProgressBar.setVisibility(View.VISIBLE);
    
      Matcher mMatcherToken  = mRegExpToken.matcher(url);
      Matcher mMatcherError  = mRegExpError.matcher(url);
      Matcher mMatcherLogout = mRegExpLogout.matcher(url);
    
      if(mMatcherToken.find()) {
        view.stopLoading();
        try {
          URLEncodedUtils.parse(new URI(url),"utf-8");
        } catch(URISyntaxException e) {
          Debug.log(WebAuthActivity.this,"url is wrong:" + e);
        }
        
        HashMap<String, String> queryMap = parseQueryString(mMatcherToken.group(1));
        String token_key  = queryMap.get("access_token");
        String user_id    = queryMap.get("user_id");
        String expires_in = queryMap.get("expires_in");
    
        AuthToken authToken = new AuthToken(getApplicationContext());
        authToken.saveToken(AuthToken.SN_VKONTAKTE, user_id, token_key, expires_in);
        mHandler.sendMessage(Message.obtain(null,AuthToken.AUTH_COMPLETE,authToken));
      } else if(mMatcherError.find() || mMatcherLogout.find()) {
        view.stopLoading();
        new AuthToken(getApplicationContext()).removeToken();
        mHandler.sendMessage(Message.obtain(null,AuthToken.AUTH_ERROR));
      }
    }
    //---------------------------------------------------------------------------
    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      mProgressBar.setVisibility(View.GONE);
    }
    //---------------------------------------------------------------------------
    public HashMap<String, String> parseQueryString(String query) {
      String[] params = query.split("&");
      HashMap<String, String> map = new HashMap<String, String>();
      for(String param : params) {
        String name  = param.split("=")[0];
        String value = param.split("=")[1];
        map.put(name, value);
      }
      return map;
    }
    //---------------------------------------------------------------------------
  }
  //---------------------------------------------------------------------------
}

