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
import com.sonetica.topface.ui.AvatarManager;
import com.sonetica.topface.ui.DoubleBigButton;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
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
  private LinkedList<Inbox> mInboxDataList;
  private AvatarManager<Inbox> mAvatarManager;
  private ProgressDialog mProgressDialog;
  private DoubleBigButton mDoubleButton;
  // Constants
  private static final int LIMIT = 40;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_inbox);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Data
    //mInboxList = Data.s_InboxList;
    mInboxDataList = new LinkedList<Inbox>();
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.inbox_header_title));
    
    // Double Button
    mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
    mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
    mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
    mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
    // Left btn
    mDoubleButton.setLeftListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        update(true,false);
      }
    });
    // Right btn
    mDoubleButton.setRightListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        update(true,true);
      }
    });
   
    // ListView
    mListView = (PullToRefreshListView)findViewById(R.id.lvInboxList);
    mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(false,true);
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
        Intent intent = new Intent(InboxActivity.this.getApplicationContext(),ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID,mInboxDataList.get(position).uid);
        startActivityForResult(intent,0);
      }
    });

    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    create();
    
    update(true,false);
    
    // обнуление информера непрочитанных сообщений
    Data.s_Messages = 0;
  }
  //---------------------------------------------------------------------------
  private void update(boolean isProgress, final boolean isNew) {
    if(isProgress)
      mProgressDialog.show();
    
    InboxRequest inboxRequest = new InboxRequest(getApplicationContext());
    inboxRequest.limit = LIMIT;
    inboxRequest.only_new = isNew;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<Inbox> inboxList = Inbox.parse(response);
        if(inboxList.size()>0) {
          mInboxDataList = inboxList;
          mAvatarManager.setDataList(inboxList);
          mAdapter.notifyDataSetChanged();
        } else
          mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
        mProgressDialog.cancel();
        mListView.onRefreshComplete();
          
      }
      @Override
      public void fail(int codeError,Response response) {
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
    mAvatarManager = new AvatarManager<Inbox>(getApplicationContext(),mInboxDataList);
    mListView.setOnScrollListener(mAvatarManager);    
    mAdapter = new InboxListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    mListView = null;
    if(mAdapter!=null)
      mAdapter.release();
    mAdapter = null;
    if(mAvatarManager!=null) {
      mAvatarManager.release();
      mAvatarManager = null;
    }
    if(mInboxDataList!=null)
      mInboxDataList.clear();
    mInboxDataList = null;
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