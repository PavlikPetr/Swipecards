package com.sonetica.topface.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
}
