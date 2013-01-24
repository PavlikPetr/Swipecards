package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.*;
import com.topface.topface.requests.*;
import com.topface.topface.ui.*;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.adapters.NewChatListAdapter;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.ui.views.SwapControl;
import com.topface.topface.utils.*;

import java.util.ArrayList;
import java.util.TimerTask;

public class ChatFragment extends BaseFragment implements View.OnClickListener,
        LocationListener {

    // Constants
    private static final int LIMIT = 50; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!

    public static final String ADAPTER_DATA = "adapter";
    public static final String WAS_FAILED = "was_failed";
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_USER_AVATAR = "user_avatar";
    public static final String INTENT_USER_SEX = "user_sex";
    public static final String INTENT_USER_AGE = "user_age";
    public static final String INTENT_USER_CITY = "user_city";
    public static final String INTENT_PROFILE_INVOKE = "profile_invoke";
    public static final String INTENT_ITEM_ID = "item_id";
    public static final String MAKE_ITEM_READ = "com.topface.topface.feedfragment.MAKE_READ";
    private static final int DIALOG_GPS_ENABLE_NO_AGPS_ID = 1;
    private static final int DIALOG_LOCATION_PROGRESS_ID = 3;
    private static final long LOCATION_PROVIDER_TIMEOUT = 10000;
    private static final int DEFAULT_CHAT_UPDATE_PERIOD = 30000;
    //TODO костыль для ChatActivity, после перехода на фрагмент - выпилить
    public static final int INTENT_CHAT_REQUEST = 371;

    private static final int DELETE_BUTTON = 1;
    private static final int COPY_BUTTON = 0;

    // Data
    private int mUserId;

    private Handler mUpdater;
    private boolean mIsUpdating;
    private boolean mProfileInvoke;
    private boolean mIsAddPanelOpened;
    private boolean mLocationDetected = false;
    private PullToRefreshListView mListView;
    private NewChatListAdapter mAdapter;
    private FeedList<History> mHistoryData;
    private EditText mEditBox;
    private LockerView mLoadingLocker;
    private RetryView mRetryView;
    private SwapControl mSwapControl;
    private static ProgressDialog mProgressDialog;
    private Button btnBack;

    private String[] editButtonsNames;
    private boolean mReceiverRegistered = false;
    private int itemId;
    private boolean wasFailed = false;

    // Managers
    private GeoLocationManager mGeoManager = null;
    private RelativeLayout mLockScreen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.ac_chat_new, null);

        Debug.log(this, "+onCreate");
        // arguments
        itemId = getArguments().getInt(INTENT_ITEM_ID, -1);
        mUserId = getArguments().getInt(INTENT_USER_ID, -1);
        mProfileInvoke = getArguments().getBoolean(INTENT_PROFILE_INVOKE, false);
        int userSex = getArguments().getInt(INTENT_USER_SEX, Static.BOY);
        String userName = getArguments().getString(INTENT_USER_NAME);
        int userAge = getArguments().getInt(INTENT_USER_AGE, 0);
        String userCity = getArguments().getString(INTENT_USER_CITY);
        String prevEntity = getArguments().getString(BaseFragmentActivity.INTENT_PREV_ENTITY, null);

        // Locker
        mLoadingLocker = (LockerView) root.findViewById(R.id.llvChatLoading);

        // Navigation bar
        initNavigationbar(userSex, userName, userAge, userCity);

        editButtonsNames = new String[]{getString(R.string.general_copy_title), getString(R.string.general_delete_title)};

        // Swap Control
        mSwapControl = ((SwapControl) root.findViewById(R.id.swapFormView));

        // Add Button
        root.findViewById(R.id.btnChatAdd).setOnClickListener(this);

        // Gift Button
        root.findViewById(R.id.btnChatGift).setOnClickListener(this);

        // Place Button
        root.findViewById(R.id.btnChatPlace).setOnClickListener(this);

        // Map Button
        /**
         * Показываем кнопки отправки произвольного местоположения когда карты доступны
         */
        if (Utils.isGoogleMapsAvailable()) {
            View chatMap = root.findViewById(R.id.btnChatMap);
            chatMap.setOnClickListener(this);
            chatMap.setVisibility(View.VISIBLE);
        }

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

    private void restoreData(Bundle savedInstanceState) {
        if (mHistoryData == null) {
            if (savedInstanceState != null) {
                boolean was_failed = savedInstanceState.getBoolean(WAS_FAILED);
                ArrayList<History> list = savedInstanceState.getParcelableArrayList(ADAPTER_DATA);
                mHistoryData = new FeedList<History>();
                mHistoryData.addAll(list);
                if (was_failed) mLockScreen.setVisibility(View.VISIBLE);
                else mLockScreen.setVisibility(View.GONE);
                mLoadingLocker.setVisibility(View.GONE);
            }
            if (mHistoryData == null) {
                mHistoryData = new FeedList<History>();
            }
        }
    }

    private void initChatHistory(View root) {
        mAdapter = new NewChatListAdapter(getActivity().getApplicationContext(), mHistoryData, getUpdaterCallback());
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
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
    }

    private void initLockScreen(View root) {
        mLockScreen = (RelativeLayout) root.findViewById(R.id.llvLockScreen);
        mRetryView = new RetryView(getActivity().getApplicationContext());
        mRetryView.addButton(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(false, "retry");
                mLockScreen.setVisibility(View.GONE);
            }
        });

        mLockScreen.addView(mRetryView);
    }

    private void initNavigationbar(int userSex, String userName, int userAge, String userCity) {
        TextView headerTitle = ((TextView) getActivity().findViewById(R.id.tvNavigationTitle));
        headerTitle.setText(userName + ", "
                + userAge);
        TextView headerSubtitle = ((TextView) getActivity().findViewById(R.id.tvNavigationSubtitle));
        headerSubtitle.setVisibility(View.VISIBLE);
        headerSubtitle.setText(userCity);

        //        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        //        btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        //        btnBack.setVisibility(View.VISIBLE);
        //
        //        if (prevEntity != null && Utils.isThereNavigationActivity(getActivity())) {
        //            btnBack.setOnClickListener(new View.OnClickListener() {
        //                @Override
        //                public void onClick(View v) {
        //                    //TODO close fragment
        //                    getActivity().finish();
        //                }
        //            });
        //            if (prevEntity.equals(ChatFragment.class.getSimpleName())) {
        //                btnBack.setText(R.string.general_chat);
        //            } else if (prevEntity.equals(DatingFragment.class.getSimpleName())) {
        //                btnBack.setText(R.string.general_dating);
        //            } else if (prevEntity.equals(DialogsFragment.class.getSimpleName())) {
        //                btnBack.setText(R.string.general_dialogs);
        //            } else if (prevEntity.equals(LikesFragment.class.getSimpleName())) {
        //                btnBack.setText(R.string.general_likes);
        //            } else if (prevEntity.equals(MutualFragment.class.getSimpleName())) {
        //                btnBack.setText(R.string.general_mutual);
        //            } else if (prevEntity.equals(VisitorsFragment.class.getSimpleName())) {
        //                btnBack.setText(R.string.general_dating);
        //            } else if (prevEntity.equals(ProfileFragment.class.getSimpleName())) {
        //                btnBack.setText(R.string.general_profile);
        //            }
        //
        //        }

        final ImageButton btnProfile = (ImageButton) getActivity().findViewById(R.id.btnNavigationProfileBar);
        switch (userSex) {
            case Static.BOY:
                btnProfile.setImageResource(R.drawable.navigation_male_profile_selector);
                break;
            case Static.GIRL:
                btnProfile.setImageResource(R.drawable.navigation_female_profile_selector);
                break;
        }
        btnProfile.setVisibility(View.VISIBLE);
        btnProfile.setOnClickListener(this);

        // View btnProfile = root.findViewById(R.id.btnHeaderProfile);
        // btnProfile.setVisibility(View.VISIBLE);
        // btnProfile.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(WAS_FAILED, wasFailed);
        outState.putParcelableArrayList(ADAPTER_DATA, mAdapter.getDataCopy());
    }

    private void deleteItem(final int position) {
        DeleteRequest dr = new DeleteRequest(getActivity());
        History item = mAdapter.getItem(position);
        if (item == null)
            return;
        dr.id = item.id;
        registerRequest(dr);
        dr.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.removeItem(position);
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Debug.log(response.toString());
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
            mLoadingLocker.setVisibility(View.VISIBLE);
        }
        HistoryRequest historyRequest = new HistoryRequest(getActivity());
        registerRequest(historyRequest);
        historyRequest.userid = mUserId;
        historyRequest.debug = type;
        historyRequest.limit = LIMIT;
        if (mAdapter != null) {
            if (pullToRefresh) {
                int id = mAdapter.getFirstItemId();
                if (id > 0) {
                    historyRequest.from = id;
                }
            } else if (scrollRefresh) {
                int id = mAdapter.getLastItemId();
                if (id > 0) {
                   historyRequest.to = id;
                }
            }
        }
        historyRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                if (itemId != -1) {
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(MAKE_ITEM_READ).putExtra(INTENT_ITEM_ID, itemId));
                    itemId = -1;
                }

                wasFailed = false;
                final FeedListData<History> dataList = new FeedListData<History>(response.jsonResult, History.class);
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter != null) {
                            if (pullToRefresh) {
                                mAdapter.addFirst(dataList.items, dataList.more, mListView.getRefreshableView());
                                if (mListView != null) {
                                    mListView.onRefreshComplete();
                                }
                            } else if (scrollRefresh) {
                                mAdapter.addAll(dataList.items, dataList.more, mListView.getRefreshableView());
                            } else {
                                mAdapter.setData(dataList.items, dataList.more, mListView.getRefreshableView());
                            }
                        }

                        if (mLoadingLocker != null) {
                            mLoadingLocker.setVisibility(View.GONE);
                        }
                    }
                });
                mIsUpdating = false;
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(getActivity(), getString(R.string.general_data_error),
//                                Toast.LENGTH_SHORT).show();
                        mLoadingLocker.setVisibility(View.GONE);
                        switch (codeError) {
                            default:
                                mRetryView.setErrorMsg(getString(R.string.general_data_error));
                                break;
                        }
                        mLockScreen.setVisibility(View.VISIBLE);
                        wasFailed = true;
                    }
                });
                mIsUpdating = false;
            }
        }).exec();
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
            case R.id.btnSend: {
                sendMessage();
                EasyTracker.getTracker().trackEvent("Chat", "SendMessage", "", 1L);
            }
            break;
            case R.id.btnChatAdd: {
                if (mIsAddPanelOpened)
                    mSwapControl.snapToScreen(0);
                else
                    mSwapControl.snapToScreen(1);
                mIsAddPanelOpened = !mIsAddPanelOpened;

                EasyTracker.getTracker().trackEvent("Chat", "AdditionalClick", "", 1L);
            }
            break;
            case R.id.btnChatGift: {
                startActivityForResult(new Intent(getActivity(), GiftsActivity.class),
                        GiftsActivity.INTENT_REQUEST_GIFT);
                EasyTracker.getTracker().trackEvent("Chat", "SendGiftClick", "", 1L);
            }
            break;
            case R.id.btnChatPlace: {
                targetLocationToSend();
                // Toast.makeText(getActivity(), "Place",
                // Toast.LENGTH_SHORT).show();
                EasyTracker.getTracker().trackEvent("Chat", "SendPlaceClick", "", 1L);
            }
            break;
            case R.id.btnChatMap: {
                if (Utils.isGoogleMapsAvailable()) {
                    startActivityForResult(new Intent(getActivity(), GeoMapActivity.class),
                            GeoMapActivity.INTENT_REQUEST_GEO);
                    // Toast.makeText(getActivity(), "Map",
                    // Toast.LENGTH_SHORT).show();
                    EasyTracker.getTracker().trackEvent("Chat", "SendMapClick", "§", 1L);
                }
            }
            break;
            case R.id.btnNavigationBackWithText: {
                getActivity().finish();
                //TODO костыль для навигации
                getActivity().setResult(Activity.RESULT_CANCELED);
            }
            break;
            case R.id.chat_message: {

            }
            break;
            default: {
                //TODO костыль для навигации
                if (mProfileInvoke) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                } else {
                    Intent intent = getActivity().getIntent();
                    intent.putExtra(ChatActivity.INTENT_USER_ID, mUserId);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                }
                getActivity().finish();
                //TODO костыль для навигации
                getActivity().setResult(Activity.RESULT_OK);
            }
            break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Если адаптер пустой, грузим с сервера
        if (mAdapter == null || mAdapter.getCount() == 0) {
            update(false, "initial");
        }

        if (!mReceiverRegistered) {
            IntentFilter filter = new IntentFilter(GCMUtils.GCM_NOTIFICATION);
            getActivity().registerReceiver(mNewMessageReceiver, filter);

            mReceiverRegistered = true;
        }
        mUpdater = new Handler();
        startTimer();
        GCMUtils.lastUserId = mUserId; //Не показываем нотификации в чате с пользователем,
        //чтобы, в случае задержки нотификации, не делать лишних
        //оповещений
        if (!Utils.isThereNavigationActivity(getActivity()) && btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), NavigationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(GCMUtils.NEXT_INTENT, BaseFragment.F_DIALOGS);
                    startActivity(intent);
                    //TODO close fragment
                    getActivity().finish();
                    //TODO костыль для навигации
                    getActivity().setResult(Activity.RESULT_CANCELED);
                }
            });
            btnBack.setText(R.string.general_dialogs);
        } else {
            if (btnBack != null) {
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO close fragment
                        getActivity().finish();
                        //TODO костыль для навигации
                        getActivity().setResult(Activity.RESULT_CANCELED);
                    }
                });
            }

        }
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
                mLoadingLocker.setVisibility(View.VISIBLE);
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

        if (mIsAddPanelOpened)
            mSwapControl.snapToScreen(0);
        mIsAddPanelOpened = false;
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
        coordRequest.callback(new ApiHandler() {

            @Override
            public void success(final ApiResponse response) {
                final Confirmation confirm = Confirmation.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (confirm.completed) {
                            History history = new History(response);
                            history.target = FeedDialog.USER_MESSAGE;
                            mAdapter.replaceMessage(fakeItem,history, mListView.getRefreshableView());
                        } else {
                            Toast.makeText(getActivity(),
                                    R.string.general_server_error, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), R.string.general_server_error,
                                Toast.LENGTH_SHORT).show();
                        mAdapter.showRetrySendMessage(fakeItem,coordRequest);
                    }
                });
            }
        }).exec();
    }

    private void sendGift(int id, final int price) {
        final History fakeItem = new History(IListLoader.ItemType.WAITING);
        mAdapter.addSentMessage(fakeItem, mListView.getRefreshableView());

        final SendGiftRequest sendGift = new SendGiftRequest(getActivity());
        registerRequest(sendGift);
        sendGift.giftId = id;
        sendGift.userId = mUserId;
        sendGift.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                SendGiftAnswer answer = SendGiftAnswer.parse(response);
                CacheProfile.likes = answer.likes;
                CacheProfile.money = answer.money;
                Debug.log(getActivity(), "likes:" + answer.likes + " money:"
                        + answer.money);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        History history = new History(response);
                        history.target = FeedDialog.USER_MESSAGE;
                        mAdapter.replaceMessage(fakeItem,history, mListView.getRefreshableView());
                    }
                });
            }

            @Override
            public void fail(int codeError, final ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (response.code == ApiResponse.PAYMENT) {
                            Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                            intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                            intent.putExtra(BuyingFragment.ARG_ITEM_TYPE, BuyingFragment.TYPE_GIFT);
                            intent.putExtra(BuyingFragment.ARG_ITEM_PRICE, price);
                            startActivity(intent);
                        }
                        mAdapter.showRetrySendMessage(fakeItem,sendGift);
                    }
                });
            }
        }).exec();
    }

    private boolean sendMessage() {
        final History fakeItem = new History(IListLoader.ItemType.WAITING);
        mAdapter.addSentMessage(fakeItem, mListView.getRefreshableView());

        final String text = mEditBox.getText().toString();
        if (text == null || text.length() == 0)
            return false;

        final MessageRequest messageRequest = new MessageRequest(getActivity());
        registerRequest(messageRequest);
        messageRequest.message = mEditBox.getText().toString();
        messageRequest.userid = mUserId;
        mEditBox.getText().clear();

        messageRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                final Confirmation confirm = Confirmation.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter != null) {
                            if (confirm.completed) {
                                History history = new History(response);
                                mAdapter.replaceMessage(fakeItem,history, mListView.getRefreshableView());

                            } else {
                                Toast.makeText(getActivity(),
                                        getString(R.string.general_server_error),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),
                                getString(R.string.general_data_error), Toast.LENGTH_SHORT)
                                .show();
                        mAdapter.showRetrySendMessage(fakeItem,messageRequest);
                    }
                });
            }
        }).exec();
        return true;
    }

    private void targetLocationToSend() {
        mLocationDetected = false;

        if (mGeoManager == null) {
            mGeoManager = new GeoLocationManager(getActivity().getApplicationContext());
        }

        if (mGeoManager.availableLocationProvider(GeoLocationManager.LocationProviderType.AGPS)) {
            mGeoManager.setLocationListener(GeoLocationManager.LocationProviderType.AGPS, this);
            showDialog(DIALOG_LOCATION_PROGRESS_ID);
        } else if (mGeoManager.availableLocationProvider(GeoLocationManager.LocationProviderType.GPS)) {
            mGeoManager.setLocationListener(GeoLocationManager.LocationProviderType.GPS, this);
            (new CountDownTimer(LOCATION_PROVIDER_TIMEOUT, LOCATION_PROVIDER_TIMEOUT) {

                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    // noinspection SynchronizeOnNonFinalField
                    synchronized (mGeoManager) {
                        if (!mLocationDetected) {
                            mGeoManager.removeLocationListener(ChatFragment.this);
                            mProgressDialog.dismiss();
                            Toast.makeText(getActivity(), R.string.chat_toast_fail_location,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }).start();
            showDialog(DIALOG_LOCATION_PROGRESS_ID);
        } else {
            showDialog(DIALOG_GPS_ENABLE_NO_AGPS_ID);
        }
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
                CoordinatesRequest coordRequest = new CoordinatesRequest(getActivity());
                registerRequest(coordRequest);
                coordRequest.userid = mUserId;
                coordRequest.latitude = latitude;
                coordRequest.longitude = longitude;
                coordRequest.type = CoordinatesRequest.COORDINATES_TYPE_SELF;
                coordRequest.address = (String) msg.obj;
                coordRequest.callback(new ApiHandler() {

                    @Override
                    public void success(final ApiResponse response) {
                        final Confirmation confirm = Confirmation.parse(response);
                        // final String address =
                        // mGeoManager.getLocationAddress(latitude, longitude);
                        updateUI(new Runnable() {

                            @Override
                            public void run() {
                                if (confirm.completed) {
                                    if (mIsAddPanelOpened)
                                        mSwapControl.snapToScreen(0);
                                    mIsAddPanelOpened = false;
                                    History history = new History(response);
//									history.target = FeedDialog.USER_MESSAGE;
                                    mAdapter.addSentMessage(history, mListView.getRefreshableView());
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getActivity(),
                                            R.string.general_server_error, Toast.LENGTH_SHORT)
                                            .show();
                                }
                                mProgressDialog.dismiss();
                            }
                        });

                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        updateUI(new Runnable() {

                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                Toast.makeText(getActivity(), R.string.general_server_error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).exec();
            }
        });

        mGeoManager.removeLocationListener(this);
        mLocationDetected = true;
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

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        AlertDialog alert;
        switch (id) {
            case DIALOG_GPS_ENABLE_NO_AGPS_ID:
                builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(this.getText(R.string.chat_dialog_gps))
                        .setCancelable(false)
                        .setPositiveButton(this.getText(R.string.general_settings),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent gpsOptionsIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(gpsOptionsIntent);
                                    }
                                });
                builder.setNegativeButton(this.getText(R.string.general_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alert = builder.create();
                return alert;
            case DIALOG_LOCATION_PROGRESS_ID:
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setCancelable(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage(this.getText(R.string.map_location_progress));
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mGeoManager.removeLocationListener(ChatFragment.this);
                    }
                });
                return mProgressDialog;
            default:
                return null;

        }
    }

    private void showDialog(int id) {
        Dialog dialog = onCreateDialog(id);
        dialog.show();
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

    public static ChatFragment newInstance(int itemId, int userId, boolean profileInvoke,
                                           int userSex, String userName, int userAge,
                                           String userCity, String prevEntity) {
        ChatFragment fragment = new ChatFragment();

        Bundle args = new Bundle();
        args.putInt(INTENT_ITEM_ID, itemId);
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
}
