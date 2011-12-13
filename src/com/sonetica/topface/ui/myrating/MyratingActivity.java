package com.sonetica.topface.ui.myrating;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/*
 *          "меня оценили"
 */
public class MyratingActivity extends Activity {
  // Data
  private ListView mListView;
  private ArrayAdapter mAdapter;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_myrating);
    Debug.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.myrating_header_title));
   
   ArrayList<String> list = new ArrayList<String>();
   for(int i=40;i>=0;--i)
     list.add("two: "+i);
   
   // Adapter
   mAdapter = new MyratingListAdapter(this,list);
   
   // ListView
   mListView = (ListView)findViewById(R.id.lvMyratingList);
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
