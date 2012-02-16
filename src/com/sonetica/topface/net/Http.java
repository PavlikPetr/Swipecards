package com.sonetica.topface.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.sonetica.topface.Data;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.widget.ImageView;

/*
 *  Класс для работы с http запросами 
 */
public class Http {
  // Data
  private static final String TAG = "Http"; 
  private static final int HTTP_GET_REQUEST  = 0;
  private static final int HTTP_POST_REQUEST = 1;
  //---------------------------------------------------------------------------
  // Проверка на наличие интернета
  public static boolean isOnline(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if(networkInfo != null && networkInfo.isConnected())
      return true;
    else
      return false;
  }
  //---------------------------------------------------------------------------
  //  Get запрос
  public static String httpGetRequest(String request) {
    return httpRequest(HTTP_GET_REQUEST,request,null,false);
  }
  //---------------------------------------------------------------------------
  //  Post запрос
  public static String httpPostRequest(String request, String postParams) {
    return httpRequest(HTTP_POST_REQUEST,request,postParams,false);
  }
  //---------------------------------------------------------------------------
  //  запрос к TopFace API
  public static String httpSendTpRequest(String request, String postParams) {
    
    Data.s_LogList.add("   [REQ]: "+postParams);  // JSON LOG   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    
    return httpRequest(HTTP_POST_REQUEST,request,postParams,true);
  }
  //---------------------------------------------------------------------------
  private static String httpRequest(int typeRequest, String request, String postParams,boolean isJson) {
    Debug.log(TAG,"req:"+postParams);
    
    String response = null;
    HttpURLConnection httpConnection = null;
    BufferedReader buffReader = null;
    try {
      // запрос
      httpConnection = (HttpURLConnection)new URL(request).openConnection();
      httpConnection.setUseCaches(false);

      // опция для запроса на TopFace API сервер
      if(isJson)
        httpConnection.setRequestProperty("Content-Type", "application/json");
      
      // отправляем post параметры
      if(typeRequest == HTTP_POST_REQUEST){
        httpConnection.setDoOutput(true);
        OutputStreamWriter osw = new OutputStreamWriter(httpConnection.getOutputStream());
        osw.write(postParams);
        osw.flush();
        osw.close();
      }
      
      // проверяет код ответа сервера
      int responseCode = httpConnection.getResponseCode();
      if(responseCode != HttpURLConnection.HTTP_OK) {
        Debug.log(TAG,"server response code:" + responseCode);
        return null;
      }

      // чтение ответа
      buffReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
      
      StringBuilder responseBuilder = new StringBuilder();
      String line;
      while((line=buffReader.readLine()) != null)
        responseBuilder.append(line);
      response = responseBuilder.toString();
      
      Data.s_LogList.add("   [RESP]: "+response);  // JSON LOG !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      
      
    } catch(MalformedURLException e) {
      Debug.log(TAG,"url is wrong:" + e);
    } catch(IOException e) {
      Debug.log(TAG,"io is fail #1:" + e);
    } finally {
      Debug.log(TAG,"resp:" + response);
      if(buffReader!=null)
        try {
          buffReader.close();
        } catch(IOException e) {
          Debug.log(TAG,"io is fail #2:" + e);
        }
      if(httpConnection!=null)
        httpConnection.disconnect();
    }
    return response;
  }
  //---------------------------------------------------------------------------
  //  запускается в UI потоке, отдельный поток создавать не нужно
  public static void imageLoader(final String url, final ImageView view) {
    // ui
    final Handler handler = new Handler() {
      @Override
      public void handleMessage(Message message) {
        Bitmap bitmap = (Bitmap)message.obj;
        if(bitmap!=null)
          view.setImageBitmap(bitmap);
      }
    };
    // download
    Thread thread = new Thread() {
      @Override
      public void run() {
        Bitmap bitmap = bitmapLoader(url);
        if(bitmap != null)
          handler.sendMessage(handler.obtainMessage(1, bitmap));
      }
    };
    thread.setPriority(3);
    thread.start();
  }
  //---------------------------------------------------------------------------
  /*
   *  для использования необходим отдельный поток
   *  при обрыве связи при скачивании фабрика возвращает null  
   */
  public static Bitmap bitmapLoader(String url) {
    HttpURLConnection   httpConnection  = null;
    BufferedInputStream buffInputStream = null;
    Bitmap bitmap = null;
    
    try {
      httpConnection = (HttpURLConnection)new URL(url).openConnection();
      httpConnection.setDoInput(true);
      httpConnection.connect();
      
      buffInputStream = new BufferedInputStream(httpConnection.getInputStream(), 8192);

      bitmap = BitmapFactory.decodeStream(buffInputStream,null,new BitmapFactory.Options());
      
      buffInputStream.close();
      httpConnection.disconnect();      
    } catch(MalformedURLException e) {
      Debug.log(TAG,"url is wrong:" + e);
    } catch(IOException e) {
      Debug.log(TAG,"io is fail #1:" + e);
    } finally {
      try {
        if(buffInputStream!=null)
          buffInputStream.close();
      } catch(IOException e) {
        Debug.log(TAG,"io is fail #2:" + e);
      }
      if(httpConnection!=null)
        httpConnection.disconnect();
    }
    
    Debug.log(TAG,"bitmap loading");

    return bitmap;
  }
  //---------------------------------------------------------------------------
  //  Возвращает поток от серевра для ручной обработки urlConnection для закрытия соединения
  public static Pair<InputStream,HttpURLConnection> rawHttpStream(String url) throws MalformedURLException, IOException {
    HttpURLConnection   httpConnection  = null;
    BufferedInputStream buffInputStream = null;
    
    httpConnection = (HttpURLConnection)new URL(url).openConnection();
    httpConnection.setDoInput(true);
    httpConnection.connect();
      
    buffInputStream = new BufferedInputStream(httpConnection.getInputStream(), 8192);
    
    Debug.log(TAG,"raw streaming");
    
    return new Pair<InputStream,HttpURLConnection>(buffInputStream,httpConnection);
  }
  //---------------------------------------------------------------------------
}
