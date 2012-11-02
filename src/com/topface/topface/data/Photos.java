package com.topface.topface.data;


import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Photos extends ArrayList<Photo>{

    public Photos(JSONArray photos) {
        this();
        addAll(parse(photos));
    }

    public Photos() {
        super();
    }

    public static Photos parse(JSONArray photoArray) {
        Photos photos = new Photos();
        if (photoArray != null) {
            for (int i = 0; i < photoArray.length(); i++) {
                try {
                    photos.addFirst(new Photo(photoArray.getJSONObject(i)));
                } catch (JSONException e) {
                    Debug.error("Photo parse error", e);
                }
            }
        }

        return photos;
    }

    /**
     * Есть ли указанное фото в данном списке
     * NOTE: Проверка идет по id фотографии,
     * соответсвенно будет не корректно работать с фотошграфиями от разных пользователей
     *
     * @param photo объект фото
     * @return флаг наличия фотографии в списке
     */
    public boolean contains(Photo photo) {
        return getByPhotoId(photo.getId()) != null;
    }

    /**
     * Есть ли указанный индекс в списке фотографий
     *
     * @param index фотографии
     * @return флаг наличия фотографии в списке
     */
    public boolean contains(int index) {
        return size() >= index + 1;
    }

    public Photo getByPhotoId(int photoId) {
        Photo result = null;
        for (Photo photo : this) {
            if (photo != null && photoId == photo.getId()) {
                result = photo;
                break;
            }
        }
        return result;
    }

    public Photo getFirst() {
    	if (!this.isEmpty())
    		return this.get(0);
    	else return null;
    }
    
    public void addFirst(Photo value) {
    	if (this != null)
    		this.add(0,value);
    }
    
//	@Override
//	public int describeContents() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {		
//		dest.writeInt(this.size());
//		for (int i = 0; i < this.size(); i++) {
//			dest.writeParcelable(this.get(i), 0);
//		}
//	}
//	
//	@SuppressWarnings("rawtypes")
//	public static final Parcelable.Creator CREATOR =
//	    	new Parcelable.Creator() {
//	            public Photos createFromParcel(Parcel in) {
//	            	Photos photos = new Photos();
//	            	int size = in.readInt();
//	            	for (int i = 0; i < size; i++) {
//	            		Photo photo = in.readParcelable(Photo.class.getClassLoader()); 
//						photos.add(photo);
//					}	            		            	
//	            	
//	                return photos;
//	            }
//	 
//	            public Photos[] newArray(int size) {
//	                return new Photos[size];
//	            }
//	        };
}
