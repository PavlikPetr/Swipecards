package com.topface.topface.ui.fragments.promo;

import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.VisitorsMarkReadedRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;

public class PromoVisitorsPopup extends PromoPopupFragment{
    @Override
    public Options.PremiumAirEntity getPremiumEntity() {
        return CacheProfile.getOptions().premium_visitors;
    }

    @Override
    public String getMainTag() {
        return "key_7_1";
    }

    @Override
    protected int getDeleteButtonText() {
        return R.string.delete_visitors;
    }

    @Override
    protected String getMessage() {
        int count = getPremiumEntity().getCount();
        int guests = CountersManager.getInstance(getActivity()).getCounter(CountersManager.VISITORS);
        count = guests > 0? guests:count;
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
        CountersManager.getInstance(getActivity()).setCounter(CountersManager.VISITORS, curVisitCounter - getPremiumEntity().getCount(), true);
    }
}
