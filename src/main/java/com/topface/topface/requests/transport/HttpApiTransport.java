package com.topface.topface.requests.transport;

import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.IRequestConnectionListener;
import com.topface.topface.utils.RequestConnectionListenerFactory;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.HttpUtils;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * HTTP транспорт для API Topface
 */
public class HttpApiTransport implements IApiTransport {

    public static final String TRANSPORT_NAME = "http";

    @Override
    public IApiResponse sendRequestAndReadResponse(IApiRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("Request is null");
        }
        String requestData = request.getRequestBodyData();
        //Формируем свои данные для отправки POST запросом
        if (TextUtils.isEmpty(requestData)) {
            return new ApiResponse(ErrorCodes.EMPTY_REQUEST, "Request data is empty");
        }

        String apiUrl = request.getApiUrl();

        logRequest(requestData, apiUrl);

        final IRequestConnectionListener listener = RequestConnectionListenerFactory.create(request.getServiceName());
        listener.onConnectionStarted();
        HttpURLConnection connection;
        try {
            connection = HttpUtils.openPostConnection(apiUrl, request.getContentType());
        } catch (IOException e) {
            Debug.error(e);
            throw (e);
        }

        ApiRequest.IConnectionConfigureListener connConfListener = createConnectionListener(listener);
        IApiResponse response;
        byte[] requestBytes = requestData.getBytes();
        //Непосредственно пишим данные в подключение
        try {
            if (writeData(connection, requestBytes, connConfListener)) {
                //Возвращаем HTTP статус ответа
                int responseCode = getResponseCode(connection);

                //Проверяем ответ
                if (HttpUtils.isCorrectResponseCode(responseCode)) {
                    //Если код ответа верный, то читаем данные из потока и создаем IApiResponse
                    response = readResponse(connection, request);
                    listener.onConnectionClose();
                } else {
                    //Если не верный, то конструируем соответсвующий ответ
                    response = new ApiResponse(ErrorCodes.WRONG_RESPONSE, "Wrong http response code HTTP/" + responseCode);
                }
            } else {
                response = new ApiResponse(ErrorCodes.EMPTY_REQUEST, "Request data is empty");
            }
        } catch (IOException e) {
            if (connection != null) {
                connection.disconnect();
            }
            Debug.error(e);
            throw (e);
        }

        return response;

    }

    protected ApiRequest.IConnectionConfigureListener createConnectionListener(final IRequestConnectionListener listener) {
        return new ApiRequest.IConnectionConfigureListener() {
            @Override
            public void onConfigureEnd() {
                listener.onConnectInvoked();
            }

            @Override
            public void onConnectionEstablished() {
                listener.onConnectionEstablished();
            }
        };
    }

    protected int getResponseCode(HttpURLConnection connection) {
        int responseCode = -1;
        //Мы не хотим что бы при ошибке получения ответа от сервера происходила внутренняя ошибка,
        //иначе запрос не отправится еще раз
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            Debug.error("Get response code exception: " + e.toString());
        }
        return responseCode;
    }

    public IApiResponse readResponse(HttpURLConnection connection, IApiRequest request) throws IOException {
        IApiResponse response;
        String rawResponse = HttpUtils.readStringFromConnection(connection);
        //Если ответ не пустой, то создаем объект ответа
        if (!TextUtils.isEmpty(rawResponse)) {
            response = new ApiResponse(rawResponse);
        } else {
            response = new ApiResponse(ErrorCodes.NULL_RESPONSE, "Response raw data is empty");
        }
        return response;
    }

    protected boolean writeData(HttpURLConnection connection, byte[] requestData, ApiRequest.IConnectionConfigureListener listener) throws IOException {
        if (requestData.length > 0) {
            //Отправляем наш  POST запрос
            HttpUtils.setContentLengthAndConnect(connection, listener, requestData.length);
            HttpUtils.sendPostData(requestData, connection);
            return true;
        } else {
            return false;
        }
    }

    protected void logRequest(String json, String apiUrl) {
        Debug.logJson(
                ConnectionManager.TAG,
                "REQUEST >>> " + apiUrl,
                json
        );
    }
}
