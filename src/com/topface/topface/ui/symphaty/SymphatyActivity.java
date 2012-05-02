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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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
  private TextView mFooterView;
  private PullToRefreshListView mListView;
  private SymphatyListAdapter mListAdapter;
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
   mFooterView = new TextView(getApplicationContext());
   mFooterView.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       getHistory();
     }
   });
   
   // Footer
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
   mAvatarManager = new AvatarManager<FeedSymphaty>(this,mSymphatyDataList);
   mListAdapter = new SymphatyListAdapter(getApplicationContext(),mAvatarManager);
   mListView.setOnScrollListener(mAvatarManager);
   mListView.setAdapter(mListAdapter);
   
   mOnlyNewData = CacheProfile.unread_symphaties > 0 ? true : false;
   
   update(true);
   
   // обнуление информера непросмотренных оценок
   CacheProfile.unread_symphaties = 0;
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
    
    mDoubleButton.setChecked(mOnlyNewData?DoubleBigButton.RIGHT_BUTTON:DoubleBigButton.LEFT_BUTTON);

    FeedSymphatyRequest symphatyRequest = new FeedSymphatyRequest(getApplicationContext());
    symphatyRequest.limit = LIMIT;
    symphatyRequest.only_new = mOnlyNewData;
    symphatyRequest.callback(new ApiHandler(){
      @Override
      public void success(ApiResponse response) {
        mSymphatyDataList.clear();
        mSymphatyDataList.addAll(FeedSymphaty.parse(response));
        
        if(mOnlyNewData)
          mFooterView.setVisibility(View.GONE);
        else
          mFooterView.setVisibility(View.VISIBLE);
        
        if(mSymphatyDataList.size() == 0)
          mFooterView.setVisibility(View.GONE);
        
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
    FeedSymphatyRequest symphatyRequest = new FeedSymphatyRequest(getApplicationContext());
    symphatyRequest.limit = LIMIT;
    symphatyRequest.only_new = false;
    symphatyRequest.from = mSymphatyDataList.get(mSymphatyDataList.size()-1).id;
    symphatyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        LinkedList<FeedSymphaty> symphatiesList = FeedSymphaty.parse(response);
        if(symphatiesList.size() > 0) {
          mSymphatyDataList.addAll(symphatiesList);
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
    mProgressDialog=null;
    mListView=null;
    
    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter = null;
    
    if(mAvatarManager!=null) {
      mAvatarManager.release();
      mAvatarManager=null;
    }
    
    if(mSymphatyDataList!=null)
      mSymphatyDataList.clear();
    mSymphatyDataList=null;
  }
  //---------------------------------------------------------------------------
}
