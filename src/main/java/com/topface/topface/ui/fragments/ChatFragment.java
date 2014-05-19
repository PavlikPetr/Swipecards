package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.*;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.requests.handlers.VipApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.IUserOnlineListener;
import com.topface.topface.ui.adapters.*;
import com.topface.topface.ui.fragments.buy.BuyingFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.views.BackButtonEditTextMaster;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.ui.views.SwapControl;
import com.topface.topface.utils.*;
import com.topface.topface.utils.social.AuthToken;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

public class ChatFragment extends BaseFragment implements View.OnClickListener {

    public static final int LIMIT = 50;

    public static final String FRIEND_FEED_USER = "user_profile";
    public static final String ADAPTER_DATA = "adapter";
    public static final String WAS_FAILED = "was_failed";
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
            ContainerActivity.ActionTypes type = (ContainerActivity.ActionTypes) intent.getSerializableExtra(ContainerActivity.TYPE);
            boolean isChanged = intent.getBooleanExtra(ContainerActivity.CHANGED, false);
            if (mActions != null && type != null) {
                switch (type) {
                    case BLACK_LIST:
                        mUser.blocked = isChanged;
                        break;
                    case BOOKMARK:
                        mUser.bookmarked = isChanged;
                        ((TextView) mActions.findViewById(R.id.bookmark_action_text))
                                .setText(isChanged ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
                        break;
                }
            }
        }
    };

    private IUserOnlineListener mUserOnlineListener;

    // Data
    private int mUserId;
    private BroadcastReceiver mNewMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("id");
            if (id != null && !id.equals("") && Integer.parseInt(id) == mUserId) {
                update(true, "update counters");
                startTimer();
                GCMUtils.cancelNotification(getActivity(), GCMUtils.GCM_TYPE_MESSAGE);
            }
        }
    };
    private Handler mUpdater;
    private boolean mIsUpdating;
    private boolean mIsAddPanelOpened;
    private boolean mIsKeyboardOpened; // Shows whether keyboard opened or not. Should be maintained very carefully, because there are no keyboard show/hide events.
    private PullToRefreshListView mListView;
    private ChatListAdapter mAdapter;
    private FeedUser mUser;
    private BackButtonEditTextMaster mEditBox;
    private SwapControl mSwapControl;
    private ImageButton mBtnChatAdd;
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

    public static ChatFragment newInstance(String itemId, int userId, boolean profileInvoke,
                                           int userSex, String userName, int userAge,
                                           String userCity, String prevEntity) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(INTENT_ITEM_ID, itemId);
        args.putInt(INTENT_USER_ID, userId);
        args.putBoolean(INTENT_PROFILE_INVOKE, profileInvoke);
        args.putInt(INTENT_USER_SEX, userSex);
        args.putString(INTENT_USER_NAME, userName);
        args.putInt(INTENT_USER_AGE, userAge);
        args.putString(INTENT_USER_CITY, userCity);
        args.putString(BaseFragmentActivity.INTENT_PREV_ENTITY, prevEntity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DateUtils.syncTime();
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mUserOnlineListener = (IUserOnlineListener) activity;
        } catch (ClassCastException e) {
            Debug.error(e.toString());
        }
        // do not recreate Adapter cause of steRetainInstance(true)
        if (mAdapter == null) {
            mAdapter = new ChatListAdapter(getActivity(), new FeedList<History>(), getUpdaterCallback());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_chat, null);
        Debug.log(this, "+onCreate");
        // mChatActions
        mChatActionsStub = (ViewStub) root.findViewById(R.id.chat_actions_stub);
        // Navigation bar
        initNavigationbar(mUserName, mUserAge, mUserCity);
        // Swap Control
        initAddPanel(root);
        // Edit Box
        mEditBox = (BackButtonEditTextMaster) root.findViewById(R.id.edChatBox);
        mEditBox.setOnEditorActionListener(mEditorActionListener);
        mEditBox.setOnKeyBoardExitedListener(new BackButtonEditTextMaster.OnKeyBoardExitedListener() {
            @Override
            public void onKeyboardExited() {
                mIsKeyboardOpened = false;
            }
        });
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateActionsReceiver, new IntentFilter(ContainerActivity.UPDATE_USER_CATEGORY));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateActionsReceiver);
    }

    @Override
    protected void restoreState() {
        mItemId = getArguments().getString(INTENT_ITEM_ID);
        mUserId = getArguments().getInt(INTENT_USER_ID, -1);
        mUserName = getArguments().getString(INTENT_USER_NAME);
        mUserSex = getArguments().getInt(INTENT_USER_SEX, Static.BOY);
        mUserAge = getArguments().getInt(INTENT_USER_AGE, 0);
        mUserCity = getArguments().getString(INTENT_USER_CITY);
    }

    private void initAddPanel(View root) {
        mSwapControl = ((SwapControl) root.findViewById(R.id.swapFormView));
        mSwapControl.setOnSizeChangedListener(new SwapControl.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                if (oldh > h) {
                    // keyboard opened
                    mIsKeyboardOpened = true;
                    toggleAddPanel(false, true);
                    closeChatActions();
                }
            }
        });
        // Add Button
        mBtnChatAdd = (ImageButton) root.findViewById(R.id.btnChatAdd);
        mBtnChatAdd.setOnClickListener(this);
        mBtnChatAdd.setSelected(false);
        // Send gift button
        root.findViewById(R.id.panel_send_gift_button).setOnClickListener(this);
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
                                        startActivity(ContainerActivity.getComplainIntent(mUserId, mAdapter.getItem(position).id));
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
        outState.putParcelableArrayList(ADAPTER_DATA, mAdapter.getDataCopy());
        if (mUser != null) {
            try {
                outState.putString(FRIEND_FEED_USER, mUser.toJson().toString());
            } catch (Exception e) {
                Debug.error(e);
            }
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
        if (!pullToRefresh && !scrollRefresh) {
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
            case R.id.btnChatAdd:
                if (mIsKeyboardOpened) {
                    toggleAddPanel(true, true);
                } else {
                    toggleAddPanel();
                }
                closeChatActions();
                EasyTracker.getTracker().sendEvent("Chat", "AdditionalClick", "", 1L);
                break;
            case R.id.panel_send_gift_button:
                startActivityForResult(
                        GiftsActivity.getSendGiftIntent(getActivity(), mUserId, false),
                        GiftsActivity.INTENT_REQUEST_GIFT
                );
                EasyTracker.getTracker().sendEvent("Chat", "SendGiftClick", "", 1L);
                break;
            case R.id.add_to_black_list_action:
                if (CacheProfile.premium) {
                    mBlackListActionController.processActionFor(mUserId);
                } else {
                    startActivityForResult(ContainerActivity.getVipBuyIntent(null, "Chat"), ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                    closeChatActions();
                }
                break;
            case R.id.acWProfile:
            case R.id.acProfile:
                Intent profileIntent = ContainerActivity.getProfileIntent(mUserId, getActivity());
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

                request.callback(new SimpleApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        super.success(response);
                        Intent intent = ContainerActivity.getIntentForActionsUpdate(ContainerActivity.ActionTypes.BOOKMARK, !mUser.bookmarked);
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
            case R.id.complain_action:
                startActivity(ContainerActivity.getComplainIntent(mUserId));
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

        // Если адаптер пустой или пользователя нет, грузим с сервера
        if (mAdapter == null || mAdapter.getCount() == 0 || mUser == null) {
            update(false, "initial");
        } else {
            update(true, "resume update");
            mAdapter.notifyDataSetChanged();
        }

        IntentFilter filter = new IntentFilter(GCMUtils.GCM_NOTIFICATION);
        getActivity().registerReceiver(mNewMessageReceiver, filter);

        mUpdater = new Handler();
        startTimer();
        GCMUtils.lastUserId = mUserId;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mNewMessageReceiver);
        stopTimer();
        GCMUtils.lastUserId = -1; //Ставим значение на дефолтное, чтобы нотификации снова показывались
        Utils.hideSoftKeyboard(getActivity(), mEditBox);
    }

    private void closeChatActions() {
        if (mBarAvatar.isChecked()) {
            onOptionsItemSelected(mBarAvatar);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

        toggleAddPanel(false);
    }

    private void toggleAddPanel() {
        toggleAddPanel(!mIsAddPanelOpened, false);
    }

    private void toggleAddPanel(boolean open) {
        toggleAddPanel(open, false);
    }

    private void toggleAddPanel(boolean open, boolean instant) {
        if (mIsAddPanelOpened == open) return;
        if (open) {
            Utils.hideSoftKeyboard(getActivity(), mEditBox);
            mIsKeyboardOpened = false;
        }
        mSwapControl.snapToScreen(!open ? 0 : 1, instant);
        mBtnChatAdd.setSelected(open);
        mIsAddPanelOpened = open;
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
                    mAdapter.removeItem(loaderItem);
                    Intent intent = ContainerActivity.getBuyingIntent("Chat");
                    intent.putExtra(BuyingFragment.ARG_ITEM_TYPE, BuyingFragment.TYPE_GIFT);
                    intent.putExtra(BuyingFragment.ARG_ITEM_PRICE, price);
                    startActivity(intent);
                } else {
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
                        animateChatActions(checked, 500);
                    } else {
                        Toast.makeText(getActivity(), R.string.user_deleted_or_banned,
                                Toast.LENGTH_LONG).show();
                    }
                }
                return true;
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
            mBlackListActionController.setInBlackList(user.blocked);
            bookmarksTv.setText(user.bookmarked ? R.string.general_bookmarks_delete : R.string.general_bookmarks_add);
            mActionsHeightHeuristic = actions.size() * Utils.getPxFromDp(40);
        }
    }

    private void animateChatActions(final boolean needToClose, long time) {
        if (mActions != null) {
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
                toggleAddPanel(false);
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

        private boolean isInBlackList = false;

        AddToBlackListViewsController(View root) {
            View actionView = root.findViewById(R.id.add_to_black_list_action);
            actionText = (TextView) actionView.findViewById(R.id.block_action_text);
            actionLoader = actionView.findViewById(R.id.blockPrBar);
            actionIcon = actionView.findViewById(R.id.blockIcon);
            // click listener for actionView is set through UserActions
            // set states for views
            initViews(isInBlackList);
        }

        public void addToBlackList(int userId) {
            if (userId > 0) {
                BlackListAddRequest blackListRequest = new BlackListAddRequest(userId, getActivity());
                execRequest(blackListRequest, true);
            }
        }

        public void removeFromBlackList(int userId) {
            if (userId > 0) {
                DeleteBlackListRequest blackListRequest = new DeleteBlackListRequest(userId, getActivity());
                execRequest(blackListRequest, false);
            }
        }

        private void execRequest(ApiRequest blackListRequest, final boolean toBlackList) {
            actionLoader.setVisibility(View.VISIBLE);
            actionIcon.setVisibility(View.INVISIBLE);
            blackListRequest.callback(new VipApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    super.success(response);
                    isInBlackList = toBlackList;
                    if (isAdded()) {
                        onChange(isInBlackList);
                    }
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    if (isAdded()) {
                        actionLoader.setVisibility(View.INVISIBLE);
                        actionIcon.setVisibility(View.VISIBLE);
                    }
                }
            }).exec();
        }

        private void initViews(boolean inBlackList) {
            int resIdText = inBlackList ? R.string.black_list_delete : R.string.black_list_add_short;
            actionText.setText(resIdText);
        }

        public void setInBlackList(boolean value) {
            isInBlackList = value;
            initViews(value);
        }

        private void onChange(boolean inBlackList) {
            initViews(inBlackList);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                    ContainerActivity.getIntentForActionsUpdate(
                            ContainerActivity.ActionTypes.BLACK_LIST,
                            !inBlackList
                    )
            );
        }

        public void processActionFor(int userId) {
            if (isInBlackList) {
                removeFromBlackList(userId);
            } else {
                addToBlackList(userId);
            }
        }
    }

}
