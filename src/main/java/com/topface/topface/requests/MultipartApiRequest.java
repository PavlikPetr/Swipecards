package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

abstract public class MultipartApiRequest extends ApiRequest {

    protected LinkedHashMap<String, IApiRequest> mRequests = new LinkedHashMap<>();

    public MultipartApiRequest(Context context) {
        super(context);
    }

    @Override
    protected boolean writeData(HttpURLConnection connection, IConnectionConfigureListener listener) throws IOException {
        //Формируем базовую часть запроса (Заголовки, json данные)
        String requests = getRequestsAsString();
        //Переводим в байты
        byte[] requestsBytes = requests.getBytes();
        //Это просто закрывающие данные запроса с boundary и переносами строк
        byte[] endBytes = getMultipartEnding().getBytes();
        //Считаем общую длинну получившегося запроса
        int contentLength = requestsBytes.length + endBytes.length;
        //Устанавливаем длину данных и Создаем исходящее подключение к серверу
        HttpUtils.setContentLengthAndConnect(connection, listener, contentLength);
        //Открываем поток на запись данных
        OutputStream outputStream = connection.getOutputStream();
        //Записываем наши данные
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
                outputStream
        ));
        dos.write(requestsBytes);
        dos.write(endBytes);
        dos.flush();
        dos.close();
        outputStream.close();
        Debug.logJson(
                ConnectionManager.TAG,
                "MULTIPART REQUEST >>> " +
                        getApiUrl() +
                        " rev:" + App.getAppConfig().getApiRevision(),
                requests
        );
        return true;
    }

    /**
     * Возвращает заголовки для подзапроса
     *
     * @param contentType       тип контента подзапроса
     * @param additionalHeaders дополнительные заголовки
     * @return строка заголовков, после которой должен идти контент подзапроса
     */
    protected String getPartHeaders(String contentType, String... additionalHeaders) {

        return
                HttpUtils.LINE_END +  //Пустая строка перед запросам
                        HttpUtils.TWO_HH + getBoundary() + HttpUtils.LINE_END + //boundary
                        "Content-Disposition: mixed" + HttpUtils.LINE_END + //это смешаный запрос (не паралельный
                        "Content-Type: " + contentType + HttpUtils.LINE_END + //тип содержимого запроса
                        formatHeaders(additionalHeaders) + //Если нужно, добавляем дополнительные заголовки
                        HttpUtils.LINE_END; //Пустая строка перед контентом
    }

    /**
     * Преобразовывает массив заголовков в строку, пригодный для подстановки в запрос формат
     */
    private String formatHeaders(String[] additionalHeaders) {
        return additionalHeaders.length > 0 ?
                TextUtils.join(HttpUtils.LINE_END, additionalHeaders) + HttpUtils.LINE_END
                : "";
    }

    /**
     * Тип основного запроса
     */
    protected abstract String getRequestType();

    /**
     * boundary основного запроса
     */
    abstract protected String getBoundary();

    /**
     * @return Строка, закрывающая множественный запрос
     */
    protected String getMultipartEnding() {
        return HttpUtils.LINE_END + HttpUtils.LINE_END + HttpUtils.TWO_HH + getBoundary() + HttpUtils.TWO_HH + HttpUtils.LINE_END;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        //Нам не нужны данные отдельного запроса, так как мы переопределяем метод writeData
        return null;
    }

    @Override
    public String getServiceName() {
        //имя сервиса нам тоже по умолчанию не нужно
        return null;
    }

    @Override
    protected String getContentType() {
        return "multipart/" + getRequestType() + "; boundary=" + getBoundary();
    }

    @Override
    public void sendHandlerMessage(IApiResponse multipartResponse) {
        //Не обрабатываем ответы с ошибками
        if (multipartResponse instanceof MultipartApiResponse) {
            MultipartApiResponse response = (MultipartApiResponse) multipartResponse;
            //У нас могут остаться запросы с ошибками (удачные мы уже разобрали в методе readResponse)
            //нужно им тоже ответы отправить
            sendHandlerMessageToRequests(response, false);
            //И отправляем ответ в основной листенер запроса (который выполняется после обработки всех вложеных запросов)
            super.sendHandlerMessage(multipartResponse);
        } else {
            super.sendHandlerMessage(multipartResponse);
        }
    }

    private void sendHandlerMessageToRequests(MultipartApiResponse response, boolean onlyCompleted) {
        HashMap<String, ApiResponse> responses = response.getResponses();
        for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
            String key = entry.getKey();
            //В зависимости от параметра onlyCompleted отправляем ответ в только успешные запросы или во все
            boolean needSendHandler = !onlyCompleted || entry.getValue().isCompleted();
            if (needSendHandler && mRequests.containsKey(key)) {
                //Отправляем в handler подзапроса результат
                mRequests.get(key).sendHandlerMessage(entry.getValue());
                //И удаляем из списка запросов
                mRequests.remove(key);
            }
        }
    }

    public MultipartApiRequest addRequest(IApiRequest request) {
        if (request != null) {
            mRequests.put(request.getId(), request);
        }
        return this;
    }

    public MultipartApiRequest addRequests(Map<String, IApiRequest> requests) {
        mRequests.putAll(requests);
        return this;
    }

    protected String getRequestsAsString() {
        String requestString = "";
        for (IApiRequest request : mRequests.values()) {
            requestString += getPartHeaders(CONTENT_TYPE) + request.toPostData();
        }
        return requestString;
    }

    public Map<String, IApiRequest> getRequests() {
        return mRequests;
    }

    @Override
    public IApiResponse readResponse() throws IOException {
        //Multipart запрос читает данные из подключения, мы не получаем данные заранее
        MultipartApiResponse multipartApiResponse = new MultipartApiResponse(mURLConnection);
        //Отпправляем удачные ответы в подзапросы, что бы в случае ошибки одного из запросов не переотправлять остальные
        sendHandlerMessageToRequests(multipartApiResponse, true);
        return multipartApiResponse;
    }


}
