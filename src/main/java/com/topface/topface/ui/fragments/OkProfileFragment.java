package com.topface.topface.ui.fragments;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.databinding.OkProfileFragmentBinding;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.CurrentUser;
import com.topface.topface.utils.social.OkAuthorizer;
import com.topface.topface.utils.social.OkUserData;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class OkProfileFragment extends ProfileInnerFragment {

    private static final String PAGE_NAME = "profile.ok";

    @Inject
    TopfaceAppState mAppState;
    private OkProfileHandler mHandler;
    private Subscription mSubscription;
    private Action1<OkUserData> mSubscriber = new Action1<OkUserData>() {
        @Override
        public void call(OkUserData okUserData) {
            if (mHandler != null && okUserData != null) {
                showProgress(false);
                mHandler.imageSrc.set(!TextUtils.isEmpty(okUserData.pic1)
                        ? okUserData.pic1
                        : getEmptyPhotoRes(TextUtils.isEmpty(okUserData.gender)
                        ? CacheProfile.getProfile().sex == Profile.BOY
                        : okUserData.isMale()));
                mHandler.avatarVisibility.set(View.VISIBLE);
                mHandler.nameVisibility.set(TextUtils.isEmpty(okUserData.name) ? View.GONE : View.VISIBLE);
                mHandler.nameText.set(okUserData.name);
            }
        }
    };

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
        new CurrentUser(new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds())).exec();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.ok_profile_fragment, null);
        OkProfileFragmentBinding binding = DataBindingUtil.bind(root);
        mHandler = new OkProfileHandler((IActivityDelegate) getActivity());
        binding.setHandler(mHandler);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscription = mAppState.getObservable(OkUserData.class).subscribe(mSubscriber);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    private String getEmptyPhotoRes(boolean isBoy) {
        return String.format(App.getCurrentLocale(), Utils.LOCAL_RES, isBoy ? R.drawable.feed_banned_male_avatar : R.drawable.feed_banned_female_avatar);
    }

    private void showProgress(boolean isEnable) {
        if (mHandler != null) {
            mHandler.rootVisibility.set(!isEnable ? View.VISIBLE : View.GONE);
            mHandler.progressVisibility.set(isEnable ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    public static class OkProfileHandler {
        private static final String GROUP_URL = "http://ok.ru/group/52746255532280";

        private IActivityDelegate mActivityDelegate;

        public ObservableInt rootVisibility = new ObservableInt(View.GONE);
        public ObservableInt avatarVisibility = new ObservableInt(View.GONE);
        public ObservableInt nameVisibility = new ObservableInt(View.GONE);
        public ObservableInt buttonVisibility = new ObservableInt(View.VISIBLE);
        public ObservableInt progressVisibility = new ObservableInt(View.VISIBLE);
        public ObservableField<String> nameText = new ObservableField<>();
        public ObservableField<String> imageSrc = new ObservableField<>();

        public OkProfileHandler(IActivityDelegate activityDelegate) {
            mActivityDelegate = activityDelegate;
        }

        @SuppressWarnings("unused")
        public void onButtonShowGroupClick(View view) {
            Utils.goToUrl(mActivityDelegate, GROUP_URL);
        }

    }
}
