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
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.CurrentUserRequest;
import com.topface.topface.utils.social.OkAuthorizer;
import com.topface.topface.utils.social.OkUserData;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

@FlurryOpenEvent(name = OkProfileFragment.PAGE_NAME)
public class OkProfileFragment extends ProfileInnerFragment {

    public static final String PAGE_NAME = "profile.ok";

    @Inject
    TopfaceAppState mAppState;
    private OkProfileHandler mHandler;
    private Subscription mSubscription;
    private Action1<OkUserData> mSubscriber = new Action1<OkUserData>() {
        @Override
        public void call(OkUserData okUserData) {
            if (mHandler != null && okUserData != null) {
                showProgress(false);
                mHandler.imageSrc.set(!TextUtils.isEmpty(okUserData.bigSquareImage)
                        ? okUserData.bigSquareImage
                        : getEmptyPhotoRes(TextUtils.isEmpty(okUserData.gender)
                        ? App.get().getProfile().sex == Profile.BOY
                        : okUserData.isMale()));
                mHandler.avatarVisibility.set(View.VISIBLE);
                String name = TextUtils.isEmpty(okUserData.name) ? App.get().getProfile().firstName : okUserData.name;
                mHandler.nameVisibility.set(TextUtils.isEmpty(name) ? View.GONE : View.VISIBLE);
                mHandler.nameText.set(name);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
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
        new CurrentUserRequest(new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds())).getObservable().subscribe(new Action1<OkUserData>() {
            @Override
            public void call(OkUserData okUserData) {
                mAppState.setData(okUserData);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
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
        return Utils.getLocalResUrl(isBoy ? R.drawable.feed_banned_male_avatar : R.drawable.feed_banned_female_avatar);
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
            Utils.goToUrl(mActivityDelegate, App.get().getOptions().aboutApp.url);
        }

    }

    @Override
    protected String getTitle() {
        return App.get().getProfile().getNameAndAge();
    }

    @Override
    protected String getSubtitle() {
        return App.get().getProfile().city.getName();
    }

    @Override
    protected Boolean isOnline() {
        return true;
    }
}
