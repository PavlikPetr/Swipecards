package com.topface.topface.ui;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.HistoryRequest;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.ui.adapters.ChatListAdapter;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity implements View.OnClickListener {
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
    public static final String INTENT_PROFILE_INVOKE = "profile_invoke";
    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_chat);
        Debug.log(this, "+onCreate");

        // Data
        mHistoryList = new LinkedList<History>();

        // Title Header
        mHeaderTitle = ((TextView)findViewById(R.id.tvHeaderTitle));

        // ListView
        mListView = (ListView)findViewById(R.id.lvChatList);

        // Progress
        mProgressBar = (ProgressBar)findViewById(R.id.prsChatLoading);

        // params
        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1);
        mProfileInvoke = getIntent().getBooleanExtra(INTENT_PROFILE_INVOKE, false);
        mHeaderTitle.setText(getIntent().getStringExtra(INTENT_USER_NAME));

        // Profile Button
        View btnProfile = findViewById(R.id.btnChatProfile);
        btnProfile.setVisibility(View.VISIBLE);
        btnProfile.setOnClickListener(this);

        // Edit Box
        mEdBox = (EditText)findViewById(R.id.edChatBox);

        // Send Button
        Button btnSend = (Button)findViewById(R.id.btnChatSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = mEdBox.getText().toString();

                if (text == null || text.length() == 0)
                    return;

                mProgressBar.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
                                History history = new History();
                                history.code = 0;
                                history.gift = 0;
                                history.owner_id = 0;
                                history.created = System.currentTimeMillis();
                                history.text = text;
                                history.type = History.MESSAGE;
                                mAdapter.addSentMessage(history);
                                mAdapter.notifyDataSetChanged();
                                mEdBox.getText().clear();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                    @Override
                    public void fail(int codeError,ApiResponse response) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }).exec();
            }
        });

        mAdapter = new ChatListAdapter(getApplicationContext(), mUserId, mHistoryList);
        mAdapter.setOnAvatarListener(this);
        mListView.setAdapter(mAdapter);

        Http.avatarOwnerPreloading();

        update();
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        if (messageRequest != null)
            messageRequest.cancel();
        if (historyRequest != null)
            historyRequest.cancel();

        release();
        Debug.log(this, "-onDestroy");
        super.onDestroy();
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
                LinkedList<History> dataList = History.parse(response);
                mAdapter.setDataList(dataList);
                post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
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
    //---------------------------------------------------------------------------
}
