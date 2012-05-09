package com.topface.topface.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
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
  private static final int HTTP_TIMEOUT = 25*1000;
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
    return httpRequest(HTTP_GET_REQUEST,request,null,null,null);
  }
  //---------------------------------------------------------------------------
  //  Post запрос
  public static String httpPostRequest(String request,String postParams) {
    return httpRequest(HTTP_POST_REQUEST,request,postParams,null,null);
  }
  //---------------------------------------------------------------------------
  //  запрос к TopFace API
  public static String httpSendTpRequest(String request,String postParams) {
    /*
    if(Data.s_LogList!=null)
      Data.s_LogList.add("   [REQ]: "+postParams);  // JSON LOG   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    */
    return httpRequest(HTTP_POST_REQUEST,request,postParams,null,null);
  }
  //---------------------------------------------------------------------------
  // загрузка фото в соц сеть, массив данных
  public static String httpPostDataRequest(String request, String postParams, byte[] dataParams) {
    return httpRequest(HTTP_POST_REQUEST,request,postParams,dataParams,null);
  }
  //---------------------------------------------------------------------------
  // загрузка фото в соц сеть, потоком
  public static String httpPostDataRequest(String request, String postParams, InputStream is) {
    return httpRequest(HTTP_POST_REQUEST,request,postParams,null,is);
  }
  //---------------------------------------------------------------------------
  private static String httpRequest(int typeRequest, String request,String postParams,byte[] dataParams,InputStream is) {
    String response = null;
    InputStream in = null;
    OutputStream out = null;
    BufferedReader buffReader = null;
    HttpURLConnection httpConnection = null;
    
    try {
      //System.setProperty("http.keepAlive", "false");
      Debug.log(TAG,"enter");
      // запрос
      httpConnection = (HttpURLConnection)new URL(request).openConnection();
      //httpConnection.setUseCaches(false);
      httpConnection.setConnectTimeout(HTTP_TIMEOUT);
      httpConnection.setReadTimeout(HTTP_TIMEOUT);
      if(typeRequest==HTTP_POST_REQUEST)
        httpConnection.setRequestMethod("POST");
      else
        httpConnection.setRequestMethod("GET");
      httpConnection.setDoOutput(true);
      httpConnection.setDoInput(true);
      httpConnection.setRequestProperty("Content-Type", "application/json");
      //httpConnection.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
      //httpConnection.setRequestProperty("Connection", "close");
      //httpConnection.setRequestProperty("Connection", "Keep-Alive");
      //httpConnection.setChunkedStreamingMode(0);

      httpConnection.connect();
      
      
      Debug.log(TAG,"req:"+postParams);   // REQUEST

      // отправляем post параметры
      if(typeRequest == HTTP_POST_REQUEST && postParams != null && dataParams == null) {
        Debug.log(TAG,"begin:");
        out  = httpConnection.getOutputStream();
        byte[] buffer = postParams.getBytes("UTF8");
        out.write(buffer);
        out.flush();
        out.close();
        Debug.log(TAG,"end:");
      }
      in = httpConnection.getInputStream();
      
     // отправляем dataParams параметры
      if(typeRequest == HTTP_POST_REQUEST && dataParams != null) {
        String lineEnd    = "\r\n";
        String twoHyphens = "--";
        String boundary   = "0xKhTmLbOuNdArY";
        httpConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        DataOutputStream dos = new DataOutputStream(out = httpConnection.getOutputStream());
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"photo.jpg\"" + lineEnd);
        dos.writeBytes("Content-Type: image/jpg" + lineEnd + lineEnd);
        dos.write(dataParams);
        dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
        dos.flush();
        dos.close();
        out.close();
      }
      
      
      // отправляем inputStream
      if(typeRequest == HTTP_POST_REQUEST && is != null) {
        String lineEnd    = "\r\n";
        String twoHyphens = "--";
        String boundary   = "0xKhTmLbOuNdArY";
        httpConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        DataOutputStream dos = new DataOutputStream(out = httpConnection.getOutputStream());
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"photo.jpg\"" + lineEnd);
        dos.writeBytes("Content-Type: image/jpg" + lineEnd + lineEnd);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024];
        while(bis.read(buffer) > 0)
          dos.write(buffer); 
        dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
        dos.flush();
        dos.close();
        out.close();
      }

      // проверяет код ответа сервера и считываем данные
      if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        StringBuilder responseBuilder = new StringBuilder();
        BufferedInputStream bis = new BufferedInputStream(in = httpConnection.getInputStream());
        byte[] buffer = new byte[1024];
        int n;
        while((n=bis.read(buffer)) > 0)
          responseBuilder.append(new String(buffer,0,n)); 
        response = responseBuilder.toString();
        bis.close();
      }        
      
        /*
        if(response.length()>500 && Data.s_LogList!=null)               // JSON LOG !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          Data.s_LogList.add("   [RESP]: "+response.substring(0,500));
        else if(Data.s_LogList!=null)
          Data.s_LogList.add("   [RESP]: "+response);
        */
      Debug.log(TAG,"resp:" + response);   // RESPONSE
      Debug.log(TAG,"exit");
    } catch(Exception e) {
      String errorResponse = null;
      try {
        StringBuilder responseBuilder = new StringBuilder();
        BufferedInputStream bis = new BufferedInputStream(in = httpConnection.getErrorStream());
        byte[] buffer = new byte[1024];
        int n;
        while((n=bis.read(buffer)) > 0)
          responseBuilder.append(new String(buffer,0,n)); 
        errorResponse = responseBuilder.toString();
        bis.close();
      } catch(Exception e1) {
        Debug.log(TAG,"http error:" + e1);
      }
      Debug.log(TAG,"http exception:" + e + "" + errorResponse);
    } finally {
      try {
        Debug.log(TAG,"disconnect");
        //if(httpConnection!=null) httpConnection.disconnect();
        if(in!=null) in.close();
        if(out!=null) out.close();
        if(buffReader!=null) buffReader.close();
      } catch(Exception e) {
        Debug.log(TAG,"http error:" + e);
      }
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
      
    } catch(MalformedURLException e) {
      Debug.log(TAG,"url is wrong:" + e);
    } catch(IOException e) {
      Debug.log(TAG,"io is fail #1:" + e);
    } finally {
      try {
        if(buffInputStream!=null) buffInputStream.close();
        if(httpConnection!=null) httpConnection.disconnect();
      } catch(IOException e) {
        Debug.log(TAG,"io is fail #2:" + e);
      }
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
  public static String httpTPRequest(String url,String params) {
    String result = null;
    HttpPost httpPost = new HttpPost(url);
    httpPost.addHeader("Content-Type","application/json");
    HttpParams httpParams = new BasicHttpParams();
    DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
    HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_TIMEOUT);
    HttpConnectionParams.setSoTimeout(httpParams, HTTP_TIMEOUT);
    try {
      httpPost.setEntity(new StringEntity(params,"UTF-8"));
      HttpResponse response = httpClient.execute(httpPost);
      HttpEntity entity = response.getEntity();
      if(entity != null) {
        //InputStream stream = entity.getContent();
        result = EntityUtils.toString(entity);
        //result = convertStreamToString(stream);
        //stream.close();
        //entity.getContent().close();
      }
    } catch (Exception e) {
      result = null;
    }
    return result;
  }
  //---------------------------------------------------------------------------
  public static String convertStreamToString(InputStream stream) {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      String line;
      while ((line = reader.readLine()) != null)
        stringBuilder.append(line);
    } catch (IOException e) {
    } finally {
      try {
        stream.close();
      } catch (IOException e) {
      }
    }
    return stringBuilder.toString();
  }
  //---------------------------------------------------------------------------
}
