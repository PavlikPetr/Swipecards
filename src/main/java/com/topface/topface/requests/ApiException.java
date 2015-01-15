package com.topface.topface.requests;

/**
 * Ошибка API (например не удалось распарсить ответ)
 */
public class ApiException extends Exception {
    public ApiException(String s) {
        super(s);
    }
}
