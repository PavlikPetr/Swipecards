package com.topface.topface.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseArray;

import com.topface.topface.utils.Debug;
import com.topface.topface.utils.PhotoLinksResolver;

public abstract class AbstractDataWithPhotos extends AbstractData {

    protected int mAvatarId;
    protected LinkedList<String> mSizeKeys = new LinkedList<String>();
    public SparseArray<HashMap<String, String>> photoLinks = new SparseArray<HashMap<String,String>>();
	
    protected static void initPhotos(JSONObject item, AbstractDataWithPhotos data) {
        try {
            // Avatar
            if(!item.isNull("photo")) {
                JSONObject avatar = item.optJSONObject("photo");
                if(avatar != null/* && photoLinksArray.length() <= 0*/) {
                    data.mAvatarId = avatar.optInt("id", -1);
                    data.photoLinks.put(data.mAvatarId, getLinksHash(avatar.optJSONObject("links"),data));              
                }
            }
            // Album            
            if(!item.isNull("photos")) {
                JSONArray photoLinksArray = item.optJSONArray("photos");
                if (photoLinksArray != null) {
                    for (int i = 0; i < photoLinksArray.length(); i++) {
                        JSONObject photo = photoLinksArray.getJSONObject(i);    
                        int id = photo.getInt("id");
                        data.photoLinks.put(id, getLinksHash(photo.optJSONObject("links"),data));
                    }
                }
            }
        } catch (JSONException e) {
            Debug.log(AbstractDataWithPhotos.class, e.toString());
        }
    }

	public String getAvatarLink(String sizeKey) {
		HashMap<String, String> links = photoLinks.get(mAvatarId);
		if (links.containsKey(sizeKey)) {
			return links.get(sizeKey);
		}
		return null;
	}

	public String[] getPhotos(String sizeKey) {
		LinkedList<String> result = new LinkedList<String>();
		if (photoLinks != null) {
			for (int i = 0; i < photoLinks.size(); i++) {
				Integer id = photoLinks.keyAt(i);
				result.add(photoLinks.get(id).get(sizeKey));
			}
		}		
		return (String[])result.toArray();
	}
	
	private static HashMap<String, String> getLinksHash(JSONObject links, AbstractDataWithPhotos data) {		
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			Iterator<String> iterKey = links.keys();
			while (iterKey.hasNext()) {
				String sizeKey = (String) iterKey.next();
				result.put(sizeKey, links.getString(sizeKey));
				data.mSizeKeys.add(sizeKey);
			}
		} catch (JSONException e) {
			Debug.log(AbstractDataWithPhotos.class, e.toString());
		}
		return result;
	}
	
	public String getOriginalLink() {
        return photoLinks.get(mAvatarId).get(PhotoLinksResolver.SIZE_ORIGIN);
    }       
	
    public String getLargeLink() {
        return photoLinks.get(mAvatarId).get(PhotoLinksResolver.SIZE_192);
    }
    
    public String getNormalLink() {
        return photoLinks.get(mAvatarId).get(PhotoLinksResolver.SIZE_128);
    }

    public String getSmallLink() {
        return photoLinks.get(mAvatarId).get(PhotoLinksResolver.SIZE_64);
    }       
    
//    public HashMap<Integer, String> getAlbumLinks() {
//        HashMap<Integer, String> album = new HashMap<Integer, String>();
//        return album;
//    }
	
}
