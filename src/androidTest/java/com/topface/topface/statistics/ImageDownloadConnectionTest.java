package com.topface.topface.statistics;

import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.topface.statistics.android.StatisticsConfiguration;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.RequestConnectionListener;

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
 * Created by kirussell on 27.03.14.
 * Tests json parsing for google products data
 */
public class ImageDownloadConnectionTest extends InstrumentationTestCase {
    String TAG = "TFAndroidTrackerTests";

    private static final int IMAGES_TO_DOWNLOAD = 100;

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

    public void testConnectionToImagesServer() {
        log("UIL connection test");
        RequestConnectionListener listener = getTimeListener();
        InputStream stream;
        for (String url : getListOfImagesUrls(IMAGES_TO_DOWNLOAD)) {
            listener.onConnectionStarted();
            try {
                stream = getStreamFromNetwork(url);
                listener.onConnectionEstablished();
                stream.close();
            } catch (IOException e) {
                log(e.toString());
            }
        }
    }

    public void testPureConnectionToImageServer() {
        log("Pure connection test");
        RequestConnectionListener listener = getTimeListener();
        InputStream stream;
        for (String url : getListOfImagesUrls(IMAGES_TO_DOWNLOAD)) {
            listener.onConnectionStarted();
            try {
                stream = createConnection(url).getInputStream();
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

    private List<String> getListOfImagesUrls(int count) {
        List<String> result = new ArrayList<>(count);
        int i = 0;
        Random random = new Random();
        int sizeOfSourceImagesList = mUrls.size();
        while (i < count) {
            result.add(mUrls.get(random.nextInt(sizeOfSourceImagesList)));
            i++;
        }
        return result;
    }

    protected static final int BUFFER_SIZE = 32 * 1024;
    protected static final int MAX_REDIRECT_COUNT = 5;

    protected InputStream getStreamFromNetwork(String imageUri) throws IOException {
        long start = System.currentTimeMillis();
        HttpURLConnection conn = createConnection(imageUri);

        int redirectCount = 0;
        while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
            conn = createConnection(conn.getHeaderField("Location"));
            redirectCount++;
        }

        log("ConnectionTime=" + (System.currentTimeMillis() - start));
        InputStream imageStream;
        try {
            imageStream = conn.getInputStream();
        } catch (IOException e) {
            // Read all data to allow reuse connection (http://bit.ly/1ad35PY)
            IoUtils.readAndCloseStream(conn.getErrorStream());
            throw e;
        }
        InputStream res = new ContentLengthInputStream(new BufferedInputStream(imageStream, BUFFER_SIZE), conn.getContentLength());
        log("ConnPlusInpStreamTime=" + (System.currentTimeMillis() - start));
        return res;
    }

    protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
        URL urlObj = new URL(encodedUrl);
        log(InetAddress.getByName(urlObj.getHost()).getHostAddress());
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setReadTimeout(20 * 1000);
        return conn;
    }

    protected void log(String msg) {
        Debug.log(TAG, msg);
    }
}
