package com.topface.topface.requests;

import com.topface.topface.utils.Debug;

/**
 * Базовый handler ответа API, что бы не определять каждый раз fail запрос
 */
public class BaseApiHandler extends ApiHandler {
    @Override
    public void success(ApiResponse response) throws NullPointerException {
        //Implement me
    }

    @Override
    public void fail(int codeError, ApiResponse response) throws NullPointerException {
        Debug.error("ApiResponse error #" + codeError + "\n" + response.toString());
    }
}
