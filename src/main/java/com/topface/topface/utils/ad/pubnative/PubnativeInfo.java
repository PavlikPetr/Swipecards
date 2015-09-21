package com.topface.topface.utils.ad.pubnative;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.topface.topface.App;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.ad.RequestInfo;

/**
 * Info for requesting pubnative ad.
 */
public class PubnativeInfo extends RequestInfo {

    private String app_token = "7168f45f334c342de403ce56afdaa5d79961e4f5cd6abff59940fef194be9758";
    private String bundle_id = "com.topface.topface";
    private int zone_id;
    private int ad_count;
    private String os = "android";
    private double os_version = Build.VERSION.SDK_INT;
    private String device_model = Build.MODEL;
    private String locale;
    private String icon_size;
    private String banner_size;
    private String portrait_banner_size;
    private String device_resolution;
    private String device_type;
    private double lat;
    private double longitude;
    private String gender;
    private int age;
    private String android_advertiser_id;
    private int no_user_id;

    private PubnativeInfo(Context context) {
        Profile profile = App.from(context).getProfile();
        gender = profile.sex == 0 ? "female" : "male";
        age = profile.age;
    }

    public static class Builder {

        public static final String PHONE = "phone";
        public static final String TABLET = "tablet";

        private int mZoneId;
        private int mAdCount;
        private String mLocale;
        private String mIconSize;
        private String mBannerSize;
        private String mPortraitBannerSize;
        private String mDeviceResolution;
        private String mDeviceType;
        private double mLatitude;
        private double mLongitude;
        private String mAdId;

        @SuppressWarnings("unused")
        public Builder zoneId(int zoneId) {
            mZoneId = zoneId;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder adCount(int adCount) {
            mAdCount = adCount;
            return this;
        }

        public Builder locale(String locale) {
            mLocale = locale;
            return this;
        }

        public Builder displayMetrics(DisplayMetrics metrics) {
            mDeviceResolution = makeSize(metrics.widthPixels, metrics.heightPixels);
            int dpi = metrics.densityDpi;
            if (dpi >= DisplayMetrics.DENSITY_XXXHIGH) {
                mIconSize = makeSize(400, 400);
            } else if (dpi >= DisplayMetrics.DENSITY_XXHIGH) {
                mIconSize = makeSize(300, 300);
            } else if (dpi >= DisplayMetrics.DENSITY_XHIGH) {
                mIconSize = makeSize(256, 256);
            } else if (dpi >= DisplayMetrics.DENSITY_HIGH) {
                mIconSize = makeSize(200, 200);
            } else if (dpi >= DisplayMetrics.DENSITY_MEDIUM) {
                mIconSize = makeSize(150, 150);
            } else {
                mIconSize = makeSize(100, 100);
            }
            return this;
        }

        private String makeSize(int width, int height) {
            return width + "x" + height;
        }

        @SuppressWarnings("unused")
        public Builder iconSize(int width, int height) {
            mIconSize = makeSize(width, height);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder bannerSize(int width, int height) {
            mBannerSize = makeSize(width, height);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder portraitBannerSize(int width, int height) {
            mPortraitBannerSize = makeSize(width, height);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder deviceResolution(int width, int height) {
            mDeviceResolution = makeSize(width, height);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder deviceType(String type) {
            mDeviceType = TextUtils.equals(type, TABLET) ? TABLET : PHONE;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder latitude(double latitude) {
            mLatitude = latitude;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder longitude(double longitude) {
            mLongitude = longitude;
            return this;
        }

        public Builder adId(String adId) {
            mAdId = adId;
            return this;
        }

        public PubnativeInfo create(int dailyShows, Context context) {
            PubnativeInfo pubnativeInfo = new PubnativeInfo(context);
            pubnativeInfo.zone_id = mZoneId;
            pubnativeInfo.ad_count = mAdCount != 0 ? mAdCount : App.getUserConfig().getRemainedPubnativeShows(dailyShows);
            pubnativeInfo.locale = mLocale;
            pubnativeInfo.icon_size = mIconSize;
            pubnativeInfo.banner_size = mBannerSize;
            pubnativeInfo.portrait_banner_size = mPortraitBannerSize;
            pubnativeInfo.device_resolution = mDeviceResolution;
            pubnativeInfo.device_type = mDeviceType;
            pubnativeInfo.lat = mLatitude;
            pubnativeInfo.longitude = mLongitude;
            if (TextUtils.isEmpty(mAdId)) {
                pubnativeInfo.no_user_id = 1;
            } else {
                pubnativeInfo.android_advertiser_id = mAdId;
            }
            return pubnativeInfo;
        }

        public Builder location(Location location) {
            if (location != null) {
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
            }
            return this;
        }
    }

    @Override
    public String asRequestParameters() {
        StringBuilder builder = new StringBuilder("app_token=").append(app_token);
        builder.append("&bundle_id=").append(bundle_id);
        if (zone_id != 0) {
            builder.append("&zone_id=").append(zone_id);
        }
        builder.append("&ad_count=").append(ad_count);
        builder.append("&os=").append(os);
        builder.append("&os_version=").append(os_version);
        builder.append("&device_model=").append(device_model);
        if (locale != null) {
            builder.append("&locale=").append(locale);
        }
        if (icon_size != null) {
            builder.append("&icon_size=").append(icon_size);
        }
        if (banner_size != null) {
            builder.append("&banner_size=").append(banner_size);
        }
        if (portrait_banner_size != null) {
            builder.append("&portrait_banner_size=").append(portrait_banner_size);
        }
        if (device_resolution != null) {
            builder.append("&device_resolution=").append(device_resolution);
        }
        if (device_type != null) {
            builder.append("&device_type=").append(device_type);
        }
        if (lat != 0) {
            builder.append("&lat=").append(lat);
        }
        if (longitude != 0) {
            builder.append("&long=").append(longitude);
        }
        builder.append("&gender=").append(gender);
        if (age > 0) {
            builder.append("&age=").append(age);
        }
        if (TextUtils.isEmpty(android_advertiser_id)) {
            builder.append("&no_user_id=").append(no_user_id);
        } else {
            builder.append("&android_advertiser_id=").append(android_advertiser_id);
        }
        return builder.toString().replaceAll(" ", "%20");
    }
}
