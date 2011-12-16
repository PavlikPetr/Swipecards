package com.sonetica.topface.ui.rates;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.net.RatesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/*
 *          "меня оценили"
 */
public class RatesActivity extends Activity {
  // Data
  private ListView mListView;
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
   mListView = (ListView)findViewById(R.id.lvRatesList);
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   mProgressDialog.show();
   
   update();
  }
  //---------------------------------------------------------------------------
  private void update() {
    RatesRequest likesRequest = new RatesRequest();
    likesRequest.offset = 0;
    likesRequest.limit  = 20;
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
        mProgressDialog.cancel();
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
