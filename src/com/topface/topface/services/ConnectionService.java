package com.topface.topface.services;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.topface.topface.utils.Http;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.BasicHttpContext;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http.FlushedInputStream;
import com.topface.topface.utils.http.AndroidHttpClient;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ConnectionService extends Service {
  private static AndroidHttpClient mHttpClient;
  // Constants
  public static final String TAG = "CC";

    //---------------------------------------------------------------------------
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    
    Debug.log(this,"+onCreate");
      create();
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Debug.log(this,"+onStartCommand");
    return START_STICKY;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    if(mHttpClient!=null)
      mHttpClient.close();
    mHttpClient = null;

      Debug.log(this,"+onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void create() {
    mHttpClient = AndroidHttpClient.newInstance("Android");    
  }
  //---------------------------------------------------------------------------
  public String request(final ApiRequest request) {
    Debug.log(TAG,"cc_req::"+request.toString());   // REQUEST
    String rawResponse = Static.EMPTY;
    HttpPost httpPost = null;
    try {
      BasicHttpContext httpContext = new BasicHttpContext();
      httpPost = new HttpPost(Static.API_URL + Static.API_VERSION);
      httpPost.addHeader("Connection", "keep-alive");
      httpPost.addHeader("Accept-Encoding", "gzip");
      httpPost.setHeader("Content-Type", "application/json");
      httpPost.setEntity(new ByteArrayEntity(request.toString().getBytes("UTF8")));
      HttpResponse httpResponse = mHttpClient.execute(httpPost, httpContext);
      HttpEntity httpEntity = httpResponse.getEntity();
      if(httpEntity != null) {
        StringBuilder sb = new StringBuilder();
        InputStream is = AndroidHttpClient.getUngzippedContent(httpEntity);
        BufferedInputStream bis = new BufferedInputStream(new FlushedInputStream(is), Http.BUFFER_SIZE);
        BufferedReader r = new BufferedReader(new InputStreamReader(bis), Http.BUFFER_SIZE);
        for(String line = r.readLine(); line != null; line = r.readLine())
          sb.append(line);
        rawResponse = sb.toString();
        is.close();
        httpEntity.consumeContent();
        Debug.log(TAG,"cc_resp::" + rawResponse);   // RESPONSE
      }
    } catch(Exception e) {
      Debug.error("ConnectionManager exception", e);
      if(httpPost != null) httpPost.abort();
      if(mHttpClient != null) mHttpClient.close();
      create();
    }
    return rawResponse;
  }

}
