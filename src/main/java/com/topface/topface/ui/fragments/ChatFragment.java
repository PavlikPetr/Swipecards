package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.PullToRefreshBase;
import com.topface.PullToRefreshListView;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.History;
import com.topface.topface.data.HistoryListData;
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
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.AttitudeHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.ComplainsActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.IUserOnlineListener;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.ChatListAdapter;
import com.topface.topface.ui.adapters.EditButtonsAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.KeyboardListenerLayout;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.UserActions;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.actionbar.ActionBarCustomViewTitleSetterDelegate;
import com.topface.topface.utils.actionbar.ActionBarOnlineSetterDelegate;
import com.topface.topface.utils.actionbar.IActionBarTitleSetter;
import com.topface.topface.utils.controllers.PopularUserChatController;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.notifications.UserNotification;
import com.topface.topface.utils.social.AuthToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class ChatFragment extends BaseFragment implements View.OnClickListener, IUserOnlineListener {

    public static final int LIMIT = 50;
    public static final int PROGRESS_BAR_DELAY = 3000;

    public static final String FRIEND_FEED_USER = "user_profile";
    public static final String ADAPTER_DATA = "adapter";
    public static final String WAS_FAILED = "was_failed";
    private static final String KEYBOARD_OPENED = "keyboard_opened";
    private static final String POPULAR_LOCK_STATE = "chat_blocked";
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_USER_SEX = "user_sex";
    public static final String INTENT_USER_CITY = "user_city";
    public static final String INTENT_USER_NAME_AND_AGE = "user_name_and_age";
    public static final String INTENT_ITEM_ID = "item_id";
    public static final String MAKE_ITEM_READ = "com.topface.topface.feedfragment.MAKE_READ";
    public static final String MAKE_ITEM_READ_BY_UID = "com.topface.topface.feedfragment.MAKE_READ_BY_UID";
    public static final String INITIAL_MESSAGE = "initial_message";
    public static final String MESSAGE = "message";
    public static final String LOADED_MESSAGES = "loaded_messages";


    private static final int DEFAULT_CHAT_UPDATE_PERIOD = 30000;

    private BroadcastReceiver mUpdateActionsReceiver = new BroadcastReceiver() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onReceive(Context context, Intent intent) {
            AttitudeHandler.ActionTypes type = (AttitudeHandler.ActionTypes) intent.getSerializableExtra(AttitudeHandler.TYPE);
            boolean value = intent.getBooleanExtra(AttitudeHandler.VALUE, false);
            if (mUser != null && type != null) {
                switch (type) {
                    case BLACK_LIST:
                        mUser.blocked = value;
                        if (mBlocked != null) {
                            ((TextView) mBlocked.findViewById(R.id.block_action_text)).setText(value ? R.string.black_list_delete : R.string.black_list_add_short);
                            if (value) {
                                mUser.bookmarked = false;
                            }
                            switchBookmarkEnabled(!value);
                        }
                        if (mActions != null) {
                            mActions.findViewById(R.id.blockPrBar).setVisibility(View.INVISIBLE);
                            mActions.findViewById(R.id.blockIcon).setVisibility(View.VISIBLE);
                        }
                        break;
                    case BOOKMARK:
                        if (mBookmarkAction != null && intent.hasExtra(AttitudeHandler.VALUE) && !mUser.blocked) {
                            mUser.bookmarked = value;
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


    private void switchBookmarkEnabled(boolean enabled) {
        if (mActions != null) {
            TextView mBookmarkAction = ((TextView) mActions.findViewById(R.id.bookmark_action_text));
            mBookmarkAction.setText(mUser.bookmarked ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
            mBookmarkAction.setTextColor(getResources().getColor(enabled ? R.color.text_white : R.color.disabled_color));
            mActions.findViewById(R.id.add_to_bookmark_action).setEnabled(enabled);
        }
    }

    // Data
    private int mUserId;
    private BroadcastReceiver mNewMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra(GCMUtils.USER_ID_EXTRA);
            final int type = intent.getIntExtra(GCMUtils.GCM_TYPE, -1);
            if (!TextUtils.isEmpty(id) && Integer.parseInt(id) == mUserId) {
                update(true, "update counters");
                startTimer();
                GCMUtils.cancelNotification(App.getContext(), type);
            }
        }
    };
    private String mMessage;
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
    private String mInitialMessage;
    private boolean wasFailed = false;
    private ArrayList<UserActions.ActionItem> mUserActions;
    private int mMaxMessageSize = CacheProfile.getOptions().maxMessageSize;
    private CountDownTimer mTimer;
    private boolean mIsBeforeFirstChatUpdate = true;
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
    private ActionBarOnlineSetterDelegate mOnlineSetter;
    private RelativeLayout mLockScreen;
    private PopularUserChatController mPopularUserLockController;
    private ViewStub mChatActionsStub;
    private String mUserCity;
    private String mUserNameAndAge;
    private int mUserSex;
    private MenuItem mBarAvatar;
    private MenuItem mBarActions;
    private RelativeLayout mBlocked;
    private TextView mBookmarkAction;
    private View mActions;
    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return actionId == EditorInfo.IME_ACTION_SEND && sendMessage();
        }
    };
    private boolean mWasNotEmptyHistory;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            updateSendMessageAbility();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private ImageButton mSendButton;
    private int mLoadedItemsCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DateUtils.syncTime();
        setRetainInstance(true);
        String text = UserNotification.getRemoteInputMessageText(getActivity().getIntent());
        if (text != null) {
            sendMessage(text, false);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // do not recreate Adapter cause of steRetainInstance(true)
        if (mAdapter == null) {
            mAdapter = new ChatListAdapter(getActivity(), new FeedList<History>(), getUpdaterCallback(), new OnLoadMessagesListener() {
                @Override
                public void onLoadMessages(int i) {
                    mLoadedItemsCount += i;
                }
            });
        }

        Bundle args = getArguments();
        mItemId = args.getString(INTENT_ITEM_ID);
        mUserId = args.getInt(INTENT_USER_ID, -1);
        mUserSex = args.getInt(INTENT_USER_SEX, Static.BOY);
        mUserCity = args.getString(INTENT_USER_CITY);
        mUserNameAndAge = args.getString(INTENT_USER_NAME_AND_AGE);
        mInitialMessage = args.getString(INITIAL_MESSAGE);

        // only DialogsFragment will hear this
        Intent intent = new Intent(ChatFragment.MAKE_ITEM_READ_BY_UID);
        intent.putExtra(ChatFragment.INTENT_USER_ID, mUserId);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final KeyboardListenerLayout root = (KeyboardListenerLayout) inflater.inflate(R.layout.fragment_chat, null);
        root.setKeyboardListener(new KeyboardListenerLayout.KeyboardListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void keyboardOpened() {
                mIsKeyboardOpened = true;
                animateHideChatAction();
                mKeyboardFreeHeight = getView().getHeight();
            }

            @Override
            public void keyboardClosed() {
                mIsKeyboardOpened = false;
            }
        });
        Debug.log(this, "+onCreate");
        mChatActionsStub = (ViewStub) root.findViewById(R.id.chat_actions_stub);
        mActions = null;
        // Swap Control
        root.findViewById(R.id.send_gift_button).setOnClickListener(this);
        //Send Button
        mSendButton = (ImageButton) root.findViewById(R.id.btnSend);
        mSendButton.setOnClickListener(this);
        // Edit Box
        mEditBox = (EditText) root.findViewById(R.id.edChatBox);
        if (mInitialMessage != null) {
            mEditBox.setText(mInitialMessage);
            mEditBox.setSelection(mInitialMessage.length());
        }
        mEditBox.setOnEditorActionListener(mEditorActionListener);
        mEditBox.addTextChangedListener(mTextWatcher);

        //LockScreen
        initLockScreen(root);
        if (savedInstanceState != null) {
            Parcelable popularLockState = savedInstanceState.getParcelable(POPULAR_LOCK_STATE);
            if (popularLockState != null) {
                mPopularUserLockController.setState((PopularUserChatController.SavedState) popularLockState);
            }
        }
        updateSendMessageAbility();
        checkPopularUserLock();
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
        mJustResumed = false;
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateActionsReceiver, new IntentFilter(AttitudeHandler.UPDATE_USER_CATEGORY));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isAdded()) {
            Intent intent = new Intent(CountersManager.UPDATE_COUNTERS);
            intent.putExtra(LOADED_MESSAGES, mLoadedItemsCount);
            intent.putExtra(ChatFragment.INTENT_USER_ID, mUserId);
            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateActionsReceiver);
    }

    private void restoreData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            try {
                mMessage = savedInstanceState.getString(MESSAGE);
                setSavedMessage(mMessage);
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

        // list view
        mListView = (PullToRefreshListView) root.findViewById(R.id.lvChatList);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                update(true, "pull to refresh");
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        final ListView mListViewFromPullToRefresh = mListView.getRefreshableView();
        mListViewFromPullToRefresh.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mListViewFromPullToRefresh.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.item_empty_footer, null));
        // detect gesture on ListView
        final GestureDetectorCompat mListViewDetector = new GestureDetectorCompat(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // hide keyBoard when chatAction invisible
                if (!animateHideChatAction()) {
                    Utils.hideSoftKeyboard(getActivity(), mEditBox);
                }
                return false;
            }
        });
        mListViewFromPullToRefresh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mListViewDetector.onTouchEvent(event);
                return false;
            }
        });
        mListViewFromPullToRefresh.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           int position, long id) {
                final int pos = position - mListViewFromPullToRefresh.getHeaderViewsCount();
                History item = mAdapter.getItem(pos);
                final EditButtonsAdapter editAdapter = new EditButtonsAdapter(getActivity(), item);
                if (item == null) return true;
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.general_spinner_title)
                        .setAdapter(editAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch ((int) editAdapter.getItemId(which)) {
                                    case EditButtonsAdapter.ITEM_DELETE:
                                        deleteItem(pos);
                                        EasyTracker.sendEvent("Chat", "DeleteItem", "", 1L);
                                        break;
                                    case EditButtonsAdapter.ITEM_COPY:
                                        mAdapter.copyText(((TextView) view.findViewById(R.id.chat_message)).getText().toString());
                                        EasyTracker.sendEvent("Chat", "CopyItemText", "", 1L);
                                        break;
                                    case EditButtonsAdapter.ITEM_COMPLAINT:
                                        startActivity(ComplainsActivity.createIntent(mUserId, mAdapter.getItem(pos).id));
                                        EasyTracker.sendEvent("Chat", "ComplainItemText", "", 1L);
                                        break;
                                }
                            }
                        }).create().show();
                return true;
            }
        });
    }

    private void initLockScreen(View root) {
        mLockScreen = (RelativeLayout) root.findViewById(R.id.llvLockScreen);
        RetryViewCreator retryView = new RetryViewCreator.Builder(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(false, "retry");
                mLockScreen.setVisibility(View.GONE);
            }
        }).messageFontColor(R.color.text_color_gray).noShadow().build();
        mLockScreen.addView(retryView.getView());

        if (mPopularUserLockController != null) {
            mPopularUserLockController.setLockScreen(mLockScreen);
        } else {
            mPopularUserLockController = new PopularUserChatController(this, mLockScreen);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                    mPopularUserLockController, new IntentFilter(CountersManager.UPDATE_VIP_STATUS));
        }
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

    private void updateSendMessageAbility() {
        if (mSendButton != null && mEditBox != null) {
            mSendButton.setEnabled(!mEditBox.getText().toString().isEmpty());
        }
    }

    @Override
    protected String getTitle() {
        return mUserNameAndAge;
    }

    @Override
    protected String getSubtitle() {
        if (TextUtils.isEmpty(mUserCity)) {
            return Static.EMPTY;
        } else {
            return mUserCity;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mMessage)) {
            outState.putString(MESSAGE, mMessage);
        }
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
        outState.putParcelable(POPULAR_LOCK_STATE, mPopularUserLockController.getSavedState()
        );
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
        new DeleteMessagesRequest(item.id, getActivity()).callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                if (isAdded()) {
                    int invertedPosition = mAdapter.getPosition(position);
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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPopularUserLockController);
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
        if (mIsUpdating) {
            return;
        }
        mIsUpdating = true;
        final boolean isPopularLockOn;
        isPopularLockOn = mAdapter != null &&
                !mAdapter.isEmpty() &&
                (mPopularUserLockController.isChatLocked() || mPopularUserLockController.isResponseLocked()) &&
                pullToRefresh;

        HistoryRequest historyRequest = new HistoryRequest(getActivity(), mUserId) {

            @Override
            public void exec() {
                mIsUpdating = true;
                if (!pullToRefresh && !scrollRefresh && !mPopularUserLockController.isChatLocked()) {
                    showLoading();
                }
                super.exec();
            }
        };

        registerRequest(historyRequest);
        historyRequest.debug = type;
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
                if (!data.items.isEmpty() && !isPopularLockOn) {
                    for (History message : data.items) {
                        mPopularUserLockController.setTexts(message.dialogTitle, message.blockText);
                        int blockStage = mPopularUserLockController.block(message);
                        if (blockStage == PopularUserChatController.FIRST_STAGE) {
                            mIsUpdating = false;
                            wasFailed = false;
                            mUser = data.user;
                            if (!mUser.isEmpty()) {
                                onUserLoaded(mUser);
                            }
                            return;
                        } else if (blockStage == PopularUserChatController.SECOND_STAGE) {
                            break;
                        }
                    }
                    mPopularUserLockController.unlockChat();
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
                } else {
                    removeFakesNotWaiting(data);
                }

                refreshActionBarTitles();
                mOnlineSetter.setOnline(data.user.online);
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
                        if (!mIsKeyboardOpened && !mWasNotEmptyHistory) {
                            Utils.showSoftKeyboard(getActivity(), mEditBox);
                            mIsKeyboardOpened = true;
                            mWasNotEmptyHistory = false;
                        }
                    } else {
                        mWasNotEmptyHistory = true;
                    }
                }
                mIsUpdating = false;
                //show keyboard if display size more then 479dp
                showKeyboardOnLargeScreen();
                mIsBeforeFirstChatUpdate = false;

                if (mLockScreen != null && mLockScreen.getVisibility() == View.VISIBLE) {
                    mLockScreen.setVisibility(View.GONE);
                }
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

    private void showKeyboardOnLargeScreen() {
        if (isShowKeyboardInChat() && mIsBeforeFirstChatUpdate) {
            Utils.showSoftKeyboard(getActivity(), mEditBox);
            mIsKeyboardOpened = true;
        }
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

    private void removeFakesNotWaiting(HistoryListData data) {
        if (!mAdapter.isEmpty()) {
            ArrayList<History> itemsToDelete = new ArrayList<>();
            for (History item : mAdapter.getData()) {
                for (History newItem : data.items) {
                    if (item.isFake() && ((!item.isWaitingItem() && !item.isRepeatItem()) || TextUtils.equals(item.text, newItem.text))) {
                        itemsToDelete.add(item);
                    }
                }
            }
            mAdapter.getData().removeAll(itemsToDelete);
        }
    }

    private void onUserLoaded(FeedUser user) {
        if (!(user.deleted || user.banned)) {
            // ставим значок онлайн в нужное состояние
            setOnline(user.online);
        }
        // ставим фото пользователя в иконку в actionbar
        setActionBarAvatar(user);
    }

    @Override
    public void setOnline(boolean online) {
        if (mOnlineSetter != null) {
            mOnlineSetter.setOnline(online);
        }
    }

    protected IActionBarTitleSetter createTitleSetter(ActionBar actionBar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mOnlineSetter = new ActionBarCustomViewTitleSetterDelegate(getActivity(), actionBar,
                    R.id.title_clickable, R.id.title, R.id.subtitle);
        } else {
            mOnlineSetter = new ActionBarOnlineSetterDelegate(actionBar, getActivity());
        }
        return mOnlineSetter;
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
        switch (v.getId()) {
            case R.id.btnSend:
                if (mUserId > 0) {
                    sendMessage();
                    EasyTracker.sendEvent("Chat", "SendMessage", "", 1L);
                }
                break;
            case R.id.add_to_black_list_action:
                if (mUser.id > 0) {
                    final ProgressBar loader = (ProgressBar) v.findViewById(R.id.blockPrBar);
                    final ImageView icon = (ImageView) v.findViewById(R.id.blockIcon);
                    loader.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.GONE);
                    ApiRequest request;
                    if (mUser.blocked) {
                        request = new DeleteBlackListRequest(mUser.id, getActivity());
                    } else {
                        request = new BlackListAddRequest(mUser.id, getActivity());
                    }
                    request.exec();
                }
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
                hideChatAction();
                break;
            case R.id.send_gift_button:
                if (mPopularUserLockController.showBlockDialog()) {
                    Debug.log("Gift can't be sent because user is too popular.");
                    break;
                }
                startActivityForResult(
                        GiftsActivity.getSendGiftIntent(getActivity(), mUserId),
                        GiftsActivity.INTENT_REQUEST_GIFT
                );
                EasyTracker.sendEvent("Chat", "SendGiftClick", "", 1L);
                break;
            case R.id.ivBarAvatar:
                onOptionsItemSelected(mBarAvatar);
                hideChatAction();
                break;
            case R.id.action_user_actions_list:
                onOptionsItemSelected(mBarActions);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        setSavedMessage(mMessage);
        if (mUserId == 0) {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        // Если адаптер пустой или пользователя нет, грузим с сервера
        if (mAdapter == null || mAdapter.getCount() == 0 || mUser == null) {
            update(false, "initial");
        } else {
            if (!mPopularUserLockController.isChatLocked()) {
                update(true, "resume update");
                mAdapter.notifyDataSetChanged();
            }
        }

        IntentFilter filter = new IntentFilter(GCMUtils.GCM_NOTIFICATION);
        LocalBroadcastManager.getInstance(App.getContext()).registerReceiver(mNewMessageReceiver, filter);

        mUpdater = new Handler();
        startTimer();

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
        deleteTimerDelay();
        LocalBroadcastManager.getInstance(App.getContext()).unregisterReceiver(mNewMessageReceiver);
        stopTimer();
        Utils.hideSoftKeyboard(getActivity(), mEditBox);
        mIsKeyboardOpened = false;
        mJustResumed = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GiftsActivity.INTENT_REQUEST_GIFT:
                if (resultCode == Activity.RESULT_OK) {
                    scrollListToTheEnd();
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        SendGiftAnswer sendGiftAnswer = extras.getParcelable(GiftsActivity.INTENT_SEND_GIFT_ANSWER);
                        sendGiftAnswer.history.target = FeedDialog.OUTPUT_USER_MESSAGE;
                        addSentMessage(sendGiftAnswer.history, null);
                        LocalBroadcastManager.getInstance(getActivity())
                                .sendBroadcast(new Intent(DialogsFragment.REFRESH_DIALOGS));
                    }
                }
                break;
        }
    }

    private void scrollListToTheEnd() {
        if (mListView != null && mAdapter != null) {
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.getRefreshableView().setSelection(mAdapter.getCount());
                }
            });
        }
    }

    private void addSentMessage(History loaderItem, ApiRequest request) {
        mAdapter.addSentMessage(loaderItem, mListView.getRefreshableView(), request);
    }

    private boolean sendMessage() {
        if (mPopularUserLockController.showBlockDialog()) {
            Debug.log("Message not sent because user is too popular.");
            return false;
        }
        Editable editText = mEditBox.getText();
        String editString = editText == null ? "" : editText.toString();
        if (editText == null || TextUtils.isEmpty(editString.trim()) || mUserId == 0) {
            return false;
        }
        if (editText.length() > mMaxMessageSize) {
            Toast.makeText(getActivity(),
                    String.format(getString(R.string.message_too_long), mMaxMessageSize),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        editText.clear();
        return sendMessage(editString, true);
    }

    public boolean sendMessage(String text, final boolean cancelable) {
        final History messageItem = new History(text, IListLoader.ItemType.TEMP_MESSAGE);
        final MessageRequest messageRequest = new MessageRequest(mUserId, text, getActivity());
        if (cancelable) {
            registerRequest(messageRequest);
        }
        if (mAdapter != null && mListView != null && cancelable) {
            addSentMessage(messageItem, messageRequest);
        }
        messageRequest.callback(new DataApiHandler<History>() {
            @Override
            protected void success(History data, IApiResponse response) {
                if (mAdapter != null && cancelable) {
                    mAdapter.replaceMessage(messageItem, data, mListView.getRefreshableView());
                }
                LocalBroadcastManager.getInstance(getActivity())
                        .sendBroadcast(new Intent(DialogsFragment.REFRESH_DIALOGS));
                scrollListToTheEnd();
            }

            @Override
            protected History parseResponse(ApiResponse response) {
                return new History(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (codeError == ErrorCodes.PREMIUM_ACCESS_ONLY) {
                    mMessage = mAdapter.getData().get(0).text;
                    mAdapter.removeLastItem();
                    startActivityForResult(PurchasesActivity.createVipBuyIntent(getResources()
                                    .getString(R.string.messaging_block_buy_vip), "SendMessage"),
                            PurchasesActivity.INTENT_BUY_VIP);
                    return;
                }
                if (mAdapter != null && cancelable) {
                    Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                    mAdapter.showRetrySendMessage(messageItem, messageRequest);
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
        startTimerDelay();
    }

    private void hideLoading() {
        deleteTimerDelay();
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_profile);
        if (item != null && mBarAvatar != null) {
            item.setChecked(mBarAvatar.isChecked());
        }
        mBarAvatar = item;
        MenuItemCompat.getActionView(mBarAvatar).findViewById(R.id.ivBarAvatar).setOnClickListener(this);
        setActionBarAvatar(mUser);

        MenuItem barActionsItem = menu.findItem(R.id.action_user_actions_list);
        if (barActionsItem != null && mBarActions != null) {
            barActionsItem.setChecked(mBarActions.isChecked());
        }
        mBarActions = barActionsItem;
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_chat;
    }

    private ArrayList<UserActions.ActionItem> getActionItems() {
        if (mUserActions == null) {
            mUserActions = new ArrayList<>();
            mUserActions.add(new UserActions.ActionItem(R.id.add_to_black_list_action, this));
            mUserActions.add(new UserActions.ActionItem(R.id.complain_action, this));
            mUserActions.add(new UserActions.ActionItem(R.id.add_to_bookmark_action, this));
        }
        return mUserActions;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                if (mUser != null) {
                    if (!(mUser.deleted || mUser.banned)) {
                        Intent profileIntent = CacheProfile.getOptions().autoOpenGallery.createIntent(mUserId, mUser.photosCount, mUser.photo, getActivity());
                        startActivity(profileIntent);
                    } else {
                        Toast.makeText(getActivity(), R.string.user_deleted_or_banned,
                                Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            case R.id.action_user_actions_list:
                if (mUser != null) {
                    initActions(mChatActionsStub, mUser, getActionItems());
                    boolean checked = item.isChecked();
                    item.setChecked(!checked);
                    changeChatActionState();
                }
                return true;
            case android.R.id.home:
                Utils.hideSoftKeyboard(getActivity(), mEditBox);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int mActionsHeightHeuristic;

    private void initActions(ViewStub actionsStub, FeedUser user, ArrayList<UserActions.ActionItem> actions) {
        if (mActions == null) {
            actionsStub.setLayoutResource(R.layout.user_actions_layout);
            mActions = actionsStub.inflate();
            // список действий в контекстном меню
            UserActions userActions = new UserActions(mActions, actions);
            mBlocked = (RelativeLayout) mActions.findViewById(R.id.add_to_black_list_action);
            ((TextView) mBlocked.findViewById(R.id.block_action_text)).setText(
                    user.blocked ? R.string.black_list_delete : R.string.black_list_add_short
            );
            mBookmarkAction = (TextView) mActions.findViewById(R.id.bookmark_action_text);
            mBookmarkAction.setText(App.getContext().getString(
                    user.bookmarked ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add
            ));
            TextView bookmarksTv = (TextView) userActions.getViewById(R.id.add_to_bookmark_action).findViewById(R.id.bookmark_action_text);
            new AddToBlackListViewsController(mActions).switchAction();
            bookmarksTv.setText(user.bookmarked ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
            switchBookmarkEnabled(!mUser.blocked);
            mActionsHeightHeuristic = actions.size() * Utils.getPxFromDp(40);
        }
    }

    private void changeChatActionState() {
        if (mActions.getVisibility() == View.VISIBLE) {
            animateHideChatAction();
        } else {
            animateShowChatAction();
        }
    }

    private void hideChatAction() {
        if (mActions == null || mActions.getVisibility() == View.INVISIBLE) {
            return;
        }
        mActions.setVisibility(View.INVISIBLE);
    }

    private boolean animateHideChatAction() {
        if (mActions == null || mActions.getVisibility() == View.INVISIBLE) {
            return false;
        }
        TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -getChatActionsViewHeight());
        ta.setDuration(getAnimationTime());
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mActions.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mActions.startAnimation(ta);
        return true;
    }

    private void animateShowChatAction() {
        if (mActions == null || mActions.getVisibility() == View.VISIBLE) {
            return;
        }
        TranslateAnimation ta = new TranslateAnimation(0, 0, -getChatActionsViewHeight(), 0);
        ta.setDuration(getAnimationTime());
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mActions.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mActions.startAnimation(ta);
    }

    private int getAnimationTime() {
        return mUserActions.size() * getActivity().getResources().getInteger(R.integer.action_animation_time);
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
    }

    private void deleteTimerDelay() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void startTimerDelay() {
        deleteTimerDelay();
        mTimer = new CountDownTimer(PROGRESS_BAR_DELAY, PROGRESS_BAR_DELAY) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (mIsUpdating) {
                    setSupportProgressBarIndeterminateVisibility(true);
                }
            }
        }.start();
    }

    private boolean isShowKeyboardInChat() {
        return Device.getMaxDisplaySize() >= getActivity().getResources().getDimension(R.dimen.min_screen_height_chat_fragment);
    }

    private void setSavedMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            mEditBox.setText(message);
            mEditBox.setSelection(message.length());
        }
    }

    public interface OnLoadMessagesListener {

        public void onLoadMessages(int i);

    }

}
