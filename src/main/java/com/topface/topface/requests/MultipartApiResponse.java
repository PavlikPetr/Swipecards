package com.topface.topface.requests;

import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.multipart.MultipartStream;
import com.topface.topface.utils.http.FlushedInputStream;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс обрабтаывающий ответ сервера и преобразующий его в несколько JSON объектов
 */
public class MultipartApiResponse implements IApiResponse {

    // Data
    public int code = ErrorCodes.RESULT_DONT_SET;
    public String message;
    public JSONObject jsonResult;
    public JSONObject jsonBan;
    private HashMap<String, ApiResponse> mResponses = new HashMap<>();
    private String mLastCompletedRequestId;

    public MultipartApiResponse(int responseCode, String contentType, String body) {
        processResponse(responseCode, contentType, new ByteArrayInputStream(body.getBytes()));
    }

    public MultipartApiResponse(HttpURLConnection connection) {
        try {
            processResponse(connection.getResponseCode(), connection.getContentType(), connection.getInputStream());
        } catch (IOException e) {
            Debug.error(e);
            setError(ErrorCodes.ERRORS_PROCESSED, "Parse response error");
        }
    }

    private void processResponse(int responseCode, String contentType, InputStream inputStream) {
        try {
            //Разбиваем ответ на подответы, в итоге получим массив json объектов
            LinkedList<String> responses = splitResponses(
                    responseCode,
                    contentType,
                    inputStream
            );
            if (responses == null || responses.isEmpty()) {
                if (isCodeEqual(ErrorCodes.RESULT_DONT_SET)) {
                    setError(ErrorCodes.NULL_RESPONSE, "Responses is null");
                }
            } else {
                //Парсим ответы.
                parseResponses(responses);
            }
        } catch (IOException e) {
            Debug.error(e);
            setError(ErrorCodes.NETWORK_CONNECT_ERROR, "Read response error");
        } catch (Exception e) {
            Debug.error(e);
            setError(ErrorCodes.ERRORS_PROCESSED, "Parse response error");
        }
    }

    private void parseResponses(LinkedList<String> parts) throws JSONException {
        code = ErrorCodes.RESULT_OK;
        for (String responseString : parts) {
            if (!TextUtils.isEmpty(responseString)) {
                ApiResponse response = new ApiResponse(responseString);
                mResponses.put(response.id, response);
                if (!response.isCompleted()) {
                    code = response.getResultCode();
                    message = response.getErrorMessage();
                    if (ErrorCodes.BAN == code) {
                        JSONObject jsonObject = new JSONObject(responseString);
                        jsonBan = jsonObject.optJSONObject("error");
                        return;
                    }
                } else {
                    response.setUpdateCountersFlag(false);
                    mLastCompletedRequestId = response.id;
                }
            } else {
                Debug.error("Wrong response part:\n" + responseString);
            }
        }
        //Последнему удачно завершенному разрешаем обновить счетчики
        if (mLastCompletedRequestId != null && mResponses.containsKey(mLastCompletedRequestId)) {
            mResponses.get(mLastCompletedRequestId).setUpdateCountersFlag(true);
        }
    }

    private LinkedList<String> splitResponses(int responseCode, String contentType, InputStream inputStream) throws IOException {
        LinkedList<String> parts = new LinkedList<>();
        //Если подключение не пустое и код ответа правильный
        if (inputStream != null && HttpUtils.isCorrectResponseCode(responseCode)) {
            //Если нужно, разархивируем поток из Gzip
            BufferedInputStream is = new BufferedInputStream(new FlushedInputStream(inputStream), HttpUtils.BUFFER_SIZE);
            String boundary = getBoundary(contentType);
            if (TextUtils.isEmpty(boundary)) {
                //В дебаг режиме еще читаем ответ сервера, что бы понять в чем проблема и куда делся boundary
                if (BuildConfig.DEBUG) {
                    try {
                        Debug.error("Boundary not found in response:\n" + getStringFromInputStream(is));
                    } catch (Exception e) {
                        Debug.error(e);
                    }
                }
                setError(ErrorCodes.WRONG_RESPONSE, "Boundary not found");
                return null;
            }
            MultipartStream multipartStream = new MultipartStream(is, boundary.getBytes());
            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                //NOTE: здесь можно проверять еще и заголовки, но пока и так сойдет
                //Читаем данные ответа
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                multipartStream.readHeaders();
                multipartStream.readBodyData(data);

                //Добавляем в массив объектов, попутно
                parts.add(new String(data.toByteArray()));

                nextPart = multipartStream.readBoundary();
            }
            inputStream.close();
            is.close();
        }

