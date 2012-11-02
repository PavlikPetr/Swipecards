package com.topface.topface.data;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

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
			for (int i = 0; i < pages.length(); i++) {
				JSONObject page = pages.getJSONObject(i);

				String pageName = page.optString("name");
				String floatType = page.optString("float");
				boolean mail = page.optBoolean("mail");
				String bannerType = page.optString("banner");

				options.pages.put(pageName, new Page(pageName, floatType, mail, bannerType));
			}
		} catch (Exception e) {
			Debug.log("Message.class", "Wrong response parsing: " + e);
		}

		CacheProfile.setOptions(options, response.jsonResult);
		return options;
	}

	public static class Page {
		public String name;
		public String floatType;
		public boolean mail;
		public String banner;

		public Page(String name, String floatType, boolean mail, String banner) {
			this.name = name;
			this.floatType = floatType;
			this.mail = mail;
			this.banner = banner;
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
