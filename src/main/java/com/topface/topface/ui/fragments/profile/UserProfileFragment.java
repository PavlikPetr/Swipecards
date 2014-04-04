package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.BookmarkAddRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteBlackListRequest;
import com.topface.topface.requests.DeleteBookmarksRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.requests.handlers.VipApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.dialogs.LeadersDialog;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.GiftsFragment;
import com.topface.topface.ui.fragments.buy.BuyingFragment;
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
    public static final String UPDATE_USER_CATEGORY = "com.topface.topface.action.USER_CATEGORY";

    private static final String ARG_TAG_PROFILE_ID = "profile_id";
    private int mProfileId;
    private String mItemId;
    // views
    private RelativeLayout mLockScreen;
    private RetryViewCreator mRetryView;
    private View mLoaderView;
    private TextView mBookmarkAction;
    private LinearLayout mUserActions;
    private ProgressBar mGiftsLoader;
    private ImageView mGiftsIcon;
    private OnGiftReceivedListener mGiftsReceivedListener = new OnGiftReceivedListener() {
        @Override
        public void onReceived() {
            if (mGiftsIcon != null) {
                mGiftsIcon.setVisibility(View.VISIBLE);
            }
            if (mGiftsLoader != null) {
                mGiftsLoader.setVisibility(View.INVISIBLE);
            }
        }
    };
    private RelativeLayout mBlocked;
    private MenuItem mBarActions;
    // controllers
    private RateController mRateController;
    private BroadcastReceiver mUpdateActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isBlackList = intent.getBooleanExtra("blackList", false);
            boolean isChanged = intent.getBooleanExtra("changed", false);

            Profile profile = getProfile();

            if (profile != null) {
                if (isBlackList) {
                    ((User) profile).inBlackList = isChanged;
                    if (mBlocked != null) {
                        ((TextView)mBlocked.findViewById(R.id.blockTV)).setText(isChanged? R.string.black_list_delete : R.string.black_list_add_short);
                    }
                } else {
                    ((User) profile).bookmarked = isChanged;
                    if (mBookmarkAction != null) {
                        mBookmarkAction.setText(isChanged ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
                    }
                }
            }
        }
    };

    public static UserProfileFragment newInstance(String itemId, int id, String className) {
        UserProfileFragment fragment = new UserProfileFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_PROFILE_ID, id);
        args.putString(ARG_FEED_ITEM_ID, itemId);
        args.putString(ARG_TAG_CALLING_CLASS, className);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if (mItemId != null) {
            Intent intent = new Intent(ChatFragment.MAKE_ITEM_READ);
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, mItemId);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }
        initUserActions(root);
        mRateController = new RateController(getActivity(), SendLikeRequest.Place.FROM_PROFILE);
        RelativeLayout bookmarksLayout = (RelativeLayout) mUserActions.findViewById(R.id.acBookmark);
        bookmarksLayout.setOnClickListener(this);
        mBlocked = (RelativeLayout) mUserActions.findViewById(R.id.acBlock);
        mUserActions.setVisibility(View.INVISIBLE);
        mBookmarkAction = (TextView) mUserActions.findViewById(R.id.favTV);
        mLoaderView = root.findViewById(R.id.llvProfileLoading);
        mLockScreen = (RelativeLayout) root.findViewById(R.id.lockScreen);
        mRetryView = RetryViewCreator.createDefaultRetryView(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserProfile(mProfileId);
                mLockScreen.setVisibility(View.GONE);
            }
        });
        mLockScreen.addView(mRetryView.getView());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateActionsReceiver, new IntentFilter(UPDATE_USER_CATEGORY));
        return root;
    }

    @Override
    protected void restoreState() {
        super.restoreState();
        mProfileId = getArguments().getInt(ARG_TAG_PROFILE_ID);
        mItemId = getArguments().getString(ARG_FEED_ITEM_ID);
    }

    @Override
    public void onDestroy() {
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
        addBodyPage(GiftsFragment.class.getName(), getResources().getString(R.string.profile_gifts));
    }

    private void initUserActions(View root) {
        mUserActions = (LinearLayout) root.findViewById(R.id.mUserActions);

        ArrayList<UserActions.ActionItem> actions = new ArrayList<>();
        actions.add(new UserActions.ActionItem(R.id.acGift, this));
        actions.add(new UserActions.ActionItem(R.id.acSympathy, this));
        actions.add(new UserActions.ActionItem(R.id.acDelight, this));
        actions.add(new UserActions.ActionItem(R.id.acChat, this));
        actions.add(new UserActions.ActionItem(R.id.acBlock, this));
        actions.add(new UserActions.ActionItem(R.id.acComplain, this));
        actions.add(new UserActions.ActionItem(R.id.acBookmark, this));
        new UserActions(mUserActions, actions);
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
                boolean checked = mBarActions.isChecked();
                mBarActions.setChecked(!checked);
                animateProfileActions(checked, 500);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_profile);
    }

    private void getUserProfile(final int profileId) {
        mLoaderView.setVisibility(View.VISIBLE);
        UserRequest userRequest = new UserRequest(profileId, getActivity());
        registerRequest(userRequest);
        userRequest.callback(new DataApiHandler<User>() {

            @Override
            protected void success(User user, IApiResponse response) {
                if (user == null) {
                    showRetryBtn();
                } else if (user.banned) {
                    showForBanned();
                } else if (user.deleted) {
                    showForDeleted();
                } else {
                    if (user.bookmarked) {
                        mBookmarkAction.setText(App.getContext().getString(R.string.general_bookmarks_delete));
                    } else {
                        mBookmarkAction.setText(App.getContext().getString(R.string.general_bookmarks_add));
                    }
                    if (user.inBlackList) {
                        ((TextView) mBlocked.findViewById(R.id.blockTV)).setText(R.string.black_list_delete);
                    } else {
                        ((TextView) mBlocked.findViewById(R.id.blockTV)).setText(R.string.black_list_add_short);
                    }
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
        showLockWithText(text, false);
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

    private void animateProfileActions(final boolean isActive, int time) {
        TranslateAnimation ta;
        if (isActive) {
            ta = new TranslateAnimation(0, 0, 0, -mUserActions.getHeight());
        } else {
            ta = new TranslateAnimation(0, 0, -mUserActions.getHeight(), 0);
        }

        ta.setDuration(time);
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mUserActions.clearAnimation();
                if (isActive) {
                    mUserActions.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (!isActive) {
            mUserActions.setVisibility(View.VISIBLE);
        }
        mUserActions.startAnimation(ta);
    }

    @Override
    public void onClick(final View v) {
        final Profile profile = getProfile();
        switch (v.getId()) {
            case R.id.acDelight:
                if (v.isEnabled()) {
                    v.setSelected(true);
                    final TextView textView = (TextView) v.findViewById(R.id.delTV);
                    final ProgressBar loader = (ProgressBar) v.findViewById(R.id.delPrBar);
                    final ImageView icon = (ImageView) v.findViewById(R.id.delIcon);

                    loader.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.GONE);

                    textView.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));
                    v.findViewById(R.id.delPrBar).setVisibility(View.VISIBLE);
                    v.setEnabled(false);
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
                                        if (textView != null) {
                                            textView.setTextColor(Color.parseColor(DEFAULT_NON_ACTIVATED));
                                        }
                                    }
                                }
                            }
                    );

                }
                break;
            case R.id.acSympathy:
                if (v.isEnabled()) {
                    v.setSelected(true);
                    TextView textView = (TextView) v.findViewById(R.id.likeTV);
                    final ProgressBar loader = (ProgressBar) v.findViewById(R.id.likePrBar);
                    final ImageView icon = (ImageView) v.findViewById(R.id.likeIcon);

                    loader.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.GONE);
                    textView.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));
                    v.setEnabled(false);
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
            case R.id.acGift:
                mGiftsLoader = (ProgressBar) v.findViewById(R.id.giftPrBar);
                mGiftsIcon = (ImageView) v.findViewById(R.id.giftIcon);
                mGiftsLoader.setVisibility(View.VISIBLE);
                mGiftsIcon.setVisibility(View.INVISIBLE);
                if (mGiftFragment != null && mGiftFragment.getActivity() != null) {
                    mGiftFragment.sendGift(mGiftsReceivedListener);
                } else {
                    startActivityForResult(
                            GiftsActivity.getSendGiftIntent(getActivity(), mProfileId),
                            GiftsActivity.INTENT_REQUEST_GIFT
                    );
                }
                break;
            case R.id.acChat:
                if (CacheProfile.premium || !CacheProfile.getOptions().block_chat_not_mutual) {
                    openChat();
                } else {
                    String callingClass = getCallingClassName();
                    if (callingClass != null && profile != null && (profile instanceof User)) {
                        if (callingClass.equals(DatingFragment.class.getName()) || callingClass.equals(LeadersDialog.class.getName())) {
                            if (!((User) profile).mutual) {
                                startActivityForResult(
                                        ContainerActivity.getVipBuyIntent(getString(R.string.chat_block_not_mutual), "ProfileChatLock"),
                                        ContainerActivity.INTENT_BUY_VIP_FRAGMENT
                                );
                                break;
                            }
                        }
                    }
                    openChat();
                }
                break;
            case R.id.acBlock:
                if (CacheProfile.premium) {
                    if (profile.uid > 0) {
                        final TextView textView = (TextView) v.findViewById(R.id.blockTV);
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
                        request.callback(new VipApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                super.success(response);
                                if (isAdded()) {
                                    loader.setVisibility(View.INVISIBLE);
                                    icon.setVisibility(View.VISIBLE);
                                    Intent intent = new Intent(UPDATE_USER_CATEGORY);
                                    intent.putExtra("blackList", true);
                                    intent.putExtra("changed", !((User) profile).inBlackList);
                                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                                    if (profile.inBlackList) {
                                        textView.setText(R.string.black_list_delete);
                                    } else {
                                        textView.setText(R.string.black_list_add_short);
                                    }
                                }
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                super.fail(codeError, response);
                                if (isAdded()) {
                                    loader.setVisibility(View.INVISIBLE);
                                    icon.setVisibility(View.VISIBLE);
                                }
                            }
                        }).exec();
                    }
                } else {
                    startActivityForResult(ContainerActivity.getVipBuyIntent(null, "ProfileSuperSkills"), ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                }
                break;
            case R.id.acBookmark:
                final TextView textView = (TextView) v.findViewById(R.id.favTV);
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

                request.callback(new SimpleApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        super.success(response);
                        Intent intent = new Intent(UPDATE_USER_CATEGORY);
                        intent.putExtra("changed", !((User) profile).bookmarked);
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        loader.setVisibility(View.INVISIBLE);
                        icon.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void always(IApiResponse response) {
                        super.always(response);
                        if (isAdded()) {
                            loader.setVisibility(View.INVISIBLE);
                            icon.setVisibility(View.VISIBLE);
                        }
                    }
                }).exec();
                break;
            case R.id.acComplain:
                startActivity(ContainerActivity.getComplainIntent(mProfileId));
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GiftsActivity.INTENT_REQUEST_GIFT:
                    if (mGiftFragment == null || !mGiftFragment.isAdded()) {
                        sendGift(data);
                        return;
                    }
                    break;
            }
            resultToNestedFragments(requestCode, resultCode, data);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            mGiftsReceivedListener.onReceived();
        }
    }

    private void sendGift(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
            final String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);
            final int price = extras.getInt(GiftsActivity.INTENT_GIFT_PRICE);

            final Profile profile = getProfile();
            if (profile != null) {
                final SendGiftRequest sendGift = new SendGiftRequest(getActivity());
                registerRequest(sendGift);
                sendGift.giftId = id;
                sendGift.userId = profile.uid;
                final FeedGift sendedGift = new FeedGift();
                sendedGift.gift = new Gift(
                        sendGift.giftId,
                        Gift.PROFILE_NEW,
                        url,
                        0
                );
                sendGift.callback(new DataApiHandler<SendGiftAnswer>() {

                    @Override
                    protected void success(SendGiftAnswer data, IApiResponse response) {
                        if (mGiftFragment != null) {
                            mGiftFragment.addGift(sendedGift);
                        } else {
                            profile.gifts.add(0, sendedGift.gift);
                        }
                        Toast.makeText(getContext(), R.string.chat_gift_out, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected SendGiftAnswer parseResponse(ApiResponse response) {
                        return SendGiftAnswer.parse(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        if (response.isCodeEqual(ErrorCodes.PAYMENT)) {
                            FragmentActivity activity = getActivity();
                            if (activity != null) {
                                Intent intent = ContainerActivity.getBuyingIntent("Profile");
                                intent.putExtra(BuyingFragment.ARG_ITEM_TYPE, BuyingFragment.TYPE_GIFT);
                                intent.putExtra(BuyingFragment.ARG_ITEM_PRICE, price);
                                startActivity(intent);
                            }
                        } else {
                            Utils.showErrorMessage();
                        }
                    }

                    @Override
                    public void always(IApiResponse response) {
                        super.always(response);
                        mGiftsReceivedListener.onReceived();
                    }
                }).exec();
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
            Intent intent = new Intent(getActivity(), ContainerActivity.class);
            intent.putExtra(ChatFragment.INTENT_USER_ID, profile.uid);
            intent.putExtra(ChatFragment.INTENT_USER_NAME, profile.firstName != null ?
                    profile.firstName : Static.EMPTY);
            intent.putExtra(ChatFragment.INTENT_USER_SEX, profile.sex);
            intent.putExtra(ChatFragment.INTENT_USER_AGE, profile.age);
            intent.putExtra(ChatFragment.INTENT_USER_CITY, profile.city == null ? "" : profile.city.name);
            intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, ((Object) this).getClass().getSimpleName());
            startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);
        }
    }

    public interface OnGiftReceivedListener {
        public void onReceived();
    }
}
