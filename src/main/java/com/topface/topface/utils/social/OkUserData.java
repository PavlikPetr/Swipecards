package com.topface.topface.utils.social;

import com.google.gson.annotations.SerializedName;
import com.topface.topface.App;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Петр on 03.04.2016.
 * Ok user data class
 */
public class OkUserData {
    private static final String MALE = "male";
    private static final String DAY_OF_BIRTH_FORMAT = "yyyy-mm-dd";
    public String uid;
    public String birthday;
    public int age;
    public String name;
    public String locale;
    public String gender;
    public OkUserLocation location;
    public String online;
    @SerializedName("first_name")
    public String firstName;
    @SerializedName("last_name")
    public String lastName;
    @SerializedName("has_email")
    public boolean hasEmail;
    @SerializedName("pic_1")
    public String smallSquareImage;
    @SerializedName("pic_2")
    public String mediumImage;
    @SerializedName("pic_3")
    public String bigSquareImage;

    public boolean isMale() {
        return MALE.equals(gender);
    }

    @SuppressWarnings("unused")
    @Nullable
    public Date getDayOfBirth() {
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat(DAY_OF_BIRTH_FORMAT, App.getCurrentLocale());
        try {
            date = format.parse(birthday);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OkUserData)) return false;
        OkUserData data = (OkUserData) o;
        if (uid != null ? !uid.equals(data.uid) : data.uid != null) return false;
        if (birthday != null ? !birthday.equals(data.birthday) : data.birthday != null)
            return false;
        if (age != data.age) return false;
        if (name != null ? !name.equals(data.name) : data.name != null) return false;
        if (locale != null ? !locale.equals(data.locale) : data.locale != null) return false;
        if (gender != null ? !gender.equals(data.gender) : data.gender != null) return false;
        if (location != null ? !location.equals(data.location) : data.location != null)
            return false;
        if (online != null ? !online.equals(data.online) : data.online != null) return false;
        if (firstName != null ? !firstName.equals(data.firstName) : data.firstName != null)
            return false;
        if (lastName != null ? !lastName.equals(data.lastName) : data.lastName != null)
            return false;
        if (hasEmail != data.hasEmail) return false;
        if (smallSquareImage != null ? !smallSquareImage.equals(data.smallSquareImage) : data.smallSquareImage != null)
            return false;
        if (mediumImage != null ? !mediumImage.equals(data.mediumImage) : data.mediumImage != null)
            return false;
        return bigSquareImage != null ? !bigSquareImage.equals(data.bigSquareImage) : data.bigSquareImage != null;
    }

    @Override
    public int hashCode() {
        int result = uid != null ? uid.hashCode() : 0;
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (online != null ? online.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (hasEmail ? 1 : 0);
        result = 31 * result + (smallSquareImage != null ? smallSquareImage.hashCode() : 0);
        result = 31 * result + (mediumImage != null ? mediumImage.hashCode() : 0);
        return 31 * result + (bigSquareImage != null ? bigSquareImage.hashCode() : 0);
    }

    public class OkUserLocation {
        public String city;
        public String country;
        public String countryCode;
        public String countryName;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof OkUserLocation)) return false;
            OkUserLocation okAccessData = (OkUserLocation) o;
            return city.equals(okAccessData.city)
                    && country.equals(okAccessData.country)
                    && countryCode.equals(okAccessData.countryCode)
                    && countryName.equals(okAccessData.countryName);

        }

        @Override
        public int hashCode() {
            int result = city != null ? city.hashCode() : 0;
            result = 31 * result + (country != null ? country.hashCode() : 0);
            result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
            result = 31 * result + (countryName != null ? countryName.hashCode() : 0);
            return result;
        }
    }
}
