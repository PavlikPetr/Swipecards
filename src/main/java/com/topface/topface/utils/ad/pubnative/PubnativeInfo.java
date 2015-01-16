package com.topface.topface.utils.ad.pubnative;

import android.os.Build;
import android.text.TextUtils;

import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ad.RequestInfo;

/**
 * Info for requesting mopub ad.
 */
public class PubnativeInfo extends RequestInfo {

    private String app_token = "7168f45f334c342de403ce56afdaa5d79961e4f5cd6abff59940fef194be9758";
    private String bundle_id = "com.topface.topface";
    private int zone_id;
    private int ad_count;
    private String os = "android";
    private double os_version = Build.VERSION.SDK_INT;
    private String device_model = Build.MODEL;
    private String icon_size;
    private String banner_size;
    private String portrait_banner_size;
    private String device_resolution;
    private String device_type;
    private double lat;
    private double longitude;
    private String gender = CacheProfile.sex == 0 ? "female" : "male";
    private int age = CacheProfile.age;
    private String android_advertiser_id;
    private int no_user_id;

    private PubnativeInfo() {
    }

    public static class Builder {

        public static final String PHONE = "phone";
        public static final String TABLET = "tablet";

        private int mZoneId;
        private int mAdCount;
        private String mIconSize;
        private String mBannerSize;
        private String mPortraitBannerSize;
        private String mDeviceResolution;
        private String mDeviceType;
        private double mLatitude;
        private double mLongitude;
        private String mAdId;

        public Builder zoneId(int zoneId) {
            mZoneId = zoneId;
            return this;
        }

        public Builder adCount(int adCount) {
            mAdCount = adCount;
            return this;
        }

        private String makeSize(int width, int height) {
            return width + "x" + height;
        }

        public Builder iconSize(int width, int height) {
            mIconSize = makeSize(width, height);
            return this;
        }

        public Builder bannerSize(int width, int height) {
            mBannerSize = makeSize(width, height);
            return this;
        }

        public Builder portraitBannerSize(int width, int height) {
            mPortraitBannerSize = makeSize(width, height);
            return this;
        }

        public Builder deviceResolution(int width, int height) {
            mDeviceResolution = makeSize(width, height);
            return this;
        }

        public Builder deviceType(String type) {
            mDeviceType = TextUtils.equals(type, TABLET) ? TABLET : PHONE;
            return this;
        }

        public Builder latitude(double latitude) {
            mLatitude = latitude;
            return this;
        }

        public Builder longitude(double longitude) {
            mLongitude = longitude;
            return this;
        }

        public Builder adId(String adId) {
            mAdId = adId;
            return this;
        }

        public PubnativeInfo create() {
            PubnativeInfo pubnativeInfo = new PubnativeInfo();
            pubnativeInfo.zone_id = mZoneId;
            pubnativeInfo.ad_count = mAdCount != 0 ? mAdCount : 4;
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
        builder.append("&age=").append(age);
        if (TextUtils.isEmpty(android_advertiser_id)) {
            builder.append("&no_user_id=").append(no_user_id);
        } else {
            builder.append("&android_advertiser_id=").append(android_advertiser_id);
        }
        return builder.toString().replaceAll(" ", "%20");
    }
}
