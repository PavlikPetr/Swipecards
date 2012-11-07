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
import com.topface.topface.ui.BanActivity;
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
    private boolean doNeedResend = true;
    // Constants
    public static final String TAG = "CM";
    public static final int WAITING_TIME = 2000;
    public static final String BAN_RESPONSE = "ban_response";

    private ConnectionManager() {
        mWorker = Executors.newFixedThreadPool(2);
        mDelayedRequestsThreads = new LinkedList<Thread>();
    }


    public static ConnectionManager getInstance() {
        if (mInstanse == null)
            mInstanse = new ConnectionManager();
        return mInstanse;
    }


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
                    String requestString = apiRequest.toString();
                    httpPost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

                    Debug.logJson(TAG, "REQUEST >>> " + Static.API_URL + " rev:" + getRevNum(), requestString);
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
                        if (apiResponse.code == ApiResponse.BAN) {
//                            sendBroadcastBanned(apiRequest,apiResponse);
                            Intent intent = new Intent(apiRequest.context, BanActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(BanActivity.BANNING_INTENT, apiResponse.jsonResult.get("message").toString());
                            apiRequest.context.startActivity(intent);
                            apiRequest.handler.fail(apiResponse.code, apiResponse);
                        } else if (apiResponse.code == ApiResponse.NULL_RESPONSE || apiResponse.code == ApiResponse.WRONG_RESPONSE) {
                            if (doNeedResend) {
                                apiRequest.handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendRequest(apiRequest);
                                    }
                                }, WAITING_TIME);
                                doNeedResend = false;
                            } else {
                                apiRequest.handler.response(apiResponse);
                                doNeedResend = true;
                            }
                        } else {
                            apiRequest.handler.response(apiResponse);
                        }
                    }

                } catch (Exception e) {
                    Debug.error(TAG + "::REQUEST::ERROR ===\n", e);
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
                Debug.logJson(TAG, "REAUTH REQUEST >>> " + Static.API_URL + " rev:" + getRevNum(), request.toString());
                httpPost.setEntity(new ByteArrayEntity(request.toString().getBytes("UTF8")));
                rawResponse = request(httpClient, httpPost);
                Debug.logJson(TAG, "REAUTH RESPONSE <<<", rawResponse);
                response = new ApiResponse(rawResponse);
            } else {
                Data.removeSSID(context);
            }
        } catch (Exception e) {
            Debug.log(TAG, "cm_reauth exception:" + e.toString());
        }

        return response;
    }

    private String getRevNum() {
        return App.isDebugMode() ? Static.REV : "";
    }

    /**
     * Добавляет в http запрос куки с номером ревизии для тестирования беты
     *
     * @param httpPost запрос к которому будет добавлен заголовок
     */
    private void setRevisionHeader(HttpPost httpPost) {
        String rev = getRevNum();
        if (rev != null && rev.length() > 0) {
            httpPost.setHeader("Cookie", "revnum=" + rev + ";");
        }
    }

    private void sendBroadcastReauth(Context context) {
        Intent intent = new Intent();
        intent.setAction(ReAuthReceiver.REAUTH_INTENT);
        context.sendBroadcast(intent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendBroadcastBanned(ApiRequest request, ApiResponse response) {
        Intent intent = new Intent();
        intent.setAction(BAN_RESPONSE);
        try {
            intent.putExtra(BanActivity.MESSAGE, response.jsonResult.get("message").toString());
            LocalBroadcastManager.getInstance(request.context).sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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


    public synchronized void notifyDelayedRequests() {
        for (Thread mDelayedRequestsThread : mDelayedRequestsThreads) {
            try {
                mDelayedRequestsThread.notify();
            } catch (Exception ex) {
                Debug.log(ex.toString());
            }
        }

        mDelayedRequestsThreads.clear();
    }
}
