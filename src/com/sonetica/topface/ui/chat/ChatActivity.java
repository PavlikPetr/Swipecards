package com.sonetica.topface.ui.chat;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.net.InboxRequest;
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
 *            "Диалоги"
 */
public class ChatActivity extends Activity {
  // Data
  private ListView mListView;
  private ArrayAdapter mAdapter;
  private ProgressDialog mProgressDialog;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_chat);
    Debug.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.chat_header_title));

   // ListView
   mListView = (ListView)findViewById(R.id.lvChatList);
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   mProgressDialog.show();
   update();
  }
  //---------------------------------------------------------------------------
  private void update() {
    InboxRequest inboxRequest = new InboxRequest();
    inboxRequest.offset = 0;
    inboxRequest.limit  = 20;
    ConnectionService.sendRequest(inboxRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        
        ArrayList<Inbox> list = resp.getMessages();
        
        mAdapter = new ChatListAdapter(ChatActivity.this,list);
        
        mListView.setAdapter(mAdapter);

        mProgressDialog.cancel();
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
