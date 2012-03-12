package com.sonetica.topface.ui.rates;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.module.pull2refresh.PullToRefreshListView;
import com.sonetica.topface.module.pull2refresh.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.RatesRequest;
import com.sonetica.topface.net.Response;
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
    //mRatesList = Data.s_RatesList;
    mRatesDataList = new LinkedList<Rate>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.rates_header_title));
   
   // Double Button
   mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   mDoubleButton.setLeftText(getString(R.string.rates_btn_dbl_left));
   mDoubleButton.setRightText(getString(R.string.rates_btn_dbl_right));
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
   mListView = (PullToRefreshListView)findViewById(R.id.lvRatesList);
   mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(RatesActivity.this.getApplicationContext(),ProfileActivity.class);
       intent.putExtra(ProfileActivity.INTENT_USER_ID,mRatesDataList.get(position).uid);
       startActivityForResult(intent,0);
     }
   });
   mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(false,true);
       mListView.onRefreshComplete();
     }
   });
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   create();
   
   update(true,Data.s_Rates>0?true:false);
   
   // обнуление информера непросмотренных оценок
   Data.s_Rates = 0;
  }
  //---------------------------------------------------------------------------
  private void update(boolean isProgress, final boolean isNew) {
    if(isProgress)
      mProgressDialog.show();

    RatesRequest likesRequest = new RatesRequest(getApplicationContext());
    likesRequest.limit = LIMIT;
    likesRequest.only_new = isNew;
    likesRequest.callback(new ApiHandler(){
      @Override
      public void success(Response response) {
        if(RatesActivity.this==null)
          return;
        LinkedList<Rate> ratesList = Rate.parse(response);
        if(ratesList.size()>0) {
         mRatesDataList = ratesList;
         mDoubleButton.setChecked(isNew==false?DoubleBigButton.LEFT_BUTTON:DoubleBigButton.RIGHT_BUTTON);
          mAvatarManager.setDataList(ratesList);
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
  }
  //---------------------------------------------------------------------------
  private void create() {
    // ListAdapter
    mAvatarManager = new AvatarManager<Rate>(this,mRatesDataList);
    mListView.setOnScrollListener(mAvatarManager);    
    mAdapter = new RatesListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setAdapter(mAdapter);
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
    if(StarView.mStarYellow!=null)
      StarView.mStarYellow.recycle();
    StarView.mStarYellow=null;
    
    mProgressDialog=null;
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
