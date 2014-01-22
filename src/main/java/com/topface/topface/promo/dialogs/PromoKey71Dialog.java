package com.topface.topface.promo.dialogs;

import android.view.View;

import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.VisitorsMarkReadedRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;

public class PromoKey71Dialog extends PromoDialog {

    private boolean counterUpdated;

    @Override
    public void initViews(View root) {
        super.initViews(root);
        int curVisitCounter = CountersManager.getInstance(getActivity()).getCounter(CountersManager.VISITORS);
        if (curVisitCounter == 0) {
            CountersManager.getInstance(getActivity()).setCounter(CountersManager.VISITORS, curVisitCounter + getPremiumEntity().getCount(), true);
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
        int guests = CountersManager.getInstance(getActivity()).getCounter(CountersManager.VISITORS);
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
        int curVisitCounter = CountersManager.getInstance(getActivity()).getCounter(CountersManager.VISITORS);
        if (counterUpdated) {
            CountersManager.getInstance(getActivity()).setCounter(CountersManager.VISITORS, curVisitCounter - getPremiumEntity().getCount(), true);
        }
    }
}
