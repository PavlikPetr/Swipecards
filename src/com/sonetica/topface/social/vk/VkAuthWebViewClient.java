package com.sonetica.topface.social.vk;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Utils;
import org.apache.http.client.utils.URLEncodedUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для аутентификации в Вконтатке для WebView компонента
 */
public class VkAuthWebViewClient extends WebViewClient {
  // Data
  private Context mContext;
  private Handler mHandler;
  private View    mProgressIndicator;
  // RegExp
  private Pattern mRegExpToken  = Pattern.compile("blank.html#(.*access_token=.+)$");
  private Pattern mRegExpError  = Pattern.compile("blank.html#(.*error=.+)$");
  private Pattern mRegExpLogout = Pattern.compile("(.*act=logout.+)$");
  //https://login.vk.com/?act=logout
  // Constants
  private static final int CLIENT_ID     = 2664589; //vokrug 2454030  //tf 2257829 //tf-d 2664589
  private static final String SCOPE = "notify,friends,photos,wall,groups,offline,messages";
  //---------------------------------------------------------------------------
  /**
   * @param webView в котором будет происходить авторизация
   * @param progressIndicator если передать view (например ProgressBar) оно будет показано во время загрузки страницы
   * @param context контекст приложения
   * @param handler будет вызван после завершения авторизации
   */
  public VkAuthWebViewClient(Context context, WebView webView, View progressIndicator, final Handler handler) {
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

    Matcher mMatcherToken  = mRegExpToken.matcher(url);
    Matcher mMatcherError  = mRegExpError.matcher(url);
    Matcher mMatcherLogout = mRegExpLogout.matcher(url);

    // Ждем подходящую строку запроса
    if(mMatcherToken.find()) {
      view.stopLoading();
      try {
        URLEncodedUtils.parse(new URI(url), "utf-8");
      } catch(URISyntaxException e) {
      }
      // Разбор строки запроса и выбор токена и user_id
      HashMap<String, String> queryMap = Utils.parseQueryString(mMatcherToken.group(1));
      String tokenKey  = queryMap.get("access_token");
      String userId    = queryMap.get("user_id");
      String expiresIn = queryMap.get("expires_in");

      // Запись данных и получение объекта токена
      AuthToken authToken = new AuthToken(mContext);
      AuthToken.Token token = authToken.setToken(AuthToken.SN_VKONTAKTE,userId,tokenKey,expiresIn);
      mHandler.sendMessage(Message.obtain(null,AuthToken.AUTH_COMPLETE,token));
    } else if(mMatcherError.find() || mMatcherLogout.find()) {
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
    return "http://api.vkontakte.ru/oauth/authorize?client_id=" + CLIENT_ID + "&scope=" + SCOPE + 
           "&redirect_uri=http://api.vkontakte.ru/blank.html&display=touch&response_type=token";
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
}//VkAuthWebViewClient
