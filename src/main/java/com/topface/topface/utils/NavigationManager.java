package com.topface.topface.utils;

import android.annotation.SuppressLint;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
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
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.TabbedDialogsFragment;
import com.topface.topface.ui.fragments.feed.TabbedLikesFragment;
import com.topface.topface.ui.fragments.feed.TabbedVisitorsFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by ppavlik on 12.05.16.
 * Navigation fragments switcher
 */
public class NavigationManager {

    public static final int CLOSE_LEFT_MENU_TIMEOUT = 250;
    private static final String USER_ID = "{userId}";
    private static final String SECRET_KEY = "{secretKey}";

    @Inject
    NavigationState mNavigationState;
    @Inject
    LifeCycleState mLifeCycleState;
    @Inject
    DrawerLayoutState mDrawerLayoutState;
    private ISimpleCallback iNeedCloseMenuCallback;
    private Subscription mDrawerLayoutStateSubscription;
    private IActivityDelegate mActivityDelegate;
    private FragmentManager mFragmentManager;
    private LeftMenuSettingsData mFragmentSettings = new LeftMenuSettingsData(FragmentIdData.UNDEFINED);
    private Subscription mSubscription;

    public NavigationManager(IActivityDelegate activityDelegate, LeftMenuSettingsData settings) {
        App.get().inject(this);
        mFragmentSettings = settings;
        mActivityDelegate = activityDelegate;
        mNavigationState.getNavigationObservable().filter(new Func1<WrappedNavigationData, Boolean>() {
            @Override
            public Boolean call(WrappedNavigationData data) {
                return data != null
                        && data.getStatesStack().contains(WrappedNavigationData.ITEM_SELECTED)
                        && !data.getStatesStack().contains(WrappedNavigationData.FRAGMENT_SWITCHED);
            }
        }).subscribe(new Action1<WrappedNavigationData>() {
            @Override
            public void call(WrappedNavigationData wrappedLeftMenuSettingsData) {
                selectFragment(wrappedLeftMenuSettingsData, false);
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
        selectFragment(new WrappedNavigationData(mFragmentSettings, WrappedNavigationData.SWITCH_EXTERNALLY), false);
    }

    private String getTag(LeftMenuSettingsData settings) {
        return "fragment_switch_controller_" + settings.getUniqueKey();
    }

    private void switchFragment(final WrappedNavigationData data, boolean executePending) {
        if (data == null || data.getData() == null || mFragmentManager == null) {
            return;
        }
        LeftMenuSettingsData leftMenuSettingsData = data.getData();
        BaseFragment oldFragment = (BaseFragment) mFragmentManager.findFragmentById(R.id.fragment_content);
        String fragmentTag = getTag(leftMenuSettingsData);
        Debug.log("NavigationManager: Try switch to fragment with tag " + fragmentTag + " (old fragment " + getTag(mFragmentSettings) + ")");
        BaseFragment newFragment = (BaseFragment) mFragmentManager.findFragmentByTag(fragmentTag);

        //Если не нашли в FragmentManager уже существующего инстанса, то создаем новый
        if (newFragment == null) {
            newFragment = getFragmentNewInstanceById(leftMenuSettingsData);
            Debug.log("NavigationManager: newFragment is null, create new instance");
        }

        if (oldFragment == null || mFragmentSettings.getUniqueKey() != leftMenuSettingsData.getUniqueKey()) {
            final String fragmnetName = newFragment.getClass().getName();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            //Меняем фрагменты анимировано, но только на новых устройствах c HW ускорением
            if (App.getAppConfig().isHardwareAccelerated()) {
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            if (oldFragment != newFragment && newFragment.isAdded()) {
                transaction.remove(newFragment);
                Debug.error("NavigationManager: try detach already added new fragment " + fragmentTag);
            }
            Debug.error("NavigationManager: try detach already added new fragment " + fragmentTag);
            transaction.replace(R.id.fragment_content, newFragment, fragmentTag);
            transaction.commit();
            //Вызываем executePendingTransactions, если передан соответвующий флаг
            //и сохраняем результат
            String transactionResult = executePending ?
                    Boolean.toString(mFragmentManager.executePendingTransactions()) :
                    "no executePending";
            Debug.log("NavigationManager: commit " + transactionResult);
            mFragmentSettings = leftMenuSettingsData;
            /**
             * подписываемся на жизненный цикл загруженного фрагмента
             * ждем его загрузки не дольше CLOSE_LEFT_MENU_TIMEOUT мс
             * потом отписываемся и шлем ивент о том, что фрагмент свичнулся
             */
            mSubscription = mLifeCycleState.getObservable(FragmentLifreCycleData.class)
                    .filter(new Func1<FragmentLifreCycleData, Boolean>() {
                        @Override
                        public Boolean call(FragmentLifreCycleData fragmentLifreCycleData) {
                            return fragmentLifreCycleData.getState() == FragmentLifreCycleData.CREATE_VIEW
                                    && fragmnetName.equals(fragmentLifreCycleData.getClassName());
                        }
                    })
                    .timeout(CLOSE_LEFT_MENU_TIMEOUT, TimeUnit.MILLISECONDS)
                    .subscribe(new Action1<FragmentLifreCycleData>() {
                        @Override
                        public void call(FragmentLifreCycleData fragmentLifreCycleData) {
                            sendNavigationFragmentSwitched(data);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Debug.error("Fragment lifecycle observable error " + throwable);
                            if (throwable.getClass().getName().equals(TimeoutException.class.getName())) {
                                sendNavigationFragmentSwitched(data);
                            }
                        }
                    });
        } else {
            Debug.error("NavigationManager: new fragment already added");
            sendNavigationFragmentSwitched(data);
        }
    }

    private void sendNavigationFragmentSwitched(WrappedNavigationData data) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mNavigationState.emmitNavigationState(data.addStateToStack(WrappedNavigationData.FRAGMENT_SWITCHED));
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
                String url = fragmentSettings.getUrl();
                if (!TextUtils.isEmpty(url)) {
                    url = url.replace(USER_ID, AuthToken.getInstance().getUserSocialId()).replace(SECRET_KEY, Ssid.get());
                }
                fragment = IntegrationWebViewFragment.newInstance(fragmentSettings.getPageName(), url);
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
        selectFragment(new WrappedNavigationData(fragmentSettings, WrappedNavigationData.SWITCH_EXTERNALLY), false);
    }

    @SuppressLint("SwitchIntDef")
    private void selectFragment(WrappedNavigationData data, boolean executePending) {
        switch (data.getData().getFragmentId()) {
            case FragmentIdData.BALLANCE:
                closeMenuAndSwitchAfter(new ISimpleCallback() {
                    @Override
                    public void onCall() {
                        if (mActivityDelegate != null) {
                            mActivityDelegate.startActivity(PurchasesActivity.createBuyingIntent("Menu", App.get().getOptions().topfaceOfferwallRedirect));
                            selectPreviousLeftMenuItem();
                        }
                    }
                });
                break;
            case FragmentIdData.INTEGRATION_PAGE:
                final IntegrationSettingsData settingsData = (IntegrationSettingsData) data.getData();
                if (settingsData.isExternal()) {
                    closeMenuAndSwitchAfter(new ISimpleCallback() {
                        @Override
                        public void onCall() {
                            Utils.goToUrl(mActivityDelegate, settingsData.getUrl());
                            selectPreviousLeftMenuItem();
                        }
                    });
                    break;
                }
            default:
                switchFragment(data, executePending);
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
                .filter(new Func1<DrawerLayoutStateData, Boolean>() {
                    @Override
                    public Boolean call(DrawerLayoutStateData drawerLayoutStateData) {
                        return drawerLayoutStateData.getState() == DrawerLayoutStateData.CLOSED;
                    }
                })
                .timeout(CLOSE_LEFT_MENU_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<DrawerLayoutStateData>() {
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
        WrappedNavigationData data = new WrappedNavigationData(mFragmentSettings, WrappedNavigationData.SELECT_ONLY);
        mNavigationState.emmitNavigationState(data);
        sendNavigationFragmentSwitched(data);
    }

    public LeftMenuSettingsData getCurrentFragmentSettings(){
        return mFragmentSettings;
    }

    public void onDestroy() {
        mActivityDelegate = null;
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        if (mDrawerLayoutStateSubscription != null && !mDrawerLayoutStateSubscription.isUnsubscribed()) {
            mDrawerLayoutStateSubscription.unsubscribe();
        }
        mFragmentManager = null;
        iNeedCloseMenuCallback = null;
    }
}
