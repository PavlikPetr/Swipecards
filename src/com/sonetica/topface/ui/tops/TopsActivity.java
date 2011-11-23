package com.sonetica.topface.ui.tops;

import com.sonetica.topface.App;
import com.sonetica.topface.R;
import com.sonetica.topface.data.User;
import com.sonetica.topface.utils.GalleryManager;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*  
 *  Класс активити для просмотра списка топ пользователей   "топы"
 */
public class TopsActivity extends Activity {
  // Data
  private int mSexType;  // пол людей
  private int mCityType; // выбранный город
  private GridView mGallary;
  private TopsGridAdapter mGridAdapter;
  private GalleryManager mGallaryManager;
  private ProgressDialog mProgressDialog;
  private SharedPreferences mPreferences;
  private ArrayList<User> mUserList = new ArrayList<User>();
  // Constants
  private static final int TOP_GIRLS = 0;
  private static final int TOP_BOYS  = 1;
  String url = "http://www.mssoft.org/data/200.json";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Utils.log(this,"+onCreate");
       
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.tops_header_title));
    
    // Восстанавливаем состояние с прошлого посещения
    mPreferences = getSharedPreferences(App.TAG, 0);
    mSexType  = mPreferences.getInt(getString(R.string.tops_prefs_sex),TOP_GIRLS);
    mCityType = mPreferences.getInt(getString(R.string.tops_prefs_city),0);
    
    // Boys Button
    Button btnBoys = (Button)findViewById(R.id.btnBarBoys);
    btnBoys.setText(getString(R.string.tops_btn_boys));
    btnBoys.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        update("param");
      }
    });
    
    // Girls Button
    Button btnGirls = (Button)findViewById(R.id.btnBarGirls);
    btnGirls.setText(getString(R.string.tops_btn_girls));
    btnGirls.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        update("param");
      }
    });
    
    // City Button
    ((Button)findViewById(R.id.btnBarCity)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(TopsActivity.this,"city",Toast.LENGTH_SHORT).show();
      }
    });
    
    // Start progress dialog    
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    // Bitmat manager
    mGallaryManager = new GalleryManager(this,"tops",mUserList);
    
    // Gallary
    mGridAdapter = new TopsGridAdapter(this,mGallaryManager);
    mGallary = (GridView)findViewById(R.id.grdTopsGallary);
    mGallary.setAdapter(mGridAdapter);
    mGallary.setNumColumns(4);
    mGallary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(TopsActivity.this,RateitActivity.class);
        //Intent intent = new Intent(TopsActivity.this,TestActivity.class);
        intent.putExtra(RateitActivity.INTENT_USER_ID,mUserList.get(position).name);
        startActivity(intent);
      }
    });
    
    // Качаем и отдаем лист линков менеджеру
    update("null"/* параметры запроса: мальчики - девочки - город */);
  }
  //---------------------------------------------------------------------------
  /*
   * Запрос списка линков на изображения с сервера (параметры запроса  ???)
   * список линков закачивается каждый раз при открытии активити (на короткий период кешировать !!!)
   */
  private void update(String params) {
    new UsersLoaderTask().execute(params);
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    mGallaryManager.stop();
    mGallaryManager.release();
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
  // class UsersLoaderTask
  private class UsersLoaderTask extends AsyncTask<String, Void, Boolean> {
    //---------------------------------
    @Override
    protected void onPreExecute(){
      mGallaryManager.stop();
      mProgressDialog.show();
      mUserList = new ArrayList<User>();
    }
    //---------------------------------
    // @params параметры для получения списка линков
    @Override
    protected Boolean doInBackground(String... params) {
      // получить массив ссылок на изображения с сервера
      String s = null;
      try {
        s = Http.httpGetRequest(url);
      } catch(Exception ex) { ex.printStackTrace(); } 
        finally { if(s == null) return false; }
      
      JSONObject obj = null;
      JSONArray  arr = null;
      try {
        obj = new JSONObject(s);
        arr = new JSONArray(obj.getString("covers"));
        for(int i=0; i<arr.length(); ++i) {
          JSONObject o = (JSONObject)arr.get(i);
          User user = new User();
          user.link = o.getString("cover");
          user.name = o.getString("artist");
          mUserList.add(user);
        }
      } catch(JSONException e) {
        e.printStackTrace();
      }
      return true;
    }
    //---------------------------------
    @Override
    protected void onPostExecute(Boolean result) {
      if(result == false)
        return;
      mGallaryManager.restart(mUserList);
      mGridAdapter.notifyDataSetChanged();
      mProgressDialog.cancel();
    }
  }// UsersLoaderTask
  //---------------------------------------------------------------------------
}// TopsActivity

// Toast.makeText(TopsActivity.this,"girls",Toast.LENGTH_SHORT).show();