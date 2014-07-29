package com.topface.topface.utils.http;

import android.os.Build;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class HttpUtils {

    public static final String GZIP_ENCODING = "gzip";
    public static final String LINE_END = "\r\n";
    public static final String TWO_HH = "--";
    public static final int BUFFER_SIZE = 8192;
    //Параметры соединения
    public static final int CONNECT_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 20000;
    public static final String USER_AGENT_APP_NAME = "Topface";
    public static final String ACCEPT_ENCODING = "gzip,deflate";
    private static String mUserAgent;

    public static String httpGetRequest(String url) {
        String result = null;
        try {
            result = readStringFromConnection(
                    openGetConnection(url, null)
            );
        } catch (Exception e) {
            Debug.error(e);
        }
        return result;
    }

    public static String readStringFromConnection(HttpURLConnection connection) throws IOException {
        String result = null;
        //Если подключение не пустое и код ответа правильный
        if (connection != null && isCorrectResponseCode(connection.getResponseCode())) {
            //Если нужно, разархивируем поток из Gzip
            InputStream stream = HttpUtils.getGzipInputStream(connection);
            BufferedReader reader = null;

            if (stream != null) {

                try {
                    //Создаем BufferReader, что бы упростить себе чтение строк
                    reader = new BufferedReader(
                            new InputStreamReader(
                                    new BufferedInputStream(
                                            new FlushedInputStream(stream),
                                            HttpUtils.BUFFER_SIZE
                                    )
                            ),
                            HttpUtils.BUFFER_SIZE
                    );
                    //Читаем содержимое потока
                    StringBuilder sb = new StringBuilder();
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        sb.append(line);
                    }
                    result = sb.toString();
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }

            }
        }
        if (connection != null) {
            connection.disconnect();
        }

        return result;
    }

    public static InputStream getGzipInputStream(HttpURLConnection connection) throws IOException {
        InputStream inputStream = null;
        if (connection != null) {
            String contentEncoding = connection.getContentEncoding();
            inputStream = connection.getInputStream();

            if (contentEncoding != null && contentEncoding.contains(GZIP_ENCODING)) {
                //Раскодируем GZIP (если нужно)
                inputStream = new GZIPInputStream(
                        inputStream
                );
            }
        }
        return inputStream;
    }

    /**
     * Создает новое HttpURLConnection
     *
     * @param type        тип HTTP запроса
     * @param url         url запроса
     * @param contentType тип контента (не обязательно)
     * @return подключение
     * @throws IOException
     */
    public static HttpURLConnection openConnection(HttpConnectionType type, String url, String contentType) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        //Тип запроса
        connection.setRequestMethod(type.toString());

        connection.setDoInput(true);

        if (type == HttpConnectionType.POST) {
            connection.setDoOutput(true);
        }

        if (contentType != null) {
            connection.setRequestProperty("Content-Type", contentType);
        }
        connection.setRequestProperty("Accept-Encoding", ACCEPT_ENCODING);
        connection.setRequestProperty("User-Agent", HttpUtils.getUserAgent());
        connection.setUseCaches(false);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            connection.setRequestProperty("Connection", "close");
        } else {
            connection.setRequestProperty("Connection", "Keep-Alive");
        }

        return connection;
    }

    public static HttpURLConnection openGetConnection(String url, String contentType) throws IOException {
        return openConnection(HttpConnectionType.GET, url, contentType);
    }

    public static HttpURLConnection openPostConnection(String url, String contentType) throws IOException {
        return openConnection(HttpConnectionType.POST, url, contentType);
    }

    public static void setContentLengthAndConnect(HttpURLConnection connection,
                                                  ApiRequest.IConnectionConfigureListener listener,
                                                  int requestDataLength) throws IOException {
        if (requestDataLength > 0) {
            //Устанавливаем длину данных
            connection.setFixedLengthStreamingMode(requestDataLength);
        }
        listener.onConfigureEnd();
        connection.connect();
        listener.onConnectionEstablished();
    }

    public static void sendPostData(byte[] requestData, HttpURLConnection connection) throws IOException {
        //Отправляем данные
        if (connection == null) {
            Debug.error("connection is null");
            return;
        }

        OutputStream outputStream = connection.getOutputStream();
        if (outputStream != null) {
            outputStream.write(requestData);
            outputStream.close();
        } else {
            Debug.error("Http.getOutputStream() is null");
        }
    }

    /**
     * Проверяет что код ответа от сервера верный и можно получать данные
     *
     * @param code код ответа от сервер
     * @throws IOException
     */
    public static boolean isCorrectResponseCode(int code) throws IOException {
        return code >= HttpURLConnection.HTTP_OK && code < HttpURLConnection.HTTP_BAD_REQUEST;
    }

    public static String getUserAgent() {
        if (mUserAgent == null) {
            final Locale locale = Locale.getDefault();
            mUserAgent = String.format(
                    USER_AGENT_APP_NAME + "/%s%s v%d (%s; %s; %s-%s)",
                    BuildConfig.VERSION_NAME,
                    TextUtils.equals(BuildConfig.BUILD_TYPE, "release") ? "" : "-" + BuildConfig.BUILD_TYPE,
                    BuildConfig.VERSION_CODE,
                    BuildConfig.BILLING_TYPE.getClientType(),
                    Utils.getClientOsVersion(),
                    locale.getLanguage(),
                    locale.getCountry()
            );
        }

        return mUserAgent;
    }

    /**
     * Типы HTTP запросов, PUT и т.п. мы не поддерживаем
     */
    public static enum HttpConnectionType {
        POST, GET
    }

}
