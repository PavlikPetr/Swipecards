package com.sonetica.topface.ui.inbox;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.module.pull2refresh.PullToRefreshListView;
import com.sonetica.topface.module.pull2refresh.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.InboxRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/*
 *            "Диалоги"
 */
public class InboxActivity extends Activity {
  // Data
  private PullToRefreshListView mListView;
  private InboxListAdapter mAdapter;
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
    setContentView(R.layout.ac_inbox);
    Debug.log(this,"+onCreate");
    
    // Data
    mInboxList = Data.s_InboxList;
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.chat_header_title));
   
    // ListView
    mListView = (PullToRefreshListView)findViewById(R.id.lvInboxList);
    mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       //update(0,true);
       mListView.onRefreshComplete();
     }});
    mListView.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v,MotionEvent event) {
        return false;
      }
    });
    mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
        Intent intent = new Intent(InboxActivity.this,ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID,mInboxList.get(position).uid);
        startActivityForResult(intent,0);
      }
    });

    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    if(mInboxList.size()==0) {
      create();
      update(0,false);
    } else
      create();
    
    // обнуление информера непрочитанных сообщений
    Data._messages = 0;
  }
  //---------------------------------------------------------------------------
  private void create() {
    // ListAdapter
    mAdapter = new InboxListAdapter(InboxActivity.this,mInboxList);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mListView!=null)       mListView = null;
    if(mAdapter!=null)        mAdapter = null;
    if(mInboxList!=null)      mInboxList = null;
    if(mProgressDialog!=null) mProgressDialog = null;
  }
  //---------------------------------------------------------------------------
  private void update(final int offset,final boolean isRefresh) {
    //if(!isRefresh)
      //mProgressDialog.show();
    
    InboxRequest inboxRequest = new InboxRequest(InboxActivity.this);
    inboxRequest.offset = offset;
    inboxRequest.limit  = 20;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<Inbox> list = Inbox.parse(response);
        if(list.size()!=0) {
          if(isRefresh)
            for(int i=list.size()-1;i>=0;--i)
              mInboxList.addFirst(list.get(i));
          else
            mInboxList.addAll(list);
        }
        
        mAdapter.notifyDataSetChanged();
        mListView.onRefreshComplete();
        //if(mProgressDialog.isShowing())
          //mProgressDialog.cancel();
          
      }
      @Override
      public void fail(int codeError) {
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