package com.sonetica.topface.ui.tops;

import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.R;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.net.*;
import com.sonetica.topface.ui.DoubleButton;
import com.sonetica.topface.ui.GalleryGridManager;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
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
  private GalleryGridManager<TopUser>  mGalleryGridManager;
  private LinkedList<TopUser> mTopsList;
  private ProgressDialog  mProgressDialog;
  private Button mCityButton;
  // class Action Data
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
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Восстановление последних параметров
    mActionData = new ActionData();
    SharedPreferences preferences = getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    mActionData.sex       = preferences.getInt(getString(R.string.s_tops_sex),GIRLS);
    mActionData.city_id   = preferences.getInt(getString(R.string.s_tops_city_id),MOSCOW);
    mActionData.city_name = preferences.getString(getString(R.string.s_tops_city_name),getString(R.string.default_city));
    mActionData.city_popup_position = preferences.getInt(getString(R.string.s_tops_city_position),-1);
    
    // Data
    mTopsList = new LinkedList<TopUser>();

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
    mGallery.setNumColumns(Data.s_gridColumn);
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
    
    create();
    update();
  }
  //---------------------------------------------------------------------------
  private void update() {
    mProgressDialog.show();
    
    mGallery.setSelection(0);
    
    TopsRequest topRequest = new TopsRequest(getApplicationContext());
    topRequest.sex  = mActionData.sex;
    topRequest.city = mActionData.city_id;
    topRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        mTopsList.clear();
        mTopsList = TopUser.parse(response);
        mGalleryGridManager.setDataList(mTopsList);
        mGridAdapter.notifyDataSetChanged();
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,Response response) {
        mProgressDialog.cancel();
        //update();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void create() {
    mGalleryGridManager = new GalleryGridManager<TopUser>(getApplicationContext(),mTopsList);
    mGridAdapter = new TopsGridAdapter(getApplicationContext(),mGalleryGridManager);
    mGallery.setAdapter(mGridAdapter);
    mGallery.setOnScrollListener(mGalleryGridManager);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mGalleryGridManager != null) {
      mGalleryGridManager.release();
      mGalleryGridManager = null;
    }
    
    mGallery = null;
    mGridAdapter = null;
    mTopsList = null;
    mProgressDialog = null;
  }
  //---------------------------------------------------------------------------
  private void choiceCity() {
    if(Data.s_CitiesList != null && Data.s_CitiesList.size() > 0) {
      showCitiesDialog();
      return;
    }
    mProgressDialog.show();
    CitiesRequest citiesRequest = new CitiesRequest(getApplicationContext());
    citiesRequest.type = "top";
    citiesRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        Data.s_CitiesList = City.parse(response);
        mProgressDialog.cancel();
        showCitiesDialog();
      }
      @Override
      public void fail(int codeError,Response response) {
        mProgressDialog.cancel();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  void showCitiesDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.filter_select_city));
    int arraySize = Data.s_CitiesList.size();
    String[] cities = new String[arraySize];
    for(int i=0; i<arraySize; ++i)
      cities[i] = Data.s_CitiesList.get(i).name;
    builder.setSingleChoiceItems(cities,mActionData.city_popup_position,new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int position) {
        City city = Data.s_CitiesList.get(position);
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
