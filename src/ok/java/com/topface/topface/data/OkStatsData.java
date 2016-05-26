package com.topface.topface.data;


import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class OkStatsData {

    public static final String COUNTER = "counter";
    public static final String SELECT = "select";
    public static final String INTERVAL = "interval";
    public static final String STATUS = "status";
    public static final String LAUNCH = "launch";
    public static final String SIGNOUT = "signout";

    @StringDef({COUNTER, SELECT, INTERVAL, STATUS, LAUNCH, SIGNOUT})
    public @interface OkStatisticsTypes {

    }

    @SerializedName("time")
    private long mTime;
    @SerializedName("version")
    private String mVersion;
    @SerializedName("stats")
    private OkStatsObjectData[] mStatsDataList;

    public OkStatsData(long time, String version, OkStatsObjectData... statsDataList) {
        mTime = time;
        mVersion = version;
        mStatsDataList = statsDataList;
    }

    public long getTime() {
        return mTime;
    }

    public String getVersion() {
        return mVersion;
    }

    public OkStatsObjectData[] getStatsDataList() {
        return mStatsDataList;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OkStatsData)) return false;
        OkStatsData data = (OkStatsData) o;
        if (mTime != data.getTime()) return false;
        if (mVersion == null || !mVersion.equals(data.getVersion())) return false;
        return Arrays.equals(mStatsDataList, data.getStatsDataList());
    }

    @Override
    public int hashCode() {
        int res = (int) mTime;
        res = (res * 31) + (mVersion != null ? mVersion.hashCode() : 0);
        res = (res * 31) + Arrays.hashCode(mStatsDataList);
        return res;
    }

    public static class OkStatsObjectData {

        @SerializedName("id")
        private String mId;
        @SerializedName("time")
        private long mTime;
        @OkStatisticsTypes
        @SerializedName("type")
        private String mType;
        @SerializedName("data")
        private String[] mData;

        public OkStatsObjectData(String id, long time, @OkStatisticsTypes String type, String... data) {
            mId = id;
            mTime = time;
            mType = type;
            mData = data;
        }

        public String getId() {
            return mId;
        }

        public long getTime() {
            return mTime;
        }

        @OkStatisticsTypes
        public String getType() {
            return mType;
        }

        public String[] getData() {
            return mData;
        }

        @Override
        public int hashCode() {
            int res = mId != null ? mId.hashCode() : 0;
            res = (res * 31) + (int) mTime;
            res = (res * 31) + (mType != null ? mType.hashCode() : 0);
            res = (res * 31) + Arrays.hashCode(mData);
            return res;
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof OkStatsObjectData)) return false;
            OkStatsObjectData data = (OkStatsObjectData) o;
            if (mId == null || !mId.equals(data.getId())) return false;
            if (mTime != data.getTime()) return false;
            if (mType == null || !mType.equals(data.getType())) return false;
            return mData != null && Arrays.equals(mData, data.getData());
        }
    }

}