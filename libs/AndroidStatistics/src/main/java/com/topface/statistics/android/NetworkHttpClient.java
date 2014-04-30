package com.topface.statistics.android;

import android.util.Log;
import com.topface.statistics.INetworkClient;
import com.topface.statistics.IRequestCallback;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by kirussell on 16.04.2014.
 * Http client wich uses HttpUrlConnection to connect
 */
public class NetworkHttpClient implements INetworkClient {
    public static final String POST = "POST";
    public static final String CONTENT_TYPE = "application/json";
    public static final int CONNECT_TIMEOUT = 3000;
    private static final String DEFAULT_URL = "http://topface.com/stats/";
    private static final int THREAD_PULL_SIZE = 3;
    private final ExecutorService mWorker;

    private String mUserAgent;
    private String mUrl;

    public NetworkHttpClient() {
        this(DEFAULT_URL, null);
    }

    public NetworkHttpClient(String url, String userAgent) {
        mWorker = Executors.newFixedThreadPool(THREAD_PULL_SIZE, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            }
        });
        mUrl = url;
        mUserAgent = userAgent == null ? "" : userAgent;
    }

    @Override
    public void sendRequest(final String data, final IRequestCallback callback) {
        mWorker.submit(new Runnable() {
            @Override
            public void run() {
                byte[] buffData = data.getBytes();
                HttpURLConnection connection = getConnection(mUrl, buffData.length);
                if (connection != null) {
                    OutputStream outputStream = null;
                    try {
                        outputStream = connection.getOutputStream();
                        outputStream.write(buffData);
                        outputStream.flush();
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            connection.getInputStream().close();
                        }
                        callback.onSuccess();
                    } catch (IOException e) {
                        Log.e(StatisticsTracker.TAG, e.toString());
                        callback.onFail();
                    } finally {
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                Log.e(StatisticsTracker.TAG, e.toString());
                            }
                        }
                        connection.disconnect();
                    }
                } else {
                    callback.onFail();
                }
                callback.onEnd();
            }
        });
    }

    private HttpURLConnection getConnection(String url, int contentLength) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setFixedLengthStreamingMode(contentLength);
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setRequestProperty("User-Agent", mUserAgent);
            connection.setRequestMethod(POST);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
        } catch (IOException e) {
            Log.e(StatisticsTracker.TAG, e.toString());
        }
        return connection;
    }

    @Override
    public INetworkClient setUrl(String url) {
        mUrl = url;
        return this;
    }

    @Override
    public INetworkClient setUserAgent(String userAgent) {
        mUserAgent = userAgent;
        return this;
    }
}
