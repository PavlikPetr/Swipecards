package com.topface.topface.utils.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.topface.topface.Static;
import com.topface.topface.utils.Base64;
import com.topface.topface.utils.Debug;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
    // Constants
    public static final int HTTP_GET_REQUEST = 0;
    public static final int HTTP_POST_REQUEST = 1;
    public static final int HTTP_TIMEOUT = 40 * 1000;
    public static final int BUFFER_SIZE = 8192;
    private static final String TAG = "Http";

    // class FlushedInputStream
    public static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0)
                        break; // we reached EOF
                    else
                        bytesSkipped = 1; // we read one byte
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    } // FlushedInputStream

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String httpGetRequest(String request) {
        return httpRequest(HTTP_GET_REQUEST, request, null, null, null);
    }

    public static String httpPostRequest(String request, String postParams) {
        return httpRequest(HTTP_POST_REQUEST, request, postParams, null, null);
    }

    public static String httpPostDataRequest(String request, String postParams, byte[] dataParams) {
        return httpRequest(HTTP_POST_REQUEST, request, postParams, dataParams, null);
    }
    
    public static String httpPostDataRequest(String request, String postParams, InputStream is) {
        return httpRequest(HTTP_POST_REQUEST, request, postParams, null, is);
    }

    public static String httpRequest(int typeRequest, String url, String postParams, byte[] dataParams, InputStream is) {
        String response = Static.EMPTY;
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection httpConnection = null;

        try {
            Debug.log(TAG, "enter");

            httpConnection = (HttpURLConnection) new URL(url).openConnection();
            httpConnection.setConnectTimeout(HTTP_TIMEOUT);
            httpConnection.setReadTimeout(HTTP_TIMEOUT);
            if (typeRequest == HTTP_POST_REQUEST)
                httpConnection.setRequestMethod("POST");
            else
                httpConnection.setRequestMethod("GET");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            Debug.log(TAG, "req:" + postParams); // REQUEST

            if (typeRequest == HTTP_POST_REQUEST && dataParams != null) {
                httpConnection.setDoOutput(true);
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "0xKhTmLbOuNdArY";
                httpConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                out = httpConnection.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"photo.jpg\"" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg" + lineEnd + lineEnd);
                dos.write(dataParams);
                dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
                dos.flush();
                dos.close();
                //out.close();
            }

            if (typeRequest == HTTP_POST_REQUEST && is != null) {
                httpConnection.setDoOutput(true);
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "0xKhTmLbOuNdArY";
                httpConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                out = httpConnection.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"photo.jpg\"" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg" + lineEnd + lineEnd);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buffer = new byte[1024];
                while (bis.read(buffer) > 0)
                    dos.write(buffer);
                dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
                dos.flush();
                dos.close();
                //out.close();
            }

            //httpConnection.connect();

            if (typeRequest == HTTP_POST_REQUEST && postParams != null && dataParams == null) {
                Debug.log(TAG, "begin:");
                out = httpConnection.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(out, BUFFER_SIZE);
                byte[] buffer = postParams.getBytes("UTF8");
                bos.write(buffer);
                bos.flush();
                bos.close();
                //out.close();
                Debug.log(TAG, "end:");
            }

            //in = httpConnection.getInputStream();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder responseBuilder = new StringBuilder();
                in = httpConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(in, BUFFER_SIZE);
                byte[] buffer = new byte[1024];
                int n;
                while ((n = bis.read(buffer)) > 0)
                    responseBuilder.append(new String(buffer, 0, n));
                response = responseBuilder.toString();
                bis.close();
            }

            Debug.log(TAG, "resp:" + response); // RESPONSE
            Debug.log(TAG, "exit");
        } catch (Exception e) {
            String errorResponse = null;
            try {
                StringBuilder responseBuilder = new StringBuilder();
                BufferedInputStream biStream = new BufferedInputStream(in = httpConnection.getErrorStream(), BUFFER_SIZE);
                byte[] buffer = new byte[1024];
                int n;
                while ((n = biStream.read(buffer)) > 0)
                    responseBuilder.append(new String(buffer, 0, n));
                errorResponse = responseBuilder.toString();
                biStream.close();
            } catch (Exception e1) {
                Debug.log(TAG, "http error:" + e1);
            }
            Debug.log(TAG, "http exception:" + e + "" + errorResponse);
        } finally {
            try {
                Debug.log(TAG, "disconnect");
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (httpConnection != null)
                    httpConnection.disconnect();
            } catch (Exception e) {
                Debug.log(TAG, "http error:" + e);
            }
        }
        return response;
    }

    public static String httpDataRequest(int typeRequest, String request, String postParams, String data) {
        String response = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader buffReader = null;
        HttpURLConnection httpConnection = null;

        try {
            //System.setProperty("http.keepAlive", "false");
            Debug.log(TAG, "enter");
            // запрос
            httpConnection = (HttpURLConnection) new URL(request).openConnection();
            //httpConnection.setUseCaches(false);
            httpConnection.setConnectTimeout(HTTP_TIMEOUT);
            httpConnection.setReadTimeout(HTTP_TIMEOUT);
            if (typeRequest == HTTP_POST_REQUEST)
                httpConnection.setRequestMethod("POST");
            else
                httpConnection.setRequestMethod("GET");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setRequestProperty("Content-Type", "application/json");
            //httpConnection.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
            //httpConnection.setRequestProperty("Connection", "close");
            //httpConnection.setRequestProperty("Connection", "Keep-Alive");
            //httpConnection.setChunkedStreamingMode(0);

            //httpConnection.connect();

            Debug.log(TAG, "req:" + postParams);   // REQUEST

            // отправляем post параметры
            if (typeRequest == HTTP_POST_REQUEST && postParams != null && data == null) {
                Debug.log(TAG, "begin:");
                out = httpConnection.getOutputStream();
                byte[] buffer = postParams.getBytes("UTF8");
                out.write(buffer);
                out.flush();
                out.close();
                Debug.log(TAG, "end:");
            }

            if (typeRequest == HTTP_POST_REQUEST && postParams != null && data != null) {
                String lineEnd = "\r\n";
                String twoHH = "--";
                String boundary = "FAfsadkfn23412034aHJSAdnk";
                httpConnection.setRequestProperty("Content-Type", "multipart/mixed; boundary=" + boundary);
                BufferedOutputStream bos = new BufferedOutputStream(httpConnection.getOutputStream());
                DataOutputStream dos =  new DataOutputStream(bos);
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
