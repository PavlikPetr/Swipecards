package com.sonetica.topface.ui.chat;

import java.util.ArrayList;
import java.util.List;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.net.InboxRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.ui.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.ui.PullToRefreshListView;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/*
 *            "Диалоги"
 */
public class ChatActivity extends Activity {
  // Data
  private PullToRefreshListView mListView;
  private ArrayAdapter mAdapter;
  private ProgressDialog mProgressDialog;
  private ArrayList<Inbox> mInboxList;
  private boolean mIsEndList;
  private int mOffset;
  private Button btnPrevMsg;
  // Constants
  private static final int LIMIT = 20;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_chat);
    Debug.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.chat_header_title));
   
   // Data   
   mInboxList = new ArrayList<Inbox>();

   // ListAdapter
   mAdapter = new ChatListAdapter(ChatActivity.this,mInboxList);
   
   // ListView
   mListView = (PullToRefreshListView)findViewById(R.id.lvChatList);
  
   btnPrevMsg = new Button(this);
   btnPrevMsg.setText("prev msg");
   btnPrevMsg.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
      if(!mIsEndList)
        update(mOffset+=LIMIT);
    }
   });
   //mListView.addFooterView(btnPrevMsg);
   mListView.setAdapter(mAdapter);
   mListView.setVisibility(View.INVISIBLE);
   mListView.setOnRefreshListener(new OnRefreshListener() {
    @Override
    public void onRefresh() {
      if(!mIsEndList)
        update(mOffset+=LIMIT);
    }});

   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   update(mOffset);
  }
  //---------------------------------------------------------------------------
  private void update(int offset) {
    //mProgressDialog.show();
    
    InboxRequest inboxRequest = new InboxRequest();
    inboxRequest.offset = offset;
    inboxRequest.limit  = LIMIT;
    ConnectionService.sendRequest(inboxRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        
        List list = resp.getMessages();
        if(list!=null)
          mInboxList.addAll(list);
        //else
          //mListView.removeFooterView(btnPrevMsg);
        //if(list.size()<LIMIT)
          //mIsEndList = true;

        mAdapter.notifyDataSetChanged();
        mListView.setVisibility(View.VISIBLE);
        mListView.onRefreshComplete();
        //mProgressDialog.cancel();
      }
    });
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mListView = null;
    mAdapter = null;
    mProgressDialog = null;
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
