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
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.BookmarkAddRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteBlackListRequest;
import com.topface.topface.requests.DeleteBookmarksRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.requests.handlers.AttitudeHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.ComplainsActivity;
import com.topface.topface.ui.EditorProfileActionsActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.dialogs.LeadersDialog;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;
import com.topface.topface.ui.fragments.gift.UserGiftsFragment;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.actionbar.TopMenu;

import java.util.ArrayList;

import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.ADD_TO_BOOKMARK_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.SEND_ADMIRATION_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.SEND_SYMPATHY_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.findTopMenuItemById;
import static com.topface.topface.utils.actionbar.TopMenu.getProfileTopMenu;
import static com.topface.topface.utils.actionbar.TopMenu.isCurrentIdTopMenuItem;

/**
 * Created by kirussell on 18.03.14.
 * Profile fragment to view profile with ui for interactions with another profile
 */
public class UserProfileFragment extends AbstractProfileFragment {

    public static final String USER_RATED_EXTRA = "USER_RATED_EXTRA";


    private int mProfileId;
    private int mLastLoadedProfileId;
    private String mItemId;
    private boolean mUserRate;
    private boolean mUserRated;
    // views
    private RelativeLayout mLockScreen;
    private RetryViewCreator mRetryView;
    private View mLoaderView;
    private MenuItem mBarActions;
    // for profile forwarding
    private ApiResponse mSavedResponse = null;
    // controllers
    private RateController mRateController;
    private BroadcastReceiver mUpdateActionsReceiver = new BroadcastReceiver() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onReceive(Context context, Intent intent) {
            AttitudeHandler.ActionTypes type = (AttitudeHandler.ActionTypes) intent.getSerializableExtra(AttitudeHandler.TYPE);
            boolean value = intent.getBooleanExtra(AttitudeHandler.VALUE, false);
            Profile profile = getProfile();
            View root = getView();
            if (profile != null && type != null && root != null) {
                switch (type) {
                    case BLACK_LIST:
                        ((User) profile).inBlackList = value;
                        initTopMenu();
                        break;
                    case BOOKMARK:
                        User user = (User) profile;
                        if (intent.hasExtra(AttitudeHandler.VALUE) && !user.inBlackList) {
                            user.bookmarked = value;
                            initTopMenu();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        mProfileId = args.getInt(AbstractProfileFragment.INTENT_UID, 0);
        mItemId = args.getString(AbstractProfileFragment.INTENT_ITEM_ID);
        mUserRated = args.containsKey(USER_RATED_EXTRA);
        mUserRate = args.getBoolean(USER_RATED_EXTRA, false);
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
        mLockScreen = (RelativeLayout) root.findViewById(R.id.lockScreen);
        mRetryView = new RetryViewCreator.Builder(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserProfile(mProfileId);
            }
        }).build();
        mLockScreen.addView(mRetryView.getView());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateActionsReceiver, new IntentFilter(AttitudeHandler.UPDATE_USER_CATEGORY));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateActionsReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserProfile(mProfileId);
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
        initTopMenu();
    }

    private void onClickAddToBookmarkAction() {
        Profile profile = getProfile();
        if (profile == null) {
            return;
        }
        ApiRequest request;

        if (profile instanceof User && ((User) profile).bookmarked) {
            request = new DeleteBookmarksRequest(profile.uid, getActivity());
        } else {
            request = new BookmarkAddRequest(profile.uid, getActivity());
        }
        if (profile instanceof User) {
            ((User) profile).bookmarked = !((User) profile).bookmarked;
            setProfile(profile);
        }
        request.exec();
    }

    private void onClickOpenChatAction() {
        Profile profile = getProfile();
        if (profile == null) {
            return;
        }
        if (CacheProfile.premium || !CacheProfile.getOptions().blockChatNotMutual) {
            openChat();
        } else {
            String callingClass = getCallingClassName();
            if (callingClass != null && (profile instanceof User)) {
                if (callingClass.equals(DatingFragment.class.getName()) || callingClass.equals(LeadersDialog.class.getName())) {
                    if (!((User) profile).mutual) {
                        startActivityForResult(
                                PurchasesActivity.createVipBuyIntent(getString(R.string.chat_block_not_mutual), "ProfileChatLock"),
                                PurchasesActivity.INTENT_BUY_VIP
                        );
                    }
                }
            }
            openChat();
        }
    }

    private void onClickSendGiftAction() {
        UserGiftsFragment giftsFragment = getGiftFragment();
        if (giftsFragment != null && giftsFragment.getActivity() != null) {
            giftsFragment.sendGift();
        } else {
            startActivityForResult(
                    GiftsActivity.getSendGiftIntent(getActivity(), mProfileId),
                    GiftsActivity.INTENT_REQUEST_GIFT
            );
        }
    }

