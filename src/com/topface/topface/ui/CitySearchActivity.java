package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.SearchCitiesRequest;
import com.topface.topface.utils.Debug;

import java.util.LinkedList;

public class CitySearchActivity extends BaseFragmentActivity {
    // Data
    private EditText mCityInputView;
    private ListView mCityListView;
    private ArrayAdapter<String> mListAdapter;
    private LinkedList<City> mTopCitiesList;
    private LinkedList<City> mDataList;
    private LinkedList<String> mNameList;
    private ProgressBar mProgressBar;
    private CitiesRequest citiesRequest;
    private SearchCitiesRequest searchCitiesRequest;
    // Constants
    public static final int INTENT_CITY_SEARCH_ACTIVITY = 100;
    public static final String INTENT_CITY_ID = "city_id";
    public static final String INTENT_CITY_NAME = "city_name";
    public static final String INTENT_CITY_FULL_NAME = "city_full";
    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ac_city);
        Debug.log(this, "+onCreate");

        // Data
        mTopCitiesList = new LinkedList<City>();
        mDataList = new LinkedList<City>();
        mNameList = new LinkedList<String>();

        // Title Header
        ((TextView)findViewById(R.id.tvNavigationTitle)).setText(getString(R.string.filter_city));

        // Progress
        mProgressBar = (ProgressBar)findViewById(R.id.prsCityLoading);

        // ListAdapter
        mListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mNameList);

        // ListView
        mCityListView = (ListView)findViewById(R.id.lvCityList);
        mCityListView.setAdapter(mListAdapter);

        // возврат значения и выход
        mCityListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0,View arg1,int position,long arg3) {
                Intent intent = CitySearchActivity.this.getIntent();
                intent.putExtra(INTENT_CITY_ID, mDataList.get(position).id);
                intent.putExtra(INTENT_CITY_NAME, mDataList.get(position).name);
                intent.putExtra(INTENT_CITY_FULL_NAME, mDataList.get(position).full);

                SharedPreferences.Editor editor = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE).edit();
                editor.putInt(Static.PREFERENCES_TOPS_CITY_ID, mDataList.get(position).id);
                editor.putString(Static.PREFERENCES_TOPS_CITY_NAME, mDataList.get(position).name);
                editor.putInt(Static.PREFERENCES_TOPS_CITY_POS, -1);
                editor.commit();
                
                Debug.log(CitySearchActivity.this, "1.city_id:" + mDataList.get(position).id);

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
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                if (s.length() > 2)
                    city(s.toString());
                else {
                    fillData(mTopCitiesList);
                    mListAdapter.notifyDataSetChanged();
                }
            }
        });

        update();
    }
    //---------------------------------------------------------------------------
    private void update() {
        mProgressBar.setVisibility(View.VISIBLE);

        citiesRequest = new CitiesRequest(this);
        registerRequest(citiesRequest);
        citiesRequest.type = "top";
        citiesRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                LinkedList<City> citiesList = City.parse(response);
                if (citiesList.size() == 0)
                    return;
                mTopCitiesList.addAll(citiesList);
                fillData(mTopCitiesList);
                post(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CitySearchActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    private void city(String prefix) {
        searchCitiesRequest = new SearchCitiesRequest(this);
        registerRequest(searchCitiesRequest);
        searchCitiesRequest.prefix = prefix;
        searchCitiesRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                LinkedList<City> citiesList = City.parse(response);
                if (citiesList.size() == 0)
                    return;
                fillData(citiesList);
                post(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                fillData(mTopCitiesList);
                post(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    private void fillData(LinkedList<City> citiesList) {
        mDataList.clear();
        mDataList.addAll(citiesList);
        mNameList.clear();
        for (City city : mDataList)
            mNameList.add(city.full);
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }
    //---------------------------------------------------------------------------
}

/*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 * android.R.layout.simple_list_item_1,
 * android.R.id.text1,
 * strings); */
/*SimpleAdapter adapter = new SimpleAdapter(this,
 * createSensorsList(),
 * android.R.layout.simple_list_item_2,
 * new String[] {"title", "vendor"},
 * new int[] {android.R.id.text1, android.R.id.text2}); */
/*SimpleAdapter adapter = new SimpleAdapter(this,
 * createSensorsList(),
 * R.layout.sensor_layout,
 * new String[] {"title", "vendor", "power"},
 * new int[] {R.id.title, R.id.content, R.id.range}); */
