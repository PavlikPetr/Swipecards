package com.topface.topface.ui;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.SearchCitiesRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

import java.util.LinkedList;

public class CitySearchActivity extends BaseFragmentActivity {
    private ArrayAdapter<String> mListAdapter;
    private LinkedList<City> mTopCitiesList;
    private LinkedList<City> mDataList;
    private LinkedList<String> mNameList;
    private ProgressBar mProgressBar;
    // Constants
    public static final int INTENT_CITY_SEARCH_ACTIVITY = 100;
    public static final int INTENT_CITY_SEARCH_FROM_FILTER_ACTIVITY = 101;
    public static final String INTENT_CITY_ID = "city_id";
    public static final String INTENT_CITY_NAME = "city_name";
    public static final String INTENT_CITY_FULL_NAME = "city_full";

    private String mAllCitiesString;
    private int mRequestKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ac_city);
        Debug.log(this, "+onCreate");

        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left);


        mRequestKey = getIntent().getIntExtra(Static.INTENT_REQUEST_KEY, 0);
        mAllCitiesString = getResources().getString(R.string.filter_cities_all);

        // Data
        mTopCitiesList = new LinkedList<City>();
        mDataList = new LinkedList<City>();
        mNameList = new LinkedList<String>();

        // Title Header        
        initHeader();

        //My City
        initMyCity();

        // Progress
        mProgressBar = (ProgressBar) findViewById(R.id.prsCityLoading);

        // ListView
        initListView();

        // EditText
        initEditText();

        update();
    }

    private void initEditText() {
        EditText cityInputView = (EditText) findViewById(R.id.etCityInput);
        cityInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2)
                    city(s.toString());
                else {
                    fillData(mTopCitiesList);
                    mListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initListView() {
        final LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
        mListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mNameList) {
            class ViewHolder {
                TextView mTitle;
                ImageView mBackground;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;

                if (convertView == null) {
                    holder = new ViewHolder();

                    convertView = mInflater.inflate(R.layout.item_edit_form_check, null, false);
                    holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                    holder.mBackground = (ImageView) convertView.findViewById(R.id.ivEditBackground);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }


                if (position == 0) {
                    if (getCount() == 1) {
                        holder.mBackground.setImageDrawable(getResources().getDrawable(
                                R.drawable.edit_big_btn_selector));
                    } else {
                        holder.mBackground.setImageDrawable(getResources().getDrawable(
                                R.drawable.edit_big_btn_top_selector));
                    }
                    convertView.setPadding(0, 10, 0, 0);
                } else if (position == getCount() - 1) {
                    holder.mBackground.setImageDrawable(getResources().getDrawable(
                            R.drawable.edit_big_btn_bottom_selector));
                    convertView.setPadding(0, 0, 0, 10);
                } else {
                    holder.mBackground.setImageDrawable(getResources().getDrawable(
                            R.drawable.edit_big_btn_middle_selector));
                    convertView.setPadding(0, 0, 0, 0);
                }

                holder.mTitle.setText(getItem(position));

                return convertView;
            }
        };

        // ListView
        ListView cityListView = (ListView) findViewById(R.id.lvCityList);
        cityListView.setAdapter(mListAdapter);

        // возврат значения и выход
        cityListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent intent = CitySearchActivity.this.getIntent();
                intent.putExtra(INTENT_CITY_ID, mDataList.get(position).id);
                intent.putExtra(INTENT_CITY_NAME, mDataList.get(position).name);
                intent.putExtra(INTENT_CITY_FULL_NAME, mDataList.get(position).full);

                Debug.log(CitySearchActivity.this, "1.city_id:" + mDataList.get(position).id);

                CitySearchActivity.this.setResult(RESULT_OK, intent);
                CitySearchActivity.this.finish();
            }
        });
    }

    private void initHeader() {
        ((TextView) findViewById(R.id.tvNavigationTitle)).setText(getString(R.string.general_city));
        findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        View btnBack = findViewById(R.id.btnNavigationBack);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initMyCity() {
        if (CacheProfile.city.isEmpty()) {
            findViewById(R.id.cbMyCity).setVisibility(View.GONE);
            findViewById(R.id.tvMyCity).setVisibility(View.GONE);
        } else {
            final ViewGroup myCity = (ViewGroup) findViewById(R.id.cbMyCity);
            ((ImageView) myCity.findViewById(R.id.ivEditBackground)).setImageDrawable(getResources().getDrawable(
                    R.drawable.edit_big_btn_selector));
            ((TextView) myCity.findViewById(R.id.tvTitle)).setText(CacheProfile.city.name);
            myCity.findViewById(R.id.ivCheck).setVisibility(View.VISIBLE);
        }
    }


    private void update() {
        mProgressBar.setVisibility(View.VISIBLE);

        CitiesRequest citiesRequest = new CitiesRequest(this);
        registerRequest(citiesRequest);
        citiesRequest.type = "top";
        citiesRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final LinkedList<City> citiesList = City.parse(response);
                if (citiesList.size() == 0 || mTopCitiesList == null) {
                    return;
                }
                post(new Runnable() {
                    @Override
                    public void run() {
                        mTopCitiesList.addAll(citiesList);
                        fillData(mTopCitiesList);
                        mListAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
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


    private void city(String prefix) {
        SearchCitiesRequest searchCitiesRequest = new SearchCitiesRequest(this);
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
            public void fail(int codeError, ApiResponse response) {
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


    private synchronized void fillData(LinkedList<City> citiesList) {
        mDataList.clear();
        if (mRequestKey == INTENT_CITY_SEARCH_FROM_FILTER_ACTIVITY)
            mDataList.add(City.createCity(City.ALL_CITIES, mAllCitiesString, mAllCitiesString));
        mDataList.addAll(citiesList);
        mNameList.clear();
        for (City city : mDataList)
            mNameList.add(city.full);
    }


    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_right);
    }
}