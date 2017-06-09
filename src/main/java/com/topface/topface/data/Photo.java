package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.topface.framework.imageloader.IPhoto;
import com.topface.framework.utils.Debug;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Фотографии пользователей из нашего стораджа фотографий (не напрямую из социальной сети)
 */
public class Photo extends AbstractData implements Parcelable, SerializableToJson, IPhoto {

    public static final String PHOTO_KEY_SIZE_PATTERN = "(\\d+|-)x(\\d+|-)";
    public static final int MAX_SQUARE_DIFFERENCE = 2;
    public static final float MAX_DIFFERENCE = 1.5f;
    public static final int SMALL_PHOTO_SIZE = 100;
    public static final String SIZE_128 = "c128x128";
    public static final String SIZE_192 = "c192x192";
    public static final String SIZE_256 = "c256x256";
    public static final String SIZE_960 = "r640x960";

    private transient HashMap<Interval, String> intervals;

    //Этот флаг нужен для того, чтобы ставить пустые фото в поиске, которые, будут подгружаться после запроса альбома
    private transient boolean isFakePhoto = false;

    public static final String SIZE_64_ONLY = "c64x-";
    public static final String SIZE_150 = "r150x-";
    public static final String SIZE_64 = "c64x64";
    public static final String SIZE_ORIGINAL = "original";
    private static String[] deprecatedSizes = {
            SIZE_64,
            SIZE_64_ONLY,
            SIZE_150,
            SIZE_ORIGINAL
    };

    private class Size {
        /**
         * Процент от максимального измерения фотографии,
         * отношение длины к ширине которое мы считаем допустимым при опредлении "квадратных" фотографий
         */
        public static final double SQUARE_MODIFICATOR = 0.10;
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

        @Override
        public boolean equals(Object o) {
            if (o instanceof Size) {
                Size s = (Size) o;
                return s.width == width && s.height == height;
            }

            return super.equals(o);    //To change body of overridden methods use File | Settings | File Templates.
        }

        public boolean isSquare() {
            //Если разница высоты и ширины меньше 10% от размера фотографии, то считаем ее условно квадратной
            return Math.abs(width - height) < Math.min(width, height) * SQUARE_MODIFICATOR;
        }

        public int getDifference(Size size) {
            return (size.getMaxSide().equals(WIDTH) || height == 0) && width != 0 ? Math.abs(width - size.width) : Math.abs(height - size.height);
        }

        public String getMaxSide() {
            return width > height ? WIDTH : HEIGHT;
        }

        public int getMaxSideSize() {
            return getMaxSide().equals(WIDTH) ? width : height;
        }
    }

    private class Interval {
        private Size minSize;
        private Size maxSize;

        public Interval(Size minSize, Size maxSize) {
            this.minSize = minSize;
            this.maxSize = maxSize;
        }

        public boolean isSizeInInterval(Size size) {
            return size.width > minSize.width && size.height > minSize.height
                    && size.width < maxSize.width && size.height < maxSize.height;
        }
    }

    /**
     * идентификатор фотографии пользователя
     */
    @SerializedName("id")
    protected int mId;
    private transient Pattern mPattern;
    public int liked;
    public boolean canBecomeLeader;
    public int position;

    public Photo(int id, HashMap<String, String> links, int position, int liked) {
        this.mId = id;
        this.links = links;
        this.liked = liked;
        this.position = position;
        initIntervals();
    }

    public Photo(Photo photo) {
        this.mId = photo.mId;
        this.liked = photo.liked;
        this.links = photo.links;
        this.canBecomeLeader = photo.canBecomeLeader;
        this.position = photo.position;
        initIntervals();
    }

    private void initIntervals() {
        intervals = new HashMap<>();
        intervals.put(new Interval(new Size(30, 30), new Size(128, 128)), SIZE_128);
        intervals.put(new Interval(new Size(129, 129), new Size(192, 192)), SIZE_192);
        intervals.put(new Interval(new Size(193, 193), new Size(256, 256)), SIZE_256);
    }

    public Photo() {
        links = new HashMap<>();
        initIntervals();
    }

    @Override
    public boolean isFake() {
        return isFakePhoto;
    }

    /**
     * ассоциативный массив ссылок на фотографии пользователя. Ключами элементов массива являются размеры фотографии пользователя в пикселах.
     * Значениями являются ссылки на фотографии пользователя с заданным размером.
     * Всегда присутствует ключ “original”, представляющий ссылку на исходное загруженное пользователем изображение
     */
    protected HashMap<String, String> links;

    /**
     * Возвращает наиболее подходящий размер фотографии из уже существующих
     */
    @Override
    public String getSuitableLink(String sizeString) {
        String url = null;
        if (links != null) {
            if (links.containsKey(sizeString)) {
                url = links.get(sizeString);
            } else {
                Size size = getSizeFromKey(sizeString);
                getSuitableLink(size.width, size.height);
            }

            if (url == null && links.containsKey(SIZE_ORIGINAL)) {
                url = links.get(SIZE_ORIGINAL);
            }
        }
        return url;
    }

    @Override
    public String getDefaultLink() {
        return getSuitableLink(SIZE_960);
    }

    @Override
    public String getSuitableLink(int width, int height) {
        String url = null;
        Size windowSize = new Size(width, height);

        if (links != null) {
            url = checkInterval(width, height);
            if (url == null) {
                url = getSuitableLinkForSameImageForm(windowSize);
                if (url == null) {
                    url = getSuitableLinkForAnotherForm(windowSize);
                }
            }
        }
        return url;
    }

