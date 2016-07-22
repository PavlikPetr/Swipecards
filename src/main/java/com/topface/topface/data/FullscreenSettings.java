package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Модель наспроек фулскринов
 * Created by tiberal on 17.06.16.
 */
public class FullscreenSettings implements Parcelable {

    public static final String SDK = "SDK";
    public static final String IMG = "IMG";
    public static final String WEB = "WEB";
    public static final String PAGE = "PAGE";
    public static final String URL = "URL";
    public static final String METHOD = "METHOD";
    public static final String OFFERWALL = "OFFERWALL";
    public static final String PURCHASE = "PURCHASE";
    public static final String VIP = "VIP";

    /**
     * {Object} banner - информация о баннере для показа
     */
    public Banner banner;

    /**
     * {Number} nextRequestNoEarlierThen - минимальный интервал между показами стартового фул-скрин баннера в секундах
     */
    public long nextRequestNoEarlierThen;

    public FullscreenSettings(Parcel parcel) {
        banner = parcel.readParcelable(((Object) this).getClass().getClassLoader());
        nextRequestNoEarlierThen = parcel.readLong();
    }

    @SuppressWarnings("unused")
    public FullscreenSettings() {
    }

    public boolean isEmpty() {
        return banner == null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(banner, flags);
        dest.writeLong(nextRequestNoEarlierThen);
    }

    public static final Parcelable.Creator<FullscreenSettings> CREATOR = new Creator<FullscreenSettings>() {
        @Override
        public FullscreenSettings createFromParcel(Parcel in) {
            return new FullscreenSettings(in);
        }

        @Override
        public FullscreenSettings[] newArray(int size) {
            return new FullscreenSettings[size];
        }
    };

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FullscreenSettings)) return false;
        FullscreenSettings that = (FullscreenSettings) o;
        if (nextRequestNoEarlierThen != that.nextRequestNoEarlierThen) return false;
        return banner != null ? banner.equals(that.banner) : that.banner == null;
    }

    @Override
    public int hashCode() {
        int result = banner != null ? banner.hashCode() : 0;
        result = 31 * result + (int) (nextRequestNoEarlierThen ^ (nextRequestNoEarlierThen >>> 32));
        return result;
    }

    public static class Banner implements Parcelable {

        public String type;
        /**
         * {String} type - тип баннера: SDK, IMG или WEB
         */
        public String name;
        /**
         * {String} name - наименование баннера
         */
        public String url;
        /**
         * {String} url - строка URL отображения изображения баннера (ОПЦИОНАЛЬНО)
         */
        public String action;
        /**
         * {String} action - идентификатор возможных действий с баннером (ОПЦИОНАЛЬНО)
         */
        public String parameter;
        /**
         * {String} parameter - строка значения параметра действия (ОПЦИОНАЛЬНО)
         */

        public static final Parcelable.Creator<Banner> CREATOR = new Creator<Banner>() {
            @Override
            public Banner createFromParcel(Parcel in) {
                return new Banner(in);
            }

            @Override
            public Banner[] newArray(int size) {
                return new Banner[size];
            }
        };

        public Banner(Parcel parcel) {
            type = parcel.readString();
            name = parcel.readString();
            url = parcel.readString();
            action = parcel.readString();
            parameter = parcel.readString();
        }

        public Banner() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(type);
            dest.writeString(name);
            dest.writeString(url);
            dest.writeString(action);
            dest.writeString(parameter);
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Banner)) return false;
            Banner banner = (Banner) o;
            if (type != null ? !type.equals(banner.type) : banner.type != null) return false;
            if (name != null ? !name.equals(banner.name) : banner.name != null) return false;
            if (url != null ? !url.equals(banner.url) : banner.url != null) return false;
            if (action != null ? !action.equals(banner.action) : banner.action != null)
                return false;
            return parameter != null ? parameter.equals(banner.parameter) : banner.parameter == null;

        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + (action != null ? action.hashCode() : 0);
            result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
            return result;
        }
    }
}
