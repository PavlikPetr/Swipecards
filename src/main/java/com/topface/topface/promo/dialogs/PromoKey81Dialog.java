package com.topface.topface.promo.dialogs;

import android.view.View;

import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.AdmirationsReadedRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class PromoKey81Dialog extends PromoDialog {

    private boolean counterUpdated;

    @Override
    public void initViews(View root) {
        super.initViews(root);
        int curVisitCounter = mCountersData.admirations;
        if (curVisitCounter == 0) {
            mCountersData.visitors = mCountersData.admirations + getPremiumEntity().getCount();
            mAppState.setData(mCountersData);
            counterUpdated = true;
        }
    }

    @Override
    public Options.PromoPopupEntity getPremiumEntity() {
        return CacheProfile.getOptions().premiumAdmirations;
    }

    @Override
    public String getMainTag() {
        return "promo.key81";
    }

    @Override
    protected int getDeleteButtonText() {
        return R.string.delete_admirations;
    }

    @Override
    protected String getMessage() {
        int count = getPremiumEntity().getCount();
        int admirations = mCountersData.admirations;
        count = admirations > 0 ? admirations : count;
        return Utils.getQuantityString(getPluralForm(), count, count);
    }

    @Override
    protected int getPluralForm() {
        return R.plurals.popup_vip_admirations;
    }

    @Override
    protected void deleteMessages() {
        AdmirationsReadedRequest request = new AdmirationsReadedRequest(getActivity());
        if (counterUpdated) {
            mCountersData.visitors = mCountersData.admirations - getPremiumEntity().getCount();
            mAppState.setData(mCountersData);
        }
        request.exec();
    }
}
