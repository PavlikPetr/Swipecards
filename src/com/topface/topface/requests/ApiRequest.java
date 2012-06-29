package com.topface.topface.requests;

import com.topface.topface.Static;
import com.topface.topface.utils.http.ConnectionManager;
import android.content.Context;

public abstract class ApiRequest {
    // Data
    public String ssid;
    public ApiHandler handler;
    public Context context;
    //---------------------------------------------------------------------------
    public ApiRequest(Context context) {
        ssid = Static.EMPTY;
        this.context = context;
    }
    //---------------------------------------------------------------------------
    public ApiRequest callback(ApiHandler handler) {
        this.handler = handler;
        return this;
    }
    //---------------------------------------------------------------------------
    public void exec() {
        ConnectionManager.getInstance().sendRequest(this);
        //ConnectionManager.getInstance().sendRequestNew(this);
        //ConnectionService.sendRequest(mContext,this);
    }
    //---------------------------------------------------------------------------
    public void cancel() {
        handler = null;
        //if(mHttpPost!=null) mHttpPost.abort();
    }
    //---------------------------------------------------------------------------
}

//private static ExecutorService mThreadsPool = Executors.newFixedThreadPool(2);
/*//---------------------------------------------------------------------------
 * public void execOld() {
 * if(mHandler == null) {
 * mHandler.response(new ApiResponse(""));
 * return;
 * }
 * Thread t = new Thread(new Runnable() {
 * //mThreadsPool.execute(new Runnable() {
 * 
 * @Override
 * public void run() {
 * String rawResponse = null;
 * ApiResponse response = null;
 * ApiRequest.this.ssid = Data.SSID;
 * 
 * rawResponse = Http.httpTPRequest(Static.API_URL, ApiRequest.this.toString());
 * //rawResponse = Http.httpRequest(Http.HTTP_POST_REQUEST, Static.API_URL,
 * ApiRequest.this.toString(), null,null);
 * 
 * response = new ApiResponse(rawResponse);
 * if(response.code == ApiResponse.SESSION_NOT_FOUND) {
 * response = reAuth();
 * if(response.code == ApiResponse.SESSION_NOT_FOUND) {
 * Data.removeSSID(mContext);
 * mContext.startActivity(new
 * Intent(mContext.getApplicationContext(),MainActivity.class));
 * }
 * } else if(response.code != ApiResponse.RESULT_OK) {
 * 
 * rawResponse = Http.httpTPRequest(Static.API_URL, ApiRequest.this.toString());
 * //rawResponse = Http.httpRequest(Http.HTTP_POST_REQUEST, Static.API_URL,
 * ApiRequest.this.toString(), null,null);
 * 
 * response = new ApiResponse(rawResponse);
 * if(response.code == ApiResponse.SESSION_NOT_FOUND) {
 * response = reAuth();
 * if(response.code == ApiResponse.SESSION_NOT_FOUND) {
 * Data.removeSSID(mContext);
 * mContext.startActivity(new
 * Intent(mContext.getApplicationContext(),MainActivity.class));
 * }
 * }
 * }
 * if(mHandler!=null)
 * mHandler.response(response);
 * // rawResponse = Http.openUrl(Static.API_URL,ApiRequest.this.toString());
 * }
 * });
 * t.setDaemon(true);
 * t.start();
 * //});
 * } */
/*//---------------------------------------------------------------------------
 * public void execJ() {
 * if(mHandler==null) {
 * mHandler.response(new ApiResponse(""));
 * return;
 * }
 * Thread t = new Thread(new Runnable() {
 * 
 * @Override
 * public void run() {
 * Debug.log(TAG,"req:2:"+ApiRequest.this.toString()); // REQUEST
 * ApiRequest.this.ssid = Data.SSID;
 * URL url;
 * HttpURLConnection connection = null;
 * try {
 * //Create connection
 * url = new URL(Static.API_URL);
 * connection = (HttpURLConnection)url.openConnection();
 * connection.addRequestProperty("Content-Type", "application/json");
 * connection.setRequestMethod("POST");
 * //connection.setRequestProperty("Content-Type",
 * "application/x-www-form-urlencoded");
 * 
 * connection.setRequestProperty("Content-Length", ""
 * +Integer.toString(ApiRequest.this.toString().getBytes().length));
 * connection.setRequestProperty("Content-Language", "en-US");
 * 
 * connection.setUseCaches(false);
 * connection.setDoInput(true);
 * connection.setDoOutput(true);
 * //Send request
 * DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
 * wr.write(ApiRequest.this.toString().getBytes("UTF8"));
 * wr.flush();
 * wr.close();
 * 
 * //Get Response
 * InputStream is = connection.getInputStream();
 * BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 * String line;
 * StringBuffer response = new StringBuffer();
 * while((line = rd.readLine()) != null) {
 * response.append(line);
 * response.append('\r');
 * }
 * rd.close();
 * 
 * if(mHandler!=null) {
 * ApiResponse apiResponse = new ApiResponse(response.toString());
 * if(apiResponse.code == ApiResponse.SESSION_NOT_FOUND)
 * apiResponse = reAuth();
 * mHandler.response(apiResponse);
 * }
 * Debug.log(TAG,"resp:2:" + response.toString()); // RESPONSE
 * } catch (Exception e) {
 * } finally {
 * if(connection != null) {
 * connection.disconnect();
 * }
 * }
 * }
 * });
 * t.setDaemon(true);
 * t.start();
 * } */