    private void onClickSendSymphatyAction() {
        Profile profile = getProfile();
        if (profile == null) {
            return;
        }
        mRateController.onLike(
                profile.uid,
                ((User) profile).mutual ?
                        SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                new RateController.OnRateRequestListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateCompleted(int mutualId) {
                        if (getActivity() != null) {
                            Toast.makeText(App.getContext(), R.string.sympathy_sended, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateFailed(int userId, int mutualId) {
                        if (getActivity() != null) {
                            Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void onClickSendAdmirationAction() {
        Profile profile = getProfile();
        if (profile == null) {
            return;
        }
        mRateController.onAdmiration(
                profile.uid,
                ((User) profile).mutual ?
                        SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                new RateController.OnRateRequestListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateCompleted(int mutualId) {
                        if (getActivity() != null) {
                            Toast.makeText(App.getContext(), R.string.admiration_sended, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateFailed(int userId, int mutualId) {
                        if (getActivity() != null) {
                            closeProfileActions();
                        }
                    }
                }
        );
    }

    private void onClickAddToBlackList() {
        Profile profile = getProfile();
        if (profile == null) {
            return;
        }
        if (CacheProfile.premium) {
            if (profile.uid > 0 && mBarActions != null && mBarActions.hasSubMenu()) {
                ApiRequest request;
                if (profile.inBlackList) {
                    request = new DeleteBlackListRequest(profile.uid, getActivity());
                } else {
                    request = new BlackListAddRequest(profile.uid, getActivity());
                }
                profile.inBlackList = !profile.inBlackList;
                if (profile.inBlackList && profile instanceof User) {
                    ((User) profile).bookmarked = false;
                }
                setProfile(profile);
                request.exec();
            }
        } else {
            startActivityForResult(PurchasesActivity.createVipBuyIntent(null, "ProfileSuperSkills"), PurchasesActivity.INTENT_BUY_VIP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        Profile user = getProfile();
        if (isCurrentIdTopMenuItem(itemId)) {
            TopMenu.TopMenuItem topMenuItem = findTopMenuItemById(itemId);
            switch (topMenuItem) {
                case SEND_SYMPATHY_ACTION:
                    onClickSendSymphatyAction();
                    break;
                case SEND_ADMIRATION_ACTION:
                    onClickSendAdmirationAction();
                    break;
                case OPEN_CHAT_ACTION:
                    onClickOpenChatAction();
                    break;
                case SEND_GIFT_ACTION:
                    onClickSendGiftAction();
                    break;
                case COMPLAIN_ACTION:
                    startActivity(ComplainsActivity.createIntent(mProfileId));
                    break;
                case OPEN_PROFILE_FOR_EDITOR_STUB:
                    if (mSavedResponse != null) {
                        startActivity(EditorProfileActionsActivity.createIntent(mProfileId, mSavedResponse));
                    }
                    break;
                case ADD_TO_BLACK_LIST_ACTION:
                    onClickAddToBlackList();
                    break;
                case ADD_TO_BOOKMARK_ACTION:
                    onClickAddToBookmarkAction();
                    break;
                default:
                    break;
            }
            initTopMenu();
        }
        switch (item.getItemId()) {
            case R.id.action_user_actions_list:
                if (user != null && mBarActions != null) {
                    if (!mBarActions.getSubMenu().hasVisibleItems()) {
                        initTopMenu();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                    return User.parse(profileId, response);
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
            onSuccess(User.parse(mProfileId, mSavedResponse), mSavedResponse);
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

    private void saveResponseForEditor(ApiResponse response) {
        if (CacheProfile.isEditor()) {
            mSavedResponse = response;
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
        Profile profile = getProfile();
        if (mBarActions != null && mBarActions.hasSubMenu()) {
            if (profile != null) {
                mBarActions.getSubMenu().clear();
                ArrayList<TopMenu.TopMenuItem> topMenuItemArray = getProfileTopMenu(CacheProfile.isEditor());
                for (int i = 0; i < topMenuItemArray.size(); i++) {
                    TopMenu.TopMenuItem item = topMenuItemArray.get(i);
                    int resourceId;
                    switch (topMenuItemArray.get(i)) {
                        case ADD_TO_BLACK_LIST_ACTION:
                            resourceId = profile.inBlackList ? item.getSecondResourceId() : item.getFirstResourceId();
                            break;
                        case ADD_TO_BOOKMARK_ACTION:
                            resourceId = ((User) profile).bookmarked ? item.getSecondResourceId() : item.getFirstResourceId();
                            break;
                        default:
                            resourceId = item.getFirstResourceId();
                            break;
                    }
                    mBarActions.getSubMenu().add(Menu.NONE, item.getId(), Menu.NONE, resourceId);
                }
                mBarActions.getSubMenu().findItem(ADD_TO_BOOKMARK_ACTION.getId()).setEnabled(!profile.inBlackList);
                if (mUserRated ? mUserRate : ((User) profile).isSympathySent) {
                    mBarActions.getSubMenu().findItem(SEND_SYMPATHY_ACTION.getId()).setEnabled(false);
                    mBarActions.getSubMenu().findItem(SEND_ADMIRATION_ACTION.getId()).setEnabled(false);
                }
            } else {
                mBarActions.getSubMenu().clear();
            }
        }
    }

    private void closeProfileActions() {
        if (mBarActions != null && mBarActions.isChecked()) {
            onOptionsItemSelected(mBarActions);
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

    private void openChat() {
        Profile profile = getProfile();
        if (profile != null) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatFragment.INTENT_USER_ID, profile.uid);
            intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, profile.getNameAndAge());
            intent.putExtra(ChatFragment.INTENT_USER_SEX, profile.sex);
            intent.putExtra(ChatFragment.INTENT_USER_CITY, profile.city == null ? "" : profile.city.name);
            startActivityForResult(intent, ChatActivity.INTENT_CHAT);
        }
    }

    @Override
    protected UserGiftsFragment getGiftFragment() {
        return (UserGiftsFragment) super.getGiftFragment();
    }
}
