package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.topface.topface.utils.UserActions;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;

/**
 * Created by kirussell on 18.03.14.
 * Profile fragment to view profile with ui for interactions with another profile
 */
public class UserProfileFragment extends AbstractProfileFragment implements View.OnClickListener {

    public static final String USER_RATED_EXTRA = "USER_RATED_EXTRA";


    private int mProfileId;
    private int mLastLoadedProfileId;
    private String mItemId;
    private ArrayList<UserActions.ActionItem> mUserActions;
    private boolean mUserRate;
    private boolean mUserRated;
    // views
    private RelativeLayout mLockScreen;
    private RetryViewCreator mRetryView;
    private View mLoaderView;
    private TextView mBookmarkAction;
    private RelativeLayout mSympathy;
    private TextView mSympathyText;
    private RelativeLayout mDelight;
    private TextView mDelightText;
    private View mActions;
    private ViewStub mUserActionsStub;
    private RelativeLayout mBlocked;
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
            if (profile != null && type != null) {
                switch (type) {
                    case BLACK_LIST:
                        ((User) profile).inBlackList = value;
                        if (mBlocked != null) {
                            ((TextView) mBlocked.findViewById(R.id.block_action_text)).setText(value ? R.string.black_list_delete : R.string.black_list_add_short);
                            if (value) {
                                ((User) profile).bookmarked = false;
                            }
                            switchBookmarkEnabled(!value);
                        }
                        getView().findViewById(R.id.blockPrBar).setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.blockIcon).setVisibility(View.VISIBLE);
                        break;
                    case BOOKMARK:
                        User user = (User) profile;
                        if (mBookmarkAction != null && intent.hasExtra(AttitudeHandler.VALUE) && !user.inBlackList) {
                            user.bookmarked = value;
                            mBookmarkAction.setText(value ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
                        }
                        View root = getView();
                        if (root != null) {
                            //Они могут быть Null, т.к. находятся внутри ViewStub
                            View favPrBar = root.findViewById(R.id.favPrBar);
                            if (favPrBar != null) {
                                favPrBar.setVisibility(View.INVISIBLE);
                            }
                            View favIcon = root.findViewById(R.id.favIcon);
                            if (favIcon != null) {
                                favIcon.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                }
            }
        }
    };

    private int getAnimationTime() {
        return mUserActions.size() * getActivity().getResources().getInteger(R.integer.action_animation_time);
    }

