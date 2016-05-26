package com.topface.topface.data;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OkStatsResponseData {

    @SerializedName("processed")
    private int mProcessed = 0;
    @SerializedName("errors")
    private HashMap<String, String> mErrors = new HashMap<>();

    public OkStatsResponseData() {
    }

    public int getProcessed() {
        return mProcessed;
    }

    @NotNull
    public HashMap<String, String> getErrors() {
        return mErrors;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OkStatsResponseData)) return false;
        OkStatsResponseData data = (OkStatsResponseData) o;
        if (mProcessed != data.getProcessed()) return false;
        return mErrors.equals(data.getErrors());
    }

    @Override
    public int hashCode() {
        int res = mProcessed;
        res = (res * 31) + mErrors.hashCode();
        return res;
    }
}