package com.topface.topface.utils.http;

import android.os.Build;
import com.topface.topface.utils.Base64;
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

    public static final int HTTP_TIMEOUT = 40 * 1000;
    public static final int BUFFER_SIZE = 8192;
    private static final String TAG = "Http";
    private static String mUserAgent;

    //Параметры соединения
    public static final int CONNECT_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 40000;
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
            connection.setRequestProperty("connection", "close");
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
        //Устанавливаем длину данных
        connection.setFixedLengthStreamingMode(requestData.length);

        //Отправляем данные
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(requestData);
        outputStream.close();
    }

    /**
     * Проверяет что код ответа от сервера верный и можно получать данные
     *
     * @param code код ответа от сервер
     * @throws IOException
     */
    public static boolean isCorrectResponseCode(int code) throws IOException {
        return code >= 200 && code < 400;
    }

    public static String getUserAgent() {
        if (mUserAgent == null) {
            final Locale locale = Locale.getDefault();
            mUserAgent = String.format(
                    USER_AGENT_APP_NAME + "/%s (%s); %s-%s",
                    Utils.getClientVersion(),
                    Utils.getClientOsVersion(),
                    locale.getLanguage(),
                    locale.getCountry()
            );
        }

        return mUserAgent;
    }

    public static String httpDataRequest(String request, String postParams, String data) {
        String response = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader buffReader = null;
        HttpURLConnection httpConnection = null;

        try {
            Debug.log(TAG, "enter");
            // запрос
            httpConnection = (HttpURLConnection) new URL(request).openConnection();
            httpConnection.setConnectTimeout(HTTP_TIMEOUT);
            httpConnection.setReadTimeout(HTTP_TIMEOUT);
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setRequestProperty("Content-Type", "application/json");

            //httpConnection.connect();

            Debug.log(TAG, "req:" + postParams);   // REQUEST

            // отправляем post параметры
            if (postParams != null && data == null) {
                Debug.log(TAG, "begin:");
                out = httpConnection.getOutputStream();
                byte[] buffer = postParams.getBytes("UTF8");
                out.write(buffer);
                out.flush();
                out.close();
                Debug.log(TAG, "end:");
            }

            if (postParams != null && data != null) {
                String lineEnd = "\r\n";
                String twoHH = "--";
                String boundary = "FAfsadkfn23412034aHJSAdnk";
                httpConnection.setRequestProperty("Content-Type", "multipart/mixed; boundary=" + boundary);
                BufferedOutputStream bos = new BufferedOutputStream(httpConnection.getOutputStream());
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHH + boundary);
                dos.writeBytes(lineEnd);
                dos.writeBytes("Content-Disposition: mixed");
                dos.writeBytes(lineEnd);
                dos.writeBytes("Content-Type: application/json");
                dos.writeBytes(lineEnd + lineEnd);
                dos.writeBytes(postParams);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHH + boundary);
                dos.writeBytes(lineEnd);
                dos.writeBytes("Content-Disposition: mixed");
                dos.writeBytes(lineEnd);
                dos.writeBytes("Content-Type: image/jpeg");
                dos.writeBytes(lineEnd + lineEnd);
//                dos.writeBytes(data);
                Base64.encodeFromFileToOutputStream(data, bos);

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHH + boundary + twoHH);
                dos.writeBytes(lineEnd);
                dos.flush();
                dos.close();
//                out.close();
            }

            in = httpConnection.getInputStream();

            // проверяет код ответа сервера и считываем данные
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder responseBuilder = new StringBuilder();
                BufferedInputStream bis = new BufferedInputStream(in = httpConnection.getInputStream());
                byte[] buffer = new byte[1024];
                int n;
                while ((n = bis.read(buffer)) > 0)
                    responseBuilder.append(new String(buffer, 0, n));
                response = responseBuilder.toString();
                bis.close();
            }

            Debug.log(TAG, "resp:" + response);   // RESPONSE
            Debug.log(TAG, "exit");
        } catch (Exception e) {
            Debug.error("HTTP::http exception", e);
        } catch (OutOfMemoryError e) {
            Debug.error("HTTP::OOM ", e);
        } finally {
            try {
                Debug.log(TAG, "disconnect");
                if (in != null) in.close();
                if (out != null) out.close();
                if (buffReader != null) buffReader.close();
                if (httpConnection != null) httpConnection.disconnect();
            } catch (Exception e) {
                Debug.log(TAG, "http closing error:" + e);
            }
        }
        return response;
    }

}
