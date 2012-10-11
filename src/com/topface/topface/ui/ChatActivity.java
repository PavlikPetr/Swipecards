package com.topface.topface.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Confirmation;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.History;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.requests.*;
import com.topface.topface.ui.adapters.ChatListAdapter;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.SwapControl;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.GeoLocationManager;
import com.topface.topface.utils.GeoLocationManager.LocationProviderType;

import java.util.LinkedList;

public class ChatActivity extends BaseFragmentActivity implements View.OnClickListener, LocationListener {
    // Data
    private int mUserId;
    private String mUserAvatarUrl;
    private int mAvatarWidth;
    private boolean mProfileInvoke;
    private boolean mIsAddPanelOpened;
    private PullToRefreshListView mListView;
    private ChatListAdapter mAdapter;
    private LinkedList<History> mHistoryList;
    private EditText mEditBox;
    private TextView mHeaderTitle;
    private LockerView mLoadingLocker;

    private SwapControl mSwapControl;
    private static ProgressDialog mProgressDialog;
    private boolean mLocationDetected = false;

    // Constants
    private static final int LIMIT = 50; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_USER_URL = "user_url";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_USER_AVATAR = "user_avatar";
    public static final String INTENT_USER_SEX = "user_sex";
    public static final String INTENT_USER_AGE = "user_age";
    public static final String INTENT_USER_CITY = "user_city";
    public static final String INTENT_PROFILE_INVOKE = "profile_invoke";
    private static final int DIALOG_GPS_ENABLE_NO_AGPS_ID = 1;
    private static final int DIALOG_LOCATION_PROGRESS_ID = 3;
    private static final long LOCATION_PROVIDER_TIMEOUT = 10000;

    //Managers
    private GeoLocationManager mGeoManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_chat);
        Debug.log(this, "+onCreate");

        // Data
        mHistoryList = new LinkedList<History>();

        // Swap Control
        mSwapControl = ((SwapControl) findViewById(R.id.swapFormView));

        // Locker
        mLoadingLocker = (LockerView) findViewById(R.id.llvChatLoading);

        // Params
        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1);
        mUserAvatarUrl = getIntent().getStringExtra(INTENT_USER_URL);
        mProfileInvoke = getIntent().getBooleanExtra(INTENT_PROFILE_INVOKE, false);
        int userSex = getIntent().getIntExtra(INTENT_USER_SEX, Static.BOY);
        mAvatarWidth = getResources().getDrawable(R.drawable.chat_avatar_frame).getIntrinsicWidth();

        // Navigation bar
        mHeaderTitle = ((TextView) findViewById(R.id.tvNavigationTitle));
        mHeaderTitle.setText(getIntent().getStringExtra(INTENT_USER_NAME) + ", " + getIntent().getIntExtra(INTENT_USER_AGE, 0));
        TextView headerSubtitle = ((TextView) findViewById(R.id.tvNavigationSubtitle));
        headerSubtitle.setVisibility(View.VISIBLE);
        headerSubtitle.setText(getIntent().getStringExtra(INTENT_USER_CITY));

        (findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);
        final Button btnBack = (Button) findViewById(R.id.btnNavigationBackWithText);
        if (mProfileInvoke) {
            btnBack.setText(getResources().getString(R.string.navigation_back_profile));
        } else {
            btnBack.setText(getResources().getString(R.string.navigation_back_dialog));
        }
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(this);

        final Button btnProfile = (Button) findViewById(R.id.btnNavigationProfileBar);
        switch (userSex) {
            case Static.BOY:
                btnProfile.setBackgroundResource(R.drawable.navigation_male_profile_selector);
                break;
            case Static.GIRL:
                btnProfile.setBackgroundResource(R.drawable.navigation_female_profile_selector);
                break;
        }
        btnProfile.setVisibility(View.VISIBLE);
        btnProfile.setOnClickListener(this);

