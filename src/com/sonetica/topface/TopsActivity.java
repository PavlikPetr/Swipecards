package com.sonetica.topface;

import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;

/*
 * Класс активити для просмотра топ списка пользователей
 */
public class TopsActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Utils.log(this,"+onCreate");
    
    // Title
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.header_tops_title));
    
    // Gallary
    GridView gallary = (GridView)findViewById(R.id.grdTopsGallary);
    gallary.setAdapter(new TopsGridLayout(this));
    //gallary.set
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
}
