package com.topface.topface.data;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.utils.Debug;

public class Gift extends AbstractDataWithPhotos implements IListLoader{
	
	public static final int ROMANTIC = 0;
	public static final int FRIENDS  = 2;
	public static final int PRESENT  = 1;
	
	public static final int PROFILE = -1;
	public static final int PROFILE_NEW = -2;
	public static final int SEND_BTN = -3;
	
	public int id;
	public int type;
	public String link;
	public int price;
	public int feedId;
	
	//Loader indicators
    public boolean isListLoader = false;
    public boolean isListLoaderRetry = false;

    public Gift() {
    	
    }

    public Gift(IListLoader.ItemType type) {
    	switch (type) {
		case LOADER:
			isListLoader = true;
			break;
		case RETRY:
			isListLoaderRetry = true;
			break;
		default:			
			break;
		}     	
    }
	
	public static LinkedList<Gift> parse(ApiResponse response) {
		LinkedList<Gift> gifts = new LinkedList<Gift>();
		
		try {
			JSONArray array = response.mJSONResult.getJSONArray("gifts");
			for (int i = 0; i < array.length(); i++) {
				JSONObject item = array.getJSONObject(i);
				Gift gift = new Gift();
				gift.id = item.optInt("id");
				gift.type = item.optInt("type");
				gift.link = item.optString("link");
				gift.price = item.optInt("price");
				
				gifts.add(gift);
			}
		} catch (JSONException e) {
			Debug.log("Gift.class","Wrong response parsing: " + e);
		}
		
		return gifts;
	}
	
	// Gets User gifts
	public static LinkedList<Gift> parse(User user) {
		LinkedList<Gift> gifts = new LinkedList<Gift>();
		gifts.add(Gift.getSendedGiftItem());
		gifts.addAll(user.gifts);		
		return gifts;
	}
	
	public static int getTypeNameResId(int type) {
	    switch (type) {
            case ROMANTIC:
                return R.string.gifts_romantic;                
            case FRIENDS:
                return R.string.gifts_friends;
            case PRESENT:
                return R.string.gifts_present;
            default:
                return R.string.gifts_romantic;
        }
	}
	
	public static Gift getSendedGiftItem() {
		Gift result = new Gift();
		result.type = Gift.SEND_BTN;
		return result;
	}
	
	@Override
	public String getOriginalLink() {
		return link;
	}
	
	@Override
	public String getSmallLink() {
		return link;
	}
	
	@Override
	public String getNormalLink() {
		return link;
	}	
	
	@Override
	public String getLargeLink() {
		return link;
	}

	@Override
	public boolean isLoader() {
		return isListLoader;
	}
	
	@Override
	public boolean isLoaderRetry() {
		return isListLoaderRetry;
	}
	
	@Override
	public void switchToLoader() {
		isListLoader = false;
		isListLoaderRetry = true;
	}
}
