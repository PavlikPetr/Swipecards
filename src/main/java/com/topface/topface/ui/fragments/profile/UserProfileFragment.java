package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;
import com.topface.topface.ui.fragments.gift.UserGiftsFragment;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.actionbar.OverflowMenuUser;

import java.util.ArrayList;

/**
 * Created by kirussell on 18.03.14.
 * Profile fragment to view profile with ui for interactions with another profile
 */
public class UserProfileFragment extends AbstractProfileFragment {

    private int mProfileId;
    private int mLastLoadedProfileId;
    private String mItemId;
    // views
    private RelativeLayout mLockScreen;
    private RetryViewCreator mRetryView;
    private View mLoaderView;
    private MenuItem mBarActions;
    private ArrayList<FeedGift> mNewGifts;
    private View mOutsideView;
    // for profile forwarding
    private ApiResponse mSavedResponse = null;
    // controllers
    private RateController mRateController;
    private OverflowMenu mProfileOverflowMenu;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        mProfileId = args.getInt(AbstractProfileFragment.INTENT_UID, 0);
        mItemId = args.getString(AbstractProfileFragment.INTENT_ITEM_ID);
        String s = args.getString(EditorProfileActionsFragment.PROFILE_RESPONSE);
        if (!TextUtils.isEmpty(s)) {
            mSavedResponse = new ApiResponse(s);
        }
        setCallingClass(args.getString(AbstractProfileFragment.INTENT_CALLING_FRAGMENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if (mItemId != null) {
            Intent intent = new Intent(ChatFragment.MAKE_ITEM_READ);
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, mItemId);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }
        mRateController = new RateController(getActivity(), SendLikeRequest.Place.FROM_PROFILE);
        mLoaderView = root.findViewById(R.id.llvProfileLoading);
        mOutsideView = root.findViewById(R.id.outsideView);
        mOutsideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeProfileActions();
                mOutsideView.setVisibility(View.GONE);
            }
        });
        mLockScreen = (RelativeLayout) root.findViewById(R.id.lockScreen);
        mRetryView = new RetryViewCreator.Builder(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserProfile(mProfileId);
            }
        }).build();
        mLockScreen.addView(mRetryView.getView());
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProfileOverflowMenu != null) {
            mProfileOverflowMenu.onDestroy();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mGiftReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserProfile(mProfileId);
        mOutsideView.setVisibility(View.GONE);
    }

    @Override
    protected void initBody() {
        super.initBody();
        addBodyPage(UserPhotoFragment.class.getName(), getResources().getString(R.string.profile_photo));
        addBodyPage(UserFormFragment.class.getName(), getResources().getString(R.string.profile_form));
        addBodyPage(UserGiftsFragment.class.getName(), getResources().getString(R.string.profile_gifts));
    }

    @Override
    protected int getProfileType() {
        return Profile.TYPE_USER_PROFILE;
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_user_profile;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem barActionsItem = menu.findItem(R.id.action_user_actions_list);
        if (barActionsItem != null && mBarActions != null) {
            barActionsItem.setChecked(mBarActions.isChecked());
        }
        mBarActions = barActionsItem;
        mProfileOverflowMenu = new OverflowMenu(getActivity(), mBarActions, mRateController, mProfileId, getGiftFragment(), mSavedResponse);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(
                        mGiftReceiver,
                        new IntentFilter(PhotoSwitcherActivity.ADD_NEW_GIFT)
                );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mProfileOverflowMenu != null) {
            mProfileOverflowMenu.onMenuClicked(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_profile);
    }

    private boolean isLoaded(int profileId) {
        return profileId == mLastLoadedProfileId;
    }

    private void getUserProfile(final int profileId) {
        if (isLoaded(profileId)) return;
        mLockScreen.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.VISIBLE);
        if (mSavedResponse == null) {
            UserRequest userRequest = new UserRequest(profileId, getActivity());
            registerRequest(userRequest);
            userRequest.callback(new DataApiHandler<User>() {

                @Override
                protected void success(User user, IApiResponse response) {
                    onSuccess(user, response);
                }

                @Override
                protected User parseResponse(ApiResponse response) {
                    return new User(profileId, response);
                }

                @Override
                public void fail(final int codeError, IApiResponse response) {
                    if (response.isCodeEqual(ErrorCodes.INCORRECT_VALUE, ErrorCodes.USER_NOT_FOUND)) {
                        showForNotExisting();
                    } else {
                        showRetryBtn();
                    }
                }
            }).exec();
        } else {
            onSuccess(new User(mProfileId, mSavedResponse), mSavedResponse);
        }
    }

    private void onSuccess(User user, IApiResponse response) {
        if (user != null) {
            saveResponseForEditor((ApiResponse) response);
        }
        if (user == null) {
            showRetryBtn();
        } else if (user.banned) {
            showForBanned();
        } else if (user.deleted) {
            showForDeleted();
        } else {
            setProfile(user);
            initTopMenu();
            if (mHeaderMainFragment != null) {
                mHeaderMainFragment.setOnline(user.online);
            }
            mLoaderView.setVisibility(View.INVISIBLE);
            if (getProfileType() == Profile.TYPE_USER_PROFILE) {
                String status = user.getStatus();
                if (status == null || TextUtils.isEmpty(status)) {
                    mHeaderPagerAdapter.removeItem(HeaderStatusFragment.class.getName());
                }
            }
        }
        mLastLoadedProfileId = mProfileId;
    }

    private User getUser() {
        return (User) getProfile();
    }

    private void saveResponseForEditor(ApiResponse response) {
        if (CacheProfile.isEditor()) {
            mSavedResponse = response;
            if (mProfileOverflowMenu != null) {
                mProfileOverflowMenu.setSavedResponse(mSavedResponse);
            }
        }
    }

    private void showForBanned() {
        showLockWithText(getString(R.string.user_baned));
    }

    private void showForDeleted() {
        showLockWithText(getString(R.string.user_is_deleted));
    }

    private void showForNotExisting() {
        showLockWithText(getString(R.string.user_does_not_exist), true);
    }

    private void showLockWithText(String text, boolean onlyMessage) {
        if (mRetryView != null && isAdded()) {
            mLoaderView.setVisibility(View.GONE);
            mLockScreen.setVisibility(View.VISIBLE);
            mRetryView.setText(text);
            mRetryView.showRetryButton(!onlyMessage);
        }
    }

    private void showLockWithText(String text) {
        showLockWithText(text, true);
    }

    private void showRetryBtn() {
        showLockWithText(getString(R.string.general_profile_error), false);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        if (mLoaderView != null) {
            mLoaderView.setVisibility(View.VISIBLE);
        }
    }

    private void initTopMenu() {
        if (mProfileOverflowMenu != null) {
            if (mProfileOverflowMenu.getOverflowMenuFieldsListener() == null) {
                mProfileOverflowMenu.setOverflowMenuFieldsListener(new OverflowMenuUser() {
                    @Override
                    public void setBlackListValue(Boolean value) {
                        Profile profile = getProfile();
                        if (profile != null) {
                            profile.inBlackList = value != null ? value : !profile.inBlackList;
                        }
                    }

                    @Override
                    public Boolean getBlackListValue() {
                        Profile profile = getProfile();
                        return profile != null ? profile.inBlackList : null;
                    }

                    @Override
                    public void setBookmarkValue(Boolean value) {
                        User user = getUser();
                        if (user != null) {
                            user.bookmarked = value != null ? value : !user.bookmarked;
                        }
                    }

                    @Override
                    public Boolean getBookmarkValue() {
                        User user = getUser();
                        return user != null ? user.bookmarked : null;
                    }

                    @Override
                    public void setSympathySentValue(Boolean value) {
                        User user = getUser();
                        if (user != null) {
                            user.isSympathySent = value != null ? value : !user.isSympathySent;
                        }
                    }

                    @Override
                    public Boolean getSympathySentValue() {
                        User user = getUser();
                        return user != null ? user.isSympathySent : null;
                    }

                    @Override
                    public Integer getUserId() {
                        Profile profile = getProfile();
                        return profile != null ? profile.uid : null;
                    }

                    @Override
                    public Intent getOpenChatIntent() {
                        Profile profile = getProfile();
                        if (profile != null) {
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra(ChatFragment.INTENT_USER_ID, profile.uid);
                            intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, profile.getNameAndAge());
                            intent.putExtra(ChatFragment.INTENT_USER_SEX, profile.sex);
                            intent.putExtra(ChatFragment.INTENT_USER_CITY, profile.city == null ? "" : profile.city.name);
                            return intent;
                        }
                        return null;
                    }

                    @Override
                    public Boolean getMutualValue() {
                        User user = getUser();
                        return user != null ? user.mutual : null;
                    }
                });
            }
            mProfileOverflowMenu.initOverfowMenu();
        }
    }

    private void closeProfileActions() {
        if (mBarActions != null && mBarActions.isChecked()) {
            onOptionsItemSelected(mBarActions);
            mOutsideView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStartActivity() {
        super.onStartActivity();
        closeProfileActions();
    }

    @Override
    public void onPageSelected(int i) {
        closeProfileActions();
    }

    @Override
    protected UserGiftsFragment getGiftFragment() {
        return (UserGiftsFragment) super.getGiftFragment();
    }

    private BroadcastReceiver mGiftReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            addNewFeedGift(getGiftFromIntent(intent));
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GiftsActivity.INTENT_REQUEST_GIFT:
                if (resultCode == Activity.RESULT_OK) {
                    addNewFeedGift(getGiftFromIntent(data));
                }
                break;
        }
    }

    public ArrayList<FeedGift> getNewGifts() {
        if (mNewGifts == null) {
            return new ArrayList<>();
        } else {
            return mNewGifts;
        }
    }

    public void clearNewFeedGift() {
        if (mNewGifts != null) {
            mNewGifts.clear();
        }
    }

    private void addNewFeedGift(FeedGift data) {
        if (data != null) {
            Profile profile = getProfile();
            if (profile != null) {
                getProfile().gifts.add(0, data.gift);
            }
            if (mNewGifts == null) {
                mNewGifts = new ArrayList<>();
            }
            mNewGifts.add(0, data);
        }
    }


    private FeedGift getGiftFromIntent(Intent data) {
        FeedGift feedGift = null;
        if (data.hasExtra(PhotoSwitcherActivity.INTENT_GIFT)) {
            feedGift = data.getParcelableExtra(PhotoSwitcherActivity.INTENT_GIFT);
        } else {
            Bundle extras = data.getExtras();
            if (extras != null) {
                SendGiftAnswer sendGiftAnswer = extras.getParcelable(GiftsActivity.INTENT_SEND_GIFT_ANSWER);
                if (sendGiftAnswer != null) {
                    feedGift = new FeedGift();
                    feedGift.gift = new Gift(Integer.parseInt(sendGiftAnswer.history.id), 0, Gift.PROFILE, sendGiftAnswer.history.link);
                }
            }
        }
        return feedGift;
    }
}
