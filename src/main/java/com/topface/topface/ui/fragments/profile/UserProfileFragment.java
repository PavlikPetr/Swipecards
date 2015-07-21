package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Gift;
import com.topface.topface.data.IUniversalUser;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.UniversalUserFactory;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MultipartApiRequest;
import com.topface.topface.requests.ParallelApiRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;
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
    private ArrayList<FeedGift> mNewGifts;
    private View mOutsideView;
    // for profile forwarding
    private ApiResponse mSavedResponse = null;
    // controllers
    private RateController mRateController;
    private boolean mIsChatAvailable;
    private boolean mIsAddToFavoritsAvailable;

    private User mRequestedUser;
    private IApiResponse mUserResponse;
    private FeedListData<FeedGift> mRequestedGifts;
    private String mUserNameAndAge;
    private String mUserCity;
    private Photo mPhoto;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        mProfileId = args.getInt(AbstractProfileFragment.INTENT_UID, 0);
        mItemId = args.getString(AbstractProfileFragment.INTENT_ITEM_ID);
        mUserNameAndAge = args.getString(ChatFragment.INTENT_USER_NAME_AND_AGE);
        mUserCity = args.getString(ChatFragment.INTENT_USER_CITY);
        mPhoto = args.getParcelable(ChatFragment.INTENT_AVATAR);
        String s = args.getString(EditorProfileActionsFragment.PROFILE_RESPONSE);
        if (!TextUtils.isEmpty(s)) {
            mSavedResponse = new ApiResponse(s);
        }
        setIsChatAvailable(args.getBoolean(AbstractProfileFragment.INTENT_IS_CHAT_AVAILABLE));
        setIsAddToFavoritsAvailable(args.getBoolean(AbstractProfileFragment.INTENT_IS_ADD_TO_FAVORITS_AVAILABLE));
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
        mLoaderView = root.findViewById(R.id.viewPagerLoader);
        mOutsideView = root.findViewById(R.id.outsideView);
        mOutsideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOverflowMenu();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        setThrownActionBarAvatar(mPhoto);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mGiftReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserProfile(mProfileId);
        if (CacheProfile.premium) {
            setIsChatAvailable(true);
        }
        mOutsideView.setVisibility(View.GONE);
    }

    @Override
    protected void initBody() {
        super.initBody();
        addBodyPage(UserPhotoFragment.class.getName(), getResources().getString(R.string.profile_photo));
        addBodyPage(UserFormFragment.class.getName(), getResources().getString(R.string.profile_form));
    }

    @Override
    protected OverflowMenu createOverflowMenu(Menu barActions) {
        return new OverflowMenu(getActivity(), barActions, mRateController, mSavedResponse);
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
    protected String getDefaultTitle() {
        return getString(R.string.general_profile);
    }

    @Override
    protected String getSubtitle() {
        return mUserCity;
    }

    @Override
    protected String getTitle() {
        if (TextUtils.isEmpty(mUserNameAndAge)) {
            return getDefaultTitle();
        }
        return mUserNameAndAge;
    }

    private boolean isLoaded(int profileId) {
        return profileId == mLastLoadedProfileId;
    }

    protected boolean isChatAvailable() {
        return mIsChatAvailable;
    }

    public void setIsChatAvailable(boolean isChatAvailable) {
        mIsChatAvailable = isChatAvailable;
    }

    protected boolean isAddToFavoriteAvailable() {
        return mIsAddToFavoritsAvailable;
    }

    public void setIsAddToFavoritsAvailable(boolean isAddToFavoritsAvailable) {
        mIsAddToFavoritsAvailable = isAddToFavoritsAvailable;
    }

    private void getUserProfile(final int profileId) {
        if (isLoaded(profileId)) return;
        mLockScreen.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.VISIBLE);
        if (mSavedResponse == null) {
            UserRequest userRequest = new UserRequest(profileId, getActivity());
            userRequest.callback(new DataApiHandler<User>() {
                @Override
                protected void success(User user, IApiResponse response) {
                    mRequestedUser = user;
                    mUserResponse = response;
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

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    requestExecuted();
                }
            });
            FeedGiftsRequest giftsRequest = new FeedGiftsRequest(getActivity());
            giftsRequest.uid = profileId;
            Resources res = getActivity().getResources();
            DisplayMetrics metrics = res.getDisplayMetrics();
            giftsRequest.limit = (int) (metrics.widthPixels / res.getDimension(R.dimen.form_gift_size));
            giftsRequest.callback(new DataApiHandler<FeedListData<FeedGift>>() {

                @Override
                public void fail(int codeError, IApiResponse response) {

                }

                @Override
                protected void success(FeedListData<FeedGift> data, IApiResponse response) {
                    mRequestedGifts = data;
                }

                @Override
                protected FeedListData<FeedGift> parseResponse(ApiResponse response) {
                    return new FeedListData<>(response.getJsonResult(), FeedGift.class);
                }
            });
            ApiRequest userAndGiftsRequest = new ParallelApiRequest(getActivity()).
                    addRequest(userRequest).addRequest(giftsRequest).
                    setFrom(getClass().getSimpleName()).
                    callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            if (mRequestedGifts != null) {
                                mRequestedUser.gifts.clear();
                                for (FeedGift feedGift : mRequestedGifts.items) {
                                    mRequestedUser.gifts.add(feedGift.gift);
                                }
                            }
                            onSuccess(mRequestedUser, mUserResponse);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            if (mRequestedUser != null && mUserResponse != null) {
                                onSuccess(mRequestedUser, mUserResponse);
                            }
                        }
                    });
            registerRequest(userAndGiftsRequest);
            userAndGiftsRequest.exec();
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
            if (CacheProfile.isEditor()) {
                setProfile(user);
                initOverflowMenuActions(getOverflowMenu());
            }
        } else if (user.deleted) {
            showForDeleted();
        } else {
            setProfile(user);
            initOverflowMenuActions(getOverflowMenu());
            mLoaderView.setVisibility(View.INVISIBLE);
        }
        mLastLoadedProfileId = mProfileId;
    }

    private User getUser() {
        return (User) getProfile();
    }

    private void saveResponseForEditor(ApiResponse response) {
        if (CacheProfile.isEditor()) {
            mSavedResponse = response;
            if (hasOverflowMenu()) {
                getOverflowMenu().setSavedResponse(mSavedResponse);
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

    @Override
    protected void initOverflowMenuActions(OverflowMenu overflowMenu) {
        if (overflowMenu != null) {
            if (overflowMenu.getOverflowMenuFieldsListener() == null) {
                overflowMenu.setOverflowMenuFieldsListener(new OverflowMenuUser() {
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
                            return ChatActivity.createIntent(profile.uid, profile.getNameAndAge(),
                                    profile.city == null ? "" : profile.city.name,
                                    null, profile.photo, false);
                        }
                        return null;
                    }

                    @Override
                    public boolean isOpenChatAvailable() {
                        return isChatAvailable();
                    }

                    @Override
                    public boolean isAddToFavoritsAvailable() {
                        return isAddToFavoriteAvailable();
                    }

                    @Override
                    public Boolean isMutual() {
                        User user = getUser();
                        return user != null ? user.mutual : null;
                    }

                    @Override
                    public void clickSendGift() {
                        startActivityForResult(
                                GiftsActivity.getSendGiftIntent(getActivity(), mProfileId),
                                GiftsActivity.INTENT_REQUEST_GIFT
                        );
                    }

                    @Override
                    public Integer getProfileId() {
                        return mProfileId;
                    }

                    @Override
                    public Boolean isBanned() {
                        User user = getUser();
                        return user != null ? user.banned : null;
                    }
                });
            }
            getOverflowMenu().initOverfowMenu();
        }
    }

    @Override
    protected void onStartActivity() {
        super.onStartActivity();
        closeOverflowMenu();
    }

    @Override
    public void onPageSelected(int i) {
        closeOverflowMenu();
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

    @Override
    protected IUniversalUser createUniversalUser() {
        return UniversalUserFactory.create(getProfile());
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

    @Override
    protected boolean isAnimationRequire() {
        return true;
    }
}