        return parts;
    }

    private String getStringFromInputStream(BufferedInputStream is) throws IOException {
        int ch;
        StringBuilder sb = new StringBuilder();
        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    public HashMap<String, ApiResponse> getResponses() {
        return mResponses;
    }

    private String getBoundary(String response) {
        String boundary = null;
        Pattern boundaryPattern = Pattern.compile("^.*(\\s|;)?boundary=(.+?)?(;|$)");
        Matcher matcher = boundaryPattern.matcher(response);
        if (matcher.matches()) {
            boundary = matcher.group(2);
            if (!TextUtils.isEmpty(boundary)) {
                boundary = boundary.trim();
            }
        }
        return boundary;
    }

    private void setError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public JSONObject getJsonResult() {
        return jsonBan;
    }

    /**
     * Проверяет, является ли этот ответ от сервера ошибокой переданно в параметре errorCode
     */
    public boolean isCodeEqual(Integer... errorCode) {
        if (code == ErrorCodes.RESULT_DONT_SET) {
            //Проверяем по очереди каждый ответ, если есть хоть один такой статус
            for (ApiResponse response : mResponses.values()) {
                if (response.isCodeEqual(errorCode)) {
                    return true;
                }
            }
        } else if (Arrays.asList(errorCode).contains(code)) {
            return true;
        }
        return false;
    }

    /**
     * Проверяет, является ли код ошибки кодом неверной авторизации
     */
    public boolean isWrongAuthError() {
        return isCodeEqual(
                ErrorCodes.UNKNOWN_PLATFORM,
                ErrorCodes.UNKNOWN_SOCIAL_USER,
                ErrorCodes.UNVERIFIED_TOKEN,
                ErrorCodes.INCORRECT_LOGIN,
                ErrorCodes.INCORRECT_PASSWORD
        );
    }

    @Override
    public int getResultCode() {
        return code;
    }

    @Override
    public boolean isCompleted() {
        return isCodeEqual(ErrorCodes.RESULT_OK);
    }

    private ApiResponse getFirstResponse() {
        ApiResponse response = null;
        if (mResponses != null && mResponses.size() > 0) {
            response = (ApiResponse) mResponses.values().toArray()[0];
        }
        return response;
    }

    @Override
    public String getMethodName() {
        ApiResponse response = getFirstResponse();
        return response != null ? response.getMethodName() : null;
    }

    @Override
    public boolean isMethodNameEquals(String method) {
        boolean result = false;
        if (mResponses != null && mResponses.size() > 0) {
            for (Map.Entry<String, ApiResponse> response : mResponses.entrySet()) {
                if (response.getValue().isMethodNameEquals(method)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public JSONObject getUnread() {
        ApiResponse response = getFirstResponse();
        return response != null ? response.getUnread() : null;
    }

    @Override
    public JSONObject getBalance() {
        ApiResponse response = getFirstResponse();
        return response != null ? response.getBalance() : null;
    }

    @Override
    public boolean isNeedUpdateCounters() {
        return false;
    }

    @Override
    public String toString() {
        String result;
        if (mResponses != null && mResponses.size() > 0) {
            result = "MultipartResponse\n";
            for (Map.Entry<String, ApiResponse> response : mResponses.entrySet()) {
                ApiResponse value = response.getValue();
                result += "\nresponse #" + value.id + "\n" + value.toString() + "\n";
            }
        } else {
            result = String.format(Locale.ENGLISH, "MultipartResponse error #%d: %s", code, message);
        }
        return result;
    }


    @Override
    public JSONObject toJson() throws JSONException {
        return jsonResult;
    }

    @Override
    public void fromJSON(String json) {

    }
}
