package com.topface.topface.promo.dialogs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.Options;
import com.topface.topface.state.CountersDataProvider;
import com.topface.topface.statistics.PromoDialogStastics;
import com.topface.topface.statistics.PromoDialogUniqueStatistics;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.FlurryManager;

import org.jetbrains.annotations.Nullable;

import static com.topface.topface.utils.FlurryManager.PayWallAction.*;


public abstract class PromoDialog extends AbstractDialogFragment implements View.OnClickListener, IPromoPopup {

    private OnCloseListener mListener;
    protected CountersData mCountersData;
    private CountersDataProvider mCountersDataProvider;

    public abstract Options.PromoPopupEntity getPremiumEntity();

    @Override
    protected boolean isModalDialog() {
        return false;
    }

    @Override
    public boolean isUnderActionBar() {
        return true;
    }

    @Nullable
    protected String getPopupName() {
        return null;
    }

    /**
     * @return id строки из ресурсов, которую нужно показать на кнопке удаления
     */
    protected abstract int getDeleteButtonText();

    /**
     * @return id строки из ресурсов, которую нужно показать как описание попапа
     */
    protected abstract String getMessage();

    @SuppressWarnings("unused")
    protected abstract int getPluralForm();

    protected abstract void deleteMessages();

    /**
     * @return Ключ статистики для данного фрагмента
     */
    public abstract String getMainTag();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCountersDataProvider = new CountersDataProvider(this);
        //Закрыть диалог нельзя
        setCancelable(false);
        //Подписываемся на обновление профиля
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(
                        mProfileReceiver,
                        new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION)
                );
        //Подписываемся на событие покупки VIP
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(
                        mVipPurchasedReceiver,
                        new IntentFilter(VipBuyFragment.VIP_PURCHASED_INTENT)
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        //Отключаем боковое меню
        FragmentActivity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK
                && requestCode == CountersDataProvider.COUNTERS_DATA_UPDATED) {
            if (data.hasExtra(CountersDataProvider.COUNTERS_DATA)) {
                mCountersData = data.getParcelableExtra(CountersDataProvider.COUNTERS_DATA);
            }
        }
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.promo_popup;
    }

    @Override
    public void initViews(View root) {
        root.setClickable(true);
        root.findViewById(R.id.buyVip).setOnClickListener(this);
        ((TextView) root.findViewById(R.id.deleteMessages)).setText(getDeleteButtonText());
        root.findViewById(R.id.deleteMessages).setOnClickListener(this);

        TextView popupText = (TextView) root.findViewById(R.id.airMessagesText);
        popupText.setText(getMessage());

        FlurryManager.sendPayWallEvent(getPopupName(), SHOW);
        EasyTracker.sendEvent(getMainTag(), "Show", "", 0L);
        PromoDialogStastics.promoDialogShowSend(getMainTag());
        PromoDialogUniqueStatistics.send(getMainTag());
    }

    private BroadcastReceiver mVipPurchasedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.log("Promo: Close fragment after VIP buy");
            closeFragment();
            EasyTracker.sendEvent(getMainTag(), "VipClose", "CloseAfterBuyVip", 1L);
            PromoDialogStastics.promoDialogCloseAfterBuyVipSend(getMainTag());
        }
    };
    private BroadcastReceiver mProfileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Если мы узнаем что пользователь премиум после обновления профиля, то закрываем фрагмент
            if (CacheProfile.premium) {
                Debug.log("Promo: Close fragment after profile update");
                closeFragment();
                EasyTracker.sendEvent(getMainTag(), "VipClose", "CloseAfterUpdateProfile", 1L);
                PromoDialogStastics.promoDialogCloseAfterUpdateProfileSend(getMainTag());
                FlurryManager.sendPayWallEvent(getPopupName(), PRODUCT_BOUGHT);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buyVip:
                startActivityForResult(
                        PurchasesActivity.createVipBuyIntent(getMessage(), getTagForBuyingFragment()),
                        PurchasesActivity.INTENT_BUY_VIP
                );
                EasyTracker.sendEvent(getMainTag(), "ClickBuyVip", "", 0L);
                PromoDialogStastics.promoDialogClickBuyVipSend(getMainTag());
                FlurryManager.sendPayWallEvent(getPopupName(), CLICK_BUY);
                break;
            case R.id.deleteMessages:
                deleteMessages();
                EasyTracker.sendEvent(getMainTag(), "Dismiss", "Delete", 0L);
                PromoDialogStastics.promoDialogDismissSend(getMainTag());
                FlurryManager.sendPayWallEvent(getPopupName(), CLICK_DELETE);
                closeFragment();
                break;
            default:

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mVipPurchasedReceiver);
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileReceiver);
        }
    }

    protected String getTagForBuyingFragment() {
        return getMainTag();
    }

    private void closeFragment() {
        if (mListener != null) {
            mListener.onClose();
        }

        //Отмечаем время закрытия попапа
        Options.PromoPopupEntity promoPopupEntity = getPremiumEntity();
        if (promoPopupEntity != null) {
            promoPopupEntity.setPopupShowTime();
        }

        FragmentActivity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(false);
        }

        dismissAllowingStateLoss();
    }

    public interface OnCloseListener {
        void onClose();
    }

    @Override
    public void setOnCloseListener(OnCloseListener listener) {
        mListener = listener;
    }

    /**
     * @see <a href="https://code.google.com/p/android/issues/detail?id=17423">Баг с retainInstance</a>
     */
    @Override
    public void onDestroyView() {
        mCountersDataProvider.unsubscribe();
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

}
