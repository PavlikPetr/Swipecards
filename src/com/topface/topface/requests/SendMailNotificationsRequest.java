package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class SendMailNotificationsRequest extends AbstractApiRequest {

	private static final String SERVICE = "notifications";
	
	public Boolean sympathy = null;
	public Boolean mutual = null;
	public Boolean chat = null;
	public Boolean guests = null;
	
	public SendMailNotificationsRequest(Context context) {
		super(context);
	}

	@Override
	protected JSONObject getRequestData() throws JSONException {
		JSONObject result = new JSONObject();
		if (sympathy != null) result.put("sympathy",sympathy);
		if (mutual != null) result.put("mutual", mutual);
		if (chat != null) result.put("chat", chat);
		if (guests != null) result.put("guests", guests);
		return result;
	}

	@Override
	protected String getServiceName() {
		return SERVICE;
	}	
	
}
