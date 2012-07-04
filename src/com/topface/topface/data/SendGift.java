package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class SendGift extends AbstractData{
	public int power;
	public int money;
	
	public static SendGift parse(ApiResponse response) {
		SendGift sendGift = new SendGift();
		
		try {
			sendGift.power = response.mJSONResult.optInt("power");
			sendGift.money = response.mJSONResult.optInt("money");
		} catch (Exception e) {
			Debug.log("SendGift.class","Wrong response parsing: " + e);
		}
		
		return sendGift;
	}
}
