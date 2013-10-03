package com.topface.topface.ui.fragments.promo;


import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class PromoAdmirationsPopup extends PromoPopupFragment{
    @Override
    public Options.PremiumAirEntity getPremiumEntity() {
        return CacheProfile.getOptions().premium_admirations;
    }

    @Override
    public String getMainTag() {
        return "key_8_1";
    }

    @Override
    protected int getDeleteButtonText() {
        return R.string.delete_admirations;
    }

    @Override
    protected String getMessage() {
        int count = getPremiumEntity().getCount();
        return Utils.getQuantityString(getPluralForm(), count, count);
    }

    @Override
    protected int getPluralForm() {
        return R.plurals.popup_vip_admirations;
    }

    @Override
    protected void deleteMessages() {
    }
}
