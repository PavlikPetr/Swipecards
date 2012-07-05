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
import com.topface.topface.ui.views.SwapControl;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
    private boolean mIsAddPanelOpened;
    private ListView mListView;
    private ChatListAdapter mAdapter;
    private LinkedList<History> mHistoryList;
    private EditText mEditBox;
    private TextView mHeaderTitle;
    private ProgressBar mProgressBar;
    private MessageRequest messageRequest;
    private HistoryRequest historyRequest;
    private SwapControl mSwapControl;
    // Constants
    private static final int LIMIT = 50;
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_PROFILE_INVOKE = "profile_invoke";
    boolean bibi;
    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_chat);
        Debug.log(this, "+onCreate");

        // Data
        mHistoryList = new LinkedList<History>();

        // Swap Control
        mSwapControl = ((SwapControl)findViewById(R.id.swapFormView));

        // Title Header
        mHeaderTitle = ((TextView)findViewById(R.id.tvHeaderTitle));

        // Progress
        mProgressBar = (ProgressBar)findViewById(R.id.prsChatLoading);

        // Params
        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1);
        mProfileInvoke = getIntent().getBooleanExtra(INTENT_PROFILE_INVOKE, false);
        mHeaderTitle.setText(getIntent().getStringExtra(INTENT_USER_NAME));

        // Profile Button
        View btnProfile = findViewById(R.id.btnChatProfile);
        btnProfile.setVisibility(View.VISIBLE);
        btnProfile.setOnClickListener(this);

        // Add Button        
        ((Button)findViewById(R.id.btnChatAdd)).setOnClickListener(this);

        // Gift Button
        ((Button)findViewById(R.id.btnChatGift)).setOnClickListener(this);

        // Place Button
        ((Button)findViewById(R.id.btnChatPlace)).setOnClickListener(this);

        // Map Button
        ((Button)findViewById(R.id.btnChatMap)).setOnClickListener(this);

        // Edit Box
        mEditBox = (EditText)findViewById(R.id.edChatBox);
        mEditBox.setOnEditorActionListener(mEditorActionListener);

        // ListView
        mListView = (ListView)findViewById(R.id.lvChatList);

        // Adapter
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
        mEditBox = null;
        mListView = null;
        if (mAdapter != null)
            mAdapter.release();
        mAdapter = null;
        mHistoryList = null;
    }
    //---------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChatAdd: {
                if (mIsAddPanelOpened)
                    mSwapControl.snapToScreen(0);
                else
                    mSwapControl.snapToScreen(1);
                mIsAddPanelOpened = !mIsAddPanelOpened;
            } break;
            case R.id.btnChatGift: {
                startActivityForResult(new Intent(this, GiftsActivity.class), GiftsActivity.INTENT_REQUEST_GIFT);
            } break;
            case R.id.btnChatPlace: {
                Toast.makeText(ChatActivity.this, "Place", Toast.LENGTH_SHORT).show();
            } break;
            case R.id.btnChatMap: {
                Toast.makeText(ChatActivity.this, "Map", Toast.LENGTH_SHORT).show();
            } break;
            default: {
                if (mProfileInvoke) {
                    finish();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra(ProfileActivity.INTENT_USER_ID, mUserId);
                intent.putExtra(ProfileActivity.INTENT_CHAT_INVOKE, true);
                intent.putExtra(ProfileActivity.INTENT_USER_NAME, mHeaderTitle.getText());
                startActivity(intent);
            } break;
        }
    }
    //---------------------------------------------------------------------------
    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v,int actionId,KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                final String text = v.getText().toString();

                if (text == null || text.length() == 0)
                    return false;

                mProgressBar.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditBox.getWindowToken(), 0);

                messageRequest = new MessageRequest(ChatActivity.this.getApplicationContext());
                messageRequest.message = mEditBox.getText().toString();
                messageRequest.userid = mUserId;
                messageRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        runOnUiThread(new Runnable() {
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
                                mEditBox.getText().clear();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                    @Override
                    public void fail(int codeError,ApiResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }).exec();
                return true;
            }
            return false;
        }
    };
    //---------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
            Bundle extras = data.getExtras();
            int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);            
            String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);
            Debug.log(this, "id:" + id + " url:" + url);
        }
    }
}
