package com.topface.topface.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.topface.topface.C2DMUtils;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.HistoryRequest;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.ui.adapters.ChatListAdapter;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class ChatActivity extends TrackedActivity implements View.OnClickListener {
    // Data
    private int mUserId;
    private boolean mProfileInvoke;
    private ListView mListView;
    private ChatListAdapter mAdapter;
    private LinkedList<History> mHistoryList;
    private EditText mEdBox;
    private TextView mHeaderTitle;
    private ProgressBar mProgressBar;
    private MessageRequest messageRequest;
    private HistoryRequest historyRequest;
    // Constants
    private static final int LIMIT = 50;
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_USER_AVATAR = "user_avatar";
    public static final String INTENT_PROFILE_INVOKE = "profile_invoke";
    private boolean mReceiverRegistered;
    private String mUserAvatar;

    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_chat);
        Debug.log(this, "+onCreate");

        // Data
        mHistoryList = new LinkedList<History>();

        // Title Header
        mHeaderTitle = ((TextView) findViewById(R.id.tvHeaderTitle));

        // ListView
        mListView = (ListView) findViewById(R.id.lvChatList);

        // Progress
        mProgressBar = (ProgressBar) findViewById(R.id.prsChatLoading);

        // params
        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1);
        mUserAvatar = getIntent().getStringExtra(INTENT_USER_AVATAR);
        mProfileInvoke = getIntent().getBooleanExtra(INTENT_PROFILE_INVOKE, false);
        mHeaderTitle.setText(getIntent().getStringExtra(INTENT_USER_NAME));

        // Profile Button
        View btnProfile = findViewById(R.id.btnChatProfile);
        btnProfile.setVisibility(View.VISIBLE);
        btnProfile.setOnClickListener(this);

        // Edit Box
        mEdBox = (EditText) findViewById(R.id.edChatBox);

        // Send Button
        Button btnSend = (Button) findViewById(R.id.btnChatSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = mEdBox.getText().toString();

                if (text == null || text.length() == 0) return;

                mProgressBar.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEdBox.getWindowToken(), 0);

                messageRequest = new MessageRequest(ChatActivity.this.getApplicationContext());
                messageRequest.message = mEdBox.getText().toString();
                messageRequest.userid = mUserId;
                messageRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                if (mAdapter != null) {
                                    History history = new History();
                                    history.code = 0;
                                    history.gift = 0;
                                    history.owner_id = 0;
                                    history.created = System.currentTimeMillis();
                                    history.text = text;
                                    history.type = History.MESSAGE;

                                    mEdBox.getText().clear();
                                    mProgressBar.setVisibility(View.GONE);

                                    mAdapter.addSentMessage(history);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showErrorMessage(ChatActivity.this);
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }).exec();
            }
        });

        mAdapter = new ChatListAdapter(this, mUserId, mUserAvatar, mHistoryList);
        mAdapter.setOnAvatarListener(this);
        mListView.setAdapter(mAdapter);

        update();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!mReceiverRegistered) {
            registerReceiver(mNewMessageReceiver, new IntentFilter(C2DMUtils.C2DM_NOTIFICATION));
            mReceiverRegistered = true;
        }
    }

    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        if (messageRequest != null) messageRequest.cancel();
        if (historyRequest != null) historyRequest.cancel();

        release();
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverRegistered) {
            unregisterReceiver(mNewMessageReceiver);
            mReceiverRegistered = false;
        }
    }

    //---------------------------------------------------------------------------
    private void update() {
        mProgressBar.setVisibility(View.VISIBLE);
        historyRequest = new HistoryRequest(getApplicationContext());
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
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(View.GONE);
                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showErrorMessage(ChatActivity.this);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }

    //---------------------------------------------------------------------------
    private void release() {
        mEdBox = null;
        mListView = null;
        if (mAdapter != null)
            mAdapter.release();
        mAdapter = null;
        mHistoryList = null;
    }

    //---------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        //Обработка клика на автарку
        if (mProfileInvoke) {
            finish();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.INTENT_USER_ID, mUserId);
        intent.putExtra(ProfileActivity.INTENT_CHAT_INVOKE, true);
        intent.putExtra(ProfileActivity.INTENT_USER_NAME, mHeaderTitle.getText());
        startActivity(intent);
    }

    private BroadcastReceiver mNewMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("id");
            if (id != null && !id.equals("") && Integer.parseInt(id) == mUserId) {
                update();
                C2DMUtils.cancelNotification(ChatActivity.this);
            }
        }
    };


    //---------------------------------------------------------------------------
}