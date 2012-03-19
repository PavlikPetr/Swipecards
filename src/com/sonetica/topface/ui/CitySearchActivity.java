package com.sonetica.topface.ui;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.City;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.CitiesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.net.SearchCitiesRequest;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class CitySearchActivity extends Activity {
  // Data
  private EditText mCityInputView;
  private ListView mCityListView;
  private ArrayAdapter<String> mListAdapter;
  private LinkedList<City> mTopCitiesList;
  private LinkedList<City> mDataList;
  private LinkedList<String> mNameList;
  private ProgressDialog  mProgressDialog;
  // Constants
  public static final int INTENT_CITY_SEARCH_ACTIVITY = 100;
  public static final String INTENT_CITY_ID   = "city_id";
  public static final String INTENT_CITY_NAME = "city_name";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.ac_city);
    Debug.log(this,"+onCreate");
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.filter_city));
    
    // Data
    mDataList = new LinkedList<City>();
    mNameList = new LinkedList<String>();
    
    // ListAdapter
    mListAdapter   = new ArrayAdapter<String>(this,
                     android.R.layout.simple_list_item_1, 
                     android.R.id.text1, 
                     mNameList);
    
    // ListView
    mCityListView  = (ListView)findViewById(R.id.lvCityList);
    mCityListView.setAdapter(mListAdapter);
    
    // возврат значения и выход
    mCityListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0,View arg1,int position,long arg3) {
        Intent intent = CitySearchActivity.this.getIntent();
        intent.putExtra(INTENT_CITY_ID,   mDataList.get(position).id);
        intent.putExtra(INTENT_CITY_NAME, mDataList.get(position).name);
        CitySearchActivity.this.setResult(RESULT_OK, intent);
        CitySearchActivity.this.finish();
      }
    });

    // EditText
    mCityInputView = (EditText)findViewById(R.id.etCityInput);
    mCityInputView.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s,int start,int count,int after) {
      }
      @Override
      public void onTextChanged(CharSequence s,int start,int before,int count) {
        if(s.length()>2)
          city(s.toString());
        else
          fillData(mTopCitiesList);
      }
      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    
    /*
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, 
        android.R.id.text1, 
        strings);
    */
    /*
        SimpleAdapter adapter = new SimpleAdapter(this, 
        createSensorsList(), 
        android.R.layout.simple_list_item_2, 
        new String[] {"title", "vendor"}, 
        new int[] {android.R.id.text1, android.R.id.text2});
    */
    /*
        SimpleAdapter adapter = new SimpleAdapter(this, 
        createSensorsList(), 
        R.layout.sensor_layout, 
        new String[] {"title", "vendor", "power"}, 
        new int[] {R.id.title, R.id.content, R.id.range});
    */
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    update();
  }
  //---------------------------------------------------------------------------
  public void update() {
    mProgressDialog.show();
    CitiesRequest citiesRequest = new CitiesRequest(this);
    citiesRequest.type = "top";
    citiesRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        mTopCitiesList = City.parse(response);
        fillData(mTopCitiesList);
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,Response response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void city(String prefix) {
    SearchCitiesRequest searchCitiesRequest = new SearchCitiesRequest(this);
    searchCitiesRequest.prefix = prefix;
    searchCitiesRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<City> citiesList = City.parse(response);
        fillData(citiesList);
      }
      @Override
      public void fail(int codeError,Response response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void fillData(LinkedList<City> citiesList) {
    mDataList.clear();
    mDataList.addAll(citiesList);
    mNameList.clear();
    for(City city : mDataList)
      mNameList.add(city.full);
    
    mListAdapter.notifyDataSetChanged();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
