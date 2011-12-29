package com.sonetica.topface.ui.tops;

import com.sonetica.topface.App;
import com.sonetica.topface.R;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.net.*;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.ui.GalleryManager;
import com.sonetica.topface.ui.album.AlbumActivity;
import com.sonetica.topface.utils.Debug;
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
  private GalleryManager  mGalleryManager;
  private ArrayList<TopUser> mUserList;
  private Button mCityButton;
  private String m_city_name;
  private int m_city_position;
  private int m_city_id;
  private int m_sex;
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
    
    // восстановление предыдущих параметров
    SharedPreferences preferences = getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    m_sex  = preferences.getInt(getString(R.string.s_tops_sex), GIRLS);
    m_city_id = preferences.getInt(getString(R.string.s_tops_city),PITER);
    m_city_position = preferences.getInt(getString(R.string.s_tops_city_position),0);
    m_city_name = preferences.getString(getString(R.string.s_tops_city_name),getString(R.string.default_city));
    
    // Girl Button
    Button btnGirls = (Button)findViewById(R.id.btnBarGirls);
    btnGirls.setText(getString(R.string.tops_btn_girls));
    btnGirls.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        m_city_id = PITER;
        update(m_sex=GIRLS,m_city_id,true);
      }
    });
    
    // Boy Button
    Button btnBoys  = (Button)findViewById(R.id.btnBarBoys);
    btnBoys.setText(getString(R.string.tops_btn_boys));
    btnBoys.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        m_city_id = PITER;
        update(m_sex=BOYS,m_city_id,true);
      }
    });
    
    // City Button
    mCityButton = (Button)findViewById(R.id.btnBarCity);
    mCityButton.setText(m_city_name);
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
        Intent intent = new Intent(TopsActivity.this,AlbumActivity.class);
        intent.putExtra(AlbumActivity.INTENT_USER_ID,mUserList.get(position).uid);
        startActivity(intent);
      }
    });
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    update(m_sex,m_city_id,false);
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
          mGalleryManager = new GalleryManager(TopsActivity.this,mUserList);
          mGridAdapter    = new TopsGridAdapter(TopsActivity.this,mGalleryManager);
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
    editor.putInt(getString(R.string.s_tops_city),m_city_id);
    editor.putInt(getString(R.string.s_tops_city_position),m_city_position);
    editor.putString(getString(R.string.s_tops_city_name),m_city_name);
    editor.commit();
    
    if(mGalleryManager!=null) {
      mGalleryManager.release();
      mGalleryManager=null;
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
        builder.setSingleChoiceItems(arr,m_city_position,new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int index) {
            int city = Integer.parseInt(cities.get(index).id);
            if(m_city_id!=city) {
              m_city_position = index;
              m_city_id = city;
              m_city_name = cities.get(index).name;
              mCityButton.setText(m_city_name);
              update(m_sex,m_city_id,true);
            }
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
