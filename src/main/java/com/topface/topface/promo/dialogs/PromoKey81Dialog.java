package com.topface.topface.promo.dialogs;

import android.os.Bundle;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.AdmirationsReadedRequest;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.utils.Utils;

public class PromoKey81Dialog extends PromoDialog {

    private final static String POPUP_NAME = "promo.express.admirations";

    private boolean counterUpdated;
    private TopfaceAppState mAppState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppState = App.getAppComponent().appState();
    }

    @Override
    protected String getPopupName() {
        return POPUP_NAME;
    }

    @Override
    public void initViews(View root) {
        super.initViews(root);
        int curVisitCounter = mCountersData.getAdmirations();
        if (curVisitCounter == 0) {
            mCountersData.setVisitors(curVisitCounter + getPremiumEntity().getCount());
            mAppState.setData(mCountersData);
            counterUpdated = true;
        }
    }

    @Override
    public Options.PromoPopupEntity getPremiumEntity() {
        return App.from(App.getContext()).getOptions().premiumAdmirations;
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
        int admirations = mCountersData.getAdmirations();
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
            mCountersData.setVisitors(mCountersData.getAdmirations() - getPremiumEntity().getCount());
            mAppState.setData(mCountersData);
        }
        request.exec();
    }
}
