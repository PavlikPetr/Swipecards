package com.topface.topface.requests;

import com.topface.topface.data.SerializableToJson;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

@SuppressWarnings("UnusedDeclaration")
public class ApiResponse implements SerializableToJson {
    // Data
    public int code = RESULT_DONT_SET;
    public String message;
    public JSONObject jsonResult;
    public JSONObject counters;
    public String method;
    // Constants
    public static final int ERRORS_PROCCESED = -2;
    public static final int RESULT_OK = -1;
    public static final int RESULT_DONT_SET = 0;
    public static final int UNKNOWN_SOCIAL_USER = 1;
    public static final int UNKNOWN_PLATFORM = 2;
    public static final int SESSION_NOT_FOUND = 3;
    public static final int UNSUPPORTED_CITIES_FILTER = 4;
    public static final int MISSING_REQUIRE_PARAMETER = 5;
    public static final int USER_NOT_FOUND = 6;
    public static final int UNSUPPORTED_LOCALE = 7;
    public static final int CANNOT_SENT_RATE = 8;
    public static final int MESSAGE_TOO_SHORT = 9;
    public static final int CANNOT_SENT_MESSAGE = 10;
    public static final int DETECT_FLOOD = 11;
    public static final int INCORRECT_PHOTO_URL = 12;
    public static final int DEFAULT_ERO_PHOTO = 13;
    public static final int PAYMENT = 14;
    public static final int INCORRECT_VOTE = 15;
    public static final int INVALID_TRANSACTION = 16;
    public static final int INVALID_PRODUCT = 17;
    public static final int INVERIFIED_RECEIPT = 18;
    public static final int ITUNES_CONNECTION = 19;
    public static final int UNVERIFIED_TOKEN = 20;
    public static final int INVALID_FORMAT = 21;
    public static final int UNVERIFIED_SIGNATURE = 22;
    public static final int INCORRECT_VALUE = 23;
    public static final int MAINTENANCE = 27;
    public static final int BAN = 28;
    public static final int NETWORK_CONNECT_ERROR = 29;
    public static final int PREMIUM_ACCESS_ONLY = 32;
    public static final int INVALID_PURCHASE_TOKEN = 34;
    public static final int CANNOT_BECOME_LEADER = 35;
    public static final int CODE_VIRUS_LIKES_ALREADY_RECEIVED = 36;
    public static final int CODE_OLD_APPLICATION_VERSION = 37;
    public static final int USER_ALREADY_REGISTERED = 39;
    public static final int INCORRECT_LOGIN = 42;
    public static final int INCORRECT_PASSWORD = 43;

    // local
    public static final int NULL_RESPONSE = 100;
    public static final int WRONG_RESPONSE = 101;

    /**
     * Конструиерует объект ответа от сервера с указаной ошибкой
     *
     * @param errorCode    код ошибки
     * @param errorMessage сообщение об ошибке
     */
    public ApiResponse(int errorCode, String errorMessage) {
        this(constructApiError(errorCode, errorMessage));
    }

    private static JSONObject constructApiError(int errorCode, String errorMessage) {
        try {
            return new JSONObject()
                    .put("error",
                            new JSONObject()
                                    .put("code", errorCode)
                                    .put("message", errorMessage)
                    );
        } catch (JSONException e) {
            Debug.error(e);
            return null;
        }
    }


    public ApiResponse(String response) {
        JSONObject json;

        if (response != null && response.length() > 0) {
            try {
                json = new JSONObject(response);
                parseJson(json);
            } catch (JSONException e) {
                code = WRONG_RESPONSE;
                Debug.error("json response is wrong: " + response, e);
            }
        }
    }

    public ApiResponse(JSONObject response) {
        parseJson(response);
    }

    public ApiResponse() {
    }

    public void parseJson(JSONObject response) {
        if (response == null) {
            Debug.error("JSON response is null");
            code = NULL_RESPONSE;
            return;
        }

        try {
            jsonResult = response;
            if (!jsonResult.isNull("error")) {
                jsonResult = jsonResult.getJSONObject("error");
                code = jsonResult.getInt("code");
                message = jsonResult.optString("message", "");
            } else if (!jsonResult.isNull("result")) {
                if (!jsonResult.isNull("counters")) {
                    counters = jsonResult.getJSONObject("counters");
                }
                if (!jsonResult.isNull("method")) {
                    method = jsonResult.optString("method");
                }
                jsonResult = jsonResult.getJSONObject("result");
                code = RESULT_OK;
            } else {
                code = WRONG_RESPONSE;
            }
        } catch (Exception e) {
            code = WRONG_RESPONSE;
            Debug.error("Json response is wrong:" + response, e);
        }
    }


    @Override
    public String toString() {
        if (method == null && jsonResult != null) {
            return String.format("Response error #%d: %s", code, message);
        } else if (jsonResult != null) {
            return jsonResult.toString();
        } else {
            return "Response is null";
        }
    }

    /**
     * Если ответ содержит поле comleted, то вернет ее значение. Нужно для парсинга простых ответов
     *
     * @return флаг выполенения запроса
     */
    public boolean isCompleted() {
        return isCodeEqual(RESULT_OK);
    }

    /**
     * Проверяет, является ли этот ответ от сервера ошибокой переданно в параметре errorCode
     */
    public boolean isCodeEqual(int errorCode) {
        return errorCode == code;
    }

    /**
     * Проверяет, является ли код ошибки кодом неверной авторизации
     */
    public boolean isWrongAuthError() {
        return Arrays.asList(
                UNKNOWN_PLATFORM,
                UNKNOWN_SOCIAL_USER,
                UNVERIFIED_TOKEN,
                INCORRECT_LOGIN,
                INCORRECT_PASSWORD
        ).contains(code);
    }

    public boolean isCorrectJson() {
        return jsonResult != null && !Arrays.asList(
                NULL_RESPONSE,
                WRONG_RESPONSE
        ).contains(code);
    }

    @Override
    public JSONObject toJson() {
        return jsonResult;
    }
}
