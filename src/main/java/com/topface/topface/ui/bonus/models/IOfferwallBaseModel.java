package com.topface.topface.ui.bonus.models;


import android.support.annotation.StringDef;

import org.jetbrains.annotations.NotNull;


public interface IOfferwallBaseModel {

    public static final String UNDEFINED = "";
    public static final String FYBER = "FYBER";
    public static final String IRON_SOURCE = "IRON_SOURCE";

    @StringDef({UNDEFINED, FYBER, IRON_SOURCE})
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

    @NotNull
    @OfferwallType
    public String getOfferwallsType();
}