/*//---------------------------------------------------------------------------
 * public void exec() {
 * if(mHandler==null) {
 * mHandler.response(new ApiResponse(Static.EMPTY));
 * return;
 * }
 * Thread t = new Thread(new Runnable() {
 * 
 * @Override
 * public void run() {
 * Debug.log(TAG,"req::"+ApiRequest.this.toString()); // REQUEST
 * String rawResponse = Static.EMPTY;
 * ApiRequest.this.ssid = Data.SSID;
 * 
 * isWorking = true;
 * 
 * //httpPost.addHeader("Accept-Encoding", "gzip");
 * mHttpPost = new HttpPost(Static.API_URL);
 * mHttpPost.addHeader("Content-Type", "application/json");
 * mHttpPost.setHeader("User-Agent", "Android");
 * mHttpPost.setHeader("Accept",
 * "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,* / *;q=0.5"
 * );
 * mHttpPost.setHeader("Content-Type", "application/json");
 * 
 * HttpParams httpParams = new BasicHttpParams();
 * HttpConnectionParams.setConnectionTimeout(httpParams, Http.HTTP_TIMEOUT);
 * HttpConnectionParams.setSoTimeout(httpParams, Http.HTTP_TIMEOUT);
 * HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
 * 
 * SchemeRegistry schemeRegistry = new SchemeRegistry();
 * schemeRegistry.register(new
 * Scheme("http",PlainSocketFactory.getSocketFactory(), 80));
 * mHttpClient = new DefaultHttpClient(new
 * ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
 * mHttpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
 * CookiePolicy.RFC_2109);
 * mLocalContext = new BasicHttpContext();
 * try {
 * mHttpPost.setEntity(new
 * ByteArrayEntity(ApiRequest.this.toString().getBytes("UTF8")));
 * HttpResponse httpResponse = mHttpClient.execute(mHttpPost,mLocalContext);
 * HttpEntity entity = httpResponse.getEntity();
 * if(entity != null) {
 * rawResponse = EntityUtils.toString(entity);
 * }
 * } catch (SocketTimeoutException e1) {
 * StackTraceElement[] q = e1.getStackTrace();
 * for(StackTraceElement st:q)
 * Debug.log(TAG,"Socket stack apireq: "+st.toString());
 * Debug.log(TAG,"SocketTimeoutException: "+e1.getMessage());
 * } catch (Exception e) {
 * Debug.log(TAG,"tp server request:"+e.getMessage());
 * } finally {
 * isWorking = false;
 * mHttpPost.abort();
 * mHttpClient.getConnectionManager().shutdown();
 * }
 * 
 * if(mHandler!=null) {
 * ApiResponse apiResponse = new ApiResponse(rawResponse);
 * if(apiResponse.code == ApiResponse.SESSION_NOT_FOUND)
 * apiResponse = reAuth();
 * mHandler.response(apiResponse);
 * }
 * 
 * Debug.log(TAG,"resp::" + rawResponse); // RESPONSE
 * }
 * });
 * t.setDaemon(true);
 * t.start();
 * } */

/*public void exec() {
 * if(mHandler==null) {
 * mHandler.response(new ApiResponse(Static.EMPTY));
 * return;
 * }
 * Thread t = new Thread(new Runnable() {
 * 
 * @Override
 * public void run() {
 * Debug.log(TAG,"req_next::"+ApiRequest.this.toString()); // REQUEST
 * String rawResponse = Static.EMPTY;
 * ApiRequest.this.ssid = Data.SSID;
 * isWorking = true;
 * try {
 * mHttpClient = AndroidHttpClient.newInstance("Android");
 * mHttpPost = new HttpPost(Static.API_URL);
 * mHttpPost.addHeader("Accept-Encoding", "gzip");
 * mHttpPost.addHeader("Content-Type", "application/json");
 * mHttpPost.setHeader("Content-Type", "application/json");
 * mHttpPost.setEntity(new
 * ByteArrayEntity(ApiRequest.this.toString().getBytes("UTF8")));
 * BasicHttpContext localContext = new BasicHttpContext();
 * HttpResponse httpResponse = mHttpClient.execute(mHttpPost, localContext);
 * HttpEntity entity = httpResponse.getEntity();
 * if(entity != null) {
 * //rawResponse = EntityUtils.toString(entity);
 * StringBuilder sb = new StringBuilder();
 * InputStream is = AndroidHttpClient.getUngzippedContent(entity);
 * BufferedInputStream bis = new BufferedInputStream(new FlushedInputStream(is),
 * 8192);
 * BufferedReader r = new BufferedReader(new InputStreamReader(bis));
 * for(String line = r.readLine(); line != null; line = r.readLine())
 * sb.append(line);
 * rawResponse = sb.toString();
 * is.close();
 * entity.consumeContent();
 * Debug.log(TAG,"resp_next::" + rawResponse); // RESPONSE
 * if(mHttpClient!=null) mHttpClient.close();
 * }
 * } catch(Exception e) {
 * Debug.log(TAG,"server request:"+e.getMessage());
 * for(StackTraceElement st : e.getStackTrace())
 * Debug.log(TAG,"api trace: " + st.toString());
 * if(mHttpPost!=null) mHttpPost.abort();
 * if(mHttpClient!=null) mHttpClient.close();
 * } finally {
 * isWorking = false;
 * }
 * 
 * if(mHandler != null) {
 * ApiResponse apiResponse = new ApiResponse(rawResponse);
 * if(apiResponse.code == ApiResponse.SESSION_NOT_FOUND)
 * apiResponse = reAuth();
 * 
 * mHandler.response(apiResponse);
 * }
 * }
 * });
 * t.setDaemon(true);
 * t.start(); */
