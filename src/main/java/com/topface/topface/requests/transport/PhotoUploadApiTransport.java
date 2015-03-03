package com.topface.topface.requests.transport;

import android.net.Uri;

import com.nostra13.universalimageloader.utils.IoUtils;
import com.topface.framework.imageloader.BitmapUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.IProgressListener;
import com.topface.topface.utils.IRequestConnectionListener;
import com.topface.topface.utils.RequestConnectionListenerFactory;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class PhotoUploadApiTransport extends HttpApiTransport {

    @Override
    final public IApiResponse sendRequestAndReadResponse(final IApiRequest request) {
        IApiResponse response;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            final PhotoAddRequest photoAddRequest = (PhotoAddRequest) request;
            //Открываем InputStream к файлу который будем отправлять
            String apiUrl = request.getApiUrl();
            Uri photoUri = photoAddRequest.getPhotoUri();
            final IProgressListener notificationUpdater = photoAddRequest.getNotificationUpdater();
            if (photoUri == null) {
                return new ApiResponse(ErrorCodes.MISSING_REQUIRE_PARAMETER, "Photo uri is null");
            }
            inputStream = BitmapUtils.getInputStream(request.getContext(), photoUri);
            int contentLength = inputStream.available();
            Debug.log("File size: " + contentLength);
            final IRequestConnectionListener listener = RequestConnectionListenerFactory.create(request.getServiceName());
            listener.onConnectionStarted();
            HttpURLConnection connection = HttpUtils.openPostConnection(apiUrl, request.getContentType());
            //Листенер для статистики качества связи
            HttpUtils.setContentLengthAndConnect(connection, createConnectionListener(listener), contentLength);
            Debug.log(
                    ConnectionManager.TAG,
                    "REQUEST upload >>> " + apiUrl + " rev:" +
                            "file: " + photoUri.toString() + " with size(bytes): " + contentLength
            );

            outputStream = connection.getOutputStream();
            IoUtils.copyStream(inputStream, outputStream, new IoUtils.CopyListener() {
                @Override
                public boolean onBytesCopied(int current, int total) {
                    if (notificationUpdater != null) {
                        Debug.log("Upload " + current + "/" + total);
                        if (current >= total) {
                            notificationUpdater.onSuccess();
                        }
                    }
                    return true;
                }
            });
            response = readResponse(connection, request);

        } catch (IOException e) {
            response = new ApiResponse(ErrorCodes.CONNECTION_ERROR, e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Debug.error(e);
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        }

        return response;

    }

}
