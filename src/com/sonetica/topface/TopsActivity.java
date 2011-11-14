package com.sonetica.topface;

import com.sonetica.topface.net.Http;
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
    
    // получить массив ссылок на изображения с сервера
    String s = Http.httpGetRequest("http://www.chrisboyd.net/wp-content/uploads/2011/10/albums.json");
    
    // Gallary
    GridView gallary = (GridView)findViewById(R.id.grdTopsGallary);
    gallary.setAdapter(new TopsGridLayout(this));
    
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
}
