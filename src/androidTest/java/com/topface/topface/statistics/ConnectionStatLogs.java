package com.topface.topface.statistics;

import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.topface.framework.utils.Debug;
import com.topface.statistics.android.StatisticsConfiguration;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.Static;
import com.topface.topface.utils.RequestConnectionListener;
import com.topface.topface.utils.http.HttpUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Code to log some connection statistics
 * REGEXP to filter LogCat messages:
 * TFAndroidTracker::\{|TFAndroidTrackerTests::IP|TFAndroidTrackerTests::http|TFAndroidTrackerTests::UIL|TFAndroidTrackerTests::Pure|ConnectionTime|Connection object time|HttpUtils|TFAndroidTrackerTests::URL
 */
public class ConnectionStatLogs extends InstrumentationTestCase {
    String TAG = "TFAndroidTrackerTests";

    private static final int REQUEST_Q = 30;

    private static List<String> mUrls = new ArrayList<>();

    static {
        mUrls.add("https://ii.cdn.tf/u20387738/r640x960/2:fx66r0.jpg");
        mUrls.add("https://ii.cdn.tf/u79731902/r640x960/14:146p31y.jpg");
        mUrls.add("https://ii.cdn.tf/u79731902/r640x960/14:1nex224.jpg");
        mUrls.add("https://ii.cdn.tf/u20387738/r640x960/13:igdwhk.jpg");
        mUrls.add("https://ii.cdn.tf/u79731902/r640x960/13:15ovty6.jpg");
        mUrls.add("https://ii.cdn.tf/u20387738/r640x960/14:ramlom.jpg");
        mUrls.add("https://ii.cdn.tf/u79731902/r640x960/13:1ojkzqg.jpg");
        mUrls.add("https://ii.cdn.tf/u79511748/r640x960/14:1g8n3sw.jpg");
        mUrls.add("https://ii.cdn.tf/u79731902/r640x960/14:1bl20z4.jpg");
        mUrls.add("https://ii.cdn.tf/u77600792/r640x960/15:1rz63pa.jpg");
        mUrls.add("https://ii.cdn.tf/u79780029/r640x960/15:l55p3c.jpg");
        mUrls.add("https://ii.cdn.tf/u79780029/r640x960/13:16v4h8o.jpg");
        mUrls.add("https://ii.cdn.tf/u79162044/r640x960/12:c7jav6.jpg");
        mUrls.add("https://ii.cdn.tf/u77310996/r640x960/9:nbbvy0.jpg");
        mUrls.add("https://ii.cdn.tf/u75214951/r640x960/14:epwteg.jpg");
        mUrls.add("https://ii.cdn.tf/u79611591/r640x960/15:yuxspw.jpg");
        mUrls.add("https://ii.cdn.tf/u79611591/r640x960/6:1c3yohw.jpg");
        mUrls.add("https://ii.cdn.tf/u79271207/r640x960/7:1nfnyom.jpg");
        mUrls.add("https://ii.cdn.tf/u79611591/r640x960/13:qbura6.jpg");
        mUrls.add("https://ii.cdn.tf/u79271207/r640x960/13:1solqnq.jpg");
        mUrls.add("https://ii.cdn.tf/u79271207/r640x960/15:h4oasy.jpg");
        mUrls.add("https://ii.cdn.tf/u70131057/r640x960/3:1gf7e6y.jpg");
        mUrls.add("https://ii.cdn.tf/u70131057/r640x960/15:1axzeig.jpg");
        mUrls.add("https://ii.cdn.tf/u7137916/r640x960/13:yxpkti.jpg");
    }

    private static List<String> mApiUrls = new ArrayList<>();

    static {
        mApiUrls.add(Static.API_URL);
    }

    private StatisticsConfiguration mSourceConfig;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        StatisticsTracker tracker = StatisticsTracker.getInstance();
        mSourceConfig = tracker.getConfiguration();
        tracker.setConfiguration(
                new StatisticsConfiguration(
                        true, 4, 30,
                        "test-android-imgDwn",
                        "localhost"
                )
        );
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        StatisticsTracker.getInstance().setConfiguration(mSourceConfig);
    }

    /**
     * HttpUtils.openConnection)()
     */

    public void testHttpUtilsOpenConnectionCdn() throws IOException {
        log("HttpUtils.openConnection test for ii.cdn.tf");
        processHttpUtilsOpenConn("https://ii.cdn.tf");
        processHttpUtilsOpenConn("http://ii.cdn.tf");
    }

    public void testHttpUtilsOpenConnectionCdnImg() throws IOException {
        log("HttpUtils.openConnection test for http://ii.cdn.tf images");
        processHttpUtilsOpenConn("https://ii.cdn.tf/u33528879/r200x-/10:17v2qq2.jpg");
        processHttpUtilsOpenConn("https://ii.cdn.tf/u79271207/r640x960/15:h4oasy.jpg");
        processHttpUtilsOpenConn("http://ii.cdn.tf/u38392883/c192x192/15:hk1l72.jpg");
    }

    public void testHttpUtilsOpenConnection() throws IOException {
        log("HttpUtils.openConnection test");
        processHttpUtilsOpenConn(Static.API_URL);
    }

    /**
     * Api
     */

    public void testPureConnectionToApiServer() {
        log("Pure connection to API; KeepAlive=true");
        processConnections(new PureStreamFactory().setKeepAlive(false), REQUEST_Q, mApiUrls);
    }

    public void testPureConnectionToApiServerWithoutKeepAlive() {
        log("Pure connection to API; KeepAlive=false");
        processConnections(new PureStreamFactory().setKeepAlive(false), REQUEST_Q, mApiUrls);
    }

    /**
     * Images
     */

    public void testConnectionToImagesServer() {
        log("UIL connection; KeepAlive=true");
        processConnections(new UILStreamFactory().setKeepAlive(true), REQUEST_Q);
    }

    public void testPureConnectionToImageServer() {
        log("Pure connection; KeepAlive=true");
        processConnections(new PureStreamFactory().setKeepAlive(true), REQUEST_Q);
    }

    public void testConnectionToImagesServerWithoutKeepAlive() {
        log("UIL connection; KeepAlive=false");
        processConnections(new UILStreamFactory().setKeepAlive(false), REQUEST_Q);
    }

    public void testPureConnectionToImageServerWithoutKeepAlive() {
        log("Pure connection; KeepAlive=false");
        processConnections(new PureStreamFactory().setKeepAlive(false), REQUEST_Q);
    }

    /**
     * Helper methods
     */

    private void processHttpUtilsOpenConn(String url) throws IOException {
        long start;
        InputStream stream;
        for (int i = 0; i < 10; i++) {
            start = System.currentTimeMillis();
            HttpURLConnection conn = HttpUtils.openGetConnection(url, "application/json");
            if (conn != null) {
                stream = conn.getInputStream();
                log(
                        "URL:" + url +
                                " IP:" + InetAddress.getByName(conn.getURL().getHost()).getHostAddress() +
                                " ConnectionTime=" + Long.toString(System.currentTimeMillis() - start)
                );
                stream.close();
            }
        }
    }

    private void processConnections(IConnectionStreamFactory connFactory, int size) {
        processConnections(connFactory, size, mUrls);
    }

    private void processConnections(IConnectionStreamFactory connFactory, int size, List<String> urlsToChoose) {
        RequestConnectionListener listener = getTimeListener();
        InputStream stream;
        for (String url : getListOfImagesUrls(size, urlsToChoose)) {
            listener.onConnectionStarted();
            try {
                listener.onConnectInvoked();
                stream = connFactory.getStream(url);
                listener.onConnectionEstablished();
                stream.close();
            } catch (IOException e) {
                log(e.toString());
            }
        }
    }

    private RequestConnectionListener getTimeListener() {
        return new RequestConnectionListener("imgDwn") {
            @Override
            protected String getConnTimeVal(long interval) {
                return Long.toString(interval);
            }

            @Override
            protected String getRequestTimeVal(long interval) {
                return Long.toString(interval);
            }
        };
    }

    private List<String> getListOfImagesUrls(int count, List<String> urls) {
        List<String> result = new ArrayList<>(count);
        int i = 0;
        Random random = new Random();
        int sizeOfSourceImagesList = urls.size();
        while (i < count) {
            result.add(urls.get(random.nextInt(sizeOfSourceImagesList)));
            i++;
        }
        return result;
    }

    protected void log(String msg) {
        Debug.log(TAG, msg);
    }

    private interface IConnectionStreamFactory {
        InputStream getStream(String url) throws IOException;
    }

    private class UILStreamFactory extends PureStreamFactory {

        @Override
        public InputStream getStream(String url) throws IOException {
            return getStreamFromNetwork(url, getKeepAlive());
        }

        protected static final int BUFFER_SIZE = 32 * 1024;
        protected static final int MAX_REDIRECT_COUNT = 5;

        protected InputStream getStreamFromNetwork(String imageUri, boolean keepAlive) throws IOException {
            HttpURLConnection conn = createConnection(imageUri, keepAlive);

            int redirectCount = 0;
            while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
                conn = createConnection(conn.getHeaderField("Location"), keepAlive);
                redirectCount++;
            }

            InputStream imageStream;
            try {
                imageStream = conn.getInputStream();
            } catch (IOException e) {
                // Read all data to allow reuse connection (http://bit.ly/1ad35PY)
                IoUtils.readAndCloseStream(conn.getErrorStream());
                throw e;
            }
            return new ContentLengthInputStream(new BufferedInputStream(imageStream, BUFFER_SIZE), conn.getContentLength());
        }
    }

    private class PureStreamFactory implements IConnectionStreamFactory {

        private boolean mKeepAlive;

        @Override
        public InputStream getStream(String url) throws IOException {
            return createConnection(url, getKeepAlive()).getInputStream();
        }

        public IConnectionStreamFactory setKeepAlive(boolean keepAlive) {
            mKeepAlive = keepAlive;
            return this;
        }

        public boolean getKeepAlive() {
            return mKeepAlive;
        }

        protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

        protected HttpURLConnection createConnection(String url, boolean keepAlive) throws IOException {
            String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
            URL urlObj = new URL(encodedUrl);
            log("IP:" + InetAddress.getByName(urlObj.getHost()).getHostAddress() + " URL:" + url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(20 * 1000);
            if (keepAlive) {
                System.setProperty("http.keepAlive", "true");
                conn.setRequestProperty("Connection", "Keep-Alive");
            } else {
                System.setProperty("http.keepAlive", "false");
                conn.setRequestProperty("Connection", "close");
            }
            return conn;
        }
    }
}
