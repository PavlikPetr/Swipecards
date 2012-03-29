package com.sonetica.topface.ui.rates;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.p2r.PullToRefreshListView;
import com.sonetica.topface.p2r.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.requests.ApiHandler;
import com.sonetica.topface.requests.ApiResponse;
import com.sonetica.topface.requests.RatesRequest;
import com.sonetica.topface.ui.AvatarManager;
import com.sonetica.topface.ui.DoubleBigButton;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/*
 *     "меня оценили"
 */
public class RatesActivity extends Activity {
  // Data
  private boolean mOnlyNewData;
  private PullToRefreshListView mListView;
  private RatesListAdapter mAdapter;
  private LinkedList<Rate> mRatesDataList;
  private AvatarManager<Rate> mAvatarManager;
  private ProgressDialog mProgressDialog;
  private DoubleBigButton mDoubleButton;
  // Constants
  private static final int LIMIT = 60;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_rates);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Data
    mRatesDataList = new LinkedList<Rate>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.rates_header_title));
   
   // Double Button
   mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   mDoubleButton.setLeftText(getString(R.string.rates_btn_dbl_left));
   mDoubleButton.setRightText(getString(R.string.rates_btn_dbl_right));
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
   mListView = (PullToRefreshListView)findViewById(R.id.lvRatesList);
   mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(RatesActivity.this.getApplicationContext(),ProfileActivity.class);
       intent.putExtra(ProfileActivity.INTENT_USER_ID,mRatesDataList.get(position).uid);
       intent.putExtra(ProfileActivity.INTENT_USER_NAME,mRatesDataList.get(position).first_name);
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

   mOnlyNewData = Data.s_Rates > 0 ? true : false;
   
   create();
   update(true);
   
   // обнуление информера непросмотренных оценок
   Data.s_Rates = 0;
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
    mAvatarManager = new AvatarManager<Rate>(this,mRatesDataList);
    mAdapter = new RatesListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setOnScrollListener(mAvatarManager);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void update(boolean isProgress) {
    if(isProgress)
      mProgressDialog.show();

    RatesRequest likesRequest = new RatesRequest(getApplicationContext());
    likesRequest.limit = LIMIT;
    likesRequest.only_new = mOnlyNewData;
    likesRequest.callback(new ApiHandler(){
      @Override
      public void success(ApiResponse response) {
        mDoubleButton.setChecked(mOnlyNewData?DoubleBigButton.RIGHT_BUTTON:DoubleBigButton.LEFT_BUTTON);
        mRatesDataList.clear();
        mRatesDataList = Rate.parse(response);
        mAvatarManager.setDataList(mRatesDataList);
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
    
    mRatesDataList=null;
    
    if(mAvatarManager!=null) {
      mAvatarManager.release();
      mAvatarManager=null;
    }
    
    mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
}
