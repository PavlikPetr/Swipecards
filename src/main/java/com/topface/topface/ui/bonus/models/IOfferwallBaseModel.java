package com.topface.topface.ui.bonus.models;


import android.support.annotation.IntDef;

import org.jetbrains.annotations.NotNull;


public interface IOfferwallBaseModel {

    public static final int UNDEFINED = 0;
    public static final int FYBER = 1;
    public static final int IRON_SOURCE = 2;

    @IntDef({UNDEFINED, FYBER, IRON_SOURCE})
    @interface OfferwallType {

    }

    @NotNull
    public String getTitle();

    @NotNull
    public String getDescription();

    public int getRewardValue();

    @NotNull
    public String getIconUrl();

    @NotNull
    public String getLink();

    @OfferwallType
    public int getOfferwallsType();
}
