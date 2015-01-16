package com.topface.topface.requests;

import android.net.Uri;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.Photo;

import java.io.File;

/**
 * Created by kirussell on 16/01/15.
 * Synchronous multiple add-photo requests
 *
 */
public class MultiplePhotoAddRequestsTest extends PhotoRequestsTestBase {

    private static final int MAX_REQUESTS = 100;
    private long mCounter = 1;
    private long mTimeAccumulator = 0L;

    public void testMultiplePhotoAddRequests() throws Throwable {
        final File file = createTestImage();
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                sendPhotoAddRequest(Uri.fromFile(file));
            }
        }, getTestName());
    }

    private void sendPhotoAddRequest(final Uri uri) {
        final long timestamp = System.currentTimeMillis();
        new PhotoAddRequest(uri, getContext(), new DebugWriterProgress()).callback(new DataApiHandler<Photo>() {
            @Override
            protected void success(Photo data, IApiResponse response) {
                Debug.debug(getTestName(), "request " + mCounter + " success()");
            }

            @Override
            protected Photo parseResponse(ApiResponse response) {
                return null;
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Debug.debug(getTestName(), "request " + mCounter + " fail()");
                assertTrue("Request codeError not from server:" + codeError, codeError >= 0);
            }

            @Override
            public void always(IApiResponse response) {
                mTimeAccumulator += System.currentTimeMillis() - timestamp;
                super.always(response);
                if (hasMore()) {
                    sendPhotoAddRequest(uri);
                } else {
                    Debug.debug(getTestName(), "Total time spent(sec):" + mTimeAccumulator/1000L);
                    Debug.debug(getTestName(), "Average time for request(sec):" + mTimeAccumulator/MAX_REQUESTS/1000L);
                    stopTest();
                }
            }
        }).exec();
    }

    private boolean hasMore() {
        return ++mCounter <= MAX_REQUESTS;
    }
}
