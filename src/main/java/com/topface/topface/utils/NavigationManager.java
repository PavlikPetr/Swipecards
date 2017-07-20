package com.topface.topface.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.statistics.generated.FBInvitesStatisticsGeneratedStatistics;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FragmentLifreCycleData;
import com.topface.topface.data.Options;
import com.topface.topface.data.leftMenu.DrawerLayoutStateData;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.IntegrationSettingsData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.state.DrawerLayoutState;
import com.topface.topface.state.LifeCycleState;
import com.topface.topface.statistics.FBInvitesStatistics;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.bonus.BonusFragment;
import com.topface.topface.ui.external_libs.ironSource.IronSourceStatistics;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.IntegrationWebViewFragment;
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.dating.DatingFragmentFactory;
import com.topface.topface.ui.fragments.editor.EditorFragment;
import com.topface.topface.ui.fragments.feed.TabbedVisitorsFragment;
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogsFragment;
import com.topface.topface.ui.fragments.feed.people_nearby.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.photoblog.PhotoblogFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.utils.config.WeakStorage;
import com.topface.topface.utils.rx.RxUtils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.topface.topface.ui.NavigationActivity.FRAGMENT_SETTINGS;

/**
 * Created by ppavlik on 12.05.16.
 * Navigation fragments switcher
 */
public class NavigationManager {

    private static final int CLOSE_LEFT_MENU_TIMEOUT = 250;

    @Inject
    NavigationState mNavigationState;
    @Inject
    LifeCycleState mLifeCycleState;
    @Inject
    DrawerLayoutState mDrawerLayoutState;
    @Inject
    WeakStorage mWeakStorage;
    private ISimpleCallback iNeedCloseMenuCallback;
    private Subscription mDrawerLayoutStateSubscription;
    private IActivityDelegate mActivityDelegate;
    private LeftMenuSettingsData mFragmentSettings = new LeftMenuSettingsData(FragmentIdData.UNDEFINED);
    private Subscription mSubscription;
    private Subscription mNavigationStateSubscription;

