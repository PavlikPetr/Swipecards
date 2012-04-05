package com.topface.topface.ui.inbox;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.HistoryRequest;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Imager;
import com.topface.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 *            "Диалоги"
 */
public class ChatActivity extends Activity implements View.OnClickListener {
  // Data
  private ListView mListView;
  private ChatListAdapter mAdapter;
  private LinkedList<History> mHistoryList;
  private ProgressDialog mProgressDialog;
  private boolean mProfileInvoke;
  private EditText mEdBox;
  private int mUserId;
  private TextView mHeaderTitle;
  // Constants
  private static final int LIMIT = 20;
  public  static final String INTENT_USER_ID = "user_id";
  public  static final String INTENT_USER_NAME = "user_name";
  public  static final String INTENT_PROFILE_INVOKE = "profile_invoke";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_chat);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Data
    mHistoryList = new LinkedList<History>();
    
    // Title Header
    mHeaderTitle = ((TextView)findViewById(R.id.tvHeaderTitle));
    
    // ListView
    mListView = (ListView)findViewById(R.id.lvChatList);
    /*
    mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(mOffset,false);
       mListView.onRefreshComplete();
     }});
    mListView.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v,MotionEvent event) {
        return false;
      }
    });
    */

    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    // params
    mUserId = getIntent().getIntExtra(INTENT_USER_ID,-1);
    mProfileInvoke = getIntent().getBooleanExtra(INTENT_PROFILE_INVOKE,false);
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
        
        if(text==null || text.length()==0)
          return;
        
        mProgressDialog.show();

        InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEdBox.getWindowToken(),0);
        
        // формирование сообщения
        MessageRequest message = new MessageRequest(ChatActivity.this.getApplicationContext());
        message.message = mEdBox.getText().toString(); 
        message.userid  = mUserId;
        message.callback(new ApiHandler() {
          @Override
          public void success(ApiResponse response) {
            History history = new History();
            history.code=0;
            history.gift=0;
            history.owner_id=0;
            history.created=System.currentTimeMillis();
            history.text=text;
            history.type=History.MESSAGE;
            mAdapter.addSentMessage(history);
            mAdapter.notifyDataSetChanged();
            mEdBox.getText().clear();
            mProgressDialog.cancel();
          }
          @Override
          public void fail(int codeError,ApiResponse response) {
            mProgressDialog.cancel();
            Toast.makeText(ChatActivity.this,"not sent",Toast.LENGTH_SHORT).show();
          }
        }).exec();
      }
    });
    
    create();
    update(0,false);
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    //App.bind(getBaseContext());
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
    //App.unbind();
    super.onStop();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    release();
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void create() {
    mAdapter = new ChatListAdapter(getApplicationContext(),mUserId,mHistoryList);
    mAdapter.setOnAvatarListener(this);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void update(final int offset,final boolean isRefresh) {
    if(!isRefresh) {
      mProgressDialog.show();
      Imager.avatarOwnerPreloading(getApplicationContext());
    }
    
    final HistoryRequest historyRequest = new HistoryRequest(getApplicationContext());
    historyRequest.userid = mUserId; 
    historyRequest.limit  = LIMIT;
    historyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        LinkedList<History> dataList = History.parse(response);
        mAdapter.setDataList(dataList);
        mAdapter.notifyDataSetChanged();
        //mListView.onRefreshComplete();
        if(mProgressDialog.isShowing())
          mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        //mListView.onRefreshComplete();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void release() {
    mEdBox = null;
    mListView = null;
    if(mAdapter!=null)
      mAdapter.release();
    mAdapter = null;
    mHistoryList = null;
    mProgressDialog = null;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View v) {
    if(mProfileInvoke) {
      finish();
      return;
    }
    Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
    intent.putExtra(ProfileActivity.INTENT_USER_ID,mUserId);
    intent.putExtra(ProfileActivity.INTENT_CHAT_INVOKE,true);
    intent.putExtra(ProfileActivity.INTENT_USER_NAME,mHeaderTitle.getText());
    startActivity(intent);
  }
  //---------------------------------------------------------------------------
}

/*
mListView.setOnScrollListener(new OnScrollListener() {
  private int prevVisibleItem;
  @Override
  public void onScrollStateChanged(AbsListView view,int scrollState) {
    mState = scrollState; 
  }
  @Override
  public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
    prevVisibleItem = firstVisibleItem;
    if(mState==SCROLL_STATE_TOUCH_SCROLL) {
      int current = firstVisibleItem+visibleItemCount+1;
      if(current==totalItemCount && prevVisibleItem<current)
        if(!mIsEndList)
          update(mOffset+=LIMIT,true);
    }
  }
});
*/