package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;

import com.topface.topface.data.Gift;
import com.topface.topface.utils.Debug;

import android.content.Context;

public class SendGiftRequest extends ApiRequest {

	static final String USER_ID = "userid";
	static final String GIFT_ID = "giftid";
	
	private String service = "gift";
	public int userId;
	public int giftId;	
	
	public SendGiftRequest(Context context) {
		super(context);
	}

	@Override
	public String toString() {
		JSONObject root = new JSONObject();
		try {
			root.put("service", service);
			root.put("ssid", ssid);
			JSONObject data = new JSONObject();
			data.put(USER_ID, userId);
			data.put(GIFT_ID, giftId);
			root.put("data", new JSONObject().put(USER_ID,userId).put(GIFT_ID,giftId));
		} catch (JSONException e) {
			Debug.log(this, "Wrong request compiling: " + e);
		}

		return root.toString();
	}	
}
