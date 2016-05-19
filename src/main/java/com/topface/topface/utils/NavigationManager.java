package com.topface.topface.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FragmentLifreCycleData;
import com.topface.topface.data.leftMenu.DrawerLayoutStateData;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.IntegrationSettingsData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.state.DrawerLayoutState;
import com.topface.topface.state.LifeCycleState;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.BonusFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.EditorFragment;
import com.topface.topface.ui.fragments.IntegrationWebViewFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.TabbedDialogsFragment;
import com.topface.topface.ui.fragments.feed.TabbedLikesFragment;
import com.topface.topface.ui.fragments.feed.TabbedVisitorsFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by ppavlik on 12.05.16.
 * Navigation fragments switcher
 */
public class NavigationManager {

    private static final String FRAGMENT_SETTINGS = "fragment_settings";
    private static final int CLOSE_LEFT_MENU_TIMEOUT = 250;

    @Inject
    NavigationState mNavigationState;
    @Inject
    LifeCycleState mLifeCycleState;
    @Inject
    DrawerLayoutState mDrawerLayoutState;
    private ISimpleCallback iNeedCloseMenuCallback;
    private Subscription mDrawerLayoutStateSubscription;
    private MenuFragment mLeftMenu;
    private Context mContex;
    private FragmentManager mFragmentManager;
    private LeftMenuSettingsData mFragmentSettings = new LeftMenuSettingsData(FragmentIdData.UNDEFINED);
    private Subscription mSubscription;
    private Bundle mSavedInstanceState;

