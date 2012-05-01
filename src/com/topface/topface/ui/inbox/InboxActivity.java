package com.topface.topface.ui.inbox;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.FeedInbox;
import com.topface.topface.p2r.PullToRefreshListView;
import com.topface.topface.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedInboxRequest;
import com.topface.topface.services.NotificationService;
import com.topface.topface.ui.AvatarManager;
import com.topface.topface.ui.DoubleBigButton;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
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
  private TextView mFooterView;
  private PullToRefreshListView mListView;
  private InboxListAdapter mListAdapter;
  private LinkedList<FeedInbox> mInboxDataList;
  private AvatarManager<FeedInbox> mAvatarManager;
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
    mInboxDataList = new LinkedList<FeedInbox>();
    
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
        ImageView iv = (ImageView)view.findViewById(R.id.ivAvatar);
        Data.s_UserAvatar = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        
        Intent intent = new Intent(InboxActivity.this.getApplicationContext(),ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID,mInboxDataList.get(position).uid);
        intent.putExtra(ChatActivity.INTENT_USER_NAME,mInboxDataList.get(position).first_name);
        startActivity(intent);
      }
    });
    
    // Footer
    mFooterView = new TextView(getApplicationContext());
    mFooterView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getHistory();
      }
    });
    mFooterView.setBackgroundResource(R.drawable.gallery_item_all_selector);
    mFooterView.setText(getString(R.string.footer_previous));
    mFooterView.setTextColor(Color.DKGRAY);
    mFooterView.setGravity(Gravity.CENTER);
    mFooterView.setVisibility(View.GONE);
    mFooterView.setTypeface(Typeface.DEFAULT_BOLD);
    mListView.getRefreshableView().addFooterView(mFooterView);
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));

    // control create
    mAvatarManager = new AvatarManager<FeedInbox>(getApplicationContext(),mInboxDataList);
    mListAdapter = new InboxListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setOnScrollListener(mAvatarManager);    
    mListView.setAdapter(mListAdapter);
    
    mOnlyNewData = CacheProfile.unread_messages > 0 ? true : false;
    
    update(true);
    
    // обнуление информера непрочитанных сообщений
    CacheProfile.unread_messages = 0;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {   
    release();
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void update(boolean isProgress) {
    if(isProgress)
      mProgressDialog.show();
    
    mDoubleButton.setChecked(mOnlyNewData ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
    
    mNotificationManager.cancel(NotificationService.NOTIFICATION_MESSAGES);
    
    FeedInboxRequest inboxRequest = new FeedInboxRequest(getApplicationContext());
    inboxRequest.limit = LIMIT;
    inboxRequest.only_new = mOnlyNewData;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        if(mOnlyNewData)
          mFooterView.setVisibility(View.GONE);
        else
          mFooterView.setVisibility(View.VISIBLE);
        mInboxDataList.clear();
        mInboxDataList.addAll(FeedInbox.parse(response));
        mListAdapter.notifyDataSetChanged();
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
  private void getHistory() {
    mProgressDialog.show();
    FeedInboxRequest inboxRequest = new FeedInboxRequest(getApplicationContext());
    inboxRequest.limit = LIMIT;
    inboxRequest.only_new = false;
    inboxRequest.from = mInboxDataList.get(mInboxDataList.size()-1).id;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        LinkedList<FeedInbox> inboxList = FeedInbox.parse(response);
        if(inboxList.size() > 0) {
          mInboxDataList.addAll(inboxList);
          mListAdapter.notifyDataSetChanged();
        } else
          mFooterView.setVisibility(View.GONE);
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
    mProgressDialog = null;
    mListView = null;
    
    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter = null;
    
    if(mAvatarManager!=null)
      mAvatarManager.release();
    mAvatarManager = null;
    
    if(mInboxDataList!=null)
      mInboxDataList.clear();
    mInboxDataList = null;

    Data.s_UserAvatar = null;
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