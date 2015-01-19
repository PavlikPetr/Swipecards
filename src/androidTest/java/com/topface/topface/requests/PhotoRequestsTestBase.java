package com.topface.topface.requests;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.IProgressListener;
import com.topface.topface.utils.config.AppConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kirussell on 16/01/15.
 * Base test with util methods for photo upload
 */
public class PhotoRequestsTestBase  extends AbstractThreadTest {

    private static final String TEST_NAME = "testPhotoRequestExec";
    private String TEST_IMAGE_FILE = Environment.getExternalStorageDirectory()
            + File.separator + "test.jpg";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setTestApiUrl();
    }

    private void setTestApiUrl() {
        AppConfig config = App.getAppConfig();
        config.setApiUrl("http://api-mkrasilnikov.stage.tf/", config.getApiRevision());
    }

    protected File createTestImage() throws IOException {
        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_launcher);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        //you can create a new file name "test.jpg" in sdcard folder.
        File f = new File(TEST_IMAGE_FILE);
        if (f.createNewFile()) {
            //write the bytes in file
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            // remember close de FileOutput
            fo.close();
        }
        bytes.close();
        Debug.debug(TEST_NAME, "Image file for test:" + f.getAbsolutePath());
        return f;
    }

    protected void stopTest() {
        stopTest(TEST_NAME);
    }

    @Override
    protected void stopTest(String testName) {
        super.stopTest(testName);
        restoreApiUrl();
    }

    private void restoreApiUrl() {
        AppConfig config = App.getAppConfig();
        config.setApiUrl("http://api-mkrasilnikov.stage.tf/", config.getApiRevision());
    }

    protected String getTestName() {
        return TEST_NAME;
    }

    protected class DebugWriterProgress implements IProgressListener {
        @Override
        public void onProgress(int percentage) {
            Debug.debug(TEST_NAME, "progress:" + percentage);
        }

        @Override
        public void onSuccess() {
            Debug.debug(TEST_NAME, "progress success");
        }
    }
}
