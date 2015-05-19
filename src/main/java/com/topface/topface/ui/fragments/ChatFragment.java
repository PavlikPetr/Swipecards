package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.ChatListAnimatedAdapter;
import com.topface.PullToRefreshBase;
import com.topface.PullToRefreshListView;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.History;
import com.topface.topface.data.HistoryListData;
import com.topface.topface.data.IUniversalUser;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.UniversalUserFactory;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteMessagesRequest;
import com.topface.topface.requests.HistoryRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.ComplainsActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.adapters.ChatListAdapter;
import com.topface.topface.ui.adapters.EditButtonsAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.HackBaseAdapterDecorator;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.dialogs.ConfirmEmailDialog;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.views.BackgroundProgressBarController;
import com.topface.topface.ui.views.KeyboardListenerLayout;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.actionbar.OverflowMenuUser;
import com.topface.topface.utils.controllers.PopularUserChatController;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.notifications.UserNotification;
import com.topface.topface.utils.social.AuthToken;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class ChatFragment extends AnimatedFragment implements View.OnClickListener {

    public static final int LIMIT = 50;

    public static final String FRIEND_FEED_USER = "user_profile";
    public static final String ADAPTER_DATA = "adapter";
    public static final String WAS_FAILED = "was_failed";
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
    public static final String CONFIRM_EMAIL_DIALOG_TAG = "configrm_email_dialog_tag";
    private static final String POPULAR_LOCK_STATE = "chat_blocked";
    private static final String HISTORY_CHAT = "history_chat";
    private static final String SOFT_KEYBOARD_LOCK_STATE = "keyboard_state";
    private static final int DEFAULT_CHAT_UPDATE_PERIOD = 30000;

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
    private ArrayList<History> mHistoryFeedList;
    private Handler mUpdater;
    private boolean mIsUpdating;
    private boolean mKeyboardWasShown;
    private PullToRefreshListView mListView;
    private ChatListAdapter mAdapter;
    private FeedUser mUser;
    private EditText mEditBox;
    private String mItemId;
    private String mInitialMessage;
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
    private int mMaxMessageSize = CacheProfile.getOptions().maxMessageSize;
    // Managers
    private RelativeLayout mLockScreen;
    private PopularUserChatController mPopularUserLockController;
    private BackgroundProgressBarController mBackgroundController = new BackgroundProgressBarController();
    private String mUserCity;
    private String mUserNameAndAge;
    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return actionId == EditorInfo.IME_ACTION_SEND && sendMessage();
        }
    };
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
    private ChatListAnimatedAdapter mAnimatedAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isShowKeyboardInChat()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
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
        // do not recreate Adapter cause of setRetainInstance(true)
        if (mAdapter == null) {
            mAdapter = new ChatListAdapter(getActivity(), new FeedList<History>(), getUpdaterCallback());
        }

        Bundle args = getArguments();
        mItemId = args.getString(INTENT_ITEM_ID);
        mUserId = args.getInt(INTENT_USER_ID, -1);
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
        KeyboardListenerLayout root = (KeyboardListenerLayout) inflater.inflate(R.layout.fragment_chat, null);
        root.setKeyboardListener(new KeyboardListenerLayout.KeyboardListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void keyboardOpened() {
                mKeyboardWasShown = true;
            }

            @Override
            public void keyboardClosed() {
                mKeyboardWasShown = false;
            }
        });
        Debug.log(this, "+onCreate");
        // Swap Control
        root.findViewById(R.id.send_gift_button).setOnClickListener(this);
        //Send Button
        mSendButton = (ImageButton) root.findViewById(R.id.btnSend);
        mSendButton.setOnClickListener(this);
        // Loader on background
        mBackgroundController.setProgressBar((ProgressBar) root.findViewById(R.id.chat_loader));
        mBackgroundController.startAnimation();
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
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isAdded()) {
            if (mAdapter != null) {
                int loadedItemsCount = 0;
                // если в адаптере нет элементов списка, то возможно на экране отображается блокировка,
                // к примеру "Популярный пользователь"
                if (mAdapter.getDataCopy().isEmpty()) {
                    if (mHistoryFeedList != null) {
                        for (History message : mHistoryFeedList) {
                            int blockStage = mPopularUserLockController.block(message);
                            // проверяем тип сообщения, если адаптер пустой по причине блокировки экрана
                            // "FIRST_STAGE", то считаем непрочитанные сообщения в истории переписки
                            if (blockStage == PopularUserChatController.FIRST_STAGE && message.unread) {
                                loadedItemsCount++;
                            }
                        }
                    }
                } else {
                    for (History item : mAdapter.getDataCopy()) {
                        if (item.unread) {
                            loadedItemsCount++;
                        }
                    }
                }
                Intent intent = new Intent(CountersManager.UPDATE_COUNTERS);
                intent.putExtra(LOADED_MESSAGES, loadedItemsCount);
                intent.putExtra(ChatFragment.INTENT_USER_ID, mUserId);
                LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
            }
        }
    }

    private void restoreData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            try {
                mMessage = savedInstanceState.getString(MESSAGE);
                setSavedMessage(mMessage);
                mKeyboardWasShown = savedInstanceState.getBoolean(SOFT_KEYBOARD_LOCK_STATE);
                boolean wasFailed = savedInstanceState.getBoolean(WAS_FAILED);
                ArrayList<History> list = savedInstanceState.getParcelableArrayList(ADAPTER_DATA);
                mHistoryFeedList = savedInstanceState.getParcelableArrayList(HISTORY_CHAT);
                FeedList<History> historyData = new FeedList<>();
                if (list != null) {
                    for (History item : list) {
                        if (item != null) {
                            historyData.add(item);
                        }
                    }
                }
                mAdapter.setData(historyData);
                mUser = JsonUtils.fromJson(savedInstanceState.getString(FRIEND_FEED_USER), FeedUser.class);
                invalidateUniversalUser();
                initOverflowMenuActions(getOverflowMenu());
                if (wasFailed) {
                    mLockScreen.setVisibility(View.VISIBLE);
                } else {
                    mLockScreen.setVisibility(View.GONE);
                }
                mBackgroundController.hide();
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
        mAnimatedAdapter = new ChatListAnimatedAdapter(new HackBaseAdapterDecorator(mAdapter));
        mAnimatedAdapter.setAbsListView(mListView.getRefreshableView());
        mListView.setAdapter(mAnimatedAdapter);

        mListView.setOnScrollListener(mAdapter);
        final ListView mListViewFromPullToRefresh = mListView.getRefreshableView();
        mListViewFromPullToRefresh.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mListViewFromPullToRefresh.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.item_empty_footer, null));
        // detect gesture on ListView
        final GestureDetectorCompat mListViewDetector = new GestureDetectorCompat(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        // hide keyBoard when chatAction invisible
                        Utils.hideSoftKeyboard(getActivity(), mEditBox);
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
                mBackgroundController.startAnimation();
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
    protected String getDefaultTitle() {
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
        outState.putParcelableArrayList(HISTORY_CHAT, mHistoryFeedList);
        outState.putParcelableArrayList(ADAPTER_DATA, mAdapter.getDataCopy());
        outState.putBoolean(SOFT_KEYBOARD_LOCK_STATE, mKeyboardWasShown);
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
    }

    /**
     * Удаляет сообщение в чате
     *
     * @param position сообщени в списке
     */
    private void deleteItem(final int position) {
        History item = mAdapter.getItem(position);
        mAnimatedAdapter.decrementAnimationAdapter(mAdapter.getCount());
        if (item != null && (item.id == null || item.isFake())) {
            Utils.showToastNotification(R.string.cant_delete_fake_item, Toast.LENGTH_LONG);
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

    private void update(boolean pullToRefresh, String type) {
        update(pullToRefresh, false, type);
    }

    private void update(boolean scrollRefresh) {
        update(false, scrollRefresh, "scroll refresh");
    }

    private void update(final boolean pullToRefresh, final boolean scrollRefresh, String type) {
        if (mIsUpdating) {
            return;
        }
        final boolean isPopularLockOn;
        isPopularLockOn = mAdapter != null &&
                !mAdapter.isEmpty() &&
                (mPopularUserLockController.isChatLocked() || mPopularUserLockController.isResponseLocked()) &&
                pullToRefresh;

        HistoryRequest historyRequest = new HistoryRequest(getActivity(), mUserId) {

            @Override
            public void exec() {
                mIsUpdating = true;
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
                mHistoryFeedList = data.items;
                if (!data.items.isEmpty() && !isPopularLockOn) {
                    for (History message : data.items) {
                        mPopularUserLockController.setTexts(message.dialogTitle, message.blockText);
                        int blockStage = mPopularUserLockController.block(message);
                        if (blockStage == PopularUserChatController.FIRST_STAGE) {
                            mIsUpdating = false;
                            wasFailed = false;
                            mUser = data.user;
                            invalidateUniversalUser();
                            if (!mUser.isEmpty()) {
                                onUserLoaded(mUser);
                                initOverflowMenuActions(getOverflowMenu());
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
                getTitleSetter().setOnline(data.user.online);
                wasFailed = false;
                mUser = data.user;
                invalidateUniversalUser();
                if (!mUser.isEmpty()) {
                    onUserLoaded(mUser);
                    initOverflowMenuActions(getOverflowMenu());
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
                }
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
                FeedList<History> data = mAdapter != null ? mAdapter.getData() : null;
                if (mLockScreen != null && (data == null || data.isEmpty())) {
                    mLockScreen.setVisibility(View.VISIBLE);
                }
                wasFailed = true;
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                mBackgroundController.hide();
                requestExecuted();
                if (pullToRefresh && mListView != null) {
                    mListView.onRefreshComplete();
                }
                mIsUpdating = false;
            }

        }).exec();
    }

    private void showKeyboardOnLargeScreen() {
        if (isShowKeyboardInChat() && mKeyboardWasShown) {
            Utils.showSoftKeyboard(getActivity(), null);
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
        setActionBarAvatar(getUniversalUser());
    }

    @Override
    public void setOnline(boolean online) {
        if (getTitleSetter() != null) {
            getTitleSetter().setOnline(online);
        }
    }

    @Override
    protected IUniversalUser createUniversalUser() {
        return UniversalUserFactory.create(mUser);
    }

    @Override
    protected OverflowMenu createOverflowMenu(MenuItem barActions) {
        return new OverflowMenu(getActivity(), barActions);
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
            case R.id.action_user_actions_list:
                onOptionsItemSelected(getBarActionsMenuItem());
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        setSavedMessage(mMessage);
        //показать клавиатуру, если она была показаны до этого(перешли в другой фрагмент, и вернулись обратно)
        showKeyboardOnLargeScreen();

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
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(App.getContext()).unregisterReceiver(mNewMessageReceiver);
        stopTimer();
        Utils.hideSoftKeyboard(getActivity(), mEditBox);
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
            Utils.showToastNotification(String.format(getString(R.string.message_too_long), mMaxMessageSize), Toast.LENGTH_SHORT);
            return false;
        }
        // вынужденная мера, Editable.clear() некорректно обрабатывается клавиатурой Lg G3
        mEditBox.setText("");
        return sendMessage(editString, true);
    }

    public boolean sendMessage(String text, final boolean cancelable) {
        final History messageItem = new History(text, IListLoader.ItemType.TEMP_MESSAGE);
        final MessageRequest messageRequest = new MessageRequest(mUserId, text, getActivity());
        if (TextUtils.equals(AuthToken.getInstance().getSocialNet(), AuthToken.SN_TOPFACE)) {
            if (!CacheProfile.emailConfirmed) {
                Toast.makeText(App.getContext(), R.string.confirm_email, Toast.LENGTH_SHORT).show();
                ConfirmEmailDialog.newInstance().show(getActivity().getSupportFragmentManager(), CONFIRM_EMAIL_DIALOG_TAG);
                return false;
            }
        }
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
                    Utils.showErrorMessage();
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

    @Override
    protected void initOverflowMenuActions(OverflowMenu overflowMenu) {
        if (overflowMenu != null) {
            if (overflowMenu.getOverflowMenuFieldsListener() == null) {
                overflowMenu.setOverflowMenuFieldsListener(new OverflowMenuUser() {
                    @Override
                    public void setBlackListValue(Boolean value) {
                        if (mUser != null) {
                            mUser.inBlacklist = value != null ? value : !mUser.inBlacklist;
                        }
                    }

                    @Override
                    public Boolean getBlackListValue() {
                        return mUser != null ? mUser.inBlacklist : null;
                    }

                    @Override
                    public void setBookmarkValue(Boolean value) {
                        if (mUser != null) {
                            mUser.bookmarked = value != null ? value : !mUser.bookmarked;
                        }
                    }

                    @Override
                    public Boolean getBookmarkValue() {
                        return mUser != null ? mUser.bookmarked : null;
                    }

                    @Override
                    public void setSympathySentValue(Boolean value) {

                    }

                    @Override
                    public Boolean getSympathySentValue() {
                        return null;
                    }

                    @Override
                    public Integer getUserId() {
                        return mUser != null ? mUser.id : null;
                    }

                    @Override
                    public Intent getOpenChatIntent() {
                        return null;
                    }

                    @Override
                    public Boolean isMutual() {
                        return null;
                    }

                    @Override
                    public boolean isOpenChatAvailable() {
                        return true;
                    }

                    @Override
                    public boolean isAddToFavoritsAvailable() {
                        return true;
                    }

                    @Override
                    public void clickSendGift() {
                        // empty processor. Haven't item "Send gift" in current fragment
                    }

                    @Override
                    public Integer getProfileId() {
                        return mUserId;
                    }

                    @Override
                    public Boolean isBanned() {
                        return null;
                    }
                });
            }
            overflowMenu.initOverfowMenu();
        }
    }

    @Override
    public void onAvatarClick() {
        if (mUser != null) {
            if (!(mUser.deleted || mUser.banned)) {
                startActivity(UserProfileActivity.createIntent(null, mUserId, mUser.feedItemId, false, false, Utils.getNameAndAge(mUser.firstName, mUser.age), mUser.city.getName()));
            } else {
                Toast.makeText(getActivity(), R.string.user_deleted_or_banned,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isShowKeyboardInChat() {
        DisplayMetrics displayMetrics = Device.getDisplayMetrics(App.getContext());
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        return dpHeight >= getActivity().getResources().
                getInteger(R.integer.min_screen_height_chat_fragment);
    }

    private void setSavedMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            mEditBox.setText(message);
            mEditBox.setSelection(message.length());
        }
    }
}