    private String checkInterval(int width, int height) {
        String targetSize = null;
        for (HashMap.Entry<Interval, String> entry : intervals.entrySet()) {
            if (entry.getKey().isSizeInInterval(new Size(width, height))) {
                targetSize = entry.getValue();
                break;
            }
        }

        if (targetSize != null) {
            return getUrlByTargetSize(targetSize);
        }
        return null;
    }

    private String getUrlByTargetSize(String target) {
        for (HashMap.Entry<String, String> entry : links.entrySet()) {
            if (entry.getKey().equals(target)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String getSuitableLinkForSameImageForm(Size windowSize) {
        String url = null;
        int minDifference = Integer.MAX_VALUE;
        int difference;
        boolean windowSquare = windowSize.isSquare();

        for (HashMap.Entry<String, String> entry : links.entrySet()) {
            String entryKey = entry.getKey();
            //Не используем редкие размеры фотографий
            if (!isSizeDeprecated(entryKey)) {
                Size entrySize = getSizeFromKey(entryKey);

                if (windowSquare && entrySize.isSquare()) {
                    difference = windowSize.getDifference(entrySize);
                    //Стараемся
                    if ((difference < entrySize.getMaxSideSize() * MAX_SQUARE_DIFFERENCE && difference < minDifference) ||
                            (isSmallPhoto(entrySize) && difference < minDifference)) {
                        minDifference = difference;
                        url = entry.getValue();
                    }
                } else {
                    if (entrySize.isSquare() == windowSquare && !isSmallPhoto(entrySize)) {
                        difference = windowSize.getDifference(entrySize);
                        int entryMaxSize = entrySize.getMaxSideSize();
                        if (difference < minDifference && difference < entryMaxSize * MAX_DIFFERENCE) {
                            minDifference = difference;
                            url = entry.getValue();
                        }
                    }
                }


            }
        }
        return url;
    }

    private boolean isSmallPhoto(Size entrySize) {
        return entrySize.getMaxSideSize() < SMALL_PHOTO_SIZE;
    }

    private String getSuitableLinkForAnotherForm(Size windowSize) {
        String url = null;
        int difference;
        int minDifference = Integer.MAX_VALUE;

        for (HashMap.Entry<String, String> entry : links.entrySet()) {
            String entryKey = entry.getKey();
            //Не используем редкие размеры фотографий
            if (!isSizeDeprecated(entryKey)) {
                Size entrySize = getSizeFromKey(entryKey);
                if (entrySize.isSquare() != windowSize.isSquare()) {
                    difference = windowSize.getDifference(entrySize);
                    if (difference < minDifference && difference < entrySize.getMaxSideSize() * MAX_DIFFERENCE) {
                        minDifference = difference;
                        url = entry.getValue();
                    }
                }

            }
        }
        if (url == null) {
            url = getAnySuitableLink(windowSize);
        }
        return url;
    }

    private String getAnySuitableLink(Size windowSize) {
        String url = null;
        int difference;
        int minDifference = Integer.MAX_VALUE;
        Debug.log("LINK::ANY");
        for (HashMap.Entry<String, String> entry : links.entrySet()) {
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
        if (SIZE_ORIGINAL.equals(key)) {
            size.width = Integer.MAX_VALUE;
            size.height = Integer.MAX_VALUE;
        } else {
            if (mPattern == null) {
                mPattern = Pattern.compile(PHOTO_KEY_SIZE_PATTERN);
            }

            Matcher matcher = mPattern.matcher(key);
            if (matcher.find()) {
                if (matcher.group(1).equals("-")) {
                    size.width = 0;
                } else
                    size.width = Integer.parseInt(matcher.group(1));
                if (matcher.group(2).equals("-")) {
                    size.height = 0;
                } else
                    size.height = Integer.parseInt(matcher.group(2));
            }
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

    public int getPosition() {
        return position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(links.size());
        dest.writeInt(liked);
        dest.writeInt(position);
        for (String key : links.keySet()) {
            dest.writeString(key);
            dest.writeString(links.get(key));
        }

    }

    @SuppressWarnings({"rawtypes", "UnusedDeclaration"})
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Photo createFromParcel(Parcel in) {
                    int id = in.readInt();
                    int hashSize = in.readInt();
                    int liked = in.readInt();
                    int pos = in.readInt();
                    HashMap<String, String> links = new HashMap<>();

                    for (int i = 0; i < hashSize; i++) {
                        links.put(in.readString(), in.readString());
                    }
                    return new Photo(id, links, pos, liked);
                }

                public Photo[] newArray(int size) {
                    return new Photo[size];
                }
            };

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (!isFakePhoto) {
            json.put("id", mId);
            json.put("liked", liked);
            json.put("position", position);
            JSONObject jsonLinks = new JSONObject();
            if (links != null) {
                for (Map.Entry<String, String> entry : links.entrySet()) {
                    jsonLinks.put(
                            entry.getKey(),
                            entry.getValue()
                    );
                }
            }
            json.put("links", jsonLinks);
        } else {
            json.put("fake", true);
        }
        return json;
    }

    @Override
    public void fromJSON(String json) {

    }

    public boolean isEmpty() {
        return links == null || links.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Photo) {
            Photo p = (Photo) o;
            return p.mId == mId && p.isFakePhoto == isFakePhoto;
        }
        return false;
    }

    //Создаем фейковую фоточку
    public static Photo createFakePhoto() {
        Photo photo = new Photo();
        photo.isFakePhoto = true;
        return photo;
    }

    public static Photo createPhotoFromGCMUser(GCMUtils.User user) {
        Photo photo = new Photo();
        photo.links.put(SIZE_128, user.photoUrl);
        return photo;
    }

}
