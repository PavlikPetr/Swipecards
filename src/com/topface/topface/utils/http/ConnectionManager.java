package com.topface.topface.utils.http;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import com.topface.topface.*;
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
    // Constants
    public static final String TAG = "CM";
    public static final int WAITING_TIME = 2000;
    public static final String BAN_RESPONSE = "ban_response";

    private ConnectionManager() {
        mWorker = Executors.newFixedThreadPool(3);
        mDelayedRequestsThreads = new LinkedList<Thread>();
        //Можно включить полный дебаг всех http заголовков и других данных Http клиента
        //DebugLogConfig.enable();

    }


    public static ConnectionManager getInstance() {
        if (mInstanse == null) {
            mInstanse = new ConnectionManager();
        }
        return mInstanse;
    }


    public RequestConnection sendRequest(final ApiRequest apiRequest) {
        final RequestConnection connection = new RequestConnection();

        // Не посылать запросы пока не истечет время бана за флуд
        if (isBlockedForFlood()) {
            Intent intent = new Intent(apiRequest.context, BanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_FLOOD);
            apiRequest.context.startActivity(intent);
            return null;
        }

        mWorker.submit(new Runnable() {
            @Override
            public void run() {
                String rawResponse;
                AndroidHttpClient httpClient = null;
                HttpPost httpPost = null;
                boolean needResend = false;


                if (apiRequest.canceled) {
                    //Проверяем, что запрос не отменен
                    return;
                }

                connection.setHttpClient(httpClient);
                connection.setHttpPost(httpPost);

                apiRequest.ssid = Data.SSID;

                try {
                    httpClient = AndroidHttpClient.newInstance("Android");
                    httpPost = new HttpPost(Static.API_URL);
                    httpClient.enableCurlLogging("Topface", Log.VERBOSE);
                    httpPost.setHeader("Accept-Encoding", "gzip");
                    httpPost.setHeader("Content-Type", "application/json");
                    setRevisionHeader(httpPost);
                    String requestString = apiRequest.toString();
                    httpPost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

                    Debug.logJson(TAG, "REQUEST >>> " + Static.API_URL + " rev:" + getRevNum(), requestString);
                    rawResponse = request(httpClient, httpPost);
                    Debug.logJson(TAG, "RESPONSE <<<", rawResponse);

                    ApiResponse apiResponse = new ApiResponse(rawResponse);
                    //Если сессия кончилась, то переотправляем запрос авторизации, после этого обрабатываем обычным способом
                    if (apiResponse.code == ApiResponse.SESSION_NOT_FOUND) {
                        apiResponse = reAuth(apiRequest.context, httpClient, httpPost, apiRequest);
                    }
                    //Если даже после переавторизации токен не верный,
                    //то отмечаем запрос как ошибку и ждем переавторизации пользователя
                    if (apiResponse.code == ApiResponse.INVERIFIED_TOKEN) {
                        sendBroadcastReauth(apiRequest.context);
                        addDelayedRequest(apiRequest);
                        apiResponse.code = ApiResponse.ERRORS_PROCCESED;
                    }
                    //Если в результате получили ответ, что забанен, прекращаем обработку, сообщаем об этом
                    if (apiResponse.code == ApiResponse.BAN) {
                        Intent intent = new Intent(apiRequest.context, BanActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_BAN);
                        intent.putExtra(BanActivity.BANNING_TEXT_INTENT, apiResponse.jsonResult.get("message").toString());
                        apiRequest.context.startActivity(intent);
                        //В запрос отправлять ничего не будем, в finally его просто отменим
                    } else if (apiResponse.code == ApiResponse.DETECT_FLOOD) {
                        Intent intent = new Intent(apiRequest.context, BanActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(BanActivity.INTENT_TYPE, BanActivity.TYPE_FLOOD);
                        apiRequest.context.startActivity(intent);
                    } else if (apiResponse.code == ApiResponse.MAINTENANCE && apiRequest.handler != null) {
                        needResend = true;
                        apiRequest.handler.post(new Runnable() {
                            @Override
                            public void run() {
                                RetryDialog retryDialog = new RetryDialog(apiRequest.context, apiRequest);
                                retryDialog.setMessage(apiRequest.context.getString(R.string.general_maintenance));
                                retryDialog.setButton(Dialog.BUTTON_POSITIVE, apiRequest.context.getString(R.string.general_dialog_retry), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        apiRequest.exec();
                                    }
                                });
                                retryDialog.show();
                            }
                        });

                    } else if (apiResponse.code == ApiResponse.NULL_RESPONSE
                            || apiResponse.code == ApiResponse.WRONG_RESPONSE
                            //Если после переавторизации у нас все же не верный ssid, то пробуем все повторить
                            || apiResponse.code == ApiResponse.SESSION_NOT_FOUND) {

                        //Если пришел пустой ответ или пришел какой то мусор, то пробуем переотправить запрос
                        if (apiRequest.isNeedResend() && apiRequest.handler != null) {
                            needResend = true;
                            apiRequest.handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendRequest(apiRequest);
                                }
                            }, WAITING_TIME);
                            int tryCnt = apiRequest.incrementResend();

                            Debug.error("Response error. Try resend #" + tryCnt);

                            //Предварительно проверяем, что есть handler и запрос не отменен
                            // (если отменен, может возникнуть ситуация, когда handler уже не сможет
                            // обработать ответ из-за убитого контекста)
                        } else if (!apiRequest.isCanceled()) {
                            needResend = true;
                            Message msg = new Message();
                            msg.obj = apiResponse;
                            apiRequest.handler.sendMessage(msg);
                        }
                    } else if (!apiRequest.isCanceled()) {
                        Message msg = new Message();
                        msg.obj = apiResponse;
                        apiRequest.handler.sendMessage(msg);
                    }

                } catch (Exception e) {
                    //Мы отлавливаем все ошибки, возникшие при запросе, не хотим что бы приложение падало из-за них
                    Debug.error(TAG + "::REQUEST::ERROR ===\n", e);
                    if (httpPost != null && !httpPost.isAborted()) {
                        httpPost.abort();
                    }
                } finally {
                    if (httpClient != null) {
                        httpClient.close();
                    }
                    if (!needResend) {
                        //Отмечаем запрос отмененным, что бы почистить
                        apiRequest.setFinished();
                    }
                }
            }
        });
        return connection;
    }


    private String request(AndroidHttpClient httpClient, HttpPost httpPost) {
        String rawResponse = Static.EMPTY;

        try {
            //BasicHttpContext httpContext = new BasicHttpContext();
            Debug.log("D_REQUEST::start");
            HttpResponse httpResponse = httpClient.execute(httpPost/* ,
                                                                    * httpContext */);
            Debug.log("D_REQUEST::end");
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                InputStream is = AndroidHttpClient.getUngzippedContent(httpEntity);
                BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FlushedInputStream(is), 8192)));
                StringBuilder sb = new StringBuilder();
                for (String line = r.readLine(); line != null; line = r.readLine())
                    sb.append(line);
                rawResponse = sb.toString();
                is.close();
                r.close();
            }
        } catch (Exception e) {
            Debug.error("ConnectionManager::Exception", e);
            if (httpPost != null && !httpPost.isAborted()) {
                httpPost.abort();
            }
        } catch (OutOfMemoryError e) {
            Debug.error("ConnectionManager:: " + e.toString());
        }

        return rawResponse;
    }


    private ApiResponse reAuth(Context context, AndroidHttpClient httpClient, HttpPost httpPost, ApiRequest request) {
        Debug.log(this, "reAuth");

        String rawResponse;
        ApiResponse response = null;
        HttpPost localHttpPost;
        AuthRequest authRequest = getAuthRequest(context);

        try {
            localHttpPost = new HttpPost(Static.API_URL);
            localHttpPost.setHeader("Accept-Encoding", "gzip");
            localHttpPost.setHeader("Content-Type", "application/json");
            setRevisionHeader(localHttpPost);
            localHttpPost.setEntity(new ByteArrayEntity(authRequest.toString().getBytes("UTF8")));

            Debug.logJson(TAG, "REAUTH REQUEST >>> " + Static.API_URL + " rev:" + getRevNum(), authRequest.toString());
            rawResponse = request(httpClient, localHttpPost); // REQUEST
            Debug.logJson(TAG, "REAUTH RESPONSE <<<", rawResponse);

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
                //Если не удалос залогиниться, сбрасываем ssid целиком и в следующий раз будем авторизовываться
                Data.removeSSID(context);
            }
        } catch (Exception e) {
            Debug.log(TAG, "С exception:" + e.toString());
        }

        return response;
    }

    private AuthRequest getAuthRequest(Context context) {
        AuthToken token = new AuthToken(context);
        AuthRequest authRequest = new AuthRequest(context);
        authRequest.platform = token.getSocialNet();
        authRequest.sid = token.getUserId();
        authRequest.token = token.getTokenKey();
        return authRequest;
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

    private boolean isBlockedForFlood() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        long endTime = preferences.getLong(BanActivity.FLOOD_ENDS_TIME, 0L);
        long now = System.currentTimeMillis();
        return endTime > now;
    }
}
