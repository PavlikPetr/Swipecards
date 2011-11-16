package com.sonetica.topface.ui.tops;

import com.sonetica.topface.App;
import com.sonetica.topface.R;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*  
 *  Класс активити для просмотра топ списка пользователей
 */
public class TopsActivity extends Activity {
  // Data
  private int mSexType;  // нажатая кнопка - девушки/парни
  private int mCityType; // сохраненный город
  private GridView mGallary;
  private SharedPreferences mPreferences;
  private TopsGridAdapter mGridAdapter;
  private ProgressDialog mProgressDialog;
  private ArrayList<String> mUrlList = new ArrayList<String>(); // Список линков на изображения
  // Constants
  private static final int TOP_GIRLS = 0;
  private static final int TOP_BOYS  = 1;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Utils.log(this,"+onCreate");
       
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.tops_header_title));
    
    // Boys Button
    Button btnBoys = (Button)findViewById(R.id.btnBarBoys);
    btnBoys.setText(R.string.tops_btn_boys);
    btnBoys.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(TopsActivity.this,"boys",Toast.LENGTH_SHORT).show();
      }
    });
    
    // Girls Button
    Button btnGirls = (Button)findViewById(R.id.btnBarGirls);
    btnGirls.setText(R.string.tops_btn_girls);
    btnGirls.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(TopsActivity.this,"girls",Toast.LENGTH_SHORT).show();
      }
    });
    
    // City Button
    ((Button)findViewById(R.id.btnBarCity)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(TopsActivity.this,"city",Toast.LENGTH_SHORT).show();
      }
    });
    
    // Достаем сохраненные состояния кнопок
    // local final
    mPreferences = getSharedPreferences(App.TAG, 0);
    mSexType  = mPreferences.getInt(getString(R.string.tops_prefs_sex),TOP_GIRLS);
    mCityType = mPreferences.getInt(getString(R.string.tops_prefs_city),0);
    
    // Start progress dialog    
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage("Loading...");
    
    // Gallary
    mGridAdapter = new TopsGridAdapter(this,mUrlList);
    mGallary = (GridView)findViewById(R.id.grdTopsGallary);
    mGallary.setAdapter(mGridAdapter);

    //заполняем лист линками на изображения
    fillUrlList(/* параметры запроса: мальчики - девочки - город */);
  }
  //---------------------------------------------------------------------------
  /*
   * Запрос списка линков на изображения с сервера (параметры запроса  ???)
   * список линков закачивается каждый раз
   */
  private void fillUrlList(/*params*/) {
    new LinkLoaderTask().execute("params");
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
  // class LinkLoaderTask
  private class LinkLoaderTask extends AsyncTask<String, Void, Void> {
    @Override
    protected void onPreExecute(){
      mProgressDialog.show();
    }
    // @params параметры для получения списка линков
    @Override
    protected Void doInBackground(String... params) {
      // получить массив ссылок на изображения с сервера
      String s = Http.httpGetRequest("http://www.chrisboyd.net/wp-content/uploads/2011/10/albums.json");
      JSONObject obj = null;
      JSONArray  arr = null;
      try {
        obj = new JSONObject(s);
        arr = new JSONArray(obj.getString("covers"));
        
        for(int i=0; i<arr.length(); i++) {
          JSONObject o = (JSONObject)arr.get(i);
          mUrlList.add(o.getString("cover"));
          Utils.log(null,"" + i);
        }
        
      } catch(JSONException e) {
        e.printStackTrace();
      }
      return null;
    }
    @Override
    protected void onPostExecute(Void result) {
      mProgressDialog.cancel();
      mGridAdapter.notifyDataSetChanged();
    }
  }// LinkLoaderTask
}// TopsActivity
