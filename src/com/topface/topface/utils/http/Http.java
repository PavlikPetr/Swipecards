package com.topface.topface.utils.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.BasicHttpContext;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

public class Http {
    // Constants
    public static final int HTTP_GET_REQUEST = 0;
    public static final int HTTP_POST_REQUEST = 1;
    public static final int HTTP_TIMEOUT = 20 * 1000;
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
            while(totalBytesSkipped < n) {
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
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

            httpConnection = (HttpURLConnection)new URL(url).openConnection();
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
                while(bis.read(buffer) > 0)
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
                while((n = bis.read(buffer)) > 0)
                    responseBuilder.append(new String(buffer, 0, n));
                response = responseBuilder.toString();
                bis.close();
            }

            Debug.log(TAG, "resp:" + response); // RESPONSE
            Debug.log(TAG, "exit");
        } catch(Exception e) {
            String errorResponse = null;
            try {
                StringBuilder responseBuilder = new StringBuilder();
                BufferedInputStream biStream = new BufferedInputStream(in = httpConnection.getErrorStream(), BUFFER_SIZE);
                byte[] buffer = new byte[1024];
                int n;
                while((n = biStream.read(buffer)) > 0)
                    responseBuilder.append(new String(buffer, 0, n));
                errorResponse = responseBuilder.toString();
                biStream.close();
            } catch(Exception e1) {
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
            } catch(Exception e) {
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
          Debug.log(TAG,"enter");
          // запрос
          httpConnection = (HttpURLConnection)new URL(request).openConnection();
          //httpConnection.setUseCaches(false);
          httpConnection.setConnectTimeout(HTTP_TIMEOUT);
          httpConnection.setReadTimeout(HTTP_TIMEOUT);
          if(typeRequest==HTTP_POST_REQUEST)
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
          
          Debug.log(TAG,"req:"+postParams);   // REQUEST

          // отправляем post параметры
          if(typeRequest == HTTP_POST_REQUEST && postParams != null && data == null) {
            Debug.log(TAG,"begin:");
            out  = httpConnection.getOutputStream();
            byte[] buffer = postParams.getBytes("UTF8");
            out.write(buffer);
            out.flush();
            out.close();
            Debug.log(TAG,"end:");
          }
          
          if(typeRequest == HTTP_POST_REQUEST && postParams != null && data != null) {
            String lineEnd  = "\n";
            String twoHH  = "--";
            String boundary = "--Asrf456BGe4h";
            httpConnection.setRequestProperty("Content-Type","multipart/mixed; boundary=" + boundary);
            DataOutputStream dos = new DataOutputStream(out = httpConnection.getOutputStream());
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHH+boundary);
            dos.writeBytes(lineEnd);
            dos.writeBytes("Content-Disposition: mixed");
            dos.writeBytes(lineEnd);
            dos.writeBytes("Content-Type: application/json");
            dos.writeBytes(lineEnd+lineEnd);
            dos.writeBytes(postParams);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHH+boundary);
            dos.writeBytes(lineEnd);
            dos.writeBytes("Content-Disposition: mixed");
            dos.writeBytes(lineEnd);
            dos.writeBytes("Content-Type: image/jpg");
            dos.writeBytes(lineEnd+lineEnd);
            /*
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buff = new byte[1024];
            while(bis.read(buff) > 0) {
              dos.write(buff); 
            }
            */
            dos.writeBytes(data);
            
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHH+boundary + "--");
            dos.writeBytes(lineEnd);
            dos.flush();
            dos.close();
            out.close();
          }
          
          in = httpConnection.getInputStream();
          
          // проверяет код ответа сервера и считываем данные
          if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StringBuilder responseBuilder = new StringBuilder();
            BufferedInputStream bis = new BufferedInputStream(in = httpConnection.getInputStream());
            byte[] buffer = new byte[1024];
            int n;
            while((n=bis.read(buffer)) > 0)
              responseBuilder.append(new String(buffer,0,n)); 
            response = responseBuilder.toString();
            bis.close();
          }  
        
          Debug.log(TAG,"resp:" + response);   // RESPONSE
          Debug.log(TAG,"exit");
        } catch(Exception e) {
          Debug.log(TAG,"http exception:" + e);
        } finally {
          try {
            Debug.log(TAG,"disconnect");
            if(in!=null) in.close();
            if(out!=null) out.close();
            if(buffReader!=null) buffReader.close();
            if(httpConnection!=null) httpConnection.disconnect();
          } catch(Exception e) {
            Debug.log(TAG,"http closing error:" + e);
          }
        }
        return response;
      }

    public static String _httpTPRequest(String url,String params) { //не используется
        Debug.log(TAG, "req_next:" + params); // REQUEST

        HttpPost httpPost = null;
        AndroidHttpClient httpClient;
        String rawResponse = Static.EMPTY;

        try {
            httpClient = AndroidHttpClient.newInstance("Android");
            httpPost = new HttpPost(url);
            httpPost.addHeader("Accept-Encoding", "gzip");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new ByteArrayEntity(params.getBytes("UTF8")));

            BasicHttpContext localContext = new BasicHttpContext();
            HttpResponse httpResponse = httpClient.execute(httpPost, localContext);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                StringBuilder sb = new StringBuilder();
                InputStream is = AndroidHttpClient.getUngzippedContent(entity);
                BufferedInputStream bis = new BufferedInputStream(new FlushedInputStream(is), 8192);
                BufferedReader r = new BufferedReader(new InputStreamReader(bis));
                for (String line = r.readLine(); line != null; line = r.readLine())
                    sb.append(line);
                rawResponse = sb.toString();
                r.close();
                entity.consumeContent();
                Debug.log(TAG, "resp_next::" + rawResponse); // RESPONSE
            }
            //if(httpClient!=null) httpClient.close();
        } catch(Exception e) {
            Debug.log(TAG, "server request:" + e.getMessage());
            for (StackTraceElement st : e.getStackTrace())
                Debug.log(TAG, "http trace: " + st.toString());
            if (httpPost != null)
                httpPost.abort();
            //if(httpClient!=null) httpClient.close();
        }

        return rawResponse;
    }

    public static Bitmap bitmapLoader(String url) { // Exp
        if (url == null)
            return null;
        return ConnectionManager.getInstance().bitmapLoader(url);
    }

    public static void bannerLoader(final String url,final ImageView view) {
        Thread t = new Thread() {
            @Override
            public void run() {
              Bitmap bitmap = bitmapLoader(url);
              if(bitmap==null) 
                  return;
              float w = bitmap.getWidth(); 
              float ratio = w/Data.screen_width;
              int height = (int)(bitmap.getHeight()/ratio);
              final Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, Data.screen_width, height, true);
              if(resizedBitmap != null)
                  view.post(new Runnable() {
                      @Override
                      public void run() {
                          Bitmap bitmap = bitmapLoader(url);
                          if (bitmap == null)
                              return;
                          float w = bitmap.getWidth();
                          float ratio = w / Data.screen_width;
                          int height = (int)(bitmap.getHeight() / ratio);
                          final Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, Data.screen_width, height, true);
                          if (resizedBitmap != null)
                              view.post(new Runnable() {
                                  @Override
                                  public void run() {
                                      view.setImageBitmap(resizedBitmap);
                                  }
                              });
                      }});
            }
        };
        
        t.setDaemon(true);
        t.start();
    }

    public static void imageLoader(final String url,final ImageView view) {
        Thread t = new Thread() {
            @Override
            public void run() {
                final Bitmap bitmap = bitmapLoader(url);
                if (bitmap != null)
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImageBitmap(bitmap);
                        }
                    });
            }
        };
        t.setDaemon(true);
        t.start();
    }

    public static void avatarOwnerPreloading() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Data.ownerAvatar != null)
                    return;
                Bitmap ava = Http.bitmapLoader(CacheProfile.getAvatarLink());
                if (ava == null)
                    return;
                ava = Utils.getRoundedBitmap(ava);
                Data.ownerAvatar = ava;
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void avatarUserPreloading(final String url) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap ava = Http.bitmapLoader(url);
                if (ava == null)
                    return;
                ava = Utils.getRoundedBitmap(ava);
                Data.friendAvatar = ava;
            }
        });
        t.setDaemon(true);
        t.start();
    }
}

