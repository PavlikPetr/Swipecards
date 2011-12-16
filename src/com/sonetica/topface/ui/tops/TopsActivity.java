package com.sonetica.topface.ui.tops;

import com.sonetica.topface.App;
import com.sonetica.topface.R;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.net.*;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.GalleryCachedManager;
import com.sonetica.topface.utils.IFrame;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;

/*  
 *  Класс активити для просмотра списка топ пользователей   "топы"
 */
public class TopsActivity extends Activity {
  // Data
  private GridView mGallery;
  private TopsGridAdapter mGridAdapter;
  private ProgressDialog  mProgressDialog;
  private GalleryCachedManager mGalleryCachedManager;
  private ArrayList<TopUser> mUserList;
  private Button mCityButton;
  private int m_sex  = 0;
  private int m_city = 0;
  private int m_city_curr = 0;
  // Constats
  private static int GIRLS = 0;
  private static int BOYS  = 1;
  private static int PITER = 2;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Debug.log(this,"+onCreate");

    // Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.tops_header_title));
    
    // Girl Button
    Button btnGirls = (Button)findViewById(R.id.btnBarGirls);
    btnGirls.setText(getString(R.string.tops_btn_girls));
    btnGirls.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        m_city = PITER;
        update(m_sex=GIRLS,m_city,true);
      }
    });
    
    // Boy Button
    Button btnBoys  = (Button)findViewById(R.id.btnBarBoys);
    btnBoys.setText(getString(R.string.tops_btn_boys));
    btnBoys.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        m_city = PITER;
        update(m_sex=BOYS,m_city,true);
      }
    });
    
    // City Button
    mCityButton = (Button)findViewById(R.id.btnBarCity);
    mCityButton.setText(getString(R.string.tops_btn_city));
    mCityButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getCities();
      }
    });
    
    // Gallery
    mGallery = (GridView)findViewById(R.id.grdTopsGallary);
    mGallery.setAnimationCacheEnabled(false);
    mGallery.setScrollingCacheEnabled(false);
    mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(TopsActivity.this,RateitActivity.class);
        intent.putExtra(RateitActivity.INTENT_USER_ID,mUserList.get(position).uid);
        startActivity(intent);
      }
    });
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    // восстановление предыдущих параметров
    SharedPreferences preferences = getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    m_sex  = preferences.getInt(getString(R.string.s_tops_sex), GIRLS);
    m_city = preferences.getInt(getString(R.string.s_tops_city),PITER);
   
    update(m_sex,m_city,false);
  }
  //---------------------------------------------------------------------------
  private void update(int sex, int city, boolean isRefreshed) { // refreshed - грузить локально или инет при первом запуске
    TopsRequest topRequest = new TopsRequest();
    topRequest.sex  = sex;
    topRequest.city = city;
    ConnectionService.sendRequest(topRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        mUserList = resp.getUsers(); 
        if(mUserList != null) {
          mGalleryCachedManager = new GalleryCachedManager(TopsActivity.this,IFrame.TOPS,mUserList);
          mGridAdapter = new TopsGridAdapter(TopsActivity.this,mGalleryCachedManager);
          mGallery.setAdapter(mGridAdapter);
        }
        mProgressDialog.cancel();
      }
    });
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    // сохранение параметров
    SharedPreferences preferences = getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putInt(getString(R.string.s_tops_sex), m_sex);
    editor.putInt(getString(R.string.s_tops_city),m_city);
    editor.commit();
    
    if(mGalleryCachedManager!=null) {
      mGalleryCachedManager.release();
      mGalleryCachedManager=null;
    }
    
    if(mGridAdapter!=null)    mGridAdapter=null;
    if(mGallery!=null)        mGallery=null;
    if(mProgressDialog!=null) mProgressDialog=null;
    if(mUserList!=null)       mUserList=null;
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void getCities() {
    mProgressDialog.show();
    CitiesRequest citiesRequest = new CitiesRequest();
    citiesRequest.type = "top";
    ConnectionService.sendRequest(citiesRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        final ArrayList<City> cities = resp.getCities();
        String[] arr = new String[cities.size()];
        for(int i=0;i<arr.length;i++)
          arr[i] = cities.get(i).name;
        
        mProgressDialog.cancel();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(TopsActivity.this);
        builder.setTitle("Chooser");
        builder.setSingleChoiceItems(arr, m_city_curr, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int index) {
            m_city = Integer.parseInt(cities.get(index).id);
            m_city_curr = index;
            mCityButton.setText(cities.get(index).name);
            update(m_sex,m_city,true);
            dialog.cancel();
          }
        });
        AlertDialog alert = builder.create();
        alert.show();
      }
    });
  }
  //---------------------------------------------------------------------------
}
