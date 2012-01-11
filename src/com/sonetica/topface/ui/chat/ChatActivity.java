package com.sonetica.topface.ui.chat;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.net.InboxRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.ui.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.ui.PullToRefreshListView;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/*
 *            "Диалоги"
 */
public class ChatActivity extends Activity {
  // Data
  private PullToRefreshListView mListView;
  private ChatListAdapter mAdapter;
  private LinkedList<Inbox> mInboxList;
  private ProgressDialog mProgressDialog;
  private int mState;
  private int mOffset;
  private boolean mIsEndList;
  // Constants
  private static final int LIMIT = 20;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_chat);
    Debug.log(this,"+onCreate");
    
    // Data
    mInboxList = Data.s_InboxList;
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.chat_header_title));
   
    // ListView
    mListView = (PullToRefreshListView)findViewById(R.id.lvChatList);
    mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(mOffset=0,false);
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
    
    if(mInboxList.size()==0)
      update(mOffset,true);
    else
      create();
  }
  //---------------------------------------------------------------------------
  private void create() {
    // ListAdapter
    mAdapter = new ChatListAdapter(ChatActivity.this,mInboxList);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mListView!=null) mListView=null;
    if(mAdapter!=null) mAdapter=null;
    if(mInboxList!=null) mInboxList=null;
    if(mProgressDialog!=null) mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
  private void update(int offset,final boolean append) {
    if(append)
      mProgressDialog.show();
    InboxRequest inboxRequest = new InboxRequest(this);
    inboxRequest.offset = offset;
    inboxRequest.limit  = LIMIT;
    ConnectionService.sendRequest(inboxRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        
        LinkedList<Inbox> list = resp.getMessages();
        if(list!=null)
          if(append)
            mInboxList.addAll(list);
          else {
            //mInboxList.clear();
            mInboxList.addAll(list);
          }
        //else
          //mListView.removeFooterView(btnPrevMsg);
        //if(list.size()<LIMIT)
          //mIsEndList = true;
        create();
        mListView.onRefreshComplete();
        mAdapter.notifyDataSetChanged();
        if(mProgressDialog.isShowing())
          mProgressDialog.cancel();
      }
    });
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