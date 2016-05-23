package com.topface.topface.data.leftMenu;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ppavlik on 06.05.16.
 * Extended fragment settings for integrated items
 */
public class IntegrationSettingsData extends LeftMenuSettingsData implements Parcelable {

    private static final int UNIQUE_KEY_CAPACITY = 100; // just a factor to create uniqueKey.xxyy - where xx - it's integration item position, and yy - fragmentId

    private int mPos;
    private String mUrl;
    private boolean mIsExternal;
    private String mPageName;

    /**
     * create new integration fragment settings
     *
     * @param fragmentId unique fragment id
     * @param pos        local integration position
     * @param url        integration url
     * @param isExternal is need to show outside the app
     * @param pageName   current integration page name
     */
    public IntegrationSettingsData(@FragmentIdData.FragmentId int fragmentId, int pos, String url, boolean isExternal, String pageName) {
        super(fragmentId);
        mPos = pos;
        mUrl = url;
        mIsExternal = isExternal;
        mPageName = pageName;
    }

    /**
     * get local integration item position
     *
     * @return integration position
     */
    public int getPos() {
        return mPos;
    }

    /**
     * get integration url
     *
     * @return integration url
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * get mode to show the link
     *
     * @return show in external browser if true
     */
    public boolean isExternal() {
        return mIsExternal;
    }

    /**
     * get unique fragment id key
     *
     * @return unique key
     */
    @Override
    public int getUniqueKey() {
        return super.getUniqueKey() + mPos * UNIQUE_KEY_CAPACITY;
    }

    /**
     * Get current integration page name
     *
     * @return page name
     */
    public String getPageName() {
        return mPageName;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IntegrationSettingsData)) return false;
        if (!super.equals(o)) return false;
        IntegrationSettingsData data = (IntegrationSettingsData) o;
        if (mPos != data.mPos) return false;
        if (mUrl != null ? !mUrl.equals(data.getUrl()) : data.getUrl() != null) return false;
        if (mPageName != null ? !mPageName.equals(data.getPageName()) : data.getPageName() != null)
            return false;
        return mIsExternal == data.isExternal();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mPos;
        result = 31 * result + (mUrl != null ? mUrl.hashCode() : 0);
        result = 31 * result + (mPageName != null ? mPageName.hashCode() : 0);
        return 31 * result + (mIsExternal ? 1 : 0);
    }

    protected IntegrationSettingsData(Parcel in) {
        super(in);
        mPos = in.readInt();
        mUrl = in.readString();
        mIsExternal = in.readInt() != 0;
        mPageName = in.readString();
    }

    public static final Parcelable.Creator<IntegrationSettingsData> CREATOR = new Parcelable.Creator<IntegrationSettingsData>() {
        @Override
        public IntegrationSettingsData createFromParcel(Parcel in) {
            return new IntegrationSettingsData(in);
        }

        @Override
        public IntegrationSettingsData[] newArray(int size) {
            return new IntegrationSettingsData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(mPos);
        out.writeString(mUrl);
        out.writeInt(mIsExternal ? 1 : 0);
        out.writeString(mPageName);
    }
}
