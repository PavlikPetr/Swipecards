package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.History;
import com.topface.topface.data.HistoryListData;
import com.topface.topface.data.Options;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.BookmarkAddRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteBlackListRequest;
import com.topface.topface.requests.DeleteBookmarksRequest;
import com.topface.topface.requests.DeleteMessagesRequest;
import com.topface.topface.requests.HistoryRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.AttitudeHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.ComplainsActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.IUserOnlineListener;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.adapters.ChatListAdapter;
import com.topface.topface.ui.adapters.EditButtonsAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.dialogs.PopularUserDialog;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.KeyboardListenerLayout;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.UserActions;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

public class ChatFragment extends BaseFragment implements View.OnClickListener {

    public static final int LIMIT = 50;
    public static final int ACTIONS_CLOSE_ANIMATION_TIME = 500;

    public static final String FRIEND_FEED_USER = "user_profile";
    public static final String ADAPTER_DATA = "adapter";
    public static final String WAS_FAILED = "was_failed";
    private static final String KEYBOARD_OPENED = "keyboard_opened";
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_USER_SEX = "user_sex";
    public static final String INTENT_USER_AGE = "user_age";
    public static final String INTENT_USER_CITY = "user_city";
    public static final String INTENT_PROFILE_INVOKE = "profile_invoke";
    public static final String INTENT_ITEM_ID = "item_id";
    public static final String MAKE_ITEM_READ = "com.topface.topface.feedfragment.MAKE_READ";

    private static final int DEFAULT_CHAT_UPDATE_PERIOD = 30000;

