package com.topface.topface.utils.http;

import android.content.Context;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.utils.AuthToken;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.Http.FlushedInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {
    // Data
    private static ConnectionManager mInstanse;
    private AndroidHttpClient mHttpClient;
    private ExecutorService mWorker;
    // Constants
    public static final String TAG = "CM";

    private ConnectionManager() {
        mHttpClient = AndroidHttpClient.newInstance("Android");
        mWorker = Executors.newFixedThreadPool(2);
        //mWorker = Executors.newSingleThreadExecutor();
        //java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        //java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
    }

    public static ConnectionManager getInstance() {
        if (mInstanse == null)
            mInstanse = new ConnectionManager();
        return mInstanse;
    }

    public void sendRequest(final ApiRequest apiRequest) {
        mWorker.execute(new Runnable() {
            @Override
            public void run() {
                String rawResponse;
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
                    rawResponse = request(httpClient, httpPost);     //
                    Debug.log(TAG, "cm_resp::" + rawResponse);        // RESPONSE

                    if (apiRequest.handler != null) {
                        ApiResponse apiResponse = new ApiResponse(rawResponse);
                        if (apiResponse.code == ApiResponse.SESSION_NOT_FOUND)
                            apiResponse = reAuth(apiRequest.context, httpClient, httpPost, apiRequest);
                        apiRequest.handler.response(apiResponse);
                    }

                } catch (Exception e) {
                    Debug.log(TAG, "cm_req exception::" + e.toString());
                    if (httpPost != null && !httpPost.isAborted()) httpPost.abort();
                } finally {
                    if (httpClient != null) {
                        httpClient.close();
                    }
                }
            }
        });
    }

    private String request(AndroidHttpClient httpClient, HttpPost httpPost) {
        String rawResponse = Static.EMPTY;

        try {
            //BasicHttpContext httpContext = new BasicHttpContext();
            HttpResponse httpResponse = httpClient.execute(httpPost/*, httpContext*/);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                InputStream is = AndroidHttpClient.getUngzippedContent(httpEntity);
                BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FlushedInputStream(is), Http.BUFFER_SIZE)));
                StringBuilder sb = new StringBuilder();
                for (String line = r.readLine(); line != null; line = r.readLine())
                    sb.append(line);
                rawResponse = sb.toString();
                is.close();
                //httpEntity.consumeContent();
            }
        } catch (Exception e) {
            Debug.error("ConnectionManager exception", e);
            if (httpPost != null && !httpPost.isAborted()) httpPost.abort();
        }

        return rawResponse;
    }

    private ApiResponse reAuth(Context context, AndroidHttpClient httpClient, HttpPost httpPost, ApiRequest request) {
        Debug.log(this, "reAuth");

        AuthToken token = new AuthToken(context);
        AuthRequest authRequest = new AuthRequest(context);
        authRequest.platform = token.getSocialNet();
        authRequest.sid = token.getUserId();
        authRequest.token = token.getTokenKey();

        String rawResponse;
        ApiResponse response = null;
        HttpPost localHttpPost;

        try {
            localHttpPost = new HttpPost(Static.API_URL);
            localHttpPost.setHeader("Accept-Encoding", "gzip");
            localHttpPost.setHeader("Content-Type", "application/json");
            localHttpPost.setEntity(new ByteArrayEntity(authRequest.toString().getBytes("UTF8")));

            Debug.log(TAG, "cm_reauth:req_0:" + authRequest.toString());
            rawResponse = request(httpClient, localHttpPost);  // REQUEST
            Debug.log(TAG, "cm_reauth:resp_0:" + rawResponse);

            response = new ApiResponse(rawResponse);
            if (response.code == ApiResponse.RESULT_OK) {
                Auth auth = Auth.parse(response);
                Data.saveSSID(context, auth.ssid);
                request.ssid = auth.ssid;
                Debug.log(TAG, "cm_reauth:req_1:" + request.toString());
                httpPost.setEntity(new ByteArrayEntity(request.toString().getBytes("UTF8")));
                rawResponse = request(httpClient, httpPost);
                Debug.log(TAG, "cm_reauth:resp_1:" + rawResponse);
                response = new ApiResponse(rawResponse);
            } else
                Data.removeSSID(context);
        } catch (Exception e) {
            Debug.log(TAG, "cm_reauth exception:" + e.toString());
        }

        return response;
    }

    public AndroidHttpClient getHttpClient() {
        return mHttpClient;
    }

    @Override
    protected void finalize() throws Throwable {
        if (mHttpClient != null) mHttpClient.close();/*mHttpClient.getConnectionManager().shutdown();*/
        super.finalize();
    }
}
