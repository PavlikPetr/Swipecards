package com.topface.topface.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseArray;

import com.topface.topface.utils.Debug;

public class AbstractDataWithPhotos extends AbstractData {
	
	// Data
	public int avatarId;
	public SparseArray<HashMap<String, String>> photoLinksArray;
	public LinkedList<String> sizeKeys; 	

	public String getAvatarLink(String sizeKey) {
		HashMap<String, String> links = photoLinksArray.get(avatarId);
		if (links.containsKey(sizeKey)) {
			return links.get(sizeKey);
		}
		return null;
	}

	public String[] getPhotos(String sizeKey) {
		LinkedList<String> result = new LinkedList<String>();
		if (photoLinksArray != null) {
			for (int i = 0; i < photoLinksArray.size(); i++) {
				Integer id = photoLinksArray.keyAt(i);
				result.add(photoLinksArray.get(id).get(sizeKey));
			}
		}		
		return (String[])result.toArray();
	}

	protected static void initPhotos(JSONObject item, AbstractDataWithPhotos data) {
		try {
			JSONArray photoLinksArray = item.optJSONArray("photos");
			if (photoLinksArray != null) {
				data.photoLinksArray = new SparseArray<HashMap<String,String>>();
				for (int i = 0; i < photoLinksArray.length(); i++) {
					JSONObject photo = photoLinksArray.getJSONObject(i);	
					int id = photo.getInt("id");
					data.photoLinksArray.put(id, getLinksHash(photo.optJSONObject("links"),data));
				}
			}
			
			JSONObject avatar = item.optJSONObject("photo");
			if(avatar != null && photoLinksArray.length() <= 0) {
				data.avatarId = avatar.optInt("id", -1);
				data.photoLinksArray.put(data.avatarId, getLinksHash(avatar.optJSONObject("links"),data));				
			}
		} catch (JSONException e) {
			Debug.log(AbstractDataWithPhotos.class,e.toString());
		}
	}
	
	private static HashMap<String, String> getLinksHash(JSONObject links, AbstractDataWithPhotos data) {		
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			Iterator<String> iterKey = links.keys();
			while (iterKey.hasNext()) {
				String sizeKey = (String) iterKey.next();
				result.put(sizeKey, links.getString(sizeKey));
				data.sizeKeys.add(sizeKey);
			}
		} catch (JSONException e) {
			Debug.log(AbstractDataWithPhotos.class,e.toString());
		}
		return result;
	}
	
}