    public NavigationManager(Context context, Bundle savedInstanceState) {
        App.get().inject(this);
        mSavedInstanceState = savedInstanceState;
        mContex = context;
        mNavigationState.getSelectionObservable().subscribe(new Action1<WrappedNavigationData>() {
            @Override
            public void call(WrappedNavigationData wrappedLeftMenuSettingsData) {
                if (wrappedLeftMenuSettingsData != null && wrappedLeftMenuSettingsData.getSenderType() != WrappedNavigationData.SWITCHED_EXTERNALY) {
                    selectFragment(wrappedLeftMenuSettingsData.getData(), wrappedLeftMenuSettingsData.getSenderType(), false);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void init(@NotNull FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        initLeftMenu();
        LeftMenuSettingsData settings = new LeftMenuSettingsData(App.get().getOptions().startPage);
        if (mSavedInstanceState != null && mSavedInstanceState.containsKey(FRAGMENT_SETTINGS)) {
            settings = mSavedInstanceState.getParcelable(FRAGMENT_SETTINGS);
        }
        selectFragment(settings, WrappedNavigationData.SWITCHED_EXTERNALY, false);
    }

    private void initLeftMenu() {
        mLeftMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        if (mLeftMenu == null) {
            mLeftMenu = new MenuFragment();
        }
        if (!mLeftMenu.isAdded()) {
            mFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_menu, mLeftMenu)
                    .commit();
        }
    }

    private String getTag(LeftMenuSettingsData settings) {
        return "fragment_switch_controller_" + settings.getUniqueKey();
    }

    private void switchFragment(LeftMenuSettingsData newFragmentSettings,
                                final @WrappedNavigationData.NavigationEventSenderType int senderType,
                                boolean executePending) {
        if (newFragmentSettings == null || mFragmentManager == null) {
            return;
        }
        BaseFragment oldFragment = (BaseFragment) mFragmentManager.findFragmentById(R.id.fragment_content);
        String fragmentTag = getTag(newFragmentSettings);
        Debug.log("MenuFragment: Try switch to fragment with tag " + fragmentTag + " (old fragment " + getTag(mFragmentSettings) + ")");
        BaseFragment newFragment = (BaseFragment) mFragmentManager.findFragmentByTag(fragmentTag);

        //Если не нашли в FragmentManager уже существующего инстанса, то создаем новый
        if (newFragment == null) {
            newFragment = getFragmentNewInstanceById(newFragmentSettings);
            Debug.log("MenuFragment: newFragment is null, create new instance");
        }

        if (oldFragment == null || mFragmentSettings.getUniqueKey() != newFragmentSettings.getUniqueKey()) {
            final String fragmnetName = newFragment.getClass().getName();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            //Меняем фрагменты анимировано, но только на новых устройствах c HW ускорением
            if (mLeftMenu != null && mLeftMenu.isHrdwareAccelerated()) {
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            if (oldFragment != newFragment && newFragment.isAdded()) {
                transaction.remove(newFragment);
                Debug.error("MenuFragment: try detach already added new fragment " + fragmentTag);
            }
            Debug.error("MenuFragment: try detach already added new fragment " + fragmentTag);
            transaction.replace(R.id.fragment_content, newFragment, fragmentTag);
            transaction.commitAllowingStateLoss();
            //Вызываем executePendingTransactions, если передан соответвующий флаг
            //и сохраняем результат
            String transactionResult = executePending ?
                    Boolean.toString(mFragmentManager.executePendingTransactions()) :
                    "no executePending";
            Debug.log("MenuFragment: commit " + transactionResult);
            mFragmentSettings = newFragmentSettings;
            /**
             * подписываемся на жизненный цикл загруженного фрагмента
             * ждем его загрузки не дольше CLOSE_LEFT_MENU_TIMEOUT мс
             * потом отписываемся и шлем ивент о том, что фрагмент свичнулся
             */
            mSubscription = mLifeCycleState.getObservable(FragmentLifreCycleData.class)
                    .timeout(CLOSE_LEFT_MENU_TIMEOUT, TimeUnit.MILLISECONDS)
                    .filter(new Func1<FragmentLifreCycleData, Boolean>() {
                        @Override
                        public Boolean call(FragmentLifreCycleData fragmentLifreCycleData) {
                            return fragmentLifreCycleData.getState() == FragmentLifreCycleData.RESUME
                                    && fragmnetName.equals(fragmentLifreCycleData.getClassName());
                        }
                    })
                    .subscribe(new Action1<FragmentLifreCycleData>() {
                        @Override
                        public void call(FragmentLifreCycleData fragmentLifreCycleData) {
                            sendNavigationFragmentSwitched(senderType);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            sendNavigationFragmentSwitched(senderType);
                        }
                    });
        } else {
            Debug.error("MenuFragment: new fragment already added");
            sendNavigationFragmentSwitched(senderType);
        }
    }

    private void sendNavigationFragmentSwitched(@WrappedNavigationData.NavigationEventSenderType int senderType) {
        mNavigationState.navigationFragmentSwitched(new WrappedNavigationData(mFragmentSettings, senderType));
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @SuppressLint("SwitchIntDef")
    private BaseFragment getFragmentNewInstanceById(LeftMenuSettingsData id) {
        BaseFragment fragment;
        switch (id.getFragmentId()) {
            case FragmentIdData.VIP_PROFILE:
            case FragmentIdData.PROFILE:
                fragment = OwnProfileFragment.newInstance();
                break;
            case FragmentIdData.DATING:
                fragment = new DatingFragment();
                break;
            case FragmentIdData.GEO:
                fragment = new PeopleNearbyFragment();
                break;
            case FragmentIdData.BONUS:
                fragment = BonusFragment.newInstance(true);
                break;
            case FragmentIdData.INTEGRATION_PAGE:
                IntegrationSettingsData fragmentSettings = (IntegrationSettingsData) id;
                fragment = IntegrationWebViewFragment.newInstance(fragmentSettings.getPageName(), fragmentSettings.getUrl());
                break;
            case FragmentIdData.TABBED_VISITORS:
                fragment = new TabbedVisitorsFragment();
                break;
            case FragmentIdData.SETTINGS:
                fragment = new SettingsFragment();
                break;
            case FragmentIdData.EDITOR:
                fragment = null;
                if (Editor.isEditor()) {
                    fragment = new EditorFragment();
                }
                break;
            case FragmentIdData.PHOTO_BLOG:
                fragment = new PhotoBlogFragment();
                break;
            case FragmentIdData.TABBED_LIKES:
                fragment = new TabbedLikesFragment();
                break;
            case FragmentIdData.TABBED_DIALOGS:
                fragment = new TabbedDialogsFragment();
                break;
            default:
                fragment = OwnProfileFragment.newInstance();
                break;
        }
        return fragment;
    }

    public void selectFragment(LeftMenuSettingsData fragmentSettings) {
        selectFragment(fragmentSettings, WrappedNavigationData.SWITCHED_EXTERNALY, false);
    }

    @SuppressLint("SwitchIntDef")
    private void selectFragment(LeftMenuSettingsData fragmentSettings, @WrappedNavigationData.NavigationEventSenderType int senderType, boolean executePending) {
        switch (fragmentSettings.getFragmentId()) {
            case FragmentIdData.BALLANCE:
                closeMenuAndSwitchAfter(new ISimpleCallback() {
                    @Override
                    public void onCall() {
                        mContex.startActivity(PurchasesActivity.createBuyingIntent("Menu", App.get().getOptions().topfaceOfferwallRedirect));
                        selectPreviousLeftMenuItem();
                    }
                });
                break;
            case FragmentIdData.INTEGRATION_PAGE:
                final IntegrationSettingsData settingsData = (IntegrationSettingsData) fragmentSettings;
                if (settingsData.isExternal()) {
                    closeMenuAndSwitchAfter(new ISimpleCallback() {
                        @Override
                        public void onCall() {
                            Utils.goToUrl(mContex, settingsData.getUrl());
                            selectPreviousLeftMenuItem();
                        }
                    });
                    break;
                }
            default:
                switchFragment(fragmentSettings, senderType, executePending);
        }
    }

    public void setNeedCloseMenuListener(ISimpleCallback callback) {
        iNeedCloseMenuCallback = callback;
    }

    private void closeMenuAndSwitchAfter(@NotNull final ISimpleCallback callback) {
        if (iNeedCloseMenuCallback != null) {
            iNeedCloseMenuCallback.onCall();
        }
        /**
         * ждем когда будет закрыто левое меню, но не дольше CLOSE_LEFT_MENU_TIMEOUT мс
         * после этого отписываемся и шлем ивент о смене подсвеченного итема в левом меню
         */
        mDrawerLayoutStateSubscription = mDrawerLayoutState.getObservable()
                .timeout(CLOSE_LEFT_MENU_TIMEOUT, TimeUnit.MILLISECONDS)
                .filter(new Func1<DrawerLayoutStateData, Boolean>() {
                    @Override
                    public Boolean call(DrawerLayoutStateData drawerLayoutStateData) {
                        return drawerLayoutStateData.getState() == DrawerLayoutStateData.CLOSED;
                    }
                }).subscribe(new Action1<DrawerLayoutStateData>() {
                    @Override
                    public void call(DrawerLayoutStateData drawerLayoutStateData) {
                        drawerLayoutStateUsubscribe();
                        callback.onCall();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        drawerLayoutStateUsubscribe();
                        callback.onCall();
                    }
                });
    }

    private void drawerLayoutStateUsubscribe() {
        if (mDrawerLayoutStateSubscription != null && !mDrawerLayoutStateSubscription.isUnsubscribed()) {
            mDrawerLayoutStateSubscription.unsubscribe();
        }
    }

    private void selectPreviousLeftMenuItem() {
        mNavigationState.leftMenuItemSelected(new WrappedNavigationData(mFragmentSettings, WrappedNavigationData.SWITCHED_EXTERNALY));
        sendNavigationFragmentSwitched(WrappedNavigationData.SWITCHED_EXTERNALY);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(FRAGMENT_SETTINGS, mFragmentSettings);
    }

    public void onDestroy() {
        mContex = null;
        mLeftMenu = null;
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        if (mDrawerLayoutStateSubscription != null && !mDrawerLayoutStateSubscription.isUnsubscribed()) {
            mDrawerLayoutStateSubscription.unsubscribe();
        }
        mSavedInstanceState = null;
        mFragmentManager = null;
        iNeedCloseMenuCallback = null;
    }
}
