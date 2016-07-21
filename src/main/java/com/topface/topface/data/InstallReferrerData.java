package com.topface.topface.data;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ppavlik on 20.07.16.
 * данные из интента INSTALL_REFERRER
 */

public class InstallReferrerData {
    private String mData;

    public InstallReferrerData(@NotNull String referrerTrack) {
        mData = referrerTrack;
    }

    public String getInstallReferrerTrackData() {
        return mData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstallReferrerData that = (InstallReferrerData) o;

        return mData != null ? mData.equals(that.mData) : that.mData == null;

    }

    @Override
    public int hashCode() {
        return mData != null ? mData.hashCode() : 0;
    }

    public static boolean isEmpty(InstallReferrerData data) {
        return data == null || TextUtils.isEmpty(data.getInstallReferrerTrackData());
    }
}
