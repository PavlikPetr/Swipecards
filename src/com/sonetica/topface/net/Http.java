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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import com.sonetica.topface.utils.Utils;

/*
 *  Класс для работы с http запросами 
 */
public class Http {
  // Data
  private static final int HTTP_GET_REQUEST  = 0;
  private static final int HTTP_POST_REQUEST = 1;
  //---------------------------------------------------------------------------
  public static boolean isOnline(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if(networkInfo != null && networkInfo.isConnected())
      return true;
    else
      return false;
  
  }
  //---------------------------------------------------------------------------
  public static String httpGetRequest(String request) {
    return httpRequest(HTTP_GET_REQUEST, request, null);
  }
  //---------------------------------------------------------------------------
  public static String httpPostRequest(String request, String postParams) {
    return httpRequest(HTTP_POST_REQUEST, request, postParams);
  }
  //---------------------------------------------------------------------------
  private static String httpRequest(int typeRequest, String request, String postParams) {
    HttpURLConnection urlConnection = null;
    try {
      // Делаем запрос
      URL url = new URL(request);
      urlConnection = (HttpURLConnection)url.openConnection();
      urlConnection.setUseCaches(false);
      
      if(typeRequest == HTTP_POST_REQUEST){
        // Отправляем post параметры
        urlConnection.setDoOutput(true);
        OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
        osw.write(postParams);
        osw.flush();
        osw.close();
      }
      
      // проверяет код ответа сервера
      final int statusCode = urlConnection.getResponseCode();
      if(statusCode != HttpURLConnection.HTTP_OK)
         return null;

      //Читаем ответ
      InputStream    inStream   = new BufferedInputStream(urlConnection.getInputStream());
      BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream));
      StringBuilder  response   = new StringBuilder();
      String line;
      while((line = buffReader.readLine()) != null)
        response.append(line);
      
      return response.toString();
      
    } catch (IOException e) {
      Utils.log(null,"I/O error while retrieving response " + e.getMessage());
    } catch (IllegalStateException e) {
      Utils.log(null,"Incorrect URL or Connection error " + e.getMessage());
    } catch (Exception e) {
      Utils.log(null,"Error while retrieving response " + e.getMessage());
    } finally {
      if(urlConnection != null)
        urlConnection.disconnect();
    }
    return null;
  }
  //---------------------------------------------------------------------------
  /*
   *  запускается в UI потоке, отдельный поток создавать не нужно
   */
  public static void imageLoader(final String url, final ImageView view) {
    if(url == null || view == null )
      return;
    
    // ui
    final Handler handler = new Handler() {
      @Override
      public void handleMessage(Message message) {
        final Bitmap image = (Bitmap)message.obj;
        view.setImageBitmap(image);
      }
    };
    
    // download
    final Thread thread = new Thread() {
      @Override
      public void run() {
        final Bitmap bitmap = bitmapLoader(url);
        if(bitmap != null) {
          final Message message = handler.obtainMessage(1, bitmap);
          handler.sendMessage(message);
        }
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
    Bitmap bitmap = null;
    HttpURLConnection httpConnection = null;
    BufferedInputStream buffInputStream = null;
    
    try {
      httpConnection = (HttpURLConnection)new URL(url).openConnection();
      httpConnection.setDoInput(true);
      httpConnection.setRequestProperty("Connection", "Keep-Alive");
      httpConnection.connect();
      
      buffInputStream = new BufferedInputStream(httpConnection.getInputStream(), 8192);
      bitmap = BitmapFactory.decodeStream(buffInputStream);
      
    } catch (MalformedURLException ex) {Utils.log(null, "Url parsing was failed: " + url);} 
      catch (IOException ex) {Utils.log(null, url + " does not exists");} 
      catch (OutOfMemoryError ex) {Utils.log(null, "Out of memory!");} 
      finally {
        if(buffInputStream != null)
          try {
            buffInputStream.close();buffInputStream = null;
          } catch(IOException ex){Utils.log(null,"error: "+ex.getMessage());}
        if(httpConnection != null) {
          httpConnection.disconnect();httpConnection = null;
        }
      }
    
    return bitmap;
  }
  //---------------------------------------------------------------------------
}
