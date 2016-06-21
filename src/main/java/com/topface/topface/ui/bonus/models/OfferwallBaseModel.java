package com.topface.topface.ui.bonus.models;


import android.support.annotation.IntDef;


public abstract class OfferwallBaseModel {

    public static final int UNDEFINED = 0;
    public static final int FYBER = 1;
    public static final int SUPERSONIC = 2;

    @IntDef({UNDEFINED, FYBER, SUPERSONIC})
    @interface OfferwallType {

    }

    abstract String getTitle();

    abstract String getDescription();

    abstract int getRewardValue();

    abstract String getIconUrl();

    abstract String getLink();

    @OfferwallType
    abstract int getOfferwallsType();
}
