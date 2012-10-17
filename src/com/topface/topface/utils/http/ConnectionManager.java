package com.topface.topface.utils.http;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.topface.topface.App;
import com.topface.topface.Data;
import com.topface.topface.ReAuthReceiver;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.Http.FlushedInputStream;
import com.topface.topface.utils.social.AuthToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {

    // Data
    private static ConnectionManager mInstanse;
    private ExecutorService mWorker;
    private LinkedList<Thread> mDelayedRequestsThreads;
    // Constants
    public static final String TAG = "CM";

    //---------------------------------------------------------------------------
    private ConnectionManager() {
        mWorker = Executors.newFixedThreadPool(2);
        mDelayedRequestsThreads = new LinkedList<Thread>();
    }

    //---------------------------------------------------------------------------
    public static ConnectionManager getInstance() {
        if (mInstanse == null)
            mInstanse = new ConnectionManager();
        return mInstanse;
    }

    //---------------------------------------------------------------------------
    public RequestConnection sendRequest(final ApiRequest apiRequest) {
        final RequestConnection connection = new RequestConnection();
        mWorker.execute(new Runnable() {
            @Override
            public void run() {
                String rawResponse;
                AndroidHttpClient httpClient = null;
                HttpPost httpPost = null;

                if (apiRequest.canceled)
                    return;
                if (apiRequest.handler == null)
                    return;

                connection.setHttpClient(httpClient);
                connection.setHttpPost(httpPost);

                apiRequest.ssid = Data.SSID;

                try {
                    httpClient = AndroidHttpClient.newInstance("Android");
                    httpPost = new HttpPost(Static.API_URL);
                    httpPost.setHeader("Accept-Encoding", "gzip");
                    httpPost.setHeader("Content-Type", "application/json");
                    setRevisionHeader(httpPost);
                    httpPost.setEntity(new ByteArrayEntity(apiRequest.toString().getBytes("UTF8")));

                    Debug.logJson(TAG, "REQUEST >>>", apiRequest.toString());
                    rawResponse = request(httpClient, httpPost);
                    Debug.logJson(TAG, "RESPONSE <<<", rawResponse);

                    if (apiRequest.handler != null) {
                        ApiResponse apiResponse = new ApiResponse(rawResponse);
                        if (apiResponse.code == ApiResponse.SESSION_NOT_FOUND)
                            apiResponse = reAuth(apiRequest.context, httpClient, httpPost, apiRequest);
                        if (apiResponse.code == ApiResponse.INVERIFIED_TOKEN) {
                            sendBroadcastReauth(apiRequest.context);
                            addDelayedRequest(apiRequest);
                            apiResponse.code = ApiResponse.ERRORS_PROCCESED;
                        }

                        apiRequest.handler.response(apiResponse);
                    }

                } catch (Exception e) {
                    Debug.log(TAG, "REQUEST::ERROR ===\n" + e.toString());
                    if (httpPost != null && !httpPost.isAborted())
                        httpPost.abort();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            }
        });
        return connection;
    }

    //---------------------------------------------------------------------------
    private String request(AndroidHttpClient httpClient, HttpPost httpPost) {
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
                r.close();
            }
        } catch (Exception e) {
            Debug.log(TAG, "cm exception:" + e.getMessage());
            for (StackTraceElement st : e.getStackTrace())
                Debug.log(TAG, "cm trace: " + st.toString());
            if (httpPost != null && !httpPost.isAborted())
                httpPost.abort();
        }

        return rawResponse;
    }
    //---------------------------------------------------------------------------

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
            setRevisionHeader(localHttpPost);
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
            } else {
                Data.removeSSID(context);
            }
        } catch (Exception e) {
            Debug.log(TAG, "cm_reauth exception:" + e.toString());
        }

        return response;
    }

    /**
     * Добавляет в http запрос куки с номером ревизии для тестирования беты
     *
     * @param httpPost запрос к которому будет добавлен заголовок
     */
    private void setRevisionHeader(HttpPost httpPost) {
        if (App.isDebugMode()) {
            httpPost.setHeader("Cookie", "revnum=" + Static.REV + ";");
        }
    }

    //---------------------------------------------------------------------------
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    //---------------------------------------------------------------------------
    private void sendBroadcastReauth(Context context) {
        Intent intent = new Intent();
        intent.setAction(ReAuthReceiver.REAUTH_INTENT);
        context.sendBroadcast(intent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //---------------------------------------------------------------------------
    private void addDelayedRequest(final ApiRequest apiRequest) {
        Thread thread = new Thread() {

            public synchronized void run() {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                apiRequest.exec();
            }

        };
        thread.start();

        mDelayedRequestsThreads.add(thread);
    }

    //---------------------------------------------------------------------------
    public synchronized void notifyDelayedRequests() {
        for (Thread mDelayedRequestsThread : mDelayedRequestsThreads) {
            mDelayedRequestsThread.notify();
        }

        mDelayedRequestsThreads.clear();
    }
}
