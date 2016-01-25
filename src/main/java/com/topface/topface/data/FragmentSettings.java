package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;

/**
 * Данный класс позволяет заадть настроки фрагмента, от enum пришлось отойти, т.к. интеграции в
 * левом меню требуют динамически создавать/корректировать данные
 */

public class FragmentSettings implements Parcelable {

    private static final HashMap<FragmentId, FragmentSettings> FRAGMENT_SETTINGS_MAP;

    static {
        FRAGMENT_SETTINGS_MAP = new HashMap<>();
        FRAGMENT_SETTINGS_MAP.put(FragmentId.VIP_PROFILE, new FragmentSettings(FragmentId.VIP_PROFILE));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.PROFILE, new FragmentSettings(FragmentId.PROFILE));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.DATING, new FragmentSettings(FragmentId.DATING, true));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.TABBED_DIALOGS, new FragmentSettings(FragmentId.TABBED_DIALOGS));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.TABBED_VISITORS, new FragmentSettings(FragmentId.TABBED_VISITORS));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.TABBED_LIKES, new FragmentSettings(FragmentId.TABBED_LIKES));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.PHOTO_BLOG, new FragmentSettings(FragmentId.PHOTO_BLOG));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.GEO, new FragmentSettings(FragmentId.GEO));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.BONUS, new FragmentSettings(FragmentId.BONUS));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.EDITOR, new FragmentSettings(FragmentId.EDITOR));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.SETTINGS, new FragmentSettings(FragmentId.SETTINGS));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.INTEGRATION_PAGE, new FragmentSettings(FragmentId.INTEGRATION_PAGE, 0));
        FRAGMENT_SETTINGS_MAP.put(FragmentId.UNDEFINED, new FragmentSettings(FragmentId.UNDEFINED));
    }

    public static FragmentSettings getFragmentSettings(@NotNull FragmentId id) {
        return getFragmentSettings(id, FragmentId.UNDEFINED);
    }

    public static FragmentSettings getFragmentSettings(@NotNull FragmentId id, @NotNull FragmentId defValue) {
        FragmentSettings fragmentSettings = FRAGMENT_SETTINGS_MAP.get(id);
        return fragmentSettings != null ? fragmentSettings : FRAGMENT_SETTINGS_MAP.get(defValue);
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
            mFragmentId = FragmentId.UNDEFINED;
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
