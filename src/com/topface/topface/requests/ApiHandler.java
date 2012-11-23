package com.topface.topface.requests;

import android.os.Handler;
import com.topface.topface.utils.Debug;

abstract public class ApiHandler extends Handler {

    public void response(ApiResponse response) {
        try {
            if (response.code == ApiResponse.ERRORS_PROCCESED) {
                //TODO: Обрабатывать результат
            } else if (response.code != ApiResponse.RESULT_OK) {
                fail(response.code, response);
            } else {
                success(response);
            }
        } catch (Exception e) {
            Debug.error("api handler exception", e);
        }
    }

    abstract public void success(ApiResponse response) throws NullPointerException;

    abstract public void fail(int codeError, ApiResponse response) throws NullPointerException;
}
