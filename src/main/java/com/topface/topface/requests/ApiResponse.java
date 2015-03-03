package com.topface.topface.requests;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.handlers.ErrorCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;

public class ApiResponse implements IApiResponse {
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
            code = ErrorCodes.NULL_RESPONSE;
            Debug.error(new ApiException("JSON response is null"));
            return;
        }

        try {
            jsonResult = response;
            if (!jsonResult.isNull("error")) {
                id = jsonResult.optString("id");
                jsonResult = jsonResult.getJSONObject("error");
                code = jsonResult.getInt("code");
                message = jsonResult.optString("message", "");
            } else if (!jsonResult.isNull("result")) {
                unread = jsonResult.optJSONObject("unread");
                balance = jsonResult.optJSONObject("balance");
                method = jsonResult.optString("method");
                id = jsonResult.optString("id");
                jsonResult = jsonResult.getJSONObject("result");
                code = ErrorCodes.RESULT_OK;
            } else {
                code = ErrorCodes.WRONG_RESPONSE;
                Debug.error(new ApiException(
                        "Json format is wrong (result and error fields not found): \n" + response + "\n"
                ));
            }
        } catch (Exception e) {
            code = ErrorCodes.WRONG_RESPONSE;
            Debug.error("Json response is wrong: \n" + response + "\n", e);
        }
    }


    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (method == null && jsonResult != null) {
            return String.format(Locale.ENGLISH, "Response error #%d: %s", code, message);
        } else if (jsonResult != null) {
            return jsonResult.toString();
        } else {
            return "Response is null";
        }
    }

    /**
     * Если ответ не содержит поля ошибки и содержит ответ, то он завершился удачно
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
        return mIsNeedUpdateCounters && isCompleted();
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
    public void fromJSON(String json) {

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
