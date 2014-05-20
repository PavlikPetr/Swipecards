package com.topface.topface.requests;

import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.multipart.MultipartStream;
import com.topface.topface.utils.http.FlushedInputStream;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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
    private HashMap<String, ApiResponse> mResponses = new HashMap<>();

    public MultipartApiResponse(HttpURLConnection connection) {
        try {
            //Разбиваем ответ на подответы, в итоге получим массив json объектов
            LinkedList<String> responses = splitResponses(connection);
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
            setError(ErrorCodes.ERRORS_PROCCESED, "Parse response error");
        }
    }

    private void parseResponses(LinkedList<String> parts) {
        code = ErrorCodes.RESULT_OK;
        boolean firstResponse = true;
        for (String responseString : parts) {
            if (!TextUtils.isEmpty(responseString)) {
                ApiResponse response = new ApiResponse(responseString);
                mResponses.put(response.id, response);
                if (!response.isCompleted()) {
                    code = response.getResultCode();
                    message = response.getErrorMessage();
                } else {
                    //Для всех ответов кроме первого отключаем обновление счетчиков
                    if (firstResponse) {
                        firstResponse = false;
                    } else {
                        response.setUpdateCountersFlag(false);
                    }
                }
            } else {
                Debug.error("Wrong response part:\n" + responseString);
            }
        }
    }

    private LinkedList<String> splitResponses(HttpURLConnection connection) throws IOException {
        LinkedList<String> parts = new LinkedList<>();
        //Если подключение не пустое и код ответа правильный
        if (connection != null && HttpUtils.isCorrectResponseCode(connection.getResponseCode())) {
            //Если нужно, разархивируем поток из Gzip
            InputStream stream = HttpUtils.getGzipInputStream(connection);
            if (stream != null) {
                BufferedInputStream is = new BufferedInputStream(new FlushedInputStream(stream), HttpUtils.BUFFER_SIZE);
                String boundary = getBoundary(connection.getContentType());
                if (TextUtils.isEmpty(boundary)) {
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
                stream.close();
                is.close();
            }
        }

        return parts;
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
        return null;
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
            result = String.format("MultipartResponse error #%d: %s", code, message);
        }
        return result;
    }


}
