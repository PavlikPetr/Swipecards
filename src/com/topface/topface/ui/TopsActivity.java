package com.topface.topface.ui;

import java.util.LinkedList;

import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.Top;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.TopRequest;
import com.topface.topface.ui.adapters.TopsGridAdapter;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.DoubleButton;
import com.topface.topface.ui.views.ThumbView;
import com.topface.topface.utils.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TopsActivity extends TrackedActivity {
  // Data
  private GridView mGallery;
  private TopsGridAdapter mGridAdapter;
  private GalleryGridManager<Top> mGalleryGridManager;
  private LinkedList<Top> mTopsList;
  private Button mCityButton;
  private ProgressBar mProgressBar;
  private ActionData mActionData;
  private TopRequest topRequest;
  private CitiesRequest citiesRequest;
  // Constats
  private static int GIRLS = 0;
  private static int BOYS = 1;
    private FloatBlock mFloatBlock;

    //---------------------------------------------------------------------------
  // class Action Data
  //---------------------------------------------------------------------------
  private class ActionData {
    public int sex; 
    public int city_id;
    public int city_popup_pos;
    public String city_name;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Debug.log(this,"+onCreate");
    
    // Data
    mTopsList = new LinkedList<Top>();

    // Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.tops_header_title));
    
    // Progress
    mProgressBar = (ProgressBar)findViewById(R.id.prsTopsLoading);

    mActionData = new ActionData();
    SharedPreferences preferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
    mActionData.sex = preferences.getInt(Static.PREFERENCES_TOPS_SEX, GIRLS);
    mActionData.city_id = preferences.getInt(Static.PREFERENCES_TOPS_CITY_ID, CacheProfile.city_id);
    mActionData.city_name = preferences.getString(Static.PREFERENCES_TOPS_CITY_NAME, CacheProfile.city_name);
    mActionData.city_popup_pos = preferences.getInt(Static.PREFERENCES_TOPS_CITY_POS, -1);

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
    mGallery.setNumColumns(Data.GRID_COLUMN);
    mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(TopsActivity.this.getApplicationContext(),ProfileActivity.class);
        intent.putExtra(ProfileActivity.INTENT_USER_ID,mTopsList.get(position).uid);
        startActivityForResult(intent,0);
      }
    });
    
    // Control creating
    mGalleryGridManager = new GalleryGridManager<Top>(getApplicationContext(),mTopsList);
    mGridAdapter = new TopsGridAdapter(getApplicationContext(),mGalleryGridManager);
    mGallery.setAdapter(mGridAdapter);

    mFloatBlock = new FloatBlock(this);
    update();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
    super.onStop();
  }

    @Override
    protected void onResume() {
        super.onResume();
        mFloatBlock.update();
    }

    //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    SharedPreferences preferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putInt(Static.PREFERENCES_TOPS_SEX, mActionData.sex);
    editor.putInt(Static.PREFERENCES_TOPS_CITY_ID, mActionData.city_id);
    editor.putString(Static.PREFERENCES_TOPS_CITY_NAME, mActionData.city_name);
    editor.putInt(Static.PREFERENCES_TOPS_CITY_POS, mActionData.city_popup_pos);
    editor.commit();
    
    if(topRequest!=null) topRequest.cancel();
    if(citiesRequest!=null) citiesRequest.cancel();
    
    release();
    ThumbView.release();
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void update() {
    mProgressBar.setVisibility(View.VISIBLE);
    mGallery.setSelection(0);
    
    topRequest = new TopRequest(getApplicationContext());
    topRequest.sex  = mActionData.sex;
    topRequest.city = mActionData.city_id;
    topRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        mTopsList.clear();
        mTopsList.addAll(Top.parse(response));
        post(new Runnable() {
          @Override
          public void run() {
            mProgressBar.setVisibility(View.GONE);
            mGridAdapter.notifyDataSetChanged();
            mGalleryGridManager.update();
          }
        });
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        post(new Runnable() {
          @Override
          public void run() {
            Utils.showErrorMessage(TopsActivity.this);
            mProgressBar.setVisibility(View.GONE);
          }
        });
      }
    }).exec();
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
  }
  //---------------------------------------------------------------------------
  private void choiceCity() {
    if(Data.cityList!=null && Data.cityList.size()>0) {
      showCitiesDialog();
      return;
    }
    mProgressBar.setVisibility(View.VISIBLE);
    citiesRequest = new CitiesRequest(getApplicationContext());
    citiesRequest.type = "top";
    citiesRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Data.cityList = City.parse(response);
        post(new Runnable() {
          @Override
          public void run() {
            mProgressBar.setVisibility(View.GONE);
            showCitiesDialog();
          }
        });
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        post(new Runnable() {
          @Override
          public void run() {
            Utils.showErrorMessage(TopsActivity.this);
            mProgressBar.setVisibility(View.GONE);
          }
        });
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void showCitiesDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.filter_select_city));
    int arraySize = Data.cityList.size();
    String[] cities = new String[arraySize];
    for(int i=0; i<arraySize; ++i)
      cities[i] = Data.cityList.get(i).name;
    builder.setSingleChoiceItems(cities,mActionData.city_popup_pos,new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int position) {
        City city = Data.cityList.get(position);
        if(mActionData.city_id != city.id) {
          mActionData.city_id = city.id;
          mActionData.city_name = city.name;
          mActionData.city_popup_pos = position;
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
}