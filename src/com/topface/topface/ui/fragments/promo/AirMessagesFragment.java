package com.topface.topface.ui.fragments.promo;

import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class AirMessagesFragment extends PromoPopupFragment{
    @Override
    public Options.PremiumAirEntity getPremiumEntity() {
        return CacheProfile.getOptions().premium_messages;
    }

    @Override
    public String getMainTag() {
        return "AirMessages";
    }

    @Override
    protected int getDeleteButtonText() {
        return R.string.general_delete_messages;
    }

    @Override
    protected String getMessage() {
        int count = getPremiumEntity().getCount();
        return Utils.getQuantityString(getPluralForm(), count, count);
    }

    @Override
    protected int getPluralForm() {
        return R.plurals.popup_vip_messages;
    }

    @Override
    protected void deleteMessages() {

    }

    @Override
    protected String getTagForBuyingFragment() {
        return "VipDelivery";
    }
}
