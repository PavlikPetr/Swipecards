package com.topface.topface.data.leftMenu;

/**
 * Created by ppavlik on 06.05.16.
 * Extended fragment settings for integrated items
 */
public class IntegrationSettingsData extends LeftMenuSettingsData {

    private int mPos;
    private String mUrl;
    private boolean mIsExternal;

    /**
     * create new integration fragment settings
     *
     * @param fragmentId  unique fragment id
     * @param isOverlayed overlay flag
     * @param pos         local integration position
     * @param url         integration url
     * @param isExternal  is need to show outside the app
     */
    public IntegrationSettingsData(@FragmentIdData.FragmentId int fragmentId, boolean isOverlayed, int pos, String url, boolean isExternal) {
        super(fragmentId, isOverlayed);
        mPos = pos;
        mUrl = url;
        mIsExternal = isExternal;
    }

    /**
     * get local integration item position
     *
     * @return integration position
     */
    public int getPos() {
        return mPos;
    }

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

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IntegrationSettingsData)) return false;
        if (!super.equals(o)) return false;
        IntegrationSettingsData data = (IntegrationSettingsData) o;
        if (mPos != data.mPos) return false;
        if (mUrl != null ? !mUrl.equals(data.getUrl()) : data.getUrl() != null) return false;
        return mIsExternal == data.isExternal();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mPos;
        result = 31 * result + (mUrl != null ? mUrl.hashCode() : 0);
        return 31 * result + (mIsExternal ? 1 : 0);
    }
}
