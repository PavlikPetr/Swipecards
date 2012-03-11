package com.sonetica.topface.ui.tops;

import com.sonetica.topface.Global;
import com.sonetica.topface.R;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.net.*;
import com.sonetica.topface.ui.DoubleButton;
import com.sonetica.topface.ui.GalleryManager;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import java.util.LinkedList;

/*  
 *  Класс активити для просмотра списка топ пользователей   "топы"
 */
public class TopsActivity extends Activity {
  // Data
  private GridView mGallery;
  private TopsGridAdapter mGridAdapter;
  private GalleryManager<TopUser>  mGalleryManager;
  private LinkedList<TopUser> mTopsList;
  private LinkedList<City> mCitiesList;
  private ProgressDialog  mProgressDialog;
  private Button mCityButton;
  // Action Data
  private class ActionData {
    public int sex; 
    public int city_id;
    public int city_popup_position;
    public String city_name;
  }
  private ActionData mActionData;
  // Constats
  private static int GIRLS  = 0;
  private static int BOYS   = 1;
  private static int MOSCOW = 1;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Debug.log(this,"+onCreate");
    
    // Восстановление последних параметров
    mActionData = new ActionData();
    SharedPreferences preferences = getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    mActionData.sex       = preferences.getInt(getString(R.string.s_tops_sex),GIRLS);
    mActionData.city_id   = preferences.getInt(getString(R.string.s_tops_city_id),MOSCOW);
    mActionData.city_name = preferences.getString(getString(R.string.s_tops_city_name),getString(R.string.default_city));
    mActionData.city_popup_position = preferences.getInt(getString(R.string.s_tops_city_position),0);
    
    // Data
    mTopsList   = new LinkedList<TopUser>();
    mCitiesList = new LinkedList<City>();

    // Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.tops_header_title));
    
    // Double Button
    DoubleButton btnDouble = (DoubleButton)findViewById(R.id.btnDoubleTops);
    btnDouble.setLeftText(getString(R.string.tops_btn_boys));
    btnDouble.setRightText(getString(R.string.tops_btn_girls));
    btnDouble.setChecked(mActionData.sex==0?DoubleButton.RIGHT_BUTTON:DoubleButton.LEFT_BUTTON);
    // BOYS
    btnDouble.setLeftListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mActionData.sex = BOYS;
        update();
      }
    });
    // GIRLS
    btnDouble.setRightListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mActionData.sex = GIRLS;
        update();
      }
    });
    
    // City Button
    mCityButton = (Button)findViewById(R.id.btnTopsBarCity);
    mCityButton.setText(mActionData.city_name);
    mCityButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        choiceCity();
      }
    });
    
    // Gallery
    mGallery = (GridView)findViewById(R.id.grdTopsGallary);
    mGallery.setAnimationCacheEnabled(false);
    mGallery.setScrollingCacheEnabled(false);
    mGallery.setNumColumns(getResources().getInteger(R.integer.grid_column_number));
    /*
    mGallery.setOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view,int scrollState) {
        if(scrollState==SCROLL_STATE_IDLE) {
          mGalleryManager.mRunning = true;
          mGallery.invalidateViews();
        } else
          mGalleryManager.mRunning = false;
      }
      @Override
      public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
      }
    });
    */
    mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //mGalleryManager.stop();
        Intent intent = new Intent(TopsActivity.this.getApplicationContext(),ProfileActivity.class);
        intent.putExtra(ProfileActivity.INTENT_USER_ID,mTopsList.get(position).uid);
        startActivityForResult(intent,0);
      }
    });
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    if(mTopsList.size()==0)
      update();
    else
      create();
  }
  //---------------------------------------------------------------------------
  private void create() {
    mGalleryManager = new GalleryManager<TopUser>(getApplicationContext(),mTopsList);
    mGridAdapter    = new TopsGridAdapter(getApplicationContext(),mGalleryManager);
    mGallery.setAdapter(mGridAdapter);
    mGallery.setOnScrollListener(mGalleryManager);
    
    //mGallery.setOnScrollListener(mGridAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mGalleryManager!=null) {
      mGalleryManager.release();
      mGalleryManager=null;
    }
    
    if(mGallery!=null)        mGallery=null;
    if(mGridAdapter!=null)    mGridAdapter=null;
    if(mTopsList!=null)       mTopsList=null;
    if(mProgressDialog!=null) mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
  private void update() { // refreshed - грузить локально или инет при первом запуске
    mProgressDialog.show();
    TopsRequest topRequest = new TopsRequest(getApplicationContext());
    topRequest.sex  = mActionData.sex;
    topRequest.city = mActionData.city_id;
    topRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        if(TopsActivity.this==null)
          return;
        mTopsList.clear();
        mTopsList.addAll(TopUser.parse(response));
        create();
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,Response response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void choiceCity() {
    if(mCitiesList.size()!=0) {
      showCitiesDialog();
      return;
    }
    mProgressDialog.show();
    CitiesRequest citiesRequest = new CitiesRequest(getApplicationContext());
    citiesRequest.type = "top";
    citiesRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        if(TopsActivity.this==null)
          return;
        mCitiesList.addAll(City.parse(response));
        mProgressDialog.cancel();
        showCitiesDialog();
      }
      @Override
      public void fail(int codeError,Response response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  void showCitiesDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Chooser");
    String[] cities = new String[mCitiesList.size()];
    for(int i=0;i<mCitiesList.size();++i)
      cities[i] = mCitiesList.get(i).name;
    builder.setSingleChoiceItems(cities,mActionData.city_popup_position,new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int position) {
        City city = mCitiesList.get(position);
        if(mActionData.city_id!=city.id) {
          mActionData.city_id = city.id;
          mActionData.city_name = city.name;
          mActionData.city_popup_position = position;
          mCityButton.setText(mActionData.city_name);
          update();
        }
        dialog.cancel();
      }
    });
    AlertDialog alert = builder.create();
    alert.show();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    //mGalleryManager.restart();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    // Сохранение параметров
    SharedPreferences preferences = getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putInt(getString(R.string.s_tops_sex),mActionData.sex);
    editor.putInt(getString(R.string.s_tops_city_id),mActionData.city_id);
    editor.putString(getString(R.string.s_tops_city_name),mActionData.city_name);
    editor.putInt(getString(R.string.s_tops_city_position),mActionData.city_popup_position);
    editor.commit();
    
    release();
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
