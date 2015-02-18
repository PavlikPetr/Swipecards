package com.topface.topface.requests;

import android.content.Context;
import android.net.Uri;

import com.topface.topface.App;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.transport.IApiTransport;
import com.topface.topface.requests.transport.PhotoUploadApiTransport;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.IProgressListener;

import org.json.JSONException;
import org.json.JSONObject;

public class PhotoAddRequest extends ApiRequest {
    public static final String SERVICE_NAME = "photo.add";
    /**
     * По логике надо бы слать application/octet-stream (абстрактные бинарные данные, т.к. мы не знаем
     * реального типа картинки и знать не хотим, пусть сервер обрабатывает), но сервер так не умеет
     * поэтому шлем image/jpeg, т.к. его сервер умеет
     */
    public static final String CONTENT_TYPE = "image/jpeg";

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

    public IProgressListener getNotificationUpdater() {
        return mProgressListener;
    }

    public Uri getPhotoUri() {
        return mUri;
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
            EasyTracker.sendEvent(getPlaceForStatistics(), "PhotoAdd", "", 1L);
        } else {
            handleFail(ErrorCodes.MISSING_REQUIRE_PARAMETER, "Need set photo Uri");
        }
    }

    @Override
    public String getApiUrl() {
        return App.getAppConfig().getApiDomain() + "v" + API_VERSION + "/photo-upload/?ssid=" + ssid;
    }

    protected String getPlaceForStatistics() {
        return "Common";
    }

    @Override
    protected IApiTransport getTransport() {
        return new PhotoUploadApiTransport();
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

}
