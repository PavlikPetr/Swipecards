package com.topface.topface.requests;

import com.topface.topface.data.SerializableToJson;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ApiResponse implements IApiResponse, SerializableToJson {
    // Data
    public int code = RESULT_DONT_SET;
    public String message = "";
    public JSONObject jsonResult;
    public JSONObject counters;
    public String method;
    public String id;

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
                if (!jsonResult.isNull("id")) {
                    id = jsonResult.optString("id");
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
    public String getErrorMessage() {
        return message;
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
    public boolean isCodeEqual(Integer... errorCode) {
        return Arrays.asList(errorCode).contains(code);
    }

    /**
     * Проверяет, является ли код ошибки кодом неверной авторизации
     */
    public boolean isWrongAuthError() {
        return isCodeEqual(
                UNKNOWN_PLATFORM,
                UNKNOWN_SOCIAL_USER,
                UNVERIFIED_TOKEN,
                INCORRECT_LOGIN,
                INCORRECT_PASSWORD
        );
    }

    @Override
    public JSONObject toJson() {
        return jsonResult;
    }

    @Override
    public JSONObject getJsonResult() {
        return jsonResult;
    }

    @Override
    public int getResultCode() {
        return code;
    }
}
