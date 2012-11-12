package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Опции приложения
 * <p/>
 * NOTICE: В данном типе данных используем значения по умолчанию
 */
public class Options extends AbstractData {

	/**
	 * Идентификаторы страниц
	 */
	public final static String PAGE_LIKES = "LIKE";
	public final static String PAGE_MUTUAL = "MUTUAL";
	public final static String PAGE_MESSAGES = "MESSAGES";
	public final static String PAGE_TOP = "TOP";
	public final static String PAGE_VISITORS = "VISITORS";
	public final static String PAGE_DIALOGS = "DIALOGS";

    public final static int NOTIFICATIONS_UNKNOWN = -1;
    public final static int NOTIFICATIONS_MESSAGE = 0;
    public final static int NOTIFICATIONS_SYMPATHY = 1;
    public final static int NOTIFICATIONS_LIKES = 2;
    public final static int NOTIFICATIONS_RATE = 3;
    public final static int NOTIFICATIONS_VISITOR = 4;

    public final static String GENERAL_MAIL_CONST = "true";
    public final static String GENERAL_APNS_CONST = "false";
    public final static String GENERAL_SEPARATOR = ":";

	/**
	 * Идентификаторы для типов блоков (лидеры, баннеры, не показывать блоки)
	 */
	public final static String FLOAT_TYPE_BANNER = "BANNER";
	public final static String FLOAT_TYPE_LEADERS = "LEADERS";
	public final static String FLOAT_TYPE_NONE = "NONE";

	private final static String DEFAULT_FLOAT_TYPE = FLOAT_TYPE_BANNER;

	/**
	 * Идентификаторы типов баннеров
	 */
	public final static String BANNER_TOPFACE = "TOPFACE";
	public final static String BANNER_ADFONIC = "ADFONIC";
	public final static String BANNER_ADMOB = "ADMOB";
	public final static String BANNER_WAPSTART = "WAPSTART";

	/**
	 * Настройки для каждого типа страниц
	 */
	public HashMap<String, Options.Page> pages = new HashMap<String, Options.Page>();
    public HashMap<Integer, TopfaceNotifications> notifications = new HashMap<Integer, TopfaceNotifications>();

	/**
	 * Стоимость отправки "Восхищения"
	 */
	public int price_highrate = 1;
	/**
	 * Стоимость вставания в лидеры
	 */
	public int price_leader = 6;

	/*
	 * Наличие адрес почты для уведомлений на сервере
	 */
	public boolean hasMail;

	public static Options parse(ApiResponse response) {
		Options options = new Options();

		try {
			options.price_highrate = response.jsonResult.optInt("price_highrate");
			options.price_leader = response.jsonResult.optInt("price_leader");
			options.hasMail = response.jsonResult.optBoolean("has_email");
			// Pages initialization
			JSONArray pages = response.jsonResult.optJSONArray("pages");
            JSONArray notifications = response.jsonResult.optJSONArray("notifications");
			for (int i = 0; i < pages.length(); i++) {
				JSONObject page = pages.getJSONObject(i);

				String pageName = page.optString("name");
				String floatType = page.optString("float");
				String bannerType = page.optString("banner");

				options.pages.put(pageName, new Page(pageName, floatType, bannerType));
			}
            for(int i=0; i < notifications.length(); i++) {
                JSONObject notification = notifications.getJSONObject(i);

                boolean mail = notification.optBoolean("mail");
                boolean apns = notification.optBoolean("apns");
                int type = notification.optInt("type");

                options.notifications.put(type,new TopfaceNotifications(apns,mail,type));
            }
		} catch (Exception e) {
			Debug.log("Message.class", "Wrong response parsing: " + e);
		}

		CacheProfile.setOptions(options, response.jsonResult);
		return options;
	}

    public static String generateKey(int type, boolean isMail) {
        return Integer.toString(type) + GENERAL_SEPARATOR + ((isMail)?GENERAL_MAIL_CONST:GENERAL_APNS_CONST);
    }

	public static class Page {
		public String name;
		public String floatType;
		public String banner;

		public Page(String name, String floatType, String banner) {
			this.name = name;
			this.floatType = floatType;
			this.banner = banner;
		}
	}

	public static class TopfaceNotifications {
        public boolean apns;
        public boolean mail;
        public int type;

		public TopfaceNotifications(boolean apns, boolean  mail, int type) {
            this.apns = apns;
            this.mail = mail;
            this.type = type;
		}
	}
}
