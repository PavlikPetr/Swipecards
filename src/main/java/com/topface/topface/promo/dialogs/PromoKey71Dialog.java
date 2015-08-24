package com.topface.topface.promo.dialogs;

import android.os.Bundle;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.VisitorsMarkReadedRequest;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import javax.inject.Inject;

public class PromoKey71Dialog extends PromoDialog {

    private boolean counterUpdated;
    @Inject
    TopfaceAppState mAppState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
    }

    @Override
    public void initViews(View root) {
        super.initViews(root);
        int curVisitCounter = mCountersData.visitors;
        if (curVisitCounter == 0) {
            mCountersData.visitors = mCountersData.visitors + getPremiumEntity().getCount();
            mAppState.setData(mCountersData);
            counterUpdated = true;
        }
    }

    @Override
    public Options.PromoPopupEntity getPremiumEntity() {
        return CacheProfile.getOptions().premiumVisitors;
    }

    @Override
    public String getMainTag() {
        return "promo.key71";
    }

    @Override
    protected int getDeleteButtonText() {
        return R.string.delete_visitors;
    }

    @Override
    protected String getMessage() {
        int count = getPremiumEntity().getCount();
        int guests = mCountersData.visitors;
        count = guests > 0 ? guests : count;
        return Utils.getQuantityString(getPluralForm(), count, count);
    }

    @Override
    protected int getPluralForm() {
        return R.plurals.popup_vip_visitors;
    }

    @Override
    protected void deleteMessages() {
        //Отправляем запрос удаления гостей
        VisitorsMarkReadedRequest request = new VisitorsMarkReadedRequest(getActivity());
        request.exec();
        //Откручиваем счетчик назад
        if (counterUpdated) {
            mCountersData.visitors = mCountersData.visitors - getPremiumEntity().getCount();
            mAppState.setData(mCountersData);
        }
    }
}