/* public static Bitmap bitmapLoaderOld(String url) {
 * Bitmap bitmap = null;
 * BufferedInputStream bin = null;
 * try {
 * bin = new BufferedInputStream(new URL(url).openStream(),BUFFER_SIZE);
 * bitmap = BitmapFactory.decodeStream(bin);
 * } catch(Exception e) {
 * Debug.log(TAG,"bitmap loader error");
 * try {
 * if(bin != null)
 * bin.close();
 * } catch(IOException e1) {
 * Debug.log(TAG,"bitmap close loader error");
 * }
 * }
 * return bitmap;
 * } */

/* //---------------------------------------------------------------------------
 * public static Bitmap bitmapLoaderNew(String url) {
 * Bitmap bitmap = null;
 * InputStream is = null;
 * HttpURLConnection conn = null;
 * try {
 * URL aURL = new URL(url);
 * conn = (HttpURLConnection)aURL.openConnection();
 * conn.setConnectTimeout(HTTP_TIMEOUT);
 * conn.setReadTimeout(HTTP_TIMEOUT);
 * conn.connect();
 * is = conn.getInputStream();
 * bitmap = BitmapFactory.decodeStream(new FlushedInputStream(is));
 * is.close();
 * } catch(Exception e) {
 * Debug.log(TAG,"bitmapLoader exception:" + e.getMessage());
 * conn.disconnect();
 * } finally {
 * //if (conn != null) conn.disconnect();
 * if(is != null)
 * try {
 * is.close();
 * } catch(IOException e) {
 * }
 * }
 * return bitmap;
 * } */
