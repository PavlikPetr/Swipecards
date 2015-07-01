package com.topface.topface.promo.dialogs;

import com.topface.topface.R;
import com.topface.topface.data.Options;

public class PromoExpressMessages extends PromoDialog {
    @Override
    public Options.PromoPopupEntity getPremiumEntity() {
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

    @Override
    public String getMainTag() {
        return null;
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.promo_express_messages;
    }
}
