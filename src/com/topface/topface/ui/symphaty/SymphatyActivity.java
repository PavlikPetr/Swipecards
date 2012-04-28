package com.topface.topface.ui.symphaty;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.data.FeedSymphaty;
import com.topface.topface.p2r.PullToRefreshListView;
import com.topface.topface.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedSymphatyRequest;
import com.topface.topface.ui.AvatarManager;
import com.topface.topface.ui.DoubleBigButton;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/*
 *     "Симпатии"
 */
public class SymphatyActivity extends Activity {
  // Data
  private boolean mOnlyNewData;
  private PullToRefreshListView mListView;
  private SymphatyListAdapter mAdapter;
  private LinkedList<FeedSymphaty> mSymphatyDataList;
  private AvatarManager<FeedSymphaty> mAvatarManager;
  private ProgressDialog mProgressDialog;
  private DoubleBigButton mDoubleButton;
  // Constants
  private static final int LIMIT = 44;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_symphaty);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Data
    mSymphatyDataList = new LinkedList<FeedSymphaty>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.symphaty_header_title));
   
   // Double Button
   mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   mDoubleButton.setLeftText(getString(R.string.symphaty_btn_dbl_left));
   mDoubleButton.setRightText(getString(R.string.symphaty_btn_dbl_right));
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
   mListView = (PullToRefreshListView)findViewById(R.id.lvSymphatyList);
   mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(SymphatyActivity.this.getApplicationContext(),ProfileActivity.class);
       intent.putExtra(ProfileActivity.INTENT_USER_ID,mSymphatyDataList.get(position).uid);
       intent.putExtra(ProfileActivity.INTENT_USER_NAME,mSymphatyDataList.get(position).first_name);
       startActivityForResult(intent,0);
     }
   });
   mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(false);
     }
   });
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));

   mOnlyNewData = CacheProfile.unread_symphaties > 0 ? true : false;
   
   create();
   update(true);
   
   // обнуление информера непросмотренных оценок
   CacheProfile.unread_symphaties = 0;
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
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
    mAvatarManager = new AvatarManager<FeedSymphaty>(this,mSymphatyDataList);
    mAdapter = new SymphatyListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setOnScrollListener(mAvatarManager);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void update(boolean isProgress) {
    if(isProgress)
      mProgressDialog.show();

    FeedSymphatyRequest symphatyRequest = new FeedSymphatyRequest(getApplicationContext());
    symphatyRequest.limit = LIMIT;
    symphatyRequest.only_new = mOnlyNewData;
    symphatyRequest.callback(new ApiHandler(){
      @Override
      public void success(ApiResponse response) {
        mDoubleButton.setChecked(mOnlyNewData?DoubleBigButton.RIGHT_BUTTON:DoubleBigButton.LEFT_BUTTON);
        mSymphatyDataList.clear();
        mSymphatyDataList = FeedSymphaty.parse(response);
        mAvatarManager.setDataList(mSymphatyDataList);
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
    mListView=null;
    
    if(mAdapter!=null)
      mAdapter.release();
    mAdapter = null;
    
    mSymphatyDataList=null;
    
    if(mAvatarManager!=null) {
      mAvatarManager.release();
      mAvatarManager=null;
    }
    
    mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
}
