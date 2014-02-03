package com.topface.topface.requests;

import org.json.JSONObject;

public interface IApiResponse {


    /**
     * Проверяет, равен ли один из переданных кодов текущему коду ответа
     *
     * @param errorCodes возможные коды ответа сервера, на которые нужно проверить
     */
    public boolean isCodeEqual(Integer... errorCodes);

    /**
     * Проверяет, является ли код ошибки кодом неверной авторизации
     */
    boolean isWrongAuthError();

    /**
     * Возвращает текст ошибки, если он есть и null, если это не ошибка
     */
    public String getErrorMessage();

    public JSONObject getJsonResult();

    public int getResultCode();

    public boolean isCompleted();

    public String getMethodName();

    public boolean isMethodNameEquals(String method);

    public JSONObject getUnread();

    public JSONObject getBalance();

    public boolean isNeedUpdateCounters();

}
