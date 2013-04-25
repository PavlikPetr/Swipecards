package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.topface.topface.requests.handlers.VipApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.GeoMapActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.ChatListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.ui.views.SwapControl;
import com.topface.topface.utils.*;
import com.topface.topface.utils.GeoUtils.GeoLocationManager;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class ChatFragment extends BaseFragment implements View.OnClickListener, LocationListener {

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

    private static final int DELETE_BUTTON = 1;
    private static final int COPY_BUTTON = 0;

    // Data
    private int mUserId;

    private Handler mUpdater;
    private boolean mIsUpdating;
    private boolean mProfileInvoke;
    private boolean mIsAddPanelOpened;
    private PullToRefreshListView mListView;
    private ChatListAdapter mAdapter;
    private FeedList<History> mHistoryData;
    private FeedUser mUser;
    private EditText mEditBox;
    private TextView mLoadingBackgroundText;
    private AnimationDrawable mLoadingBackgroundDrawable;
    private RetryViewCreator mRetryView;
    private SwapControl mSwapControl;
    private Button mAddToBlackList;
    private ImageButton mBtnChatAdd;
    private ActionBar mActionBar;

    private String[] editButtonsNames;
    private boolean mReceiverRegistered = false;
    private String itemId;
    private boolean wasFailed = false;
    private boolean isInBlackList = false;

    // Managers
    private GeoLocationManager mGeoManager = null;
    private RelativeLayout mLockScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DateUtils.syncTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.ac_chat, null);

        Debug.log(this, "+onCreate");
        // arguments
        itemId = getArguments().getString(INTENT_ITEM_ID);
        mUserId = getArguments().getInt(INTENT_USER_ID, -1);
        mProfileInvoke = getArguments().getBoolean(INTENT_PROFILE_INVOKE, false);
        String userName = getArguments().getString(INTENT_USER_NAME);
        int userAge = getArguments().getInt(INTENT_USER_AGE, 0);
        String userCity = getArguments().getString(INTENT_USER_CITY);

        // Locker
        mLoadingBackgroundText = (TextView) root.findViewById(R.id.tvBackgroundText);
        Drawable drawable = mLoadingBackgroundText.getCompoundDrawables()[0];
        if (drawable instanceof AnimationDrawable) {
            mLoadingBackgroundDrawable = (AnimationDrawable) drawable;
        }

        // Navigation bar
        initNavigationbar(root, userName, userAge, userCity);

        editButtonsNames = new String[]{getString(R.string.general_copy_title), getString(R.string.general_delete_title)};

        // Swap Control
        initAddPanel(root);

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

        GCMUtils.cancelNotification(getActivity().getApplicationContext(), GCMUtils.GCM_TYPE_MESSAGE);

        return root;
    }

    private void initAddPanel(View root) {
        mSwapControl = ((SwapControl) root.findViewById(R.id.swapFormView));

        // Add Button
        mBtnChatAdd = (ImageButton) root.findViewById(R.id.btnChatAdd);
        mBtnChatAdd.setOnClickListener(this);
        mBtnChatAdd.setSelected(false);

        // Gift Button
        root.findViewById(R.id.btnChatGift).setOnClickListener(this);

        // Map Button
        View chatMap = root.findViewById(R.id.btnChatPlace);
        if (Utils.isGoogleMapsAvailable()) {
            chatMap.setOnClickListener(this);
            chatMap.setVisibility(View.VISIBLE);
        } else {
            chatMap.setVisibility(View.GONE);
        }

        // Photo Button
        root.findViewById(R.id.btnChatPhoto).setEnabled(false);

        //Add to blacklist button
        mAddToBlackList = (Button) root.findViewById(R.id.btnAddToBlackList);

        //Buy VIP button
        Button buyVip = (Button) root.findViewById(R.id.btnBuyVip);
        TextView title = (TextView) root.findViewById(R.id.tvBuyVipTitle);

        // Check premium possibilities
        if (CacheProfile.premium) {
            mAddToBlackList.setOnClickListener(this);
            title.setVisibility(View.INVISIBLE);
            buyVip.setVisibility(View.GONE);
        } else {
            buyVip.setOnClickListener(this);
            title.setVisibility(View.VISIBLE);
            buyVip.setVisibility(View.VISIBLE);
            mAddToBlackList.setVisibility(View.GONE);
        }
    }

    private void restoreData(Bundle savedInstanceState) {
        if (mHistoryData == null) {
            if (savedInstanceState != null) {
                try {
                    boolean was_failed = savedInstanceState.getBoolean(WAS_FAILED);
                    ArrayList<History> list = savedInstanceState.getParcelableArrayList(ADAPTER_DATA);
                    mHistoryData = new FeedList<History>();
                    if (list != null) {
                        for (History item : list) {
                            if (item != null) {
                                mHistoryData.add(item);
                            }
                        }
                    }
                    mUser = new FeedUser(new JSONObject(savedInstanceState.getString(FRIEND_FEED_USER)));
                    if (!mUser.isEmpty()) {
                        onUserLoaded();
                    }

                    if (was_failed) {
                        mLockScreen.setVisibility(View.VISIBLE);
                    } else {
                        mLockScreen.setVisibility(View.GONE);
                    }
                    showLoadingBackground();
                } catch (Exception e) {
                    Debug.error(e);
                } catch (OutOfMemoryError e) {
                    Debug.error(e);
                }
            }
            if (mHistoryData == null) {
                mHistoryData = new FeedList<History>();
            }
        }
    }

    private void initChatHistory(View root) {
        mAdapter = new ChatListAdapter(getActivity(), mHistoryData, getUpdaterCallback());
        mAdapter.setUser(mUser);
        mAdapter.setOnAvatarListener(this);
        mAdapter.setOnItemLongClickListener(new OnListViewItemLongClickListener() {

            @Override
            public void onLongClick(final int position, final View v) {

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.general_spinner_title)
                        .setItems(editButtonsNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DELETE_BUTTON:
                                        deleteItem(position);
                                        EasyTracker.getTracker().trackEvent("Chat", "DeleteItem", "", 1L);
                                        break;
                                    case COPY_BUTTON:
                                        mAdapter.copyText(((TextView) v).getText().toString());
                                        EasyTracker.getTracker().trackEvent("Chat", "CopyItemText", "", 1L);
                                        break;
                                }
                            }
                        }).create().show();
            }
        });

        mListView = (PullToRefreshListView) root.findViewById(R.id.lvChatList);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                update(true, "pull to refresh");
            }
        });
        mListView.setClickable(true);
        if (mAdapter.isEmpty()) {
            mAdapter.addHeader(mListView.getRefreshableView());
        }
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.getRefreshableView().addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.item_empty_footer, null));
    }

    private void initLockScreen(View root) {
        mLockScreen = (RelativeLayout) root.findViewById(R.id.llvLockScreen);

        mRetryView = RetryViewCreator.createDefaultRetryView(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(false, "retry");
                mLockScreen.setVisibility(View.GONE);
            }
        });

        mLockScreen.addView(mRetryView.getView());
    }

    private void initNavigationbar(View root, String userName, int userAge, String userCity) {
        mActionBar = getActionBar(root);

        mActionBar.showBackButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO костыль для навигации
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });

        mActionBar.showProfileAvatar(R.drawable.feed_banned_male_avatar, null);

        setNavigationTitles(userName, userAge, userCity);
    }

    private void setNavigationTitles(String userName, int userAge, String userCity) {
        String userTitle = (TextUtils.isEmpty(userName) && userAge == 0) ? Static.EMPTY : (userName + "," + userAge);
        mActionBar.setTitleText(userTitle);
        mActionBar.setSubTitleText(userCity);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(WAS_FAILED, wasFailed);
        outState.putParcelableArrayList(ADAPTER_DATA, mAdapter.getDataCopy());

        try {
            outState.putString(FRIEND_FEED_USER, mUser.toJson().toString());
        } catch (Exception e) {
            Debug.error(e);
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

        DeleteRequest dr = new DeleteRequest(item.id, getActivity());
        dr.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
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
            public void fail(int codeError, ApiResponse response) {
                Debug.log(response.toString());
                Utils.showErrorMessage(App.getContext());
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
            hideLoadingBackground();
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
            protected void success(HistoryListData data, ApiResponse response) {
                if (itemId != null) {
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(MAKE_ITEM_READ).putExtra(INTENT_ITEM_ID, itemId));
                    itemId = null;
                }

                removeAlreadyLoadedItems(data);

                setNavigationTitles(data.user.first_name, data.user.age, data.user.city.name);
                wasFailed = false;
                mUser = data.user;
                if (!mUser.isEmpty()) {
                    onUserLoaded();
                }
                if (mAdapter != null) {
                    if (!data.items.isEmpty()) {
                        if (pullToRefresh) {
                            mAdapter.addFirst(data.items, data.more, mListView.getRefreshableView());
                        } else if (scrollRefresh) {
                            mAdapter.addAll(data.items, data.more, mListView.getRefreshableView());
                        } else {
                            mAdapter.setData(data.items, data.more, mListView.getRefreshableView());
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
            public void fail(int codeError, ApiResponse response) {
                showLoadingBackground();
                if (mLockScreen != null && mAdapter.getData().isEmpty()) {
                    mLockScreen.setVisibility(View.VISIBLE);
                }
                wasFailed = true;
                mIsUpdating = false;
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);

                showLoadingBackground();
                if (pullToRefresh && mListView != null) {
                    mListView.onRefreshComplete();
                }
            }
        }).exec();
    }

    private void removeAlreadyLoadedItems(HistoryListData data) {
        if (!mAdapter.isEmpty() && !data.items.isEmpty()) {
            FeedList<History> items = mAdapter.getData();
            int size = items.size();
            for (int i = size - 1; i > 0 && i > size - LIMIT; i--) {
                List<History> itemsToDelete = new ArrayList<History>();
                for (History item : data.items) {
                    if (item.id.equals(items.get(i).id)) {
                        itemsToDelete.add(item);
                    }
                }
                data.items.removeAll(itemsToDelete);
            }
        }
    }

    private void onUserLoaded() {
        if (mActionBar != null) {
            if (mUser.deleted || mUser.banned || mUser.photo == null || mUser.photo.isEmpty()) {
                mActionBar.showProfileAvatar(mUser.sex == Static.BOY ? R.drawable.feed_banned_male_avatar : R.drawable.feed_banned_female_avatar, null);
            } else {
                mActionBar.showProfileAvatar(mUser.photo, this);
            }
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
    public void onClick(View v) {
        if (v instanceof ImageView) {
            if (v.getTag() instanceof History) {
                History history = (History) v.getTag();
                if (Utils.isGoogleMapsAvailable() && (history.type == FeedDialog.MAP || history.type == FeedDialog.ADDRESS)) {
                    Intent intent = new Intent(getActivity(), GeoMapActivity.class);
                    intent.putExtra(GeoMapActivity.INTENT_GEO, history.geo);
                    startActivity(intent);
                    return;
                }
            }
        }
        switch (v.getId()) {
            case R.id.btnSend:
                if (mUserId > 0) {
                    sendMessage();
                    EasyTracker.getTracker().trackEvent("Chat", "SendMessage", "", 1L);
                }
                break;
            case R.id.btnChatAdd:
                toggleAddPanel();
                EasyTracker.getTracker().trackEvent("Chat", "AdditionalClick", "", 1L);
                break;
            case R.id.btnChatGift:
                startActivityForResult(new Intent(getActivity(), GiftsActivity.class),
                        GiftsActivity.INTENT_REQUEST_GIFT);
                EasyTracker.getTracker().trackEvent("Chat", "SendGiftClick", "", 1L);
                break;
            case R.id.btnChatPlace:
                if (Utils.isGoogleMapsAvailable()) {
                    startActivityForResult(new Intent(getActivity(), GeoMapActivity.class),
                            GeoMapActivity.INTENT_REQUEST_GEO);
                    EasyTracker.getTracker().trackEvent("Chat", "SendMapClick", "§", 1L);
                }
                break;
            case R.id.btnNavigationProfileBar:
            case R.id.btnNavigationBarAvatar:
                if (mProfileInvoke) {
                    getActivity().finish();
                } else {
                    if (mUserId > 0) {
                        startActivity(ContainerActivity.getProfileIntent(mUserId, getActivity()));
                    }
                }
                break;
            case R.id.btnBuyVip:
                Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                break;
            case R.id.btnAddToBlackList:
                if (isInBlackList) {
                    removeFromBlackList();
                } else {
                    addToBlackList();
                }
                break;
            default: {

            }
            break;
        }
    }

    private void removeFromBlackList() {
        if (mUserId > 0) {
            BlackListDeleteRequest deleteBlackListRequest = new BlackListDeleteRequest(mUserId, getActivity());
            mAddToBlackList.setEnabled(false);
            deleteBlackListRequest.callback(new VipApiHandler() {

                @Override
                public void success(ApiResponse response) {
                    super.success(response);
                    isInBlackList = false;
                    if (mAddToBlackList != null) {
                        mAddToBlackList.setText(R.string.black_list_add);
                    }
                }

                @Override
                public void always(ApiResponse response) {
                    super.always(response);
                    if (mAddToBlackList != null) {
                        mAddToBlackList.setEnabled(true);
                    }
                }
            }).exec();
        }
    }

    private void addToBlackList() {
        if (mUserId > 0) {
            BlackListAddRequest blackListRequest = new BlackListAddRequest(mUserId, getActivity());
            mAddToBlackList.setEnabled(false);
            blackListRequest.callback(new VipApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    super.success(response);
                    isInBlackList = true;
                    if (mAddToBlackList != null) {
                        mAddToBlackList.setText(R.string.black_list_delete);
                    }
                }

                @Override
                public void always(ApiResponse response) {
                    super.always(response);
                    if (mAddToBlackList != null) {
                        mAddToBlackList.setEnabled(true);
                    }
                }
            }).exec();
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
            mAdapter.notifyDataSetChanged();
        }

        if (!mReceiverRegistered) {
            IntentFilter filter = new IntentFilter(GCMUtils.GCM_NOTIFICATION);
            getActivity().registerReceiver(mNewMessageReceiver, filter);

            mReceiverRegistered = true;
        }
        mUpdater = new Handler();
        startTimer();
        GCMUtils.lastUserId = mUserId;
    }

    @Override
    public void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        if (mReceiverRegistered && mNewMessageReceiver != null) {
            getActivity().unregisterReceiver(mNewMessageReceiver);
            mReceiverRegistered = false;
        }
        stopTimer();
        GCMUtils.lastUserId = -1; //Ставим значение на дефолтное, чтобы нотификации снова показывались
        Utils.hideSoftKeyboard(getActivity(), mEditBox);
    }

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return actionId == EditorInfo.IME_ACTION_SEND && sendMessage();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                Bundle extras = data.getExtras();
                final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
                final int price = extras.getInt(GiftsActivity.INTENT_GIFT_PRICE);
                sendGift(id, price);
            } else if (requestCode == GeoMapActivity.INTENT_REQUEST_GEO) {
                Bundle extras = data.getExtras();
                final Geo geo = extras.getParcelable(GeoMapActivity.INTENT_GEO);
                sendCoordinates(geo);
            }
        }

        toggleAddPanel();
    }

    private void toggleAddPanel() {
        if (!mIsAddPanelOpened) {
            Utils.hideSoftKeyboard(getActivity(), mEditBox);
        }
        mSwapControl.snapToScreen(mIsAddPanelOpened ? 0 : 1);
        mBtnChatAdd.setSelected(!mIsAddPanelOpened);
        mIsAddPanelOpened = !mIsAddPanelOpened;
    }

    private void sendCoordinates(Geo geo) {
        final History fakeItem = new History(IListLoader.ItemType.WAITING);
        mAdapter.addSentMessage(fakeItem, mListView.getRefreshableView());

        final CoordinatesRequest coordRequest = new CoordinatesRequest(getActivity());
        registerRequest(coordRequest);
        coordRequest.userid = mUserId;
        final Coordinates coordinates = geo.getCoordinates();
        if (coordinates != null) {
            coordRequest.latitude = coordinates.getLatitude();
            coordRequest.longitude = coordinates.getLongitude();
        }
        coordRequest.type = CoordinatesRequest.COORDINATES_TYPE_PLACE;
        coordRequest.address = geo.getAddress();
        coordRequest.callback(new DataApiHandler<History>() {
            @Override
            protected void success(History data, ApiResponse response) {
                data.target = FeedDialog.OUTPUT_USER_MESSAGE;
                if (mAdapter != null) {
                    mAdapter.replaceMessage(fakeItem, data, mListView.getRefreshableView());
                }
            }

            @Override
            protected History parseResponse(ApiResponse response) {
                return new History(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                mAdapter.showRetrySendMessage(fakeItem, coordRequest);
            }
        }).exec();
    }

    private void sendGift(int id, final int price) {

        if (id <= 0) {
            showLoadingBackground();
            Toast.makeText(getActivity(), R.string.general_server_error, Toast.LENGTH_SHORT);
            return;
        }

        final History fakeItem = new History(IListLoader.ItemType.WAITING);
        mAdapter.addSentMessage(fakeItem, mListView.getRefreshableView());

        final SendGiftRequest sendGift = new SendGiftRequest(getActivity());
        registerRequest(sendGift);
        sendGift.giftId = id;
        sendGift.userId = mUserId;

        sendGift.callback(new DataApiHandler<SendGiftAnswer>() {
            @Override
            protected void success(SendGiftAnswer data, ApiResponse response) {
                CacheProfile.likes = data.likes;
                CacheProfile.money = data.money;
                Debug.log(getActivity(), "likes:" + data.likes + " money:" + data.money);
                data.history.target = FeedDialog.OUTPUT_USER_MESSAGE;
                if (mAdapter != null) {
                    mAdapter.replaceMessage(fakeItem, data.history, mListView.getRefreshableView());
                }
            }

            @Override
            protected SendGiftAnswer parseResponse(ApiResponse response) {
                return SendGiftAnswer.parse(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (response.code == ApiResponse.PAYMENT) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                    intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                    intent.putExtra(BuyingFragment.ARG_ITEM_TYPE, BuyingFragment.TYPE_GIFT);
                    intent.putExtra(BuyingFragment.ARG_ITEM_PRICE, price);
                    startActivity(intent);
                }
                mAdapter.showRetrySendMessage(fakeItem, sendGift);
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);
                showLoadingBackground();
            }
        }).exec();
    }

    private boolean sendMessage() {
        if (TextUtils.isEmpty(mEditBox.getText().toString().trim())) {
            return false;
        }

        final History fakeItem = new History(IListLoader.ItemType.WAITING);
        if (mAdapter != null && mListView != null) {
            mAdapter.addSentMessage(fakeItem, mListView.getRefreshableView());
        }

        final String text = mEditBox.getText().toString();
        if (text == null || TextUtils.isEmpty(text.trim()) || mUserId == 0) return false;

        final MessageRequest messageRequest = new MessageRequest(getActivity());
        registerRequest(messageRequest);
        messageRequest.message = mEditBox.getText().toString();
        messageRequest.userid = mUserId;
        mEditBox.getText().clear();

        messageRequest.callback(new DataApiHandler<History>() {
            @Override
            protected void success(History data, ApiResponse response) {
                if (mAdapter != null) {
                    mAdapter.replaceMessage(fakeItem, data, mListView.getRefreshableView());
                }
            }

            @Override
            protected History parseResponse(ApiResponse response) {
                return new History(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (mAdapter != null) {
                    Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                    mAdapter.showRetrySendMessage(fakeItem, messageRequest);
                }
            }
        }).exec();
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Debug.log(this, location.getLatitude() + " / " +
        // location.getLongitude());
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        OsmManager.getAddress(latitude, longitude, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final History fakeItem = new History(IListLoader.ItemType.WAITING);
                mAdapter.addSentMessage(fakeItem, mListView.getRefreshableView());

                final CoordinatesRequest coordRequest = new CoordinatesRequest(getActivity());
                registerRequest(coordRequest);
                coordRequest.userid = mUserId;
                coordRequest.latitude = latitude;
                coordRequest.longitude = longitude;
                coordRequest.type = CoordinatesRequest.COORDINATES_TYPE_SELF;
                coordRequest.address = (String) msg.obj;

                coordRequest.callback(new DataApiHandler<History>() {
                    @Override
                    protected void success(History data, ApiResponse response) {
                        toggleAddPanel();
                        if (mAdapter != null) {
                            mAdapter.replaceMessage(fakeItem, data, mListView.getRefreshableView());
                        }
                    }

                    @Override
                    protected History parseResponse(ApiResponse response) {
                        return new History(response);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                        mAdapter.showRetrySendMessage(fakeItem, coordRequest);
                    }
                }).exec();

            }
        });

        mGeoManager.removeLocationListener(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

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

    public interface OnListViewItemLongClickListener {
        public void onLongClick(int position, View v);
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

    private void showLoadingBackground() {
        if (mLoadingBackgroundText != null) {
            mLoadingBackgroundText.setVisibility(View.GONE);
            if (mLoadingBackgroundDrawable != null) {
                mLoadingBackgroundDrawable.stop();
            }
        }
    }

    private void hideLoadingBackground() {
        if (mLoadingBackgroundText != null) {
            mLoadingBackgroundText.setVisibility(View.VISIBLE);
            if (mLoadingBackgroundDrawable != null) {
                mLoadingBackgroundDrawable.start();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
            if (mIsAddPanelOpened) toggleAddPanel();
        }
    }
}
