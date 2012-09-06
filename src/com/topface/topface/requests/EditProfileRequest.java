package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;

import com.topface.topface.utils.Debug;

import android.content.Context;

public class EditProfileRequest extends ApiRequest{

	private String service = "questionary";
	
	public String key;
	public String value;
	
	public EditProfileRequest(Context context) {
		super(context);
	}

	@Override
	public String toString() {
		JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            JSONObject data = new JSONObject();            
            data.put(key, value);
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
	}

}
