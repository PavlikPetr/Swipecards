package com.sonetica.topface.ui.rates;

import java.util.ArrayList;
import org.json.JSONObject;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.net.ApiHandler;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 *          "меня оценили"
 */
public class RatesActivity extends Activity {
  // Data
  private PullToRefreshListView mListView;
  private ArrayAdapter<Rate> mAdapter;
  private ProgressDialog  mProgressDialog;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_rates);
    Debug.log(this,"+onCreate");
    
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
   
   update();
  }
  //---------------------------------------------------------------------------
  private void update() {
    //mProgressDialog.show();
    
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
        
        ArrayList<Rate> rates = resp.getRates();        
        if(rates!=null) {
          mAdapter = new RatesListAdapter(RatesActivity.this,rates);
          mListView.setAdapter(mAdapter);
        }
        mListView.onRefreshComplete();
        //mProgressDialog.cancel();
      }
    });
    
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
