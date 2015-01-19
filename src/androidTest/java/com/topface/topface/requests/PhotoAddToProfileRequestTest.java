package com.topface.topface.requests;

import android.net.Uri;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.AddedPhoto;

import java.io.File;

/**
 * Created by kirussell on 19/01/15.
 * Photo Add to Profile
 */
public class PhotoAddToProfileRequestTest extends PhotoRequestsTestBase {


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
        new PhotoAddProfileRequest(uri, getContext(), new DebugWriterProgress()).callback(new DataApiHandler<AddedPhoto>() {
            @Override
            protected void success(AddedPhoto data, IApiResponse response) {
                Debug.debug(getTestName(), "request success()");
                assertNotNull(data.getHash());
                assertNotNull(data.getPhoto());
                stopTest();
            }

            @Override
            protected AddedPhoto parseResponse(ApiResponse response) {
                return new AddedPhoto(response);
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
