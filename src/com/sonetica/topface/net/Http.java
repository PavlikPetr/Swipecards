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
  //---------------------------------------------------------------------------
  public static String httpGetRequest(String request) {
    HttpURLConnection urlConnection = null;
    try {
      // Делаем запрос
      URL url = new URL(request);
      urlConnection = (HttpURLConnection)url.openConnection();
      urlConnection.setUseCaches(false);
      
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
  public static String httpPostRequest(String request, String postParams) {
    HttpURLConnection urlConnection = null;
    try {
      // Делаем запрос
      URL url = new URL(request);
      urlConnection = (HttpURLConnection)url.openConnection();
      urlConnection.setUseCaches(false);
      urlConnection.setDoOutput(true);
      
      // Отправляем post параметры
      OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
      osw.write(postParams);
      osw.flush();
      osw.close();
      
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
  public static boolean isOnline(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if(networkInfo != null && networkInfo.isConnected())
      return true;
    else
      return false;
  
  }
  //---------------------------------------------------------------------------
  public static void imageLoader(final String url, final ImageView view) {
    if(url == null || view == null )
      return;
    
    final Handler handler = new Handler() {
      @Override
      public void handleMessage(Message message) {
        final Bitmap image = (Bitmap)message.obj;
        view.setImageBitmap(image);
      }
    };

    final Thread thread = new Thread() {
      @Override
      public void run() {
        final Bitmap image = httpBitmapLoader(url);
        if(image != null) {
          final Message message = handler.obtainMessage(1, image);
          handler.sendMessage(message);
        }
      }
    };

    thread.setPriority(3);
    thread.start();
  }
  //---------------------------------------------------------------------------
  private static Bitmap httpBitmapLoader(String url) {
    Bitmap bitmap = null;
    HttpURLConnection conn = null;
    BufferedInputStream buf_stream = null;
    try {
      conn = (HttpURLConnection)new URL(url).openConnection();
      conn.setDoInput(true);
      conn.setRequestProperty("Connection", "Keep-Alive");
      conn.connect();
      buf_stream = new BufferedInputStream(conn.getInputStream(), 8192);
      bitmap = BitmapFactory.decodeStream(buf_stream);
      buf_stream.close();
      conn.disconnect();
      buf_stream = null;
      conn = null;
    } catch (MalformedURLException ex) {
      Utils.log(null, "Url parsing was failed: " + url);
    } catch (IOException ex) {
      Utils.log(null, url + " does not exists");
    } catch (OutOfMemoryError e) {
      Utils.log(null, "Out of memory!");
      return null;
    } finally {
      if(buf_stream != null)
        try { 
          buf_stream.close(); 
        } catch (IOException ex) {}
      if(conn != null)
        conn.disconnect();
    }
    return bitmap;
  }
  //---------------------------------------------------------------------------
}
