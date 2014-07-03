package com.topface.topface.promo.dialogs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;

public abstract class PromoDialog extends AbstractDialogFragment implements View.OnClickListener, IPromoPopup {

    private OnCloseListener mListener;

    public abstract Options.PromoPopupEntity getPremiumEntity();

    /**
     * @return id строки из ресурсов, которую нужно показать на кнопке удаления
     */
    protected abstract int getDeleteButtonText();

    /**
     * @return id строки из ресурсов, которую нужно показать как описание попапа
     */
    protected abstract String getMessage();

    protected abstract int getPluralForm();

    protected abstract void deleteMessages();

    /**
     * @return Ключ статистики для данного фрагмента
     */
    public abstract String getMainTag();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();

        EasyTracker.sendEvent(getMainTag(), "Show", "", 0L);
    }

    private BroadcastReceiver mVipPurchasedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.log("Promo: Close fragment after VIP buy");
            closeFragment();
            EasyTracker.sendEvent(getMainTag(), "VipClose", "CloseAfterBuyVip", 1L);
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
                break;
            case R.id.deleteMessages:
                deleteMessages();
                EasyTracker.sendEvent(getMainTag(), "Dismiss", "Delete", 0L);
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
        getPremiumEntity().setPopupShowTime();

        FragmentActivity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(false);
        }

        dismissAllowingStateLoss();
    }

    public interface OnCloseListener {
        public void onClose();
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
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

}
