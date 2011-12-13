package com.sonetica.topface.ui.chat;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.R.id;
import com.sonetica.topface.R.layout;
import com.sonetica.topface.R.string;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
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
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_chat);
    Debug.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.chat_header_title));

   ArrayList<String> list = new ArrayList<String>();
   for(int i=40;i>=0;--i)
     list.add("one: "+i);
   
   // Adapter
   mAdapter = new ChatListAdapter(this,list);
   
   // ListView
   mListView = (ListView)findViewById(R.id.lvChatList);
   mListView.setAdapter(mAdapter);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
