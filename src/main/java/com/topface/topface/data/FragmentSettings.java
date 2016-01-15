package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;

public class FragmentSettings implements Parcelable {

    private static final HashMap<FragmentId, FragmentSettings> map;

    static {
        map = new HashMap<>();
        map.put(FragmentId.VIP_PROFILE, new FragmentSettings(FragmentId.VIP_PROFILE));
        map.put(FragmentId.PROFILE, new FragmentSettings(FragmentId.PROFILE));
        map.put(FragmentId.DATING, new FragmentSettings(FragmentId.DATING, true));
        map.put(FragmentId.TABBED_DIALOGS, new FragmentSettings(FragmentId.TABBED_DIALOGS));
        map.put(FragmentId.TABBED_VISITORS, new FragmentSettings(FragmentId.TABBED_VISITORS));
        map.put(FragmentId.TABBED_LIKES, new FragmentSettings(FragmentId.TABBED_LIKES));
        map.put(FragmentId.PHOTO_BLOG, new FragmentSettings(FragmentId.PHOTO_BLOG));
        map.put(FragmentId.GEO, new FragmentSettings(FragmentId.GEO));
        map.put(FragmentId.BONUS, new FragmentSettings(FragmentId.BONUS));
        map.put(FragmentId.EDITOR, new FragmentSettings(FragmentId.EDITOR));
        map.put(FragmentId.SETTINGS, new FragmentSettings(FragmentId.SETTINGS));
        map.put(FragmentId.INTEGRATION_PAGE, new FragmentSettings(FragmentId.INTEGRATION_PAGE, 0));
        map.put(FragmentId.UNDEFINED, new FragmentSettings(FragmentId.UNDEFINED));
    }

    public static FragmentSettings getFragmentSettings(FragmentId id) {
        return getFragmentSettings(id, FragmentId.UNDEFINED);
    }

    public static FragmentSettings getFragmentSettings(FragmentId id, FragmentId defValue) {
        FragmentSettings fragmentSettings = map.get(id);
        return fragmentSettings != null ? fragmentSettings : map.get(defValue);
    }

    private FragmentId mFragmentId;
    private boolean mIsOverlayed;
    private int mPos;

    public FragmentSettings(FragmentId number) {
        this(number, false);
    }

    public FragmentSettings(FragmentId fragmentId, boolean isOverlayed) {
        mFragmentId = fragmentId;
        mIsOverlayed = isOverlayed;
    }

    public FragmentSettings(FragmentId fragmentId, int pos) {
        mFragmentId = fragmentId;
        mPos = pos;
    }

    protected FragmentSettings(Parcel in) {
        try {
            mFragmentId = FragmentId.valueOf(in.readString());
        } catch (IllegalArgumentException x) {
            mFragmentId = null;
        }
        mIsOverlayed = in.readByte() != 0;
        mPos = in.readInt();
    }

    public static final Creator<FragmentSettings> CREATOR = new Creator<FragmentSettings>() {
        @Override
        public FragmentSettings createFromParcel(Parcel in) {
            return new FragmentSettings(in);
        }

        @Override
        public FragmentSettings[] newArray(int size) {
            return new FragmentSettings[size];
        }
    };

    public int getPos() {
        return mPos;
    }

    public FragmentId getFragmentId() {
        return mFragmentId;
    }

    public final int getId() {
        return mFragmentId.getId();
    }

    public boolean isOverlayed() {
        return mIsOverlayed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mFragmentId == null ? FragmentId.UNDEFINED.name() : mFragmentId.name());
        out.writeInt(mIsOverlayed ? 1 : 0);
        out.writeInt(mPos);
    }
}