    public NavigationManager(IActivityDelegate activityDelegate, LeftMenuSettingsData settings) {
        App.getAppComponent().inject(this);
        mFragmentSettings = settings;
        mActivityDelegate = activityDelegate;
        mNavigationStateSubscription = mNavigationState.getNavigationObservable().filter(new Func1<WrappedNavigationData, Boolean>() {
            @Override
            public Boolean call(WrappedNavigationData data) {
                return data != null
                        && data.getStatesStack().contains(WrappedNavigationData.ITEM_SELECTED)
                        && !data.getStatesStack().contains(WrappedNavigationData.FRAGMENT_SWITCHED);
            }
        }).subscribe(new Action1<WrappedNavigationData>() {
            @Override
            public void call(WrappedNavigationData wrappedLeftMenuSettingsData) {
                if (mActivityDelegate != null) {
                    if (mActivityDelegate.isActivityRestoredState()) {
                        selectFragment(wrappedLeftMenuSettingsData);
                    } else {
                        mActivityDelegate.getIntent().putExtra(FRAGMENT_SETTINGS, wrappedLeftMenuSettingsData.getData());
                    }
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void init() {
        selectFragment(mFragmentSettings);
    }

    private String getTag(LeftMenuSettingsData settings) {
        return "fragment_switch_controller_" + settings.getUniqueKey();
    }

    private void switchFragment(final WrappedNavigationData data) {
        if (data == null || data.getData() == null || mActivityDelegate == null || !mActivityDelegate.isActivityRestoredState()) {
            return;
        }
        LeftMenuSettingsData leftMenuSettingsData = data.getData();
        FragmentManager fm = mActivityDelegate.getSupportFragmentManager();
        BaseFragment oldFragment = (BaseFragment) fm.findFragmentById(R.id.fragment_content);
        String fragmentTag = getTag(leftMenuSettingsData);
        Debug.log("NavigationManager: Try switch to fragment with tag " + fragmentTag + " (old fragment " + getTag(mFragmentSettings) + ")");
        BaseFragment newFragment = (BaseFragment) fm.findFragmentByTag(fragmentTag);

        //Если не нашли в FragmentManager уже существующего инстанса, то создаем новый
        if (newFragment == null) {
            newFragment = getFragmentNewInstanceById(leftMenuSettingsData);
            Debug.log("NavigationManager: newFragment is null, create new instance");
        }

        if (oldFragment == null || mFragmentSettings.getUniqueKey() != leftMenuSettingsData.getUniqueKey()) {
            final String fragmentName = newFragment.getClass().getName();
            FragmentTransaction transaction = fm.beginTransaction();
            if (oldFragment != newFragment && newFragment.isAdded()) {
                transaction.remove(newFragment);
                Debug.error("NavigationManager: try detach already added new fragment " + fragmentTag);
            }
            transaction.replace(R.id.fragment_content, newFragment, fragmentTag);
            transaction.commit();
            Debug.log("NavigationManager: commit " + fm.executePendingTransactions());
            mFragmentSettings = leftMenuSettingsData;
            /*
             * подписываемся на жизненный цикл загруженного фрагмента
             * ждем его загрузки не дольше CLOSE_LEFT_MENU_TIMEOUT мс
             * потом отписываемся и шлем ивент о том, что фрагмент свичнулся
             */
            mSubscription = Observable.merge(mLifeCycleState.getObservable(FragmentLifreCycleData.class)
                    .filter(new Func1<FragmentLifreCycleData, Boolean>() {
                        @Override
                        public Boolean call(FragmentLifreCycleData fragmentLifreCycleData) {
                            return fragmentLifreCycleData.getState() == FragmentLifreCycleData.CREATE_VIEW
                                    && fragmentName.equals(fragmentLifreCycleData.getClassName());
                        }
                    }), Observable.timer(CLOSE_LEFT_MENU_TIMEOUT, TimeUnit.MILLISECONDS))
                    .first()
                    .subscribe(new RxUtils.ShortSubscription<Object>() {
                        @Override
                        public void onNext(Object object) {
                            sendNavigationFragmentSwitched(data);
                        }
                    });
        } else {
            Debug.error("NavigationManager: new fragment already added");
            sendNavigationFragmentSwitched(data);
        }
    }

    private void sendNavigationFragmentSwitched(WrappedNavigationData data) {
        RxUtils.safeUnsubscribe(mSubscription);
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
                fragment = new DatingFragmentFactory(mWeakStorage.getIsTranslucentDating()).construct();
                break;
            case FragmentIdData.GEO:
                fragment = App.get().getOptions().peopleNearbyRedesignEnabled ?
                        new com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyFragment() :
                        new PeopleNearbyFragment();
                break;
            case FragmentIdData.BONUS:
                fragment = BonusFragment.Companion.newInstance(true, IronSourceStatistics.LEFT_MENU_PLC);
                break;
            case FragmentIdData.INTEGRATION_PAGE:
                IntegrationSettingsData fragmentSettings = (IntegrationSettingsData) id;
                String url = fragmentSettings.getUrl();
                if (!TextUtils.isEmpty(url)) {
                    url = Utils.prepareUrl(url);
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
                fragment = new PhotoblogFragment();
                break;
            case FragmentIdData.TABBED_LIKES:
//                fragment = new LikesFragment();
                fragment = new com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.TabbedLikesFragment();
                break;
            case FragmentIdData.TABBED_DIALOGS:
                fragment = new DialogsFragment();
                break;
            default:
                fragment = OwnProfileFragment.newInstance();
                break;
        }
        return fragment;
    }

    public void selectFragment(LeftMenuSettingsData fragmentSettings) {
        if (mActivityDelegate != null) {
            if (mActivityDelegate.isActivityRestoredState()) {
                selectFragment(new WrappedNavigationData(fragmentSettings, WrappedNavigationData.SWITCH_EXTERNALLY));
            } else {
                mActivityDelegate.getIntent().putExtra(FRAGMENT_SETTINGS, fragmentSettings);
            }
        }
    }

    @SuppressLint("SwitchIntDef")
    private void selectFragment(WrappedNavigationData data) {
        if (mActivityDelegate != null) {
            mActivityDelegate.getIntent().putExtra(FRAGMENT_SETTINGS, new LeftMenuSettingsData(FragmentIdData.UNDEFINED));
        }
        switch (data.getData().getFragmentId()) {
            case FragmentIdData.BECOME_VIP:
                closeMenuAndSwitchAfter(new ISimpleCallback() {
                    @Override
                    public void onCall() {
                        if (mActivityDelegate != null) {
                            mActivityDelegate.startActivityForResult(PurchasesActivity
                                    .createVipBuyIntent(null, "LeftMenu"), PurchasesActivity.INTENT_BUY_VIP);
                        }
                        selectPreviousLeftMenuItem();
                    }
                });
                break;
            case FragmentIdData.BALLANCE:
                closeMenuAndSwitchAfter(new ISimpleCallback() {
                    @Override
                    public void onCall() {
                        if (mActivityDelegate != null) {
                            mActivityDelegate.startActivity(PurchasesActivity.createBuyingIntent("Menu", App.get().getOptions().topfaceOfferwallRedirect));
                        }
                        selectPreviousLeftMenuItem();
                    }
                });
                break;
            case FragmentIdData.FB_INVITE_FRIENDS:
                closeMenuAndSwitchAfter(new ISimpleCallback() {
                    @Override
                    public void onCall() {
                        String uid = Integer.toString(App.get().getProfile().uid);
                        FBInvitesStatisticsGeneratedStatistics.sendNow_FB_INVITE_BUTTON_CLICK();
                        FBInvitesStatisticsGeneratedStatistics
                                .sendNow_FB_INVITE_BUTTON_CLICK_UNIQUE(uid.concat("_")
                                        .concat(FBInvitesStatistics.FB_INVITE_BUTTON_CLICK_UNIQUE));
                        Options options = App.get().getOptions();
                        BaseFragment fragment = mActivityDelegate != null ?
                                (BaseFragment) mActivityDelegate.getSupportFragmentManager()
                                        .findFragmentById(R.id.fragment_content) : null;
                        Activity activity = fragment != null ? fragment.getActivity() : null;
                        if (activity != null && FBInvitesUtils.INSTANCE.isFBInviteApplicable(options)) {
                            FBInvitesStatisticsGeneratedStatistics.sendNow_FB_INVITE_SHOW();
                            FBInvitesStatisticsGeneratedStatistics
                                    .sendNow_FB_INVITE_SHOW_UNIQUE(uid.concat("_")
                                            .concat(FBInvitesStatistics.FB_INVITE_SHOW_UNIQUE));
                            FBInvitesUtils.INSTANCE.showFBInvitePopup(activity, options.fbInviteSettings.getAppLink(),
                                    options.fbInviteSettings.getIconUrl());
                        }
                        selectPreviousLeftMenuItem();
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
                } else {
                    switchFragment(data);
                }
            case FragmentIdData.UNDEFINED:
                return;
            default:
                switchFragment(data);
        }
    }

    public void setNeedCloseMenuListener(ISimpleCallback callback) {
        iNeedCloseMenuCallback = callback;
    }

    private void closeMenuAndSwitchAfter(@NotNull final ISimpleCallback callback) {
        if (iNeedCloseMenuCallback != null) {
            iNeedCloseMenuCallback.onCall();
        }
        /*
         * ждем когда будет закрыто левое меню, но не дольше CLOSE_LEFT_MENU_TIMEOUT мс
         * после этого отписываемся и шлем ивент о смене подсвеченного итема в левом меню
         */
        mDrawerLayoutStateSubscription = Observable.merge(mDrawerLayoutState.getObservable()
                .filter(new Func1<DrawerLayoutStateData, Boolean>() {
                    @Override
                    public Boolean call(DrawerLayoutStateData drawerLayoutStateData) {
                        return drawerLayoutStateData.getState() == DrawerLayoutStateData.CLOSED;
                    }
                }), Observable.timer(CLOSE_LEFT_MENU_TIMEOUT, TimeUnit.MILLISECONDS))
                .first()
                .subscribe(new RxUtils.ShortSubscription<Object>() {
                    @Override
                    public void onNext(Object object) {
                        callback.onCall();
                    }
                });
    }

    private void selectPreviousLeftMenuItem() {
        WrappedNavigationData data = new WrappedNavigationData(mFragmentSettings, WrappedNavigationData.SELECT_ONLY);
        mNavigationState.emmitNavigationState(data);
        sendNavigationFragmentSwitched(data);
    }

    public LeftMenuSettingsData getCurrentFragmentSettings() {
        return mFragmentSettings;
    }

    public void onDestroy() {
        RxUtils.safeUnsubscribe(mNavigationStateSubscription);
        mActivityDelegate = null;
        RxUtils.safeUnsubscribe(mSubscription);
        RxUtils.safeUnsubscribe(mDrawerLayoutStateSubscription);
        iNeedCloseMenuCallback = null;
    }
}
