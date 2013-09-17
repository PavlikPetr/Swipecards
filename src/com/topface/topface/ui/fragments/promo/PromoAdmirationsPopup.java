package com.topface.topface.ui.fragments.promo;


import com.topface.topface.data.Options;

public class PromoAdmirationsPopup extends PromoPopupFragment{
    @Override
    public Options.PremiumAirEntity getPremiumEntity() {
        return null;
    }

    @Override
    public String getMainTag() {
        return null;
    }

    @Override
    protected int getDeleteButtonText() {
        return 0;
    }

    @Override
    protected String getMessage() {
        return null;
    }

    @Override
    protected int getPluralForm() {
        return 0;
    }

    @Override
    protected void deleteMessages() {
    }
}
