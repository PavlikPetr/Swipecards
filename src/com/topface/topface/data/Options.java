package com.topface.topface.data;


import org.json.JSONArray;
import org.json.JSONObject;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

/**
 * Опции приложения
 * <p/>
 * NOTICE: В данном типе данных используем значения по умолчанию
 */
public class Options extends AbstractData {
    /**
     * Показывать баннер
     */
    public final static String FLOAT_TYPE_BANNER = "BANNER";
    /**
     * Показывать лидеров
     */
    public final static String FLOAT_TYPE_LEADERS = "LEADERS";
    /**
     * По умолчанию (в нашем случае баннеры)
     */
    public final static String FLOAT_TYPE_DEFAULT = "DEFAULT";

    private final static String DEFAULT_FLOAT_TYPE = FLOAT_TYPE_BANNER;

    /**
     * Стоимость отправки "Восхищения"
     */
    public int price_highrate = 1;
    /**
     * Стоимость вставания в лидеры
     */
    public int price_leader = 6;
    /**
     * Показывать баннеры или лидеров на странице лайков
     */
    public String float_type_like = FLOAT_TYPE_BANNER;
    /**
     * Показывать баннеры или лидеров на странице чатов
     */
    public String float_type_dialogs = FLOAT_TYPE_BANNER;
    /**
     * Показывать баннеры или лидеров на странице топа
     */
    public String float_type_top = FLOAT_TYPE_BANNER;

    /**
     * Настройки уведомлений на почту
     */
    public MailNotifications mail_notifications;
    
    public static Options parse(ApiResponse response) {
        Options options = new Options();

        try {
            options.price_highrate = response.jsonResult.optInt("price_highrate");
            options.price_leader = response.jsonResult.optInt("price_leader");
            options.float_type_like = setFloatType(response.jsonResult.optString("float_type_like"));
            options.float_type_dialogs = setFloatType(response.jsonResult.optString("float_type_dialogs"));
            options.float_type_top = setFloatType(response.jsonResult.optString("float_type_top"));
            
            options.mail_notifications = !response.jsonResult.has("mailnotifications") ? 
            		null : (new MailNotifications(response.jsonResult.optJSONObject("mailnotifications")));            
            
        } catch (Exception e) {
            Debug.log("Message.class", "Wrong response parsing: " + e);
        }

        return options;
    }

    private static String setFloatType(String floatType) {
        if (floatType.equals(FLOAT_TYPE_LEADERS)) {
            return FLOAT_TYPE_LEADERS;
        } else if (floatType.equals(FLOAT_TYPE_LEADERS)) {
            return FLOAT_TYPE_BANNER;
        } else if (floatType.equals(FLOAT_TYPE_DEFAULT)) {
            return DEFAULT_FLOAT_TYPE;
        } else {
            return DEFAULT_FLOAT_TYPE;
        }
    }

    public static class MailNotifications {
    	public boolean sympathy;
    	public boolean mutual;
    	public boolean chat;
    	public boolean guests;
    	
    	public MailNotifications(JSONObject obj) {
    		sympathy = obj.optBoolean("sympathy");
    		mutual = obj.optBoolean("mutual");
    		chat = obj.optBoolean("chat");
    		guests = obj.optBoolean("guests");
		}
    }
    
}
