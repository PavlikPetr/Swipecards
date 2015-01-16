package com.topface.topface.requests;

import android.net.Uri;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.Photo;

import java.io.File;

/**
 * Created by kirussell on 16/01/15.
 * Test for PhotoAddRequest - loads test image file through api
 *
 */
public class PhotoAddRequestTest extends PhotoRequestsTestBase {

    public void testPhotoAddRequest() throws Throwable {
        final File file = createTestImage();
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                sendPhotoAddRequest(Uri.fromFile(file));
            }
        }, getTestName());
    }

    private void sendPhotoAddRequest(Uri uri) {
        new PhotoAddRequest(uri, getContext(), new DebugWriterProgress()).callback(new DataApiHandler<Photo>() {
            @Override
            protected void success(Photo data, IApiResponse response) {
                Debug.debug(getTestName(), "request success()");
                stopTest();
            }

            @Override
            protected Photo parseResponse(ApiResponse response) {
                return null;
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Debug.debug(getTestName(), "request fail()");
                assertTrue("Request codeError not from server:" + codeError, codeError >= 0);
                stopTest();
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                stopTest();
            }
        }).exec();
    }


}
