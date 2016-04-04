package com.topface.topface.ui.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.OkProfileFragmentBinding;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.CurrentUser;
import com.topface.topface.utils.social.OkAuthorizer;
import com.topface.topface.utils.social.OkUserData;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class OkProfileFragment extends ProfileInnerFragment {

    @Inject
    TopfaceAppState mAppState;
    private OkProfileFragmentBinding mBinding;
    private OkProfileHandler mHandler;
    private Subscription mSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
        new CurrentUser().getUser(new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds()));
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.ok_profile_fragment, null);
        mBinding = DataBindingUtil.bind(root);
        mHandler = new OkProfileHandler((IActivityDelegate) getActivity());
        mBinding.setHandler(mHandler);
        mSubscription = mAppState.getObservable(OkUserData.class).subscribe(new Action1<OkUserData>() {
            @Override
            public void call(OkUserData okUserData) {
                if (mBinding != null && okUserData != null) {
                    mBinding.okProfileAvatar.setRemoteSrc(okUserData.pic1);
                    mBinding.okProfileName.setText(okUserData.name);
                }
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    public static class OkProfileHandler {
        private static final String GROUP_URL = "https://apiok.ru/wiki/pages/viewpage.action?pageId=89982206";

        private IActivityDelegate mActivityDelegate;

        public OkProfileHandler(IActivityDelegate activityDelegate) {
            mActivityDelegate = activityDelegate;
        }

        @SuppressWarnings("unused")
        public void onButtonShowGroupClick(View view) {
            Utils.goToUrl(mActivityDelegate, GROUP_URL);
        }

    }
}
