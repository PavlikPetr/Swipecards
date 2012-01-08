package com.sonetica.topface.ui.rates;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.net.RatesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.ui.PullToRefreshListView;
import com.sonetica.topface.ui.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/*
 *     "меня оценили"
 */
public class RatesActivity extends Activity {
  // Data
  private PullToRefreshListView mListView;
  private ArrayAdapter<Rate> mAdapter;
  private LinkedList<Rate> mRatesList;
  private ProgressDialog  mProgressDialog;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_rates);
    Debug.log(this,"+onCreate");
    
    // Data
    mRatesList = Data.s_RatesList;
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.rates_header_title));

   // ListView
   mListView = (PullToRefreshListView)findViewById(R.id.lvRatesList);
   mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
         update();
     }});
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   if(mRatesList.size()==0)
     update();
   else
     create();
  }
  //---------------------------------------------------------------------------
  private void create() {
    // ListAdapter
    mAdapter = new RatesListAdapter(RatesActivity.this,mRatesList);
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mListView!=null) mListView=null;
    if(mAdapter!=null) mAdapter=null;
    if(mRatesList!=null) mRatesList=null;
    if(mProgressDialog!=null) mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
  private void update() {
    mProgressDialog.show();
    
    RatesRequest likesRequest = new RatesRequest();
    likesRequest.offset = 0;
    likesRequest.limit  = 20;
    /*
    likesRequest.callback(new ApiHandler(){
      @Override
      public void fail(int codeError) {
        Toast.makeText(RatesActivity.this,"fail:"+codeError,Toast.LENGTH_SHORT).show();
      }
      @Override
      public void success(Response response) {
        Toast.makeText(RatesActivity.this,"success",Toast.LENGTH_SHORT).show();
        
        ArrayList<Rate> rates = response.getRates();        
        if(rates!=null) {
          mAdapter = new RatesListAdapter(RatesActivity.this,rates);
          mListView.setAdapter(mAdapter);
        }
        mListView.onRefreshComplete();
        //mProgressDialog.cancel();
        
      }
    }).exec(this);
    */
    ConnectionService.sendRequest(likesRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        
        mRatesList.addAll(resp.getRates());
        create();
        mListView.onRefreshComplete();
        mAdapter.notifyDataSetChanged();
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
