package com.sonetica.topface.ui.rates;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.RatesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 *     "меня оценили"
 */
public class RatesActivity extends Activity {
  // Data
  private ListView mListView;
  //private RatesListAdapter mAdapter;
  private RatesListAdapter mAdapter;
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
   mListView = (ListView)findViewById(R.id.lvRatesList);
   /*
   mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
         update(); 
     }});
   */
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   if(mRatesList.size()==0)
     update();
   else
     create();
   
   // обнуление информера непросмотренных оценок
   Data.mRates = 0;
  }
  //---------------------------------------------------------------------------
  private void create() {
    // ListAdapter
    mAdapter = new RatesListAdapter(RatesActivity.this,mRatesList);
    //mAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new String[]{"one","two"});
    mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mListView!=null)       mListView=null;
    if(mAdapter!=null)        mAdapter=null;
    if(mRatesList!=null)      mRatesList=null;
    if(mProgressDialog!=null) mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
  private void update() {
    mProgressDialog.show();

    RatesRequest likesRequest = new RatesRequest(this);
    likesRequest.offset = 0;
    likesRequest.limit  = 20;
    likesRequest.callback(new ApiHandler(){
      @Override
      public void success(Response response) {
        mRatesList.addAll(response.getRates());
        create();
        //mListView.onRefreshComplete();
        mAdapter.notifyDataSetChanged();
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError) {
        Toast.makeText(RatesActivity.this,"fail:"+codeError,Toast.LENGTH_SHORT).show();
      }
    }).exec();
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