//        View btnProfile = findViewById(R.id.btnHeaderProfile);
//        btnProfile.setVisibility(View.VISIBLE);
//        btnProfile.setOnClickListener(this);

        // Add Button        
        findViewById(R.id.btnChatAdd).setOnClickListener(this);

        // Gift Button
        findViewById(R.id.btnChatGift).setOnClickListener(this);

        // Place Button
        findViewById(R.id.btnChatPlace).setOnClickListener(this);

        // Map Button
        findViewById(R.id.btnChatMap).setOnClickListener(this);

        // Edit Box
        mEditBox = (EditText) findViewById(R.id.edChatBox);
        mEditBox.setOnEditorActionListener(mEditorActionListener);

        // ListView
        mListView = (PullToRefreshListView) findViewById(R.id.lvChatList);

        // Adapter
        mAdapter = new ChatListAdapter(getApplicationContext(), mHistoryList);
        mAdapter.setOnAvatarListener(this);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                update(true);
            }
        });
        mListView.setAdapter(mAdapter);

        //Сперва пробуем восстановить данные, если это просто поворот устройства
        Object data = getLastCustomNonConfigurationInstance();
        if (data != null) {
            mAdapter.setDataList((LinkedList<History>) data);
            mLoadingLocker.setVisibility(View.GONE);
            return;
        } else {
            //Если это не получилось, грузим с сервера
            update(false);
        }
    }

    @Override
    protected void onDestroy() {
        release();
        Data.friendAvatar = null;
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    private void update(final boolean pullToRefresh) {
        if (!pullToRefresh) {
            mLoadingLocker.setVisibility(View.VISIBLE);
        }
        HistoryRequest historyRequest = new HistoryRequest(getApplicationContext());
        registerRequest(historyRequest);
        historyRequest.userid = mUserId;
        historyRequest.limit = LIMIT;
        historyRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final LinkedList<History> dataList = History.parse(response);
                post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setDataList(dataList);
                        if (pullToRefresh) {
                            mListView.onRefreshComplete();
                        }
                        mLoadingLocker.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mLoadingLocker.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }

    private void release() {
        mEditBox = null;
        mListView = null;
        if (mAdapter != null)
            mAdapter.release();
        mAdapter = null;
        mHistoryList = null;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ImageView) {
            if (v.getTag() instanceof History) {
                History history = (History) v.getTag();
                if (history.type == FeedDialog.MAP) {
                    Intent intent = new Intent(this, GeoMapActivity.class);
                    intent.putExtra(GeoMapActivity.INTENT_LATITUDE_ID, history.latitude);
                    intent.putExtra(GeoMapActivity.INTENT_LONGITUDE_ID, history.longitude);
                    startActivity(intent);
                    return;
                }
            }
        }
        switch (v.getId()) {
            case R.id.btnChatAdd: {
                if (mIsAddPanelOpened)
                    mSwapControl.snapToScreen(0);
                else
                    mSwapControl.snapToScreen(1);
                mIsAddPanelOpened = !mIsAddPanelOpened;
            }
            break;
            case R.id.btnChatGift: {
                startActivityForResult(new Intent(this, GiftsActivity.class), GiftsActivity.INTENT_REQUEST_GIFT);
            }
            break;
            case R.id.btnChatPlace: {
                sendUserCurrentLocation();
//                Toast.makeText(ChatActivity.this, "Place", Toast.LENGTH_SHORT).show();
            }
            break;
            case R.id.btnChatMap: {
                startActivityForResult(new Intent(this, GeoMapActivity.class), GeoMapActivity.INTENT_REQUEST_GEO);
//                Toast.makeText(ChatActivity.this, "Map", Toast.LENGTH_SHORT).show();
            }
            break;
            case R.id.btnNavigationBackWithText: {
                finish();
            }
            break;
            default: {
                if (mProfileInvoke) {
                    finish();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.INTENT_USER_ID, mUserId);
                intent.putExtra(UserProfileActivity.INTENT_CHAT_INVOKE, true);
                intent.putExtra(UserProfileActivity.INTENT_USER_NAME, mHeaderTitle.getText());
                startActivity(intent);
            }
            break;
        }
    }

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                final String text = v.getText().toString();

                if (text == null || text.length() == 0)
                    return false;

                mLoadingLocker.setVisibility(View.VISIBLE);

                MessageRequest messageRequest = new MessageRequest(ChatActivity.this.getApplicationContext());
                registerRequest(messageRequest);
                messageRequest.message = mEditBox.getText().toString();
                messageRequest.userid = mUserId;
                messageRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        final Confirmation confirm = Confirmation.parse(response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (confirm.completed) {
                                    History history = new History();
//	                                history.code = 0;
//	                                history.gift = 0;
                                    history.uid = CacheProfile.uid;
                                    history.created = System.currentTimeMillis();
                                    history.text = text;
                                    history.type = FeedDialog.MESSAGE;
                                    mAdapter.addSentMessage(history);
                                    mAdapter.notifyDataSetChanged();
                                    mEditBox.getText().clear();
                                    mLoadingLocker.setVisibility(View.GONE);

                                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(mEditBox.getWindowToken(), 0);
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.general_server_error), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                                mLoadingLocker.setVisibility(View.GONE);
                            }
                        });
                    }
                }).exec();
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                mLoadingLocker.setVisibility(View.VISIBLE);
                Bundle extras = data.getExtras();
                final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
                final String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);
                final int price = extras.getInt(GiftsActivity.INTENT_GIFT_PRICE);
                Debug.log(this, "id:" + id + " url:" + url);
                SendGiftRequest sendGift = new SendGiftRequest(getApplicationContext());
                registerRequest(sendGift);
                sendGift.giftId = id;
                sendGift.userId = mUserId;
                if (mIsAddPanelOpened)
                    mSwapControl.snapToScreen(0);
                mIsAddPanelOpened = false;
                sendGift.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) throws NullPointerException {
                        SendGiftAnswer answer = SendGiftAnswer.parse(response);
                        CacheProfile.power = answer.power;
                        CacheProfile.money = answer.money;
                        Debug.log(ChatActivity.this, "power:" + answer.power + " money:" + answer.money);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                History history = new History();
//	                            history.code = 0;
                                history.gift = id;
                                history.uid = CacheProfile.uid;
                                history.created = System.currentTimeMillis();
                                history.text = Static.EMPTY;
                                history.type = FeedDialog.GIFT;
                                history.link = url;
                                mAdapter.addSentMessage(history);
                                mAdapter.notifyDataSetChanged();
                                mLoadingLocker.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void fail(int codeError, final ApiResponse response) throws NullPointerException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.code == ApiResponse.PAYMENT) {
                                    Intent intent = new Intent(getApplicationContext(), BuyingActivity.class);
                                    intent.putExtra(BuyingActivity.INTENT_USER_COINS, price - CacheProfile.money);
                                    startActivity(intent);
                                }
                                mLoadingLocker.setVisibility(View.GONE);
                            }
                        });
                    }
                }).exec();
            } else if (requestCode == GeoMapActivity.INTENT_REQUEST_GEO) {
                Bundle extras = data.getExtras();
                final double latitude = extras.getDouble(GeoMapActivity.INTENT_LATITUDE_ID);
                final double longitude = extras.getDouble(GeoMapActivity.INTENT_LONGITUDE_ID);
//        		final String address = extras.getString(GeoMapActivity.INTENT_ADDRESS_ID);        		

                CoordinatesRequest coordRequest = new CoordinatesRequest(getApplicationContext());
                registerRequest(coordRequest);
                coordRequest.userid = mUserId;
                coordRequest.latitude = latitude;
                coordRequest.longitude = longitude;
                mLoadingLocker.setVisibility(View.VISIBLE);
                coordRequest.callback(new ApiHandler() {

                    @Override
                    public void success(ApiResponse response) throws NullPointerException {
                        final Confirmation confirm = Confirmation.parse(response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (confirm.completed) {
                                    History history = new History();
                                    history.type = FeedDialog.MAP;
                                    history.currentLocation = false;
                                    history.latitude = latitude;
                                    history.longitude = longitude;
                                    mAdapter.addSentMessage(history);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(ChatActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                }
                                mLoadingLocker.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response)
                            throws NullPointerException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                mLoadingLocker.setVisibility(View.GONE);
                            }
                        });
                    }
                }).exec();

                if (mIsAddPanelOpened)
                    mSwapControl.snapToScreen(0);
                mIsAddPanelOpened = false;
            }
        } else {
            if (mIsAddPanelOpened)
                mSwapControl.snapToScreen(0);
            mIsAddPanelOpened = false;
        }
    }

    private void sendUserCurrentLocation() {
        mLocationDetected = false;

        if (mGeoManager == null) {
            mGeoManager = new GeoLocationManager(getApplicationContext());
        }

        if (mGeoManager.availableLocationProvider(LocationProviderType.AGPS)) {
            mGeoManager.setLocationListener(LocationProviderType.AGPS, this);
            showDialog(DIALOG_LOCATION_PROGRESS_ID);
        } else if (mGeoManager.availableLocationProvider(LocationProviderType.GPS)) {
            mGeoManager.setLocationListener(LocationProviderType.GPS, this);
            (new CountDownTimer(LOCATION_PROVIDER_TIMEOUT, LOCATION_PROVIDER_TIMEOUT) {

                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    //noinspection SynchronizeOnNonFinalField
                    synchronized (mGeoManager) {
                        if (!mLocationDetected) {
                            mGeoManager.removeLocationListener(ChatActivity.this);
                            mProgressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, R.string.chat_toast_fail_location, Toast.LENGTH_SHORT).show();
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
//		Debug.log(this, location.getLatitude() + " / " + location.getLongitude());
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        CoordinatesRequest coordRequest = new CoordinatesRequest(getApplicationContext());
        registerRequest(coordRequest);
        coordRequest.userid = mUserId;
        coordRequest.latitude = latitude;
        coordRequest.longitude = longitude;
        coordRequest.callback(new ApiHandler() {

            @Override
            public void success(ApiResponse response) throws NullPointerException {
                final Confirmation confirm = Confirmation.parse(response);
//				final String address = mGeoManager.getLocationAddress(latitude, longitude);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (confirm.completed) {
                            if (mIsAddPanelOpened)
                                mSwapControl.snapToScreen(0);
                            mIsAddPanelOpened = false;

                            History history = new History();
                            history.type = FeedDialog.MAP;
                            history.currentLocation = true;
                            history.latitude = latitude;
                            history.longitude = longitude;
                            mAdapter.addSentMessage(history);
                            mAdapter.notifyDataSetChanged();

//					        Toast.makeText(ChatActivity.this, history.latitude + " " + history.longitude, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });

            }

            @Override
            public void fail(int codeError, ApiResponse response)
                    throws NullPointerException {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        Toast.makeText(ChatActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).exec();

        mGeoManager.removeLocationListener(this);
        mLocationDetected = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    protected android.app.Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        AlertDialog alert;
        switch (id) {
            case DIALOG_GPS_ENABLE_NO_AGPS_ID:
                builder = new AlertDialog.Builder(this);
                builder.setMessage(this.getText(R.string.chat_dialog_gps))
                        .setCancelable(false)
                        .setPositiveButton(this.getText(R.string.chat_dialog_btn_gps_settings),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(gpsOptionsIntent);
                                    }
                                });
                builder.setNegativeButton(this.getText(R.string.chat_dialog_btn_gps_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alert = builder.create();
                return alert;
            case DIALOG_LOCATION_PROGRESS_ID:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage(this.getText(R.string.map_location_progress));
                return mProgressDialog;
            default:
                return super.onCreateDialog(id);

        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mAdapter.getDataCopy();
    }
}
