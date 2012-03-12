package com.sonetica.topface.ui.inbox;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.History;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.HistoryRequest;
import com.sonetica.topface.net.MessageRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/*
 *            "Диалоги"
 */
public class ChatActivity extends Activity implements View.OnClickListener {
  // Data
  private ListView mListView;
  private ChatListAdapter mAdapter;
  private LinkedList<History> mHistoryList;
  private ProgressDialog mProgressDialog;
  //private InputMethodManager mInputManager;
  private EditText mEdBox;
  private int mUserId;
  //private int mOffset;
  // Constants
  private static final int LIMIT = 20;
  public  static final String INTENT_USER_ID = "user_id";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_chat);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Data
    mHistoryList = new LinkedList<History>();
    
    // Клавиатура
    //mInputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.inbox_header_title));
    
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
    
    // Profile Button
    ImageButton btnProfile = (ImageButton)findViewById(R.id.btnChatProfile);
    btnProfile.setVisibility(View.VISIBLE);
    btnProfile.setOnClickListener(this);
    
    // Edit Box
    mEdBox = (EditText)findViewById(R.id.edChatBox);
    
    // Send Button
    Button btnSend = (Button)findViewById(R.id.btnChatSend);
    btnSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // закрытие клавиатуры
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        /*
        InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEdBox.getWindowToken(),0);
        imm = null;
        */
        /*
        // скрыть клавиатуру
        //mInputManager.hideSoftInputFromWindow(mEdBox.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);
        */
        // формирование сообщения
        MessageRequest message = new MessageRequest(ChatActivity.this.getApplicationContext());
        message.message = mEdBox.getText().toString(); 
        message.userid  = mUserId;
        message.callback(new ApiHandler() {
          @Override
          public void success(Response response) {
            History history = new History();
            history.code=0;
            history.gift=0;
            history.owner_id=0;
            history.created=System.currentTimeMillis();
            history.text=mEdBox.getText().toString();
            history.type=History.MESSAGE;
            mAdapter.addSentMessage(history);
            mAdapter.notifyDataSetChanged();
            mEdBox.getText().clear();
          }
          @Override
          public void fail(int codeError,Response response) {
            //Toast.makeText(ChatActivity.this,"msg sending failed",Toast.LENGTH_SHORT).show();
          }
        }).exec();
      }
    });
    
    create();
    
    update(0,false);
    
    // обнуление информера непрочитанных сообщений
    Data.s_Messages = 0;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View v) {
    Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
    intent.putExtra(ProfileActivity.INTENT_USER_ID,mUserId);
    startActivity(intent);
  }
  //---------------------------------------------------------------------------
  private void update(final int offset,final boolean isRefresh) {
    if(!isRefresh)
      mProgressDialog.show();
    
    final HistoryRequest historyRequest = new HistoryRequest(getApplicationContext());
    historyRequest.userid = mUserId; 
    historyRequest.limit  = LIMIT;
    historyRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<History> dataList = History.parse(response);
        mAdapter.setDataList(dataList);
        mAdapter.notifyDataSetChanged();
        //mListView.onRefreshComplete();
        if(mProgressDialog.isShowing())
          mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,Response response) {
        //mListView.onRefreshComplete();
      }
    }).exec();
    

    /*
    InboxRequest inboxRequest = new InboxRequest(ChatActivity.this);
    inboxRequest.offset = offset;
    inboxRequest.limit  = 6;
    ConnectionService.sendRequest(inboxRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        Toast.makeText(ChatActivity.this,"result",Toast.LENGTH_SHORT).show();
        try {Thread.sleep(1000*4);} catch(InterruptedException e) {}
        mListView.onRefreshComplete();
      }
    });
    */
  }
  //---------------------------------------------------------------------------
  private void create() {
    // ListAdapter
    mAdapter = new ChatListAdapter(getApplicationContext(),mUserId,mHistoryList);
    mAdapter.setOnAvatarListener(this);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    //mInputManager = null;
    if(mEdBox!=null)
      mEdBox.destroyDrawingCache();
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
  protected void onDestroy() {
    release();
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
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