package com.topface.topface.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.topface.topface.Data;
import com.topface.topface.Static;
import android.graphics.Bitmap;
import com.topface.topface.imageloader.DefaultImageLoader;

public class Http {
    // Constants
    public static final int HTTP_GET_REQUEST = 0;
    public static final int HTTP_POST_REQUEST = 1;
    public static final int HTTP_TIMEOUT = 45 * 1000;
    public static final int BUFFER_SIZE = 8192; //1024
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

    public static String httpGetRequest(String request) {
        return httpRequest(HTTP_GET_REQUEST, request, null, null, null);
    }

    public static String httpPostRequest(String request, String postParams) {
        return httpRequest(HTTP_POST_REQUEST, request, postParams, null, null);
    }

    public static String httpPostDataRequest(String request, String postParams, byte[] dataParams) {
        return httpRequest(HTTP_POST_REQUEST, request, postParams, dataParams, null);
    }

    public static String httpRequest(int typeRequest, String request, String postParams, byte[] dataParams, InputStream is) {
        String response = Static.EMPTY;
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection httpConnection = null;

        try {
            Debug.log(TAG, "enter");

            httpConnection = (HttpURLConnection) new URL(request).openConnection();
            httpConnection.setConnectTimeout(HTTP_TIMEOUT);
            httpConnection.setReadTimeout(HTTP_TIMEOUT);
            if (typeRequest == HTTP_POST_REQUEST)
                httpConnection.setRequestMethod("POST");
            else
                httpConnection.setRequestMethod("GET");
            httpConnection.setDoInput(true);

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
                dos.writeBytes("Content-Type: image/jpg" + lineEnd + lineEnd);
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
                dos.writeBytes("Content-Type: image/jpg" + lineEnd + lineEnd);
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
                httpConnection.setDoOutput(true);
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
                if (in != null) in.close();
                if (out != null) out.close();
                if (httpConnection != null) httpConnection.disconnect();
            } catch (Exception e) {
                Debug.log(TAG, "http error:" + e);
            }
        }
        return response;
    }

    //---------------------------------------------------------------------------
    public static void avatarOwnerPreloading() {
        if (Data.ownerAvatar == null) {
            DefaultImageLoader.getInstance().preloadImage(CacheProfile.avatar_small, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(Bitmap bitmap) {
                    super.onLoadingComplete(bitmap);
                    Data.ownerAvatar = Utils.getRoundedCornerBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), 12);
                }
            });
        }
    }

    //---------------------------------------------------------------------------
    public static void avatarUserPreloading(final String url) {
        if (Data.userAvatar == null) {
            DefaultImageLoader.getInstance().preloadImage(url, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(Bitmap bitmap) {
                    super.onLoadingComplete(bitmap);
                    Data.ownerAvatar = Utils.getRoundedCornerBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), 12);
                }
            });
        }
    }
}