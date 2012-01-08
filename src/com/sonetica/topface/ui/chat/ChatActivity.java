package com.sonetica.topface.ui.chat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 *            "Диалоги"
 */
public class ChatActivity extends Activity {
  // Data
  private PullToRefreshListView mListView;
  private ProgressDialog mProgressDialog;
  private ArrayAdapter mAdapter;
  private LinkedList<Inbox> mInboxList = new LinkedList<Inbox>();
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
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.chat_header_title));

    // ListAdapter
    mAdapter = new ChatListAdapter(ChatActivity.this,mInboxList);
   
    // ListView
    mListView = (PullToRefreshListView)findViewById(R.id.lvChatList);
    mListView.setAdapter(mAdapter);
    mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(mOffset=0,false);
     }});
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
    mListView.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v,MotionEvent event) {
        return false;
      }
    });
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));

    update(mOffset,true);
  }
  //---------------------------------------------------------------------------
  private void update(int offset,final boolean append) {
    if(append)
      mProgressDialog.show();
    InboxRequest inboxRequest = new InboxRequest();
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

        mAdapter.notifyDataSetChanged();
        mListView.onRefreshComplete();
        if(mProgressDialog.isShowing())
          mProgressDialog.cancel();
      }
    });
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mListView = null;
    mAdapter = null;
    mProgressDialog = null;
    mInboxList = null;
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
