package com.sonetica.topface.social.fb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Класс для аутентификации в Facebook для WebView компонента
 */
public class FbAuthWebViewClient extends WebViewClient {
  // Data
  private Context mContext;
  private Handler mHandler;
  private View    mProgressIndicator;
  // RegExp
  private Pattern mRegExpToken = Pattern.compile("login_success.html#(.*access_token=.+)$");
  private Pattern mRegExpError = Pattern.compile("login_success.html#(.*error=.+)$");
  // Constants
  private static final long CLIENT_ID     = 161347997227885L;
  private static final String SCOPE = "manage_pages,user_photos,user_videos,publish_stream,offline_access,user_checkins,friends_checkins";
  //---------------------------------------------------------------------------
  /**
   * @param webView в котором будет происходить авторизация
   * @param progressIndicator если передать view (например ProgressBar) оно будет показано во время загрузки страницы
   * @param context контекст приложения
   * @param handler будет вызван после завершения авторизации
   */
  public FbAuthWebViewClient(Context context, WebView webView, View progressIndicator, final Handler handler) {
    super();

    mContext = context;
    mHandler = handler;
    mProgressIndicator = progressIndicator;

    // Передаем строку запроса на авторизацию в соц.сети
    webView.loadUrl(getAuthUrl());
  }
  //---------------------------------------------------------------------------
  /**
   * Событие на начало загрузки страницы
   * @param view WebView в котором грузится страницы
   * @param url страницы
   * @param favicon страницы
   */
  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    super.onPageStarted(view, url, favicon);
    showProgressBar();

    Matcher mMatcherToken = mRegExpToken.matcher(url);
    Matcher mMatcherError = mRegExpError.matcher(url);

    if(mMatcherToken.find()) {
      view.stopLoading();
      try {
        URLEncodedUtils.parse(new URI(url), "utf-8");
      } catch(URISyntaxException e) {
        Debug.log(this,"Error parse url");
      }
      
      // Разбор строки запроса и выбор токена
      HashMap<String, String> queryMap = Utils.parseQueryString(mMatcherToken.group(1));
      String tokenKey  = queryMap.get("access_token");
      String expiresIn = queryMap.get("expires_in");
      
      // Дополнительный запрос для получения user_id пользователя
      String userId = getUserId(tokenKey);

      // Запись данных и получение объекта токена
      AuthToken authToken = new AuthToken(mContext);
      AuthToken.Token token = authToken.setToken(AuthToken.SN_VKONTAKTE,userId,tokenKey,expiresIn);
      mHandler.sendMessage(Message.obtain(null,AuthToken.AUTH_COMPLETE,token));
    } else if (mMatcherError.find()) {
      view.stopLoading();
      // Очистка токена при отмене аутентификации
      new AuthToken(mContext).remove();
      mHandler.sendMessage(Message.obtain(null,AuthToken.AUTH_ERROR));
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
    hideProgressBar();
  }
  //---------------------------------------------------------------------------
  private String getAuthUrl() {
    return "https://graph.facebook.com/oauth/authorize?client_id=" + CLIENT_ID + "&scope=" + SCOPE + 
           "&redirect_uri=http://www.facebook.com/connect/login_success.html&type=user_agent&display=touch";
  }
  //---------------------------------------------------------------------------
  private void showProgressBar() {
    if(mProgressIndicator != null)
      mProgressIndicator.setVisibility(View.VISIBLE);
  }
  //---------------------------------------------------------------------------  
  private void hideProgressBar() {
    if(mProgressIndicator != null) 
      mProgressIndicator.setVisibility(View.GONE);
  }
  //---------------------------------------------------------------------------
  public String getUserId(String token) {
    StringBuilder request = new StringBuilder("https://graph.facebook.com/");
    request.append("me");
    request.append("&access_token=" + token);
    JSONObject jsonResult = null;
    try {
      String response = Http.httpGetRequest(request.toString());
      jsonResult = new JSONObject(response);
    } catch(JSONException ex) {
      ex.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
    if(jsonResult == null)
      return null;
    String id = "";
    try {
      id = (String)jsonResult.get("id");
    } catch(JSONException e) {
      Debug.log(this, "'user_id' isn't received");
    } 
    return id;
  }
  //---------------------------------------------------------------------------
}//FbAuthWebViewClient

//https://www.facebook.com/dialog/oauth?client_id=161347997227885&scope=notify,friends,photos,wall,groups,offline,messages&redirect_uri=http://api.vkontakte.ru/blank.html&display=touch&response_type=token
//https://graph.facebook.com/oauth/authorize?client_id=161347997227885&redirect_uri=http://www.facebook.com/connect/login_success.html&scope=manage_pages,user_photos,user_videos,publish_stream,offline_access,user_checkins,friends_checkins&type=user_agent&display=touch
//https://graph.facebook.com/me&access_token=acc8AAACSvsIPW20BAF4lO8IizlvPb4zkeC3a7juYCQgB41Uz4onxZBCSTcUjdlIQMUlV0xZCKGqNZAZAEDi8yuQshkj1ACVJ3JeGgvQr6xF9rQZDZD&expires_in=0&code=AQDO-CvA_6RW5NwxzfDZMzGXnqi7Y4KS2OUS-Yb1KtT7Iylb2in7nW-fuiVQ4hQFdfa-7aGHqY4Txynh1D84PGbD3bqdIyyQQRppIE3_0BOF1RCDzBIFud4zhbRQ6bQ_w5AeHx0H8VyeGYf_ltJ5CEx0rrI06R9pORm8p57eZnso0lWNbwbofwI3innkTk_Lo5s

