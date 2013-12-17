package com.topface.topface.promo.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.dialogs.BaseDialogFragment;
import com.topface.topface.ui.fragments.VipBuyFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

public abstract class PromoFragment extends BaseDialogFragment implements View.OnClickListener, IPromoPopup {

    private OnCloseListener mListener;
    /**
     * Общий между всем промо-попапами флаг того, видны ли они в данный момент
     */
    private static boolean mPromoIsVisible = false;

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
        //По стилю это у нас не диалог, а кастомный дизайн -
        //закрывает весь экран оверлеем и ниже ActionBar показывает контент
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
        //Подписываемся на обновление профиля
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(
                        mProfileReceiver,
                        new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION)
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
        mPromoIsVisible = true;

        //Отключаем боковое меню
        FragmentActivity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(true);
            ((NavigationActivity) activity).setMenuEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPromoIsVisible = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.promo_popup, container, false);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Магия для того, чтобы клики не проходили в дэйтинг фрагмент
            }
        });

        root.findViewById(R.id.buyVip).setOnClickListener(this);
        ((TextView) root.findViewById(R.id.deleteMessages)).setText(getDeleteButtonText());
        root.findViewById(R.id.deleteMessages).setOnClickListener(this);

        TextView popupText = (TextView) root.findViewById(R.id.airMessagesText);
        popupText.setText(getMessage());
        EasyTracker.getTracker().sendEvent(getMainTag(), "Show", "", 0L);
        return root;
    }

    private BroadcastReceiver mVipPurchasedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.log("Promo: Close fragment after VIP buy");
            closeFragment();
            EasyTracker.getTracker().sendEvent(getMainTag(), "VipClose", "CloseAfterBuyVip", 1L);
        }
    };
    private BroadcastReceiver mProfileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Если мы узнаем что пользователь премиум после обновления профиля, то закрываем фрагмент
            if (CacheProfile.premium) {
                Debug.log("Promo: Close fragment after profile update");
                closeFragment();
                EasyTracker.getTracker().sendEvent(getMainTag(), "VipClose", "CloseAfterUpdateProfile", 1L);
            }
        }
    };

    @Override
    public void onClick(View v) {
        boolean isClicked = false;
        switch (v.getId()) {
            case R.id.buyVip:
                startActivityForResult(
                        ContainerActivity.getVipBuyIntent(getMessage(), getTagForBuyingFragment()),
                        ContainerActivity.INTENT_BUY_VIP_FRAGMENT
                );
                EasyTracker.getTracker().sendEvent(getMainTag(), "ClickBuyVip", "", 0L);
                break;
            case R.id.deleteMessages:
                deleteMessages();
                EasyTracker.getTracker().sendEvent(getMainTag(), "Dismiss", "Delete", 0L);
                closeFragment();
                isClicked = true;
                break;
            default:

        }

        if (isClicked) {
            //Отмечаем время закрытия попапа
            getPremiumEntity().setPopupShowTime();

            //Включаем боковое меню
            FragmentActivity activity = getActivity();
            if (activity instanceof NavigationActivity) {
                ((NavigationActivity) activity).setPopupVisible(false);
                ((NavigationActivity) activity).setMenuEnabled(true);
            }
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

    public static boolean isPromoVisible() {
        return mPromoIsVisible;
    }

}
