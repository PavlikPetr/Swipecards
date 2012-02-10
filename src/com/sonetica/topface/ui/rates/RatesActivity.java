package com.sonetica.topface.ui.rates;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.RatesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.AvatarManager;
import com.sonetica.topface.ui.DoubleBigButton;
import com.sonetica.topface.ui.DoubleButton;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.ui.inbox.InboxActivity;
import com.sonetica.topface.ui.inbox.InboxListAdapter;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/*
 *     "меня оценили"
 */
public class RatesActivity extends Activity {
  // Data
  private ListView mListView;
  private RatesListAdapter mAdapter;
  private LinkedList<Rate> mRatesList;
  private AvatarManager mAvatarManager;
  private ProgressDialog  mProgressDialog;
  private boolean mIsNewMessages;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_rates);
    Debug.log(this,"+onCreate");
    
    // Data
    //mRatesList = Data.s_RatesList;
    mRatesList = new LinkedList<Rate>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.rates_header_title));
   
   // Double Button
   DoubleBigButton btnDouble = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   btnDouble.setLeftText(getString(R.string.rates_btn_dbl_left));
   btnDouble.setRightText(getString(R.string.rates_btn_dbl_right));
   btnDouble.setChecked(mIsNewMessages==false?DoubleBigButton.LEFT_BUTTON:DoubleBigButton.RIGHT_BUTTON);
   // Left btn
   btnDouble.setLeftListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       Toast.makeText(RatesActivity.this,"All",Toast.LENGTH_SHORT).show(); 
     }
   });
   // Right btn
   btnDouble.setRightListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       Toast.makeText(RatesActivity.this,"New",Toast.LENGTH_SHORT).show();
     }
   });

   // ListView
   mListView = (ListView)findViewById(R.id.lvRatesList);
   /*
   mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
         update(); 
     }});
   */
   
   mListView.setOnItemClickListener(new OnItemClickListener(){
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
       Intent intent = new Intent(RatesActivity.this,ChatActivity.class);
       int x = mRatesList.get(position).uid;
       intent.putExtra(ChatActivity.INTENT_USER_ID,mRatesList.get(position).uid);
       startActivityForResult(intent,0);
     }
   });
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   create();
   if(mRatesList.size()==0)
     update();
   
   // обнуление информера непросмотренных оценок
   Data.s_Rates = 0;
  }
  //---------------------------------------------------------------------------
  private void create() {
    // ListAdapter
    //mAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new String[]{"one","two"});
    mAvatarManager = new AvatarManager<Rate>(this,mRatesList);
    mListView.setOnScrollListener(mAvatarManager);    
    mAdapter = new RatesListAdapter(this,mAvatarManager);
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
    likesRequest.limit  = 40;
    likesRequest.callback(new ApiHandler(){
      @Override
      public void success(Response response) {
        LinkedList<Rate> ratesList = Rate.parse(response);
        mRatesList = ratesList;
        mAvatarManager.setDataList(ratesList);
        mAdapter.notifyDataSetChanged();
        mProgressDialog.cancel();
        //mListView.onRefreshComplete();
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
