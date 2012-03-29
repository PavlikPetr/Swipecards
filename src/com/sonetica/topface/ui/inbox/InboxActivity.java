package com.sonetica.topface.ui.inbox;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.p2r.PullToRefreshListView;
import com.sonetica.topface.p2r.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.requests.ApiHandler;
import com.sonetica.topface.requests.ApiResponse;
import com.sonetica.topface.requests.InboxRequest;
import com.sonetica.topface.services.NotificationService;
import com.sonetica.topface.ui.AvatarManager;
import com.sonetica.topface.ui.DoubleBigButton;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/*
 *            "Диалоги"
 */
public class InboxActivity extends Activity {
  // Data
  private boolean mOnlyNewData;
  private PullToRefreshListView mListView;
  private InboxListAdapter mAdapter;
  private LinkedList<Inbox> mInboxDataList;
  private AvatarManager<Inbox> mAvatarManager;
  private ProgressDialog mProgressDialog;
  private DoubleBigButton mDoubleButton;
  private NotificationManager mNotificationManager;
  // Constants
  private static final int LIMIT = 40;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_inbox);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    
    // Data
    mInboxDataList = new LinkedList<Inbox>();
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.inbox_header_title));
    
    // Double Button
    mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
    mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
    mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
    mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
    mDoubleButton.setLeftListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mOnlyNewData = false;
        update(true);
      }
    });
    mDoubleButton.setRightListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mOnlyNewData = true;
        update(true);
      }
    });
   
    // ListView
    mListView = (PullToRefreshListView)findViewById(R.id.lvInboxList);
    mListView.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        update(false);
      }
    });
    mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
        Data.s_UserDrw = ((ImageView)view.findViewById(R.id.ivAvatar)).getDrawable();
        
        Intent intent = new Intent(InboxActivity.this.getApplicationContext(),ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID,mInboxDataList.get(position).uid);
        intent.putExtra(ChatActivity.INTENT_USER_NAME,mInboxDataList.get(position).first_name);
        startActivity(intent);
      }
    });
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));

    mOnlyNewData = Data.s_Messages > 0 ? true : false;
    
    create();
    update(true);
    
    //App.delete();
    
    // обнуление информера непрочитанных сообщений
    Data.s_Messages = 0;
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
    mAvatarManager = new AvatarManager<Inbox>(getApplicationContext(),mInboxDataList);
    mAdapter = new InboxListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setOnScrollListener(mAvatarManager);    
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void update(boolean isProgress) {
    if(isProgress)
      mProgressDialog.show();
    
    mNotificationManager.cancel(NotificationService.TP_NOTIFICATION);
    
    InboxRequest inboxRequest = new InboxRequest(getApplicationContext());
    inboxRequest.limit = LIMIT;
    inboxRequest.only_new = mOnlyNewData;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        mDoubleButton.setChecked(mOnlyNewData ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
        mInboxDataList.clear();
        mInboxDataList = Inbox.parse(response);
        mAvatarManager.setDataList(mInboxDataList);
        mAdapter.notifyDataSetChanged();
        mProgressDialog.cancel();
        mListView.onRefreshComplete();
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        mProgressDialog.cancel();
        mListView.onRefreshComplete();
      }
    }).exec();
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
    
    Data.s_UserDrw = null;
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