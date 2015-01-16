package com.topface.topface.requests;

import android.content.Context;
import android.net.Uri;

import com.nostra13.universalimageloader.utils.IoUtils;
import com.topface.framework.imageloader.BitmapUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.IProgressListener;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.HttpUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class PhotoAddRequest extends ApiRequest {
    public static final String SERVICE_NAME = "photo.add";

    private Uri mUri = null;
    private IProgressListener mProgressListener;

    public PhotoAddRequest(Uri uri, Context context) {
        super(context);
        mUri = uri;
    }

    public PhotoAddRequest(Uri uri, Context context, IProgressListener listener) {
        this(uri, context);
        mProgressListener = listener;
    }

    @Override
    protected boolean writeData(HttpURLConnection connection, IConnectionConfigureListener listener) throws IOException {
        //Открываем InputStream к файлу который будем отправлять
        setSsid(ssid);
        InputStream inputStream = BitmapUtils.getInputStream(getContext(), mUri);
        int contentLength = inputStream.available();
        Debug.log("File size: " + contentLength);
        HttpUtils.setContentLengthAndConnect(connection, listener, contentLength);
        if (contentLength > 0) {
            try {
                Debug.logJson(
                        ConnectionManager.TAG,
                        "REQUEST upload >>> " + getApiUrl() + " rev:" + getRevNum(),
                        "file: " + mUri.toString() + " with size(bytes): "+contentLength
                );
                writeRequest(inputStream, connection);
            } finally {
                inputStream.close();
            }
            return true;
        } else {
            return false;
        }
    }

    private void writeRequest(@NotNull InputStream inputStream,
                              HttpURLConnection connection) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = connection.getOutputStream();
            IoUtils.copyStream(inputStream, outputStream, new IoUtils.CopyListener() {
                @Override
                public boolean onBytesCopied(int current, int total) {
                    if (mProgressListener != null) {
                        if (current >= total) {
                            mProgressListener.onSuccess();
                        } else {
                            mProgressListener.onProgress(100 * current / total);
                        }
                    }
                    return true;
                }
            });
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        if (mUri != null) {
            super.exec();
            EasyTracker.sendEvent("Profile", "PhotoAdd", "", 1L);
        } else {
            handleFail(ErrorCodes.MISSING_REQUIRE_PARAMETER, "Need set photo Uri");
        }
    }

    @Override
    protected String getApiUrl() {
        return App.getAppConfig().getApiDomain() + "v" + API_VERSION + "/photo_upload/?ssid=" + ssid;
    }
}
