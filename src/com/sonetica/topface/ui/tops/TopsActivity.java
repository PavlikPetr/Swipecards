package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.R.id;
import com.sonetica.topface.R.layout;
import com.sonetica.topface.R.string;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.concurrent.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*  
 * Класс активити для просмотра топ списка пользователей
 */
public class TopsActivity extends Activity {
  // Data
  private ProgressDialog mProgressDialog;
  GridView gallary;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Utils.log(this,"+onCreate");
    
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage("Loading...");
    mProgressDialog.show();
       
    // Title
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.header_tops_title));

    final ArrayList<String> urls = new ArrayList<String>();
    
    // Проверить есть ли объекты для отображения в кеше
    // Объект кеш weak soft references
    new Thread(new Runnable() {
      @Override
      public void run() {
        // получить массив ссылок на изображения с сервера
        String s = Http.httpGetRequest("http://www.chrisboyd.net/wp-content/uploads/2011/10/albums.json");
        JSONObject obj = null;
        JSONArray  arr = null;
        try {
          obj = new JSONObject(s);
          arr = new JSONArray(obj.getString("covers"));
          
          for(int i=0; i<arr.length(); i++) {
            JSONObject o = (JSONObject)arr.get(i);
            urls.add(o.getString("cover"));
            Utils.log(null,"" + i);
          }
          
        } catch(JSONException e) {
          e.printStackTrace();
        } 
        mTopsHandler.sendEmptyMessage(0);
        Utils.log(this,"size:" + urls.size());
      }
    }).start();
 
    // Gallary
    gallary = (GridView)findViewById(R.id.grdTopsGallary);
    gallary.setAdapter(new TopsGridLayout(this,urls));
    
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
  // class TopsHandler
  TopsHandler mTopsHandler = new TopsHandler();
  public class TopsHandler extends Handler {
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if(mProgressDialog!=null)
        mProgressDialog.cancel();
      ((TopsGridLayout)gallary.getAdapter()).notifyDataSetChanged();
    }
  }
  //---------------------------------------------------------------------------
}