/* //---------------------------------------------------------------------------
 * public static String httpTPRequest(String url,String params) {
 * Debug.log(TAG,"req:" + params); // REQUEST
 * 
 * String response = Static.EMPTY;
 * HttpEntity entity = null;
 * HttpPost httpPost = new HttpPost(url);
 * httpPost.addHeader("Content-Type","application/json");
 * //httpPost.addHeader("Accept-Encoding", "gzip");
 * HttpParams httpParams = new BasicHttpParams();
 * DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
 * HttpConnectionParams.setConnectionTimeout(httpParams,HTTP_TIMEOUT);
 * HttpConnectionParams.setSoTimeout(httpParams,HTTP_TIMEOUT);
 * try {
 * httpPost.setEntity(new ByteArrayEntity(params.getBytes("UTF8")));
 * //httpPost.setEntity(new StringEntity(params,"UTF-8"));
 * BasicHttpContext localContext = new BasicHttpContext();
 * HttpResponse httpResponse = httpClient.execute(httpPost,localContext);
 * entity = httpResponse.getEntity();
 * if(entity != null) {
 * response = EntityUtils.toString(entity);
 * entity.consumeContent();
 * }
 * } catch(SocketTimeoutException e1) {
 * StackTraceElement[] q = e1.getStackTrace();
 * for(StackTraceElement st : q)
 * Debug.log(TAG,"Socket stack http: " + st.toString());
 * Debug.log(TAG,"SocketTimeoutException: " + e1.getMessage());
 * } catch(Exception e) {
 * Debug.log(TAG,"tp server request:" + e.getMessage());
 * if(httpPost != null) httpPost.abort();
 * } finally {
 * if(httpClient != null) httpClient.getConnectionManager().shutdown();
 * }
 * 
 * Debug.log(TAG,"resp:" + response); // RESPONSE
 * return response;
 * } */

/***********
 * //---------------------------------------------------------------------------
 * public static Bitmap bitmapLoader(String url) { // Exp
 * if(url == null)
 * return null;
 * final AndroidHttpClient httpClient =
 * AndroidHttpClient.newInstance("Android");
 * final HttpGet httpGet = new HttpGet(url);
 * Bitmap bitmap = null;
 * try {
 * BasicHttpContext localContext = new BasicHttpContext();
 * HttpResponse response = httpClient.execute(httpGet,localContext);
 * final int statusCode = response.getStatusLine().getStatusCode();
 * if(statusCode != HttpStatus.SC_OK)
 * return null;
 * 
 * HttpEntity entity = response.getEntity();
 * if(entity != null) {
 * //final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
 * InputStream is = entity.getContent();
 * bitmap = BitmapFactory.decodeStream(new FlushedInputStream(is));
 * is.close();
 * entity.consumeContent();
 * }
 * //if(httpClient != null) httpClient.close();
 * } catch(Exception e) {
 * //if(httpClient != null) httpClient.close();
 * if(httpGet != null) httpGet.abort();
 * }
 * return bitmap;
 * }
 */
