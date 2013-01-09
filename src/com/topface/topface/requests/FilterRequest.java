package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterRequest extends AbstractApiRequest {
    private static final String SERVICE_NAME = "filter";

    /**
     * флаг "только красивые"
     */
    public boolean beautiful;

    /**
     * код пола пользователей для поиска
     */
    public int sex;

    /**
     * идентификатор города для поиска пользователей
     */
    public int city;

    /**
     * начальный возраст пользователей в выборке поиска
     */
    public int agebegin = 16;

    /**
     * конечный возраст пользователей в выборке поиска
     */
    public int ageend = 99;

    /**
     * цель знакомства
     */
    public int xstatus;

    /**
     * код состояния в барке (расширенный параметр)
     */
    public int marriage;

    /**
     * код характера (расширенный параметр)
     */
    public int character;

    /**
     * код отношения к алкоголю (расширенный параметр)
     */
    public int alcohol;


    /**
     * размер груди (расширенный параметр)
     */
    public int breast;

    /**
     * код финансового состояния (расширенный параметр)
     */
    public int finances;

    public FilterRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("beautiful", beautiful)
                .put("sex", sex)
                .put("city", city)
                .put("agebegin", agebegin)
                .put("ageend", ageend)
                .put("xstatus", xstatus)
                .put("marriage", marriage)
                .put("character", character)
                .put("alcohol", alcohol)
                .put("breast", breast)
                .put("finances", finances);

    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().trackEvent("Dating", "ChangeFilter", "", 1L);
    }
}
