package com.topface.topface.utils.http;

import android.os.Build;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class HttpUtils {

    public static final String GZIP_ENCODING = "gzip";

    /**
     * Типы HTTP запросов, PUT и т.п. мы не поддерживаем
     */
    public static enum HttpConnectionType {
        POST, GET
    }

    public static final int BUFFER_SIZE = 8192;
    private static String mUserAgent;

    //Параметры соединения
    public static final int CONNECT_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 10000;
    public static final String USER_AGENT_APP_NAME = "Topface";
    public static final String ACCEPT_ENCODING = "gzip,deflate";

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

            if (stream != null) {
                //Создаем BufferReader, что бы упростить себе чтение строк
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new BufferedInputStream(new FlushedInputStream(stream), HttpUtils.BUFFER_SIZE)
                ), HttpUtils.BUFFER_SIZE);

                //Читаем содержимое потока
                StringBuilder sb = new StringBuilder();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    sb.append(line);
                }
                result = sb.toString();

                stream.close();
                reader.close();
            }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            connection.setRequestProperty("Connection", "Keep-Alive");
        } else {
            connection.setRequestProperty("Connection", "close");
        }

        return connection;
    }

    public static HttpURLConnection openGetConnection(String url, String contentType) throws IOException {
        return openConnection(HttpConnectionType.GET, url, contentType);
    }

    public static HttpURLConnection openPostConnection(String url, String contentType) throws IOException {
        return openConnection(HttpConnectionType.POST, url, contentType);
    }

    public static void sendPostData(byte[] requestData, HttpURLConnection connection) throws IOException {
        //Отправляем данные
        if (connection == null) {
            Debug.error("connection is null");
            return;
        }
        OutputStream outputStream = getOutputStream(requestData.length, connection);
        if (outputStream != null) {
            outputStream.write(requestData);
            outputStream.close();
        } else {
            Debug.error("Http.getOutputStream() is null");
        }
    }

    public static OutputStream getOutputStream(int contentLength, HttpURLConnection connection) throws IOException {
        //Устанавливаем длину данных
        if (contentLength > 0) {
            connection.setFixedLengthStreamingMode(contentLength);
        }
        /*else {
            //У нас какие то проблемы с поддержкой сервера
            connection.setChunkedStreamingMode(-1);
        }*/

        return connection.getOutputStream();
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
                    USER_AGENT_APP_NAME + "/%s (%s; %s-%s)",
                    Utils.getClientVersion(),
                    Utils.getClientOsVersion(),
                    locale.getLanguage(),
                    locale.getCountry()
            );
        }

        return mUserAgent;
    }

}
