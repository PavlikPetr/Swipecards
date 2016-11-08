package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;

/**
 * Покупка вип в профиле пользователя
 * Created by ppavlik on 08.11.16.
 */

public class BuyVipInsideProfile extends VipBuyFragment {
    @Override
    protected String getTitle() {
        return App.get().getProfile().getNameAndAge();
    }

    @Override
    protected String getSubtitle() {
        return App.get().getProfile().city.getName();
    }

    @Override
    protected Boolean isOnline() {
        return true;
    }
}
