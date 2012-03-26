package com.sonetica.topface.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
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
  private static final int HTTP_TIMEOUT = 5*1000;
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
    return httpRequest(HTTP_GET_REQUEST,request,null,null,null,false);
  }
  //---------------------------------------------------------------------------
  //  Post запрос
  public static String httpPostRequest(String request,String postParams) {
    return httpRequest(HTTP_POST_REQUEST,request,postParams,null,null,false);
  }
  //---------------------------------------------------------------------------
  //  запрос к TopFace API
  public static String httpSendTpRequest(String request,String postParams) {
    /*
    if(Data.s_LogList!=null)
      Data.s_LogList.add("   [REQ]: "+postParams);  // JSON LOG   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    */
    return httpRequest(HTTP_POST_REQUEST,request,postParams,null,null,true);
  }
  //---------------------------------------------------------------------------
  // загрузка фото в соц сеть, массив данных
  public static String httpPostDataRequest(String request, String postParams, byte[] dataParams) {
    return httpRequest(HTTP_POST_REQUEST,request,postParams,dataParams,null,false);
  }
  //---------------------------------------------------------------------------
  // загрузка фото в соц сеть, потоком
  public static String httpPostDataRequest(String request, String postParams, InputStream is) {
    return httpRequest(HTTP_POST_REQUEST,request,postParams,null,is,false);
  }
  //---------------------------------------------------------------------------
  private static String httpRequest(int typeRequest, String request,String postParams,byte[] dataParams,InputStream is,boolean isJson) {
    
    Debug.log(TAG,"req:"+postParams);   // REQUEST
    
    String response = null;
    HttpURLConnection httpConnection = null;
    BufferedReader buffReader = null;
    try {
      // запрос
      httpConnection = (HttpURLConnection)new URL(request).openConnection();
      httpConnection.setUseCaches(false);
      httpConnection.setConnectTimeout(HTTP_TIMEOUT);
      httpConnection.setReadTimeout(HTTP_TIMEOUT);
      
      // опция для запроса на TopFace API сервер
      if(isJson)
        httpConnection.setRequestProperty("Content-Type", "application/json");
      
      // отправляем post параметры
      if(typeRequest == HTTP_POST_REQUEST && postParams != null) {
        httpConnection.setRequestMethod("POST");
        httpConnection.setDoOutput(true);
        OutputStreamWriter osw = new OutputStreamWriter(httpConnection.getOutputStream());
        osw.write(postParams);
        osw.flush();
        osw.close();
      }
      
     // отправляем data параметры
      if(typeRequest == HTTP_POST_REQUEST && dataParams != null) {
        String lineEnd    = "\r\n";
        String twoHyphens = "--";
        String boundary   = "0xKhTmLbOuNdArY";
        httpConnection.setRequestMethod("POST");
        httpConnection.setDoOutput(true);
        httpConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        DataOutputStream dos = new DataOutputStream(httpConnection.getOutputStream());
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"photo.jpg\"" + lineEnd);
        dos.writeBytes("Content-Type: image/jpg" + lineEnd + lineEnd);
        dos.write(dataParams);
        dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
        dos.flush();
        dos.close();
      }
      
      // отправляем inputStream
      if(typeRequest == HTTP_POST_REQUEST && is != null) {
        String lineEnd    = "\r\n";
        String twoHyphens = "--";
        String boundary   = "0xKhTmLbOuNdArY";
        httpConnection.setRequestMethod("POST");
        httpConnection.setDoOutput(true);
        httpConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        DataOutputStream dos = new DataOutputStream(httpConnection.getOutputStream());
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"photo.jpg\"" + lineEnd);
        dos.writeBytes("Content-Type: image/jpg" + lineEnd + lineEnd);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buff = new byte[1024];
        while(bis.read(buff) > 0) {
          dos.write(buff); 
        }
        dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
        dos.flush();
        dos.close();
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
      
      /*
      if(response.length()>500 && Data.s_LogList!=null)               // JSON LOG !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Data.s_LogList.add("   [RESP]: "+response.substring(0,500));
      else if(Data.s_LogList!=null)
        Data.s_LogList.add("   [RESP]: "+response);
      */
    
    } catch(SocketTimeoutException e) {
      Debug.log(TAG,"socket timeout:" + postParams);
    } catch(IOException e) {
      Debug.log(TAG,"io exception:" + e);
    } catch(Exception e) {
      Debug.log(TAG,"http exception:" + e);
    } finally {
      try {
        if(buffReader!=null) buffReader.close();
        if(httpConnection!=null) httpConnection.disconnect();
      } catch(Exception e) {
        Debug.log(TAG,"error:" + e);
      }
      
      Debug.log(TAG,"resp:" + response);   // RESPONSE
    }
    return response;
  }
  //---------------------------------------------------------------------------
  public static Bitmap bitmapLoader(String url) {
    Bitmap bitmap = null;
    BufferedInputStream bin = null;
    try {
      bin = new BufferedInputStream(new URL(url).openStream());
      bitmap = BitmapFactory.decodeStream(bin);
      bin.close();
    } catch (Exception e) {}
    return bitmap;
  }
  //---------------------------------------------------------------------------
  /*
   *  для использования необходим отдельный поток
   *  при обрыве связи при скачивании фабрика возвращает null  
   */
  public static Bitmap bitmapLoaderEx(String url) {
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
    LeaksManager.getInstance().monitorObject(thread);
    thread.start();
  }
  //---------------------------------------------------------------------------
  //  запускается в UI потоке, отдельный поток создавать не нужно
  public static void imageLoaderExp(final String url, final ImageView view) {
    Thread thread = new Thread() {
      @Override
      public void run() {
        final Bitmap bitmap = bitmapLoader(url);
        if(bitmap != null)
          view.post(new Runnable() {
            @Override
            public void run() {
              view.setImageBitmap(bitmap);
            }
          });
      }
    };
    thread.setPriority(3);
    LeaksManager.getInstance().monitorObject(thread);
    thread.start();
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
