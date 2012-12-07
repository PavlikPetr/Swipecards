package com.topface.topface.requests;

import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiResponse {
    // Data
    public int code = -1;
    public JSONObject jsonResult;
    public JSONObject counters;
    public String method;
    private boolean mIsErrorResponse;
    // Constants
    public static final int ERRORS_PROCCESED = -2;
    public static final int RESULT_OK = -1;
    public static final int SYSTEM = 0;
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
    public static final int INVERIFIED_TOKEN = 20;
    public static final int BAN = 28;
    public static final int PREMIUM_ACCESS_ONLY = 32;
    // local
    public static final int NULL_RESPONSE = 100;
    public static final int WRONG_RESPONSE = 101;

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
        JSONObject json = null;

        if (response != null && response.length() > 0) {
            try {
                json = new JSONObject(response);
            } catch (JSONException e) {
                code = WRONG_RESPONSE;
                Debug.error("json response is wrong: " + response, e);
            }
        }

        parseJson(json);
    }

    public ApiResponse(JSONObject response) {
        parseJson(response);
    }

    public void parseJson(JSONObject response) {
        try {
            if (response == null) {
                Debug.error("JSON response is null");
                code = NULL_RESPONSE;
                return;
            }

            jsonResult = response;
            if (!jsonResult.isNull("error")) {
                jsonResult = jsonResult.getJSONObject("error");
                mIsErrorResponse = true;
                code = jsonResult.getInt("code");
            } else if (!jsonResult.isNull("result")) {
                if(!jsonResult.isNull("counters"))  {
                    counters = jsonResult.getJSONObject("counters");
                }
                if(!jsonResult.isNull("method")) {
                    method = jsonResult.optString("method");
                }
                jsonResult = jsonResult.getJSONObject("result");
            }
            else
                code = WRONG_RESPONSE;
        } catch (Exception e) {
            code = WRONG_RESPONSE;
            Debug.error("json resonse is wrong:" + response, e);
        }
    }


    @Override
    public String toString() {
        if (jsonResult != null) {
            return jsonResult.toString();
        } else {
            return "response is null";
        }
    }

    public boolean isError() {
        return mIsErrorResponse;
    }


}
