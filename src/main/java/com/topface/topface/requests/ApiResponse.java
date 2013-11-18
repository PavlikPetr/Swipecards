package com.topface.topface.requests;

import com.topface.topface.data.SerializableToJson;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.Debug;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ApiResponse implements IApiResponse, SerializableToJson {
    private boolean mIsNeedUpdateCounters = true;
    // Data
    protected int code = ErrorCodes.RESULT_DONT_SET;
    public String message = "";
    public JSONObject jsonResult;
    // can be null
    public JSONObject unread;
    public JSONObject balance;
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
                code = ErrorCodes.WRONG_RESPONSE;
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
            code = ErrorCodes.NULL_RESPONSE;
            return;
        }

        try {
            jsonResult = response;
            if (!jsonResult.isNull("error")) {
                jsonResult = jsonResult.getJSONObject("error");
                code = jsonResult.getInt("code");
                message = jsonResult.optString("message", "");
            } else if (!jsonResult.isNull("result")) {
                if (!jsonResult.isNull("unread")) {
                    unread = jsonResult.optJSONObject("unread");
                }
                if (!jsonResult.isNull("balance")) {
                    balance = jsonResult.optJSONObject("balance");
                }
                if (!jsonResult.isNull("method")) {
                    method = jsonResult.optString("method");
                }
                if (!jsonResult.isNull("id")) {
                    id = jsonResult.optString("id");
                }
                jsonResult = jsonResult.getJSONObject("result");
                code = ErrorCodes.RESULT_OK;
            } else {
                code = ErrorCodes.WRONG_RESPONSE;
            }
        } catch (Exception e) {
            code = ErrorCodes.WRONG_RESPONSE;
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
        return isCodeEqual(ErrorCodes.RESULT_OK);
    }

    @Override
    public String getMethodName() {
        return method;
    }

    @Override
    public boolean isMethodNameEquals(String method) {
        return this.method != null && this.method.equals(method);
    }

    @Override
    public JSONObject getUnread() {
        return unread;
    }

    @Override
    public JSONObject getBalance() {
        return balance;
    }

    @Override
    public boolean isNeedUpdateCounters() {
        return mIsNeedUpdateCounters;
    }

    public void setUpdateCountersFlag(boolean isNeedUpdateCounters) {
        mIsNeedUpdateCounters = isNeedUpdateCounters;
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
                ErrorCodes.UNKNOWN_PLATFORM,
                ErrorCodes.UNKNOWN_SOCIAL_USER,
                ErrorCodes.UNVERIFIED_TOKEN,
                ErrorCodes.INCORRECT_LOGIN,
                ErrorCodes.INCORRECT_PASSWORD
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
