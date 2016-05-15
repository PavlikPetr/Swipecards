package com.topface.topface.utils;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.ActivityLifreCycleData;
import com.topface.topface.data.FragmentLifreCycleData;
import com.topface.topface.data.FragmentSettings;
import com.topface.topface.data.leftMenu.FragmentId;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.IntegrationSettingsData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.state.LifeCycleState;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.BonusFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.EditorFragment;
import com.topface.topface.ui.fragments.IntegrationWebViewFragment;
import com.topface.topface.ui.fragments.NewMenuFragment;
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
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by ppavlik on 12.05.16.
 */
public class NavigationManager {

    @Inject
    NavigationState mNavigationState;
    @Inject
    LifeCycleState mLifeCycleState;
    private NewMenuFragment mLeftMenu;
    private Context mContex;
    private FragmentManager mFragmentManager;
    private LeftMenuSettingsData mFragmentSettings = new LeftMenuSettingsData(FragmentIdData.UNDEFINED);
    private Subscription mSubscription;

    private Action1<Throwable> mCatchOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            Debug.showChunkedLogError("TEST", "on error " + throwable);
        }
    };

    public NavigationManager(Context context) {
        App.get().inject(this);
        mContex = context;
        mNavigationState.getSelectionObservable().subscribe(new Action1<LeftMenuSettingsData>() {
            @Override
            public void call(LeftMenuSettingsData leftMenuSettingsData) {
                Debug.showChunkedLogError("NewMenuFragment", "mSelectionOnNext " + (leftMenuSettingsData != null ? leftMenuSettingsData.getUniqueKey() : "null"));
                switchFragment(leftMenuSettingsData, false);
            }
        }, mCatchOnError);
        mLifeCycleState.getObservable(FragmentLifreCycleData.class).subscribe(new Action1<FragmentLifreCycleData>() {
            @Override
            public void call(FragmentLifreCycleData fragmentLifreCycleData) {
                Debug.showChunkedLogError("NewMenuFragment1", "fragment " + fragmentLifreCycleData.getClassName() + " state " + fragmentLifreCycleData.getState());
            }
        }, mCatchOnError);
    }

    public void init(@NotNull FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        initLeftMenu();
    }

    private void initLeftMenu() {
        mLeftMenu = (NewMenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        if (mLeftMenu == null) {
            mLeftMenu = new NewMenuFragment();
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

    @FragmentIdData.FragmentId
    private int getFragmentId(BaseFragment fragment) {
        @FragmentIdData.FragmentId
        int id = FragmentIdData.UNDEFINED;
        if (fragment != null) {
            Class cls = fragment.getClass();
            if (cls.isAnnotationPresent(FragmentId.class)) {
                id = ((FragmentId) cls.getAnnotation(FragmentId.class)).fragmentId();
            }
        }
        return id;
    }

    private void switchFragment(final LeftMenuSettingsData newFragmentSettings, boolean executePending) {
        Debug.showChunkedLogError("TEST", "switchFragment");
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
            Debug.showChunkedLogError("TEST", "new fragment");
            final String fragmnetName = newFragment.getClass().getName();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            //Меняем фрагменты анимировано, но только на новых устройствах c HW ускорением
//            if (mHardwareAccelerated) {
//                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
//            }
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
            mSubscription = mLifeCycleState.getObservable(FragmentLifreCycleData.class).filter(new Func1<FragmentLifreCycleData, Boolean>() {
                @Override
                public Boolean call(FragmentLifreCycleData fragmentLifreCycleData) {
                    Debug.showChunkedLogError("TEST", "fragment " + fragmentLifreCycleData.getClassName() + " state " + fragmentLifreCycleData.getState());
                    return fragmentLifreCycleData.getState() == FragmentLifreCycleData.RESUME
                            && fragmnetName.equals(fragmentLifreCycleData.getClassName());
                }
            })
                    .subscribe(new Action1<FragmentLifreCycleData>() {
                        @Override
                        public void call(FragmentLifreCycleData fragmentLifreCycleData) {
                            Debug.showChunkedLogError("TEST", "switched");
                            mNavigationState.navigationFragmentSwitched(newFragmentSettings);
                            if (mSubscription != null && !mSubscription.isUnsubscribed()) {
                                mSubscription.unsubscribe();
                            }
                        }
                    }, mCatchOnError, new Action0() {
                        @Override
                        public void call() {
                            Debug.showChunkedLogError("TEST", "on completed");
                            mNavigationState.navigationFragmentSwitched(newFragmentSettings);
                        }
                    });
        } else {
            Debug.error("MenuFragment: new fragment already added");
            Debug.showChunkedLogError("TEST", "old fragment");
            mNavigationState.navigationFragmentSwitched(newFragmentSettings);
        }
        //Закрываем меню только после создания фрагмента
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 250);
    }

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
                fragment = IntegrationWebViewFragment.newInstance("Test", fragmentSettings.getUrl());
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

    public void selectMenu(FragmentSettings fragmentSettings) {

    }

    public void showBalance() {
        mContex.startActivity(PurchasesActivity.createBuyingIntent("Menu", App.get().getOptions().topfaceOfferwallRedirect));
    }
}
