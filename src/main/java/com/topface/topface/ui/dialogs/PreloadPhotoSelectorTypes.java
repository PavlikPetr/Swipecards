package com.topface.topface.ui.dialogs;

import com.topface.topface.R;

public enum PreloadPhotoSelectorTypes {
    PRELOAD_OFF(0, R.string.preload_photo_type_preload_off),
    WIFI(1, R.string.preload_photo_type_wifi),
    WIFI_3G(2, R.string.preload_photo_type_wifi_3g),
    ALWAYS_ON(3, R.string.preload_photo_type_always_on);

    private int mNumber;
    private int mNameId;

    PreloadPhotoSelectorTypes(int number, int nameId) {
        mNumber = number;
        mNameId = nameId;
    }

    public int getId() {
        return mNumber;
    }

    public int getName() {
        return mNameId;
    }
}
