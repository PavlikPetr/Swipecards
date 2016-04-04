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
    public String pic1;
    @SerializedName("pic_2")
    public String pic2;
    @SerializedName("pic_3")
    public String pic3;

    public boolean isMale() {
        return MALE.equals(gender);
    }

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

    public class OkUserLocation {
        public String city;
        public String country;
        public String countryCode;
        public String countryName;
    }
}
