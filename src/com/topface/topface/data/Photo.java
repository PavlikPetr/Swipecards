package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Фотографии пользователей из нашего стораджа фотографий (не напрямую из социальной сети)
 */
public class Photo extends AbstractData implements Parcelable{

    public static final String SIZE_ORIGINAL = "original";
    public static final String SIZE_64 = "c64x64";
    public static final String SIZE_150 = "r150x-";
    public static final String SIZE_64_ONLY = "c64x-";
    public static final String SIZE_128 = "c128x128";
    public static final String SIZE_192 = "c192x192";
    public static final String SIZE_256 = "c256x256";
    public static final String SIZE_960 = "r640x960";

    public static final int MIN_AVAILABLE_DIFFERENCE = 20;

    public static final String PHOTO_KEY_SIZE_PATTERN = "(\\d+|-)x(\\d+|-)";

    private String[] deprecatedSizes = {
            SIZE_64,
            SIZE_64_ONLY,
            SIZE_150
    };

    private class Size {
        public int width;
        public int height;

        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";

        public Size(int w, int h) {
            width = w;
            height = h;
        }

        public Size() {
            width = 0;
            height = 0;
        }

        public boolean isSquare() {
            return width == height;
        }

        public int getDifference(Size size) {
            return (size.getMaxSide().equals(WIDTH) || height == 0) && width != 0 ? Math.abs(width - size.width) : Math.abs(height - size.height);
        }

        public String getMaxSide() {
            if(width>height) return WIDTH;
            return HEIGHT;
        }
    }
    /**
     * идентификатор фотографии пользователя
     */
    protected int mId;
    private Pattern mPattern;

    public Photo(int id, HashMap<String, String> links) {
        this.mId = id;
        this.links = links;
    }

    public Photo(JSONObject data) {
        super(data);
    }

    @Override
    protected void fillData(JSONObject photoItem) {
        super.fillData(photoItem);

        if (photoItem.has("id")) {
            mId = photoItem.optInt("id");
            JSONObject linksJson = photoItem.optJSONObject("links");
            if (linksJson != null) {
                @SuppressWarnings("rawtypes")
				Iterator photoKeys = linksJson.keys();
                links = new HashMap<String, String>();

                while (photoKeys.hasNext()) {
                    String key = photoKeys.next().toString();

                    links.put(key, linksJson.optString(key));
                }
            }
        }
    }

    /**
     * ассоциативный массив ссылок на фотографии пользователя. Ключами элементов массива являются размеры фотографии пользователя в пикселах.
     * Значениями являются ссылки на фотографии пользователя с заданным размером.
     * Всегда присутствует ключ “original”, представляющий ссылку на исходное загруженное пользователем изображение
     */
    protected HashMap<String, String> links;

    public static Photo parse(JSONObject photoItem) {
        return new Photo(photoItem);
    }

    /**
     * Возвращает наиболее подходящий размер фотографии из уже существующих
     *
     *
     *
     * @param sizeString
     */
    public String getSuitableLink(String sizeString) {
        String url = null;
        if (links.containsKey(sizeString)) {
            url = links.get(sizeString);
        } else {
            Size size = getSizeFromKey(sizeString);
            getSuitableLink(size.width,size.height);
        }

        if (url == null && links.containsKey(SIZE_ORIGINAL)) {
            url = links.get(SIZE_ORIGINAL);
        }

        return url;
    }

    public String getSuitableLink(int width, int height) {
        String url = null;
        Size windowSize = new Size(width,height);
        if (links != null) {
            url = getSuitableLinkForSameImageForm(windowSize);
            if(url == null) {
                url = getSuitableLinkForAnotherForm(windowSize);
            }
        }
        return url;
    }

    private String getSuitableLinkForSameImageForm(Size windowSize) {
        String url = null;
        int minDifference = Integer.MAX_VALUE;
        int difference;

        for (HashMap.Entry<String, String> entry : links.entrySet()) {
            String entryKey = entry.getKey();
            //Не используем редкие размеры фотографий
            if (!isSizeDeprecated(entryKey)) {
                Size entrySize = getSizeFromKey(entryKey);

                if(entrySize.isSquare() == windowSize.isSquare()) {
                    difference = windowSize.getDifference(entrySize);
                    if (difference < minDifference && difference <= MIN_AVAILABLE_DIFFERENCE) {
                        minDifference = difference;
                        url = entry.getValue();
                    }
                }


            }
        }
        return url;
    }

    private String getSuitableLinkForAnotherForm(Size windowSize) {
        String url = null;
        int difference;
        int minDifference = Integer.MAX_VALUE;

        for(HashMap.Entry<String,String> entry : links.entrySet()) {
            String entryKey = entry.getKey();
            //Не используем редкие размеры фотографий
            if (!isSizeDeprecated(entryKey)) {
                Size entrySize = getSizeFromKey(entryKey);
                if(entrySize.isSquare() != windowSize.isSquare()) {
                    difference = windowSize.getDifference(entrySize);
                    if (difference < minDifference && difference<= MIN_AVAILABLE_DIFFERENCE) {
                        minDifference = difference;
                        url = entry.getValue();
                    }
                }

            }
        }
        if(url == null) {
            url = getAnySuitableLink(windowSize);
        }
        return url;
    }

    private String getAnySuitableLink(Size windowSize) {
        String url = null;
        int difference;
        int minDifference = Integer.MAX_VALUE;
        Debug.log("LINK::ANY");
        for(HashMap.Entry<String,String> entry : links.entrySet()) {
            String entryKey = entry.getKey();
            //Не используем редкие размеры фотографий
            if (!isSizeDeprecated(entryKey)) {
                Size entrySize = getSizeFromKey(entryKey);
                difference = windowSize.getDifference(entrySize);

                if (difference < minDifference) {
                    minDifference = difference;
                    url = entry.getValue();
                }
            }
        }

        if (url == null && links.containsKey(SIZE_ORIGINAL)) {
            url = links.get(SIZE_ORIGINAL);
        }
        return url;
    }

    protected Size getSizeFromKey(String key) {
        Size size = new Size();
        if (mPattern == null) {
            mPattern = Pattern.compile(PHOTO_KEY_SIZE_PATTERN);
        }

        Matcher matcher = mPattern.matcher(key);
        if (matcher.find()) {
            if(matcher.group(1).equals("-")) {
                size.width = 0;
            } else
                size.width = Integer.parseInt(matcher.group(1));
            if(matcher.group(2).equals("-")) {
                size.height = 0;
            } else
                size.height = Integer.parseInt(matcher.group(2));
        }

        return size;
    }

    private boolean isSizeDeprecated(String size) {
        for (String deprecatedSize : deprecatedSizes) {
            if (size.equals(deprecatedSize)) {
                return true;
            }
        }
        return false;
    }

    public int getId() {
        return mId;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeInt(links.size());
		for (String key : links.keySet()) {
			dest.writeString(key);
			dest.writeString(links.get(key));
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR =
	    	new Parcelable.Creator() {
	            public Photo createFromParcel(Parcel in) {
	            	int id = in.readInt();
	            	int hashSize = in.readInt();
	            	HashMap<String, String> links = new HashMap<String, String>();
	            	
	            	for (int i = 0; i < hashSize; i++) {
						links.put(in.readString(),in.readString());
					}
	            	
	                return new Photo(id,links);
	            }
	 
	            public Photo[] newArray(int size) {
	                return new Photo[size];
	            }
	        };
}