    private void switchBookmarkEnabled(boolean enabled) {
        if (mActions != null) {
            mBookmarkAction.setText(((User) getProfile()).bookmarked ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
            mBookmarkAction.setTextColor(getResources().getColor(enabled ? R.color.text_white : R.color.disabled_color));
            mActions.findViewById(R.id.add_to_bookmark_action).setEnabled(enabled);
        }
    }

    private int mActionsHeightHeuristic;

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
        mUserActionsStub = (ViewStub) root.findViewById(R.id.user_actions_stub);
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
    protected void restoreState() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroy();
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

    private void initActions(ViewStub stub, User user, ArrayList<UserActions.ActionItem> actions) {
        if (mActions == null) {
            stub.setLayoutResource(R.layout.user_actions_layout);
            mActions = stub.inflate();
            activateEditorActions(mActions);
            RelativeLayout bookmarksLayout = (RelativeLayout) mActions.findViewById(R.id.add_to_bookmark_action);
            bookmarksLayout.setOnClickListener(this);
            new UserActions(mActions, actions);
            mBlocked = (RelativeLayout) mActions.findViewById(R.id.add_to_black_list_action);
            ((TextView) mBlocked.findViewById(R.id.block_action_text)).setText(
                    user.inBlackList ? R.string.black_list_delete : R.string.black_list_add_short
            );
            mBookmarkAction = (TextView) mActions.findViewById(R.id.bookmark_action_text);
            mBookmarkAction.setText(App.getContext().getString(
                    user.bookmarked ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add
            ));
            mSympathy = (RelativeLayout) mActions.findViewById(R.id.send_sympathy_action);
            mSympathyText = (TextView) mSympathy.findViewById(R.id.likeTV);
            mDelight = (RelativeLayout) mActions.findViewById(R.id.send_admiration_action);
            mDelightText = (TextView) mDelight.findViewById(R.id.delTV);
            if (mUserRated ? mUserRate : user.isSympathySent) {
                disableSympathyDelight();
            }
            mActionsHeightHeuristic = actions.size() * Utils.getPxFromDp(40);
            switchBookmarkEnabled(!user.inBlackList);
        }
    }

    private void activateEditorActions(View actions) {
        if (CacheProfile.isEditor()) {
            ViewStub stub = (ViewStub) actions.findViewById(R.id.open_profile_for_editor_stub);
            stub.inflate();
        }
    }

    private ArrayList<UserActions.ActionItem> getActionItems() {
        if (mUserActions == null) {
            mUserActions = new ArrayList<>();
            mUserActions.add(new UserActions.ActionItem(R.id.send_gift_action, this));
            mUserActions.add(new UserActions.ActionItem(R.id.send_sympathy_action, this));
            mUserActions.add(new UserActions.ActionItem(R.id.send_admiration_action, this));
            mUserActions.add(new UserActions.ActionItem(R.id.open_chat_action, this));
            mUserActions.add(new UserActions.ActionItem(R.id.add_to_black_list_action, this));
            mUserActions.add(new UserActions.ActionItem(R.id.complain_action, this));
            mUserActions.add(new UserActions.ActionItem(R.id.add_to_bookmark_action, this));
            if (CacheProfile.isEditor()) {
                mUserActions.add(new UserActions.ActionItem(R.id.open_profile_for_editor, this));
            }
        }
        return mUserActions;
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user_actions_list:
                Profile user = getProfile();
                if (user != null) {
                    initActions(mUserActionsStub, (User) user, getActionItems());
                    boolean checked = mBarActions.isChecked();
                    mBarActions.setChecked(!checked);
                    animateProfileActions(checked);
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

    private void animateProfileActions(final boolean isActive) {
        if (mActions != null) {
            TranslateAnimation ta;
            if (isActive) {
                ta = new TranslateAnimation(0, 0, 0, -getChatActionsViewHeight());
            } else {
                ta = new TranslateAnimation(0, 0, -getChatActionsViewHeight(), 0);
            }

            ta.setDuration(getAnimationTime());
            ta.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mActions.clearAnimation();
                    if (isActive) {
                        mActions.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            if (!isActive) {
                mActions.setVisibility(View.VISIBLE);
            }
            mActions.startAnimation(ta);
        }
    }

    private int getChatActionsViewHeight() {
        int height = mActions.getHeight();
        if (height <= 0) {
            return mActionsHeightHeuristic;
        }
        return height;
    }

    private void disableSympathyDelight() {
        mSympathyText.setTextColor(getResources().getColor(R.color.disabled_color));
        mSympathy.setEnabled(false);
        mSympathy.findViewById(R.id.likeIcon).setEnabled(false);

        mDelightText.setTextColor(getResources().getColor(R.color.disabled_color));
        mDelight.setEnabled(false);
        mDelight.findViewById(R.id.delIcon).setEnabled(false);
    }


    @Override
    public void onClick(final View v) {
        final Profile profile = getProfile();
        if (profile == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.send_admiration_action:
                if (v.isEnabled()) {
                    final ProgressBar loader = (ProgressBar) v.findViewById(R.id.delPrBar);
                    final ImageView icon = (ImageView) v.findViewById(R.id.delIcon);

                    loader.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.GONE);

                    v.findViewById(R.id.delPrBar).setVisibility(View.VISIBLE);
                    mRateController.onAdmiration(
                            profile.uid,
                            ((User) profile).mutual ?
                                    SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                            new RateController.OnRateRequestListener() {
                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onRateCompleted(int mutualId) {
                                    if (v != null && getActivity() != null) {
                                        Toast.makeText(App.getContext(), R.string.admiration_sended, Toast.LENGTH_SHORT).show();
                                        loader.setVisibility(View.INVISIBLE);
                                        icon.setVisibility(View.VISIBLE);
                                        disableSympathyDelight();
                                    }
                                }

                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onRateFailed(int userId, int mutualId) {
                                    if (v != null && getActivity() != null) {
                                        closeProfileActions();
                                        loader.setVisibility(View.INVISIBLE);
                                        icon.setVisibility(View.VISIBLE);
                                        if (CacheProfile.money > 0) {
                                            Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                        }
                                        v.setEnabled(true);
                                        v.setSelected(false);
                                        if (mDelightText != null) {
                                            mDelightText.setTextColor(Color.parseColor(DEFAULT_NON_ACTIVATED));
                                        }
                                    }
                                }
                            }
                    );

                }
                break;
            case R.id.send_sympathy_action:
                if (v.isEnabled()) {
                    final ProgressBar loader = (ProgressBar) v.findViewById(R.id.likePrBar);
                    final ImageView icon = (ImageView) v.findViewById(R.id.likeIcon);

                    loader.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.GONE);
                    mRateController.onLike(
                            profile.uid,
                            ((User) profile).mutual ?
                                    SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                            new RateController.OnRateRequestListener() {
                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onRateCompleted(int mutualId) {
                                    if (v != null && getActivity() != null) {
                                        Toast.makeText(App.getContext(), R.string.sympathy_sended, Toast.LENGTH_SHORT).show();
                                        loader.setVisibility(View.INVISIBLE);
                                        icon.setVisibility(View.VISIBLE);
                                        disableSympathyDelight();
                                    }
                                }

                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onRateFailed(int userId, int mutualId) {
                                    if (v != null && getActivity() != null) {
                                        Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                        loader.setVisibility(View.INVISIBLE);
                                        icon.setVisibility(View.VISIBLE);
                                        v.setEnabled(true);
                                        v.setSelected(false);
                                        if (v instanceof TextView) {
                                            TextView view = (TextView) v;
                                            view.setTextColor(Color.parseColor(DEFAULT_NON_ACTIVATED));
                                        }
                                    }
                                }
                            }
                    );


                    //noinspection deprecation
//                    ((TextView) v).setAlpha(80);
                }
                break;
            case R.id.send_gift_action:
                UserGiftsFragment giftsFragment = getGiftFragment();
                if (giftsFragment != null && giftsFragment.getActivity() != null) {
                    giftsFragment.sendGift();
                } else {
                    startActivityForResult(
                            GiftsActivity.getSendGiftIntent(getActivity(), mProfileId),
                            GiftsActivity.INTENT_REQUEST_GIFT
                    );
                }
                break;
            case R.id.open_chat_action:
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
                                break;
                            }
                        }
                    }
                    openChat();
                }
                break;
            case R.id.add_to_black_list_action:
                if (CacheProfile.premium) {
                    if (profile.uid > 0) {
                        final ProgressBar loader = (ProgressBar) v.findViewById(R.id.blockPrBar);
                        final ImageView icon = (ImageView) v.findViewById(R.id.blockIcon);

                        loader.setVisibility(View.VISIBLE);
                        icon.setVisibility(View.GONE);
                        ApiRequest request;
                        if (profile.inBlackList) {
                            request = new DeleteBlackListRequest(profile.uid, getActivity());
                        } else {
                            request = new BlackListAddRequest(profile.uid, getActivity());
                        }
                        request.exec();
                    }
                } else {
                    startActivityForResult(PurchasesActivity.createVipBuyIntent(null, "ProfileSuperSkills"), PurchasesActivity.INTENT_BUY_VIP);
                }
                break;
            case R.id.add_to_bookmark_action:
                final ProgressBar loader = (ProgressBar) v.findViewById(R.id.favPrBar);
                final ImageView icon = (ImageView) v.findViewById(R.id.favIcon);

                loader.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
                ApiRequest request;

                if (profile instanceof User && ((User) profile).bookmarked) {
                    request = new DeleteBookmarksRequest(profile.uid, getActivity());
                } else {
                    request = new BookmarkAddRequest(profile.uid, getActivity());
                }

                request.exec();
                break;
            case R.id.complain_action:
                startActivity(ComplainsActivity.createIntent(mProfileId));
                break;
            case R.id.open_profile_for_editor:
                if (mSavedResponse != null) {
                    startActivity(EditorProfileActionsActivity.createIntent(mProfileId, mSavedResponse));
                }
                break;
            default:
                break;
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
