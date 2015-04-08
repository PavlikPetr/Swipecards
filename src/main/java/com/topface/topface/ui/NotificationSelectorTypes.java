package com.topface.topface.ui;

import com.topface.topface.R;

/**
 * Created by saharuk on 17.03.15.
 */
public enum NotificationSelectorTypes {
    ON_PHONE(0, R.string.on_phone),
    ON_MAIL(1, R.string.on_mail);

    private int mNumber;
    private int mNameId;

    NotificationSelectorTypes(int number, int nameId) {
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
