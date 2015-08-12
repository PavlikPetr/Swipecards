package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.transport.IApiTransport;
import com.topface.topface.requests.transport.MultipartHttpApiTransport;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.debug.HockeySender;
import com.topface.topface.utils.http.HttpUtils;

import org.acra.sender.ReportSenderException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract public class MultipartApiRequest extends ApiRequest {

    public static final int MAX_SUBREQUESTS_NUMBER = 10;
    private String mFrom;

    protected LinkedHashMap<String, IApiRequest> mRequests = new LinkedHashMap<>();
    private volatile MultipartHttpApiTransport mDefaultTransport;

    public MultipartApiRequest(Context context) {
        super(context);
    }

    @Override
    public String getRequestBodyData() {
        return getRequestsAsString() + getMultipartEnding();
    }

    @Override
    public boolean isCanResend() {
        return getResendCounter() < ApiRequest.MAX_RESEND_CNT;
    }

    /**
     * It's enough to check first request beacause AuthRequest has sence only in first place.
     *
     * @return whether request contains AuthRequest or not
     */
    @Override
    public boolean containsAuth() {
        return mRequests.size() > 0 && mRequests.values().iterator().next() instanceof AuthRequest;
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
        StringBuilder name = new StringBuilder("multipart:");
        for (IApiRequest request : mRequests.values()) {
            name.append(":").append(request.getServiceName());
        }
        //имя сервиса нам тоже по умолчанию не нужно
        return name.toString();
    }


    @Override
    public String getContentType() {
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

    public MultipartApiRequest setFrom(String mFrom) {
        this.mFrom = mFrom;
        return this;
    }

    private AuthRequest getAuthRequest() {
        for (Map.Entry<String, IApiRequest> entry : mRequests.entrySet()) {
            IApiRequest request = entry.getValue();
            if (request instanceof AuthRequest) {
                return ((AuthRequest) request);
            }
        }
        return null;
    }

    private void handleAllAbortedRequests() {
        for (Map.Entry<String, IApiRequest> entry : mRequests.entrySet()) {
            ((ApiRequest) entry.getValue())
                    .handleFail(ErrorCodes.EMPTY_REQUEST, "AuthRequest has empty fields");
        }
    }

    @Override
    public void exec() {

        AuthRequest request = getAuthRequest();
        if (request != null && !request.isValidRequest()) {
            Utils.sendHockeyMessage(getContext(), getRequestsAsString());
            handleAllAbortedRequests();
            return;
        }
        // Check number of subrequests. One position is reserved for optional auth request.
        // So maximum allowed number is MAX - 1.
        if (mRequests.size() >= MAX_SUBREQUESTS_NUMBER) {
            throw new RuntimeException("Multiple request with " + mRequests.size() +
                    " subrequests. " + (MAX_SUBREQUESTS_NUMBER - 1) + " is maximum.");
        }
        if (mRequests.size() == 0) {
            Utils.sendHockeyMessage(getContext(), "Empty multipart request sent from : " + mFrom);
            return;
        }
        super.exec();
    }

    private void sendHandlerMessageToRequests(MultipartApiResponse response, boolean onlyCompleted) {
        if (response == null) {
            Debug.error(new IllegalArgumentException("MultipartApiResponse is null"));
            return;
        }
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

    @SuppressWarnings("unused")
    public MultipartApiRequest addRequests(Map<String, IApiRequest> requests) {
        mRequests.putAll(requests);
        return this;
    }

    public MultipartApiRequest addRequests(List<IApiRequest> requests) {
        for (IApiRequest request : requests) {
            mRequests.put(request.getId(), request);
        }
        return this;
    }

    protected String getRequestsAsString() {
        String requestString = "";
        for (IApiRequest request : mRequests.values()) {
            requestString += getPartHeaders(request.getContentType()) + request.getRequestBodyData();
        }
        return requestString;
    }

    public Map<String, IApiRequest> getRequests() {
        return mRequests;
    }

    @Override
    public IApiResponse sendRequestAndReadResponse() throws Exception {
        IApiResponse response = super.sendRequestAndReadResponse();
        if (response != null && response.isCompleted()) {
            sendHandlerMessageToRequests((MultipartApiResponse) response, true);
        }
        return response;
    }

    @Override
    protected IApiTransport getDefaultTransport() {
        if (mDefaultTransport == null) {
            mDefaultTransport = new MultipartHttpApiTransport();
        }
        return mDefaultTransport;
    }

    @Override
    public RequestBuilder intoBuilder(RequestBuilder requestBuilder) {
        return requestBuilder.multipleRequest(this);
    }
}