    private BroadcastReceiver mUpdateActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AttitudeHandler.ActionTypes type = (AttitudeHandler.ActionTypes) intent.getSerializableExtra(AttitudeHandler.TYPE);
            boolean hasValue = intent.hasExtra(AttitudeHandler.VALUE);
            boolean value = intent.getBooleanExtra(AttitudeHandler.VALUE, false);
            if (type != null && mActions != null) {
                switch (type) {
                    case BLACK_LIST:
                        if (hasValue) {
                            mUser.blocked = value;
                            mBlackListActionController.switchAction();
                            TextView mBookmarkAction = ((TextView) mActions.findViewById(R.id.bookmark_action_text));
                            if (value) {
                                mUser.bookmarked = false;

                            }
                            switchBookmarkEnabled(!value);
                            mActions.findViewById(R.id.add_to_bookmark_action).setEnabled(!value);
                        }
                        mBlackListActionController.setViewsToNormalState();
                        break;
                    case BOOKMARK:
                        if (hasValue) {
                            TextView mBookmarkAction = ((TextView) mActions.findViewById(R.id.bookmark_action_text));
                            if (mBookmarkAction != null && !mUser.blocked) {
                                mUser.bookmarked = value;
                                mBookmarkAction.setText(value ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
                            }
                        }
                        mActions.findViewById(R.id.favPrBar).setVisibility(View.INVISIBLE);
                        mActions.findViewById(R.id.favIcon).setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    };

    private void switchBookmarkEnabled(boolean enabled) {
        if (mActions != null) {
            TextView mBookmarkAction = ((TextView) mActions.findViewById(R.id.bookmark_action_text));
            mBookmarkAction.setText(mUser.bookmarked? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
            mBookmarkAction.setTextColor(getResources().getColor(enabled? R.color.text_white : R.color.disabled_color));
            mActions.findViewById(R.id.add_to_bookmark_action).setEnabled(enabled);
        }
    }

    private IUserOnlineListener mUserOnlineListener;

    // Data
    private int mUserId;
    private BroadcastReceiver mNewMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra(GCMUtils.USER_ID_EXTRA);
            if (id != null && !id.equals("") && Integer.parseInt(id) == mUserId) {
                update(true, "update counters");
                startTimer();
                GCMUtils.cancelNotification(getActivity(), GCMUtils.GCM_TYPE_MESSAGE);
            }
        }
    };
    private Handler mUpdater;
    private boolean mIsUpdating;
    private boolean mIsKeyboardOpened;
    private int mKeyboardFreeHeight;
    private boolean mJustResumed;
    private PullToRefreshListView mListView;
    private ChatListAdapter mAdapter;
    private FeedUser mUser;
    private EditText mEditBox;
    private String mItemId;
    private boolean wasFailed = false;
    TimerTask mUpdaterTask = new TimerTask() {
        @Override
        public void run() {
            updateUI(new Runnable() {
                @Override
                public void run() {
                    if (mUpdater != null && !wasFailed) {
                        update(true, "timer");
                        mUpdater.postDelayed(this, DEFAULT_CHAT_UPDATE_PERIOD);
                    }
                }
            });
        }
    };
    // Managers
    private RelativeLayout mLockScreen;
    private ViewStub mChatActionsStub;
    private PopularUserChatController mPopularUserLockController = new PopularUserChatController();
    private String mUserName;
    private int mUserAge;
    private String mUserCity;
    private int mUserSex;
    private MenuItem mBarAvatar;
    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return actionId == EditorInfo.IME_ACTION_SEND && sendMessage();
        }
    };
    private ArrayList<UserActions.ActionItem> mChatActions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DateUtils.syncTime();
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IUserOnlineListener) {
            mUserOnlineListener = (IUserOnlineListener) activity;
        }
        // do not recreate Adapter cause of steRetainInstance(true)
        if (mAdapter == null) {
            mAdapter = new ChatListAdapter(getActivity(), new FeedList<History>(), getUpdaterCallback());
        }

        Bundle args = getArguments();
        mItemId = args.getString(INTENT_ITEM_ID);
        mUserId = args.getInt(INTENT_USER_ID, -1);
        mUserName = args.getString(INTENT_USER_NAME);
        mUserSex = args.getInt(INTENT_USER_SEX, Static.BOY);
        mUserAge = args.getInt(INTENT_USER_AGE, 0);
        mUserCity = args.getString(INTENT_USER_CITY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final KeyboardListenerLayout root = (KeyboardListenerLayout) inflater.inflate(R.layout.fragment_chat, null);
        root.setKeyboardListener(new KeyboardListenerLayout.KeyboardListener() {
            @Override
            public void keyboardOpened() {
                mIsKeyboardOpened = true;
                if (mActions != null && mActions.getVisibility() == View.VISIBLE) {
                    animateChatActions(ACTIONS_CLOSE_ANIMATION_TIME);
                }
                mKeyboardFreeHeight = getView().getHeight();
            }

            @Override
            public void keyboardClosed() {
                mIsKeyboardOpened = false;
            }
        });
        Debug.log(this, "+onCreate");
        // mChatActions
        mChatActionsStub = (ViewStub) root.findViewById(R.id.chat_actions_stub);
        mActions = null;
        // Navigation bar
        initNavigationbar(mUserName, mUserAge, mUserCity);
        // Swap Control
        root.findViewById(R.id.send_gift_button).setOnClickListener(this);
        // Edit Box
        mEditBox = (EditText) root.findViewById(R.id.edChatBox);
        mEditBox.setOnEditorActionListener(mEditorActionListener);
        //LockScreen
        initLockScreen(root);
        //Send Button
        Button sendButton = (Button) root.findViewById(R.id.btnSend);
        sendButton.setOnClickListener(this);
        //init data
        restoreData(savedInstanceState);
        // History ListView & ListAdapter
        initChatHistory(root);
        if (mUser != null && !mUser.isEmpty()) {
            onUserLoaded(mUser);
        }
        if (!AuthToken.getInstance().isEmpty()) {
            GCMUtils.cancelNotification(getActivity().getApplicationContext(), GCMUtils.GCM_TYPE_MESSAGE);
        }
        //регистрируем здесь, потому что может быть такая ситуация, что обновить надо, когда активити находится не на топе стека
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateActionsReceiver, new IntentFilter(AttitudeHandler.UPDATE_USER_CATEGORY));
        mJustResumed = false;
        return root;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateActionsReceiver);
        mPopularUserLockController.releaseLock();
        super.onDestroyView();
    }

    private void restoreData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            try {
                boolean was_failed = savedInstanceState.getBoolean(WAS_FAILED);
                ArrayList<History> list = savedInstanceState.getParcelableArrayList(ADAPTER_DATA);
                FeedList<History> historyData = new FeedList<>();
                if (list != null) {
                    for (History item : list) {
                        if (item != null) {
                            historyData.add(item);
                        }
                    }
                }
                mAdapter.setData(historyData);
                mUser = new FeedUser(new JSONObject(savedInstanceState.getString(FRIEND_FEED_USER)));
                if (was_failed) {
                    mLockScreen.setVisibility(View.VISIBLE);
                } else {
                    mLockScreen.setVisibility(View.GONE);
                }
                hideLoading();
                mIsKeyboardOpened = savedInstanceState.getBoolean(KEYBOARD_OPENED, false);
            } catch (Exception | OutOfMemoryError e) {
                Debug.error(e);
            }
        }
    }

    private void initChatHistory(View root) {
        // adapter
        mAdapter.setUser(mUser);
        mAdapter.setOnAvatarListener(this);
        mAdapter.setOnItemLongClickListener(new OnListViewItemLongClickListener() {

            @Override
            public void onLongClick(final int position, final View v) {
                History item = mAdapter.getItem(position);
                final EditButtonsAdapter editAdapter = new EditButtonsAdapter(getActivity(), item);
                if (item == null) return;
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.general_spinner_title)
                        .setAdapter(editAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch ((int) editAdapter.getItemId(which)) {
                                    case EditButtonsAdapter.ITEM_DELETE:
                                        deleteItem(position);
                                        EasyTracker.getTracker().sendEvent("Chat", "DeleteItem", "", 1L);
                                        break;
                                    case EditButtonsAdapter.ITEM_COPY:
                                        mAdapter.copyText(((TextView) v).getText().toString());
                                        EasyTracker.getTracker().sendEvent("Chat", "CopyItemText", "", 1L);
                                        break;
                                    case EditButtonsAdapter.ITEM_COMPLAINT:
                                        startActivity(ComplainsActivity.createIntent(mUserId, mAdapter.getItem(position).id));
                                        EasyTracker.getTracker().sendEvent("Chat", "ComplainItemText", "", 1L);
                                        break;
                                }
                            }
                        }).create().show();
            }
        });
        // list view
        mListView = (PullToRefreshListView) root.findViewById(R.id.lvChatList);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                update(true, "pull to refresh");
            }
        });
        mListView.setClickable(true);
        mListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        if (mAdapter.isEmpty()) {
            mAdapter.addHeader(mListView.getRefreshableView());
        }
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.getRefreshableView().addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.item_empty_footer, null));
    }

    private void initLockScreen(View root) {
        mLockScreen = (RelativeLayout) root.findViewById(R.id.llvLockScreen);
        RetryViewCreator retryView = RetryViewCreator.createDefaultRetryView(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(false, "retry");
                mLockScreen.setVisibility(View.GONE);
            }
        }, getResources().getColor(R.color.bg_main));
        mLockScreen.addView(retryView.getView());
        checkPopularUserLock();
    }

    private boolean checkPopularUserLock() {
        if (mPopularUserLockController.isChatLocked()) {
            mPopularUserLockController.blockChat();
            return true;
        } else if (mPopularUserLockController.isDialogOpened()) {
            mPopularUserLockController.showBlockDialog();
        }
        return false;
    }

    private void initNavigationbar(String userName, int userAge, String userCity) {
        setNavigationTitles(userName, userAge, userCity);
    }

    private void setNavigationTitles(String userName, int userAge, String userCity) {
        String userTitle = (TextUtils.isEmpty(userName) && userAge == 0) ? Static.EMPTY : (userName + ", " + userAge);
        setActionBarTitles(userTitle, userCity);
    }

    @Override
    protected String getTitle() {
        if (TextUtils.isEmpty(mUserName) && mUserAge == 0) {
            return Static.EMPTY;
        } else {
            return mUserName + ", " + mUserAge;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(WAS_FAILED, wasFailed);
        outState.putBoolean(KEYBOARD_OPENED, mIsKeyboardOpened);
        outState.putParcelableArrayList(ADAPTER_DATA, mAdapter.getDataCopy());
        if (mUser != null) {
            try {
                outState.putString(FRIEND_FEED_USER, mUser.toJson().toString());
            } catch (Exception e) {
                Debug.error(e);
            }
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mIsKeyboardOpened = savedInstanceState.getBoolean(KEYBOARD_OPENED);
        }
    }

    /**
     * Удаляет сообщение в чате
     *
     * @param position сообщени в списке
     */
    private void deleteItem(final int position) {
        History item = mAdapter.getItem(position);
        if (item != null && (item.id == null || item.isFake())) {
            Toast.makeText(getActivity(), R.string.cant_delete_fake_item, Toast.LENGTH_LONG).show();
            return;
        } else if (item == null) {
            return;
        }
        DeleteMessagesRequest dr = new DeleteMessagesRequest(item.id, getActivity());
        dr.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                if (isAdded()) {
                    int invertedPosition = mAdapter.getPosition(position);
                    if (mAdapter.getFirstItemId().equals(mAdapter.getData().get(invertedPosition).id)) {
                        LocalBroadcastManager.getInstance(getActivity())
                                .sendBroadcast(new Intent(DialogsFragment.UPDATE_DIALOGS));
                    }
                    mAdapter.removeItem(invertedPosition);
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Debug.log(response.toString());
                Utils.showErrorMessage();
            }
        }).exec();
    }

    @Override
    public void onDestroy() {
        release();
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    private void update(final boolean pullToRefresh, String type) {
        update(pullToRefresh, false, type);
    }

    private void update(final boolean scrollRefresh) {
        update(false, scrollRefresh, "scroll refresh");
    }

    private void update(final boolean pullToRefresh, final boolean scrollRefresh, String type) {
        mIsUpdating = true;
        if (!pullToRefresh && !scrollRefresh && !mPopularUserLockController.isChatLocked()) {
            showLoading();
        }
        HistoryRequest historyRequest = new HistoryRequest(getActivity());
        registerRequest(historyRequest);
        historyRequest.userid = mUserId;
        historyRequest.debug = type;
        historyRequest.limit = LIMIT;
        if (mAdapter != null) {
            if (pullToRefresh) {
                String id = mAdapter.getFirstItemId();
                if (id != null) {
                    historyRequest.from = id;
                }
            } else if (scrollRefresh) {
                String id = mAdapter.getLastItemId();
                if (id != null) {
                    historyRequest.to = id;
                }
            }
        }

        historyRequest.callback(new DataApiHandler<HistoryListData>() {
            @Override
            protected void success(HistoryListData data, IApiResponse response) {
                if (!data.items.isEmpty()) {
                    if (mPopularUserLockController.block(data.items.getFirst())) {
                        mIsUpdating = false;
                        wasFailed = false;
                        mUser = data.user;
                        if (!mUser.isEmpty()) {
                            onUserLoaded(mUser);
                        }
                        return;
                    }
                }
                if (mItemId != null) {
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(MAKE_ITEM_READ).putExtra(INTENT_ITEM_ID, mItemId));
                    mItemId = null;
                }

                // delete duplicates
                if (pullToRefresh) {
                    removeOutdatedItems(data);
                } else if (scrollRefresh) {
                    removeAlreadyLoadedItems(data);
                }

                setNavigationTitles(data.user.first_name, data.user.age, data.user.city.name);
                wasFailed = false;
                mUser = data.user;
                if (!mUser.isEmpty()) {
                    onUserLoaded(mUser);
                }
                if (mAdapter != null) {
                    if (!data.items.isEmpty()) {
                        if (pullToRefresh) {
                            mAdapter.addFirst(data.items, data.more, mListView.getRefreshableView());
                        } else if (scrollRefresh) {
                            mAdapter.addAll(data.items, data.more, mListView.getRefreshableView());
                        } else {
                            mAdapter.addAll(data.items, data.more, mListView.getRefreshableView());
                        }
                    } else {
                        if (!data.more && !pullToRefresh) mAdapter.forceStopLoader();
                    }

                    if (mAdapter.getCount() <= 0) {
                        mAdapter.setUser(mUser);
                    }
                }

                mIsUpdating = false;
            }

            @Override
            protected HistoryListData parseResponse(ApiResponse response) {
                return new HistoryListData(response.jsonResult, History.class);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                hideLoading();
                FeedList<History> data = mAdapter != null ? mAdapter.getData() : null;
                if (mLockScreen != null && (data == null || data.isEmpty())) {
                    mLockScreen.setVisibility(View.VISIBLE);
                }
                wasFailed = true;
                mIsUpdating = false;
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                hideLoading();
                if (pullToRefresh && mListView != null) {
                    mListView.onRefreshComplete();
                }
            }
        }).exec();
    }

    private void removeOutdatedItems(HistoryListData data) {
        if (!mAdapter.isEmpty() && !data.items.isEmpty()) {
            ArrayList<History> itemsToDelete = new ArrayList<>();
            for (History item : mAdapter.getData()) {
                for (History newItem : data.items) {
                    if (newItem.id.equals(item.id)) {
                        itemsToDelete.add(item);
                    }
                }
            }
            mAdapter.getData().removeAll(itemsToDelete);
        }
    }

    private void removeAlreadyLoadedItems(HistoryListData data) {
        if (!mAdapter.isEmpty() && !data.items.isEmpty()) {
            FeedList<History> items = mAdapter.getData();
            for (History item1 : items) {
                List<History> itemsToDelete = new ArrayList<>();
                for (History item : data.items) {
                    if (item.id.equals(item1.id)) {
                        itemsToDelete.add(item);
                    }
                }
                data.items.removeAll(itemsToDelete);
            }
        }
    }

    private void onUserLoaded(FeedUser user) {
        if (!(user.deleted || user.banned)) {
            // ставим значок онлайн в нужное состояние
            if (mUserOnlineListener != null) {
                mUserOnlineListener.setUserOnline(user.online);
            }
        }
        // ставим фото пользователя в иконку в actionbar
        setActionBarAvatar(user);
    }

    private void setActionBarAvatar(FeedUser user) {
        if (mBarAvatar == null) return;
        if (user != null && !user.banned && !user.deleted && user.photo != null && !user.photo.isEmpty()) {
            ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                    .findViewById(R.id.ivBarAvatar))
                    .setPhoto(user.photo);
        } else {
            ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                    .findViewById(R.id.ivBarAvatar))
                    .setImageResource(mUserSex == Static.GIRL ?
                            R.drawable.feed_banned_female_avatar :
                            R.drawable.feed_banned_male_avatar);
        }
    }

    private void release() {
        mEditBox = null;
        mListView = null;
        if (mAdapter != null) {
            mAdapter.release();
        }
        mAdapter = null;
    }

    @Override
    public void onClick(final View v) {
        if (v instanceof ImageView) {
            if (v.getTag() instanceof History) {
                History history = (History) v.getTag();
                if (history.type == FeedDialog.MAP || history.type == FeedDialog.ADDRESS) {
                    String uri = String.format(Locale.ENGLISH,
                            "geo:%f,%f?q=%f,%f" + history.text,
                            (float) history.geo.getCoordinates().getLatitude(),
                            (float) history.geo.getCoordinates().getLongitude(),
                            (float) history.geo.getCoordinates().getLatitude(),
                            (float) history.geo.getCoordinates().getLongitude());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            }
        }
        switch (v.getId()) {
            case R.id.btnSend:
                if (mUserId > 0) {
                    sendMessage();
                    EasyTracker.getTracker().sendEvent("Chat", "SendMessage", "", 1L);
                }
                break;
            case R.id.send_gift_button:
                startActivityForResult(
                        GiftsActivity.getSendGiftIntent(getActivity(), mUserId, false),
                        GiftsActivity.INTENT_REQUEST_GIFT
                );
                EasyTracker.getTracker().sendEvent("Chat", "SendGiftClick", "", 1L);
                break;
            case R.id.add_to_black_list_action:
                mBlackListActionController.processActionFor(mUserId);
                break;
            case R.id.acWProfile:
            case R.id.acProfile:
                Intent profileIntent = UserProfileActivity.createIntent(mUserId, getActivity());
                startActivity(profileIntent);
                closeChatActions();
                break;
            case R.id.add_to_bookmark_action:
                final ProgressBar loader = (ProgressBar) v.findViewById(R.id.favPrBar);
                final ImageView icon = (ImageView) v.findViewById(R.id.favIcon);

                loader.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
                ApiRequest request;

                if (mUser.bookmarked) {
                    request = new DeleteBookmarksRequest(mUserId, getActivity());
                } else {
                    request = new BookmarkAddRequest(mUserId, getActivity());
                }

                request.exec();
                break;
            case R.id.complain_action:
                startActivity(ComplainsActivity.createIntent(mUserId));
                closeChatActions();
                break;
            case R.id.ivBarAvatar:
                onOptionsItemSelected(mBarAvatar);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mUserId == 0) {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        checkPopularUserLock();

        // Если адаптер пустой или пользователя нет, грузим с сервера
        if (mAdapter == null || mAdapter.getCount() == 0 || mUser == null) {
            update(false, "initial");
        } else {
            if (mPopularUserLockController.isChatLocked()) {
                update(true, "resume update");
                mAdapter.notifyDataSetChanged();
            }
        }

        IntentFilter filter = new IntentFilter(GCMUtils.GCM_NOTIFICATION);
        getActivity().registerReceiver(mNewMessageReceiver, filter);

        mUpdater = new Handler();
        startTimer();
        GCMUtils.lastUserId = mUserId;

        if (getView().getHeight() == mKeyboardFreeHeight) {
            mIsKeyboardOpened = true;
        }
        if (mIsKeyboardOpened && mJustResumed) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mNewMessageReceiver);
        stopTimer();
        GCMUtils.lastUserId = -1; //Ставим значение на дефолтное, чтобы нотификации снова показывались
        mJustResumed = true;
    }

    /**
     * Note: if you starting new activity and need actions' menu to be closed after,
     * then first call this method. Actions' menu view will fully disappear before new
     * activity will be shown
     */
    private void closeChatActions() {
        if (mBarAvatar.isChecked()) {
            onOptionsItemSelected(mBarAvatar);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GiftsActivity.INTENT_REQUEST_GIFT:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                            final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
                            final int price = extras.getInt(GiftsActivity.INTENT_GIFT_PRICE);
                            sendGift(id, price);
                        }
                    }
                }
                break;
            case ContainerActivity.INTENT_BUY_VIP_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getBooleanExtra(PurchasesFragment.IS_VIP_EXTRA, false) == true) {
                        if (mPopularUserLockController.isChatLocked()) {
                            mLockScreen.setVisibility(View.GONE);
                        }
                        mPopularUserLockController.reset();
                    }
                }
                break;
        }

    }

    private void sendGift(int id, final int price) {
        if (id <= 0) {
            hideLoading();
            Toast.makeText(getActivity(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
            return;
        }
        final SendGiftRequest sendGift = new SendGiftRequest(getActivity());
        registerRequest(sendGift);
        sendGift.giftId = id;
        sendGift.userId = mUserId;
        final History loaderItem = new History(IListLoader.ItemType.WAITING);
        addSentMessage(loaderItem, sendGift);
        sendGift.callback(new DataApiHandler<SendGiftAnswer>() {
            @Override
            protected void success(SendGiftAnswer data, IApiResponse response) {
                data.history.target = FeedDialog.OUTPUT_USER_MESSAGE;
                if (mAdapter != null) {
                    mAdapter.replaceMessage(loaderItem, data.history, mListView.getRefreshableView());
                }
            }

            @Override
            protected SendGiftAnswer parseResponse(ApiResponse response) {
                return SendGiftAnswer.parse(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (response.isCodeEqual(ErrorCodes.PAYMENT)) {
                    if (mAdapter != null) {
                        mAdapter.removeItem(loaderItem);
                    }
                    Intent intent = PurchasesActivity.createBuyingIntent("Chat");
                    intent.putExtra(PurchasesFragment.ARG_ITEM_TYPE, PurchasesFragment.TYPE_GIFT);
                    intent.putExtra(PurchasesFragment.ARG_ITEM_PRICE, price);
                    startActivity(intent);
                } else if (mAdapter != null) {
                    mAdapter.showRetrySendMessage(loaderItem, sendGift);
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                hideLoading();
            }
        }).exec();
    }

    private void addSentMessage(History loaderItem, ApiRequest request) {
        mAdapter.addSentMessage(loaderItem, mListView.getRefreshableView(), request);
    }

    private boolean sendMessage() {
        if (mPopularUserLockController.showBlockDialog()) {
            return false;
        }
        Editable editText = mEditBox.getText();
        String editString = editText == null ? "" : editText.toString();
        if (editText == null || TextUtils.isEmpty(editString.trim()) || mUserId == 0) {
            return false;
        }
        editText.clear();
        final History loaderItem = new History(IListLoader.ItemType.WAITING);
        final MessageRequest messageRequest = new MessageRequest(mUserId, editString, getActivity());
        registerRequest(messageRequest);
        if (mAdapter != null && mListView != null) {
            addSentMessage(loaderItem, messageRequest);
        }
        messageRequest.callback(new DataApiHandler<History>() {
            @Override
            protected void success(History data, IApiResponse response) {
                if (mAdapter != null) {
                    mAdapter.replaceMessage(loaderItem, data, mListView.getRefreshableView());
                }
            }

            @Override
            protected History parseResponse(ApiResponse response) {
                return new History(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (mAdapter != null) {
                    Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                    mAdapter.showRetrySendMessage(loaderItem, messageRequest);
                }
            }
        }).exec();
        return true;
    }

    private void startTimer() {
        if (mUpdater != null) {
            mUpdater.removeCallbacks(mUpdaterTask);
            mUpdater.postDelayed(mUpdaterTask, DEFAULT_CHAT_UPDATE_PERIOD);
        }
    }

    private void stopTimer() {
        if (mUpdater != null) {
            mUpdater.removeCallbacks(mUpdaterTask);
            mUpdater = null;
        }
    }

    protected FeedAdapter.Updater getUpdaterCallback() {
        return new FeedAdapter.Updater() {
            @Override
            public void onUpdate() {
                if (!mIsUpdating) {
                    update(true); //refresh on scroll
                }
            }
        };
    }

    private void showLoading() {
        setSupportProgressBarIndeterminateVisibility(true);
    }

    private void hideLoading() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mBarAvatar = menu.findItem(R.id.action_profile);
        MenuItemCompat.getActionView(mBarAvatar).findViewById(R.id.ivBarAvatar).setOnClickListener(this);
        MenuItem item = menu.findItem(R.id.action_profile);
        if (item != null) {
            item.setChecked(false);
        }
        setActionBarAvatar(mUser);
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_chat;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                if (mUser != null) {
                    if (!(mUser.deleted || mUser.banned)) {
                        initActions(mChatActionsStub, mUser, getActions(mUser));
                        boolean checked = item.isChecked();
                        item.setChecked(!checked);
                        animateChatActions(ACTIONS_CLOSE_ANIMATION_TIME);
                    } else {
                        Toast.makeText(getActivity(), R.string.user_deleted_or_banned,
                                Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            case android.R.id.home:
                Utils.hideSoftKeyboard(getActivity(), mEditBox);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface OnListViewItemLongClickListener {
        public void onLongClick(int position, View v);
    }

    private ArrayList<UserActions.ActionItem> getActions(FeedUser user) {
        if (mChatActions == null) {
            mChatActions = new ArrayList<>();
            mChatActions.add(new UserActions.ActionItem(user.sex == 1 ? R.id.acProfile : R.id.acWProfile, this));
            mChatActions.add(new UserActions.ActionItem(R.id.add_to_black_list_action, this));
            mChatActions.add(new UserActions.ActionItem(R.id.complain_action, this));
            mChatActions.add(new UserActions.ActionItem(R.id.add_to_bookmark_action, this));
        }
        return mChatActions;
    }

    private View mActions;
    private int mActionsHeightHeuristic;
    private AddToBlackListViewsController mBlackListActionController;

    private void initActions(ViewStub actionsStub, FeedUser user, ArrayList<UserActions.ActionItem> actions) {
        if (mActions == null) {
            actionsStub.setLayoutResource(R.layout.user_actions_layout);
            mActions = actionsStub.inflate();
            // список действий в контекстном меню
            UserActions userActions = new UserActions(mActions, actions);
            TextView bookmarksTv = (TextView) userActions.getViewById(R.id.add_to_bookmark_action).findViewById(R.id.bookmark_action_text);
            mBlackListActionController = new AddToBlackListViewsController(mActions);
            mBlackListActionController.switchAction();
            bookmarksTv.setText(user.bookmarked ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
            switchBookmarkEnabled(!mUser.blocked);
            mActionsHeightHeuristic = actions.size() * Utils.getPxFromDp(40);
        }
    }

    private void animateChatActions(long time) {
        if (mActions != null) {
            final boolean needToClose = mActions.getVisibility() == View.VISIBLE;
            TranslateAnimation ta;
            int height = getChatActionsViewHeight();
            if (needToClose) {
                ta = new TranslateAnimation(0, 0, 0, -height);
            } else {
                ta = new TranslateAnimation(0, 0, -height, 0);
            }
            ta.setDuration(time);
            ta.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (!needToClose) {
                        mActions.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mActions.clearAnimation();
                    if (needToClose) {
                        mActions.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            if (!needToClose) {
                Utils.hideSoftKeyboard(getActivity(), mEditBox);
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

    private class AddToBlackListViewsController {

        final TextView actionText;
        final View actionLoader;
        final View actionIcon;

        AddToBlackListViewsController(View root) {
            View actionView = root.findViewById(R.id.add_to_black_list_action);
            actionText = (TextView) actionView.findViewById(R.id.block_action_text);
            actionLoader = actionView.findViewById(R.id.blockPrBar);
            actionIcon = actionView.findViewById(R.id.blockIcon);
            // click listener for actionView is set through UserActions
            // set states for views
            switchAction();
        }

        public void switchAction() {
            actionText.setText(mUser.blocked ? R.string.black_list_delete : R.string.black_list_add_short);
        }

        public void processActionFor(int userId) {
            if (CacheProfile.premium) {
                if (userId > 0) {
                    actionLoader.setVisibility(View.VISIBLE);
                    actionIcon.setVisibility(View.GONE);

                    ApiRequest request;
                    if (mUser.blocked) {
                        request = new DeleteBlackListRequest(userId, getActivity());
                    } else {
                        request = new BlackListAddRequest(userId, getActivity());
                    }
                    request.exec();
                }
            } else {
                startActivityForResult(PurchasesActivity.createVipBuyIntent(null, "Chat"), PurchasesActivity.INTENT_BUY_VIP);
                closeChatActions();
            }
        }

        public void setViewsToNormalState() {
            actionLoader.setVisibility(View.INVISIBLE);
            actionIcon.setVisibility(View.VISIBLE);
        }
    }

    private class PopularUserChatController {

        public static final int NO_BLOCK = -1;
        public static final int FIRST_STAGE = 35;
        public static final int SECOND_STAGE = 36;

        private int mStage = NO_BLOCK;

        private View mPopularChatBlocker;
        private PopularUserDialog mPopularMessageBlocker;
        private String mMaleLockText;
        private String mFemaleLockText;
        private boolean isInExperement57_2;

        public PopularUserChatController() {
            Options options = CacheProfile.getOptions();
            isInExperement57_2 = options.popularUserLock != null;
            if (isInExperement57_2) {
                mMaleLockText = options.popularUserLock.maleLockText;
                mFemaleLockText = options.popularUserLock.femaleLockText;
            }
        }

        public boolean isAccessAllowed() {
            return CacheProfile.premium || !isInExperement57_2;
        }

        public boolean checkChatBlock(History message) {
            return (mStage = message.type) == FIRST_STAGE;
        }

        public boolean checkMessageBlock(History message) {
            return (mStage = message.type) == SECOND_STAGE;
        }

        public boolean block(History message) {
            if (!isAccessAllowed()) {
                if (checkChatBlock(message)) {
                    blockChat();
                    return true;
                } else if (checkMessageBlock(message)) {
                    initBlockDialog();
                    return false;
                }
            } else if (mPopularChatBlocker != null && mPopularChatBlocker.getVisibility() == View.VISIBLE) {
                mPopularChatBlocker.setVisibility(View.GONE);
                mLockScreen.setVisibility(View.GONE);
            }
            return false;
        }

        public void blockChat() {
            ViewStub stub = (ViewStub) mLockScreen.findViewById(R.id.famousBlockerStub);
            for (int i = 0; i < mLockScreen.getChildCount(); i++) {
                View v = mLockScreen.getChildAt(i);
                if (v != mPopularChatBlocker) {
                    mLockScreen.getChildAt(i).setVisibility(View.GONE);
                }
            }
            if (mPopularChatBlocker == null) {
                mPopularChatBlocker = stub.inflate();
                TextView lockText = (TextView) mPopularChatBlocker.findViewById(R.id.popular_user_lock_text);
                lockText.setText(mUserName + " " + (mUserSex == Static.BOY ? mMaleLockText : mFemaleLockText));
                mPopularChatBlocker.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.btnBuyVip:
                                EasyTracker.getTracker().sendEvent(getTrackName(), "BuyVipStatus", "", 1L);
                                Intent intent = ContainerActivity.getVipBuyIntent(null, "PopularUserChatBlock");
                                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                                break;
                        }
                    }
                });
            } else {
                if (mLockScreen != mPopularChatBlocker.getParent()) {
                    mLockScreen.addView(mPopularChatBlocker);
                }
            }
            mLockScreen.requestLayout();
            mLockScreen.invalidate();
            mLockScreen.setVisibility(View.VISIBLE);
        }

        public void releaseLock() {
            mLockScreen.removeView(mPopularChatBlocker);
        }

        public void initBlockDialog() {
            if (mPopularMessageBlocker == null) {
                mPopularMessageBlocker = new PopularUserDialog(mUserName, mUserSex);
            }
        }

        public boolean showBlockDialog() {
            if (mStage != NO_BLOCK) {
                if (mPopularMessageBlocker == null) {
                    initBlockDialog();
                }
                Fragment dialog = getFragmentManager().findFragmentByTag("POPULAR_USER_DIALOG");
                if (dialog == null || !dialog.isAdded()) {
                    mPopularMessageBlocker.show(getFragmentManager(), "POPULAR_USER_DIALOG");
                }
                return true;
            }
            return false;
        }

        public boolean isDialogOpened() {
            return mPopularMessageBlocker != null && mPopularMessageBlocker.isOpened();
        }

        public boolean isChatLocked() {
            return mStage == FIRST_STAGE;
        }

        public void reset() {
            mStage = NO_BLOCK;
        }

    }
}
