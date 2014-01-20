package com.topface.topface.promo.fragments;

import android.view.View;

import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.AdmirationsReadedRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;

public class PromoKey81Fragment extends PromoFragment {

    private boolean counterUpdated;

    @Override
    public void initViews(View root) {
        super.initViews(root);
        int curVisitCounter = CountersManager.getInstance(getActivity()).getCounter(CountersManager.ADMIRATIONS);
        if (curVisitCounter == 0) {
            CountersManager.getInstance(getActivity()).setCounter(CountersManager.ADMIRATIONS, curVisitCounter + getPremiumEntity().getCount(), true);
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
        int admirations = CountersManager.getInstance(getActivity()).getCounter(CountersManager.ADMIRATIONS);
        count = admirations > 0 ? admirations : count;
        return Utils.getQuantityString(getPluralForm(), count, count);
    }

    @Override
    protected int getPluralForm() {
        return R.plurals.popup_vip_admirations;
    }

    @Override
    protected void deleteMessages() {
        int curVisitCounter = CountersManager.getInstance(getActivity()).getCounter(CountersManager.ADMIRATIONS);
        AdmirationsReadedRequest request = new AdmirationsReadedRequest(getActivity());
        if (counterUpdated) {
            CountersManager.getInstance(getActivity()).setCounter(CountersManager.ADMIRATIONS, curVisitCounter - getPremiumEntity().getCount(), true);
        }
        request.exec();
    }
}
