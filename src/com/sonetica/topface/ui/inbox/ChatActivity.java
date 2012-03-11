package com.sonetica.topface.ui.inbox;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.History;
import com.sonetica.topface.module.pull2refresh.PullToRefreshListView;
import com.sonetica.topface.module.pull2refresh.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.HistoryRequest;
import com.sonetica.topface.net.MessageRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 *            "Диалоги"
 */
public class ChatActivity extends Activity {
  // Data
  private PullToRefreshListView mListView;
  private ChatListAdapter mAdapter;
  private LinkedList<History> mHistoryList;
  private ProgressDialog mProgressDialog;
  private EditText mEdBox;
  private int mUserId;
  private int mOffset;
  // Constants
  private static final int LIMIT = 20;
  public  static final String INTENT_USER_ID = "user_id";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_chat);
    Debug.log(this,"+onCreate");
    
    // Data
    mHistoryList = new LinkedList<History>();
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.inbox_header_title));
   
    // ListView
    mListView = (PullToRefreshListView)findViewById(R.id.lvChatList);
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

    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    // params
    mUserId = getIntent().getIntExtra(INTENT_USER_ID,-1);
    
    // Edit Box
    mEdBox = (EditText)findViewById(R.id.edChatBox);
    
    // Send Button
    Button btnSend = (Button)findViewById(R.id.btnChatSend);
    btnSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // закрытие клавиатуры
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager)ChatActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEdBox.getWindowToken(),0);
        // формирование сообщения
        MessageRequest message = new MessageRequest(ChatActivity.this);
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
            Toast.makeText(ChatActivity.this,"msg sending failed",Toast.LENGTH_SHORT).show();
          }
        }).exec();
      }
    });
    
    create();
    if(mHistoryList.size()==0)
      update(0,false);

    
    // обнуление информера непрочитанных сообщений
    Data.s_Messages = 0;
  }
  //---------------------------------------------------------------------------
  private void create() {
    // ListAdapter
    mAdapter = new ChatListAdapter(ChatActivity.this,mUserId,mHistoryList);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mListView!=null)    mListView = null;
    if(mAdapter!=null)      mAdapter = null;
    if(mHistoryList!=null)   mHistoryList = null;
    if(mProgressDialog!=null) mProgressDialog = null;
  }
  //---------------------------------------------------------------------------
  private void update(final int offset,final boolean isRefresh) {
    if(!isRefresh)
      mProgressDialog.show();
    
    final HistoryRequest historyRequest = new HistoryRequest(ChatActivity.this);
    historyRequest.userid = mUserId; 
    historyRequest.offset = offset;
    historyRequest.limit  = LIMIT;
    historyRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<History> dataList = History.parse(response);
        
        if(dataList!=null)
          mAdapter.setDataList(dataList);

        mAdapter.notifyDataSetChanged();
        mListView.onRefreshComplete();
        if(mProgressDialog.isShowing())
          mProgressDialog.cancel();
        
        mOffset += LIMIT;
          
      }
      @Override
      public void fail(int codeError,Response response) {
        mListView.onRefreshComplete();
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