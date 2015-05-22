package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.utils.config.UserConfig;

public class Novice {
    public static boolean giveNoviceLikes = false;
    public static int giveNoviceLikesQuantity = 0;

    private final UserConfig mConfig;

    public Novice() {
        mConfig = App.getUserConfig();
    }

    public boolean isShowSympathiesBonus() {
        return Novice.giveNoviceLikes;
    }

    public void completeShowNoviceSympathiesBonus() {
        Novice.giveNoviceLikes = false;
    }
}
