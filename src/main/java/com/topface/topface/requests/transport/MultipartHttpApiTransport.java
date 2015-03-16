package com.topface.topface.requests.transport;

import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MultipartApiResponse;

import java.io.IOException;
import java.net.HttpURLConnection;

public class MultipartHttpApiTransport extends HttpApiTransport {
    @Override
    public IApiResponse readResponse(HttpURLConnection connection, IApiRequest request) throws IOException {
        //Отпправляем удачные ответы в подзапросы, что бы в случае ошибки одного из запросов не переотправлять остальные
        return new MultipartApiResponse(connection);
    }
}
