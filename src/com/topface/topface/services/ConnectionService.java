package com.topface.topface.services;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.BasicHttpContext;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.AndroidHttpClient;
import com.topface.topface.utils.http.Http.FlushedInputStream;
import com.topface.topface.utils.social.AuthToken;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

public class ConnectionService extends Service {
    // Data
    private static ConnectionService mService;
    private static AndroidHttpClient mHttpClient;
    // Constants
    public static final String TAG = "CC";
    //---------------------------------------------------------------------------
    public static void sendRequest(Context context,ApiRequest request) {
        if (mService == null)
            context.startService(new Intent(context, ConnectionService.class));
        else
            mService.send(request);
    }
    //---------------------------------------------------------------------------
    public static Bitmap bitmapRequest0(String url) {
        return mService.bitmapLoader(url);
    }
    //---------------------------------------------------------------------------
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //---------------------------------------------------------------------------
    @Override
    public void onCreate() {
        super.onCreate();

        Debug.log(this, "+onCreate");
        mService = ConnectionService.this;
        create();
    }
    //---------------------------------------------------------------------------
    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        Debug.log(this, "+onStartCommand");
        return START_STICKY;
    }
    //---------------------------------------------------------------------------
    @Override
    public void onDestroy() {
        if (mHttpClient != null)
            mHttpClient.close();
        mHttpClient = null;
        mService = null;

        Debug.log(this, "+onDestroy");
        super.onDestroy();
    }
    //---------------------------------------------------------------------------
    private void create() {
        mHttpClient = AndroidHttpClient.newInstance("Android");
    }
    //---------------------------------------------------------------------------
    public String request(final ApiRequest request) {
        Debug.log(TAG, "cc_req::" + request.toString()); // REQUEST
        String rawResponse = Static.EMPTY;
        HttpPost httpPost = null;
        try {
            BasicHttpContext httpContext = new BasicHttpContext();
            httpPost = new HttpPost(Static.API_URL);
            httpPost.addHeader("Connection", "keep-alive");
            httpPost.addHeader("Accept-Encoding", "gzip");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new ByteArrayEntity(request.toString().getBytes("UTF8")));
            HttpResponse httpResponse = mHttpClient.execute(httpPost, httpContext);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                StringBuilder sb = new StringBuilder();
                InputStream is = AndroidHttpClient.getUngzippedContent(httpEntity);
                BufferedInputStream bis = new BufferedInputStream(new FlushedInputStream(is), 8192);
                BufferedReader r = new BufferedReader(new InputStreamReader(bis));
                for (String line = r.readLine(); line != null; line = r.readLine())
                    sb.append(line);
                rawResponse = sb.toString();
                r.close();
                is.close();
                httpEntity.consumeContent();
                Debug.log(TAG, "cc_resp::" + rawResponse); // RESPONSE
            }
        } catch(Exception e) {
            Debug.log(TAG, "cm exception:" + e.getMessage());
            for (StackTraceElement st : e.getStackTrace())
                Debug.log(TAG, "cm trace: " + st.toString());
            if (httpPost != null)
                httpPost.abort();
            if (mHttpClient != null)
                mHttpClient.close();
            create();
        }
        return rawResponse;
    }
    //---------------------------------------------------------------------------
    public void send(final ApiRequest request) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (request.handler == null) {
                    request.handler.response(new ApiResponse(Static.EMPTY));
                    return;
                }

                String rawResponse = request(request);
                if (rawResponse == null || rawResponse.equals(Static.EMPTY))
                    rawResponse = request(request);

                if (request.handler != null) {
                    ApiResponse apiResponse = new ApiResponse(rawResponse);
                    if (apiResponse.code == ApiResponse.SESSION_NOT_FOUND)
                        apiResponse = reAuth(request);
                    request.handler.response(apiResponse);
                }
            }
        }).start();
    }
    //---------------------------------------------------------------------------
    private ApiResponse reAuth(ApiRequest request) {
        Debug.log(this, "reAuth");

        AuthToken token = new AuthToken(getApplicationContext());
        AuthRequest authRequest = new AuthRequest(getApplicationContext());
        authRequest.platform = token.getSocialNet();
        authRequest.sid = token.getUserId();
        authRequest.token = token.getTokenKey();
        String rawResponse = request(authRequest);
        ApiResponse response = new ApiResponse(rawResponse);
        if (response.code == ApiResponse.RESULT_OK) {
            Auth auth = Auth.parse(response);
            Data.saveSSID(getApplicationContext(), auth.ssid);
            request.ssid = auth.ssid;
            response = new ApiResponse(request(request));
        } else
            Data.removeSSID(getApplicationContext());
        Debug.log(TAG, "cc_reauth::" + rawResponse); // RESPONSE
        return response;
    }
    //---------------------------------------------------------------------------
    public Bitmap bitmapLoader(String url) {
        if (url == null)
            return null;
        Bitmap bitmap = null;
        HttpGet httpGet = new HttpGet(url);
        try {
            BasicHttpContext localContext = new BasicHttpContext();
            HttpResponse response = mHttpClient.execute(httpGet, localContext);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK)
                return null;
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream is = entity.getContent();
                bitmap = BitmapFactory.decodeStream(new FlushedInputStream(is));
                is.close();
                entity.consumeContent();
            }
        } catch(Exception e) {
            if (httpGet != null)
                httpGet.abort();
            if (mHttpClient != null)
                mHttpClient.close();
            create();
        }
        return bitmap;
    }
    //---------------------------------------------------------------------------
}
