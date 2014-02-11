package com.topface.topface.promo.dialogs;

import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class PromoKey31Dialog extends PromoDialog {
    @Override
    public Options.PromoPopupEntity getPremiumEntity() {
        return CacheProfile.getOptions().premiumMessages;
    }

    @Override
    public String getMainTag() {
        return "promo.key31";
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
}
