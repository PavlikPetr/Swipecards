package com.topface.topface.utils.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.BasicHttpContext;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.utils.AuthToken;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http.FlushedInputStream;

public class ConnectionManager {
    // Data
    private static ConnectionManager mInstanse;
    private AndroidHttpClient mHttpClient;
    private ExecutorService mWorker;
    // Constants
    public static final String TAG = "CM";
    //---------------------------------------------------------------------------
    private ConnectionManager() {
        mHttpClient = AndroidHttpClient.newInstance("Android"); // For Avatar Bitmaps
        mWorker = Executors.newFixedThreadPool(2);
        //mWorker = Executors.newSingleThreadExecutor();
        //java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        //java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
    }
    //---------------------------------------------------------------------------
    public static ConnectionManager getInstance() {
        if (mInstanse == null)
            mInstanse = new ConnectionManager();
        return mInstanse;
    }
    //---------------------------------------------------------------------------
    public void sendRequest(final ApiRequest apiRequest) {
        mWorker.execute(new Runnable() {
            @Override
            public void run() {
                String rawResponse = Static.EMPTY;
                AndroidHttpClient httpClient = null;
                HttpPost httpPost = null;

                if (apiRequest.handler == null)
                    return;

                apiRequest.ssid = Data.SSID;

                try {
                    httpClient = AndroidHttpClient.newInstance("Android");
                    httpPost = new HttpPost(Static.API_URL);
                    httpPost.setHeader("Accept-Encoding", "gzip");
                    httpPost.setHeader("Content-Type", "application/json");
                    httpPost.setEntity(new ByteArrayEntity(apiRequest.toString().getBytes("UTF8")));

                    Debug.log(TAG, "cm_req::" + apiRequest.toString()); // REQUEST
                    rawResponse = request(httpClient, httpPost); // 
                    Debug.log(TAG, "cm_resp::" + rawResponse); // RESPONSE

                    if (apiRequest.handler != null) {
                        ApiResponse apiResponse = new ApiResponse(rawResponse);
                        if (apiResponse.code == ApiResponse.SESSION_NOT_FOUND)
                            apiResponse = reAuth(apiRequest.context, httpClient, httpPost, apiRequest);
                        apiRequest.handler.response(apiResponse);
                    }

                } catch(Exception e) {
                    Debug.log(TAG, "cm_req exception::" + e.toString());
                    if (httpPost != null && !httpPost.isAborted())
                        httpPost.abort();
                }
                httpClient.close();
            }
        });
    }
    //---------------------------------------------------------------------------
    private String request(AndroidHttpClient httpClient,HttpPost httpPost) {
        String rawResponse = Static.EMPTY;

        try {
            //BasicHttpContext httpContext = new BasicHttpContext();
            HttpResponse httpResponse = httpClient.execute(httpPost/* ,
                                                                    * httpContext */);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                InputStream is = AndroidHttpClient.getUngzippedContent(httpEntity);
                BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FlushedInputStream(is), 8192)));
                StringBuilder sb = new StringBuilder();
                for (String line = r.readLine(); line != null; line = r.readLine())
                    sb.append(line);
                rawResponse = sb.toString();
                is.close();
                //httpEntity.consumeContent();
            }
        } catch(Exception e) {
            Debug.log(TAG, "cm exception:" + e.getMessage());
            for (StackTraceElement st : e.getStackTrace())
                Debug.log(TAG, "cm trace: " + st.toString());
            if (httpPost != null && !httpPost.isAborted())
                httpPost.abort();
        }

        return rawResponse;
    }
    //---------------------------------------------------------------------------
    private ApiResponse reAuth(Context context,AndroidHttpClient httpClient,HttpPost httpPost,ApiRequest request) {
        Debug.log(this, "reAuth");

        AuthToken token = new AuthToken(context);
        AuthRequest authRequest = new AuthRequest(context);
        authRequest.platform = token.getSocialNet();
        authRequest.sid = token.getUserId();
        authRequest.token = token.getTokenKey();

        String rawResponse = Static.EMPTY;
        ApiResponse response = null;
        HttpPost localHttpPost = null;

        try {
            localHttpPost = new HttpPost(Static.API_URL);
            localHttpPost.setHeader("Accept-Encoding", "gzip");
            localHttpPost.setHeader("Content-Type", "application/json");
            localHttpPost.setEntity(new ByteArrayEntity(authRequest.toString().getBytes("UTF8")));

            Debug.log(TAG, "cm_reauth:req0:" + authRequest.toString());
            rawResponse = request(httpClient, localHttpPost); // REQUEST
            Debug.log(TAG, "cm_reauth:resp0:" + rawResponse);

            response = new ApiResponse(rawResponse);
            if (response.code == ApiResponse.RESULT_OK) {
                Auth auth = Auth.parse(response);
                Data.saveSSID(context, auth.ssid);
                request.ssid = auth.ssid;
                Debug.log(TAG, "cm_reauth:req1:" + request.toString());
                httpPost.setEntity(new ByteArrayEntity(request.toString().getBytes("UTF8")));
                rawResponse = request(httpClient, httpPost);
                Debug.log(TAG, "cm_reauth:resp1:" + rawResponse);
                response = new ApiResponse(rawResponse);
            } else
                Data.removeSSID(context);
        } catch(Exception e) {
            Debug.log(TAG, "cm_reauth exception:" + e.toString());
        }

        return response;
    }
    //---------------------------------------------------------------------------
    public void sendRequestNew(final ApiRequest apiRequest) {
        mWorker.execute(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                String rawResponse = Static.EMPTY;
                try {
                    apiRequest.ssid = Data.SSID;

                    Debug.log(TAG, "s_req::" + apiRequest.toString()); // REQUEST

                    socket = new Socket();
                    socket.connect(new InetSocketAddress("46.182.29.182", 80), 5000);
                    socket.setKeepAlive(false);
                    socket.setSoTimeout(10000);

                    String path = "/?v=1";
                    BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
                    byte[] buffer = apiRequest.toString().getBytes("UTF8");
                    output.write("POST " + path + " HTTP/1.0\r\n");
                    output.write("Content-Length: " + buffer.length + "\r\n");
                    output.write("Content-Type: application/json\r\n");
                    output.write("\r\n");
                    output.write(apiRequest.toString());
                    output.flush();
                    //output.close();

                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    boolean reading = false;
                    for (line = input.readLine(); line != null; line = input.readLine()) {
                        if (line.equals("") || reading == true) {
                            reading = true;
                            sb.append(line);
                        }
                    }
                    rawResponse = sb.toString();
                    //input.close();

                    Debug.log(TAG, "s_resp::" + rawResponse); // RESPONSE

                } catch(Exception e) {
                    rawResponse = e.toString();

                    Debug.log(TAG, "s_exception:" + e.getMessage());
                    for (StackTraceElement st : e.getStackTrace())
                        Debug.log(TAG, "s_trace: " + st.toString());
                } finally {

                    try {
                        socket.shutdownInput();
                        socket.shutdownOutput();
                        socket.close();
                    } catch(IOException e1) {
                        Debug.log(TAG, "s_exception CLOSE:" + e1.getMessage());
                    }

                    if (apiRequest.handler != null) {
                        ApiResponse apiResponse = new ApiResponse(rawResponse);
                        if (apiResponse.code == ApiResponse.SESSION_NOT_FOUND)
                            apiResponse = reAuthNew(apiRequest.context, apiRequest);
                        apiRequest.handler.response(apiResponse);
                    }
                    try {
                        if (socket != null && !socket.isClosed())
                            socket.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    //---------------------------------------------------------------------------
    private ApiResponse reAuthNew(Context context,ApiRequest request) {
        Debug.log(this, "reAuth");
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
        HttpPost httpPost = new HttpPost(Static.API_URL);
        httpPost.addHeader("Accept-Encoding", "gzip");
        httpPost.setHeader("Content-Type", "application/json");

        AuthToken token = new AuthToken(context);
        AuthRequest authRequest = new AuthRequest(context);
        authRequest.platform = token.getSocialNet();
        authRequest.sid = token.getUserId();
        authRequest.token = token.getTokenKey();
        
        String rawResponse = Static.EMPTY;
        ApiResponse response = null;

        try {
            final HttpPost localHttpPost = new HttpPost(Static.API_URL);
            localHttpPost.addHeader("Accept-Encoding", "gzip");
            localHttpPost.setHeader("Content-Type", "application/json");
            try {
                localHttpPost.setEntity(new ByteArrayEntity(authRequest.toString().getBytes("UTF8")));
            } catch(Exception e) {
            }
    
            rawResponse = request(httpClient, localHttpPost);
            response = new ApiResponse(rawResponse);
            if (response.code == ApiResponse.RESULT_OK) {
                Auth auth = Auth.parse(response);
                Data.saveSSID(context, auth.ssid);
                request.ssid = auth.ssid;
                httpPost.setEntity(new ByteArrayEntity(request.toString().getBytes("UTF8")));
                rawResponse = request(httpClient, httpPost);
                response = new ApiResponse(rawResponse);
            } else
                Data.removeSSID(context);
        } catch (Exception e) {
        }
        Debug.log(TAG, "cm_reauth::" + rawResponse); // RESPONSE
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
                //entity.consumeContent();
            }
            entity = null;
        } catch(Exception e) {
            if (httpGet != null && !httpGet.isAborted())
                httpGet.abort();
        }
        return bitmap;
    }
    //---------------------------------------------------------------------------
    @Override
    protected void finalize() throws Throwable {
        if (mHttpClient != null)
            mHttpClient.close();/* mHttpClient.getConnectionManager().shutdown(); */
        super.finalize();
    }
    //---------------------------------------------------------------------------
}
