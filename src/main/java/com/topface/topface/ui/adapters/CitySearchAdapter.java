package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;

import java.util.LinkedList;

public class CitySearchAdapter extends BaseAdapter {
    private Context mContext;
    private int mCellView;
    private String mSearchPhrase;
    private ListView mCityListView;
    private LinkedList<City> mDataList;
    private LinkedList<String> mNameList;
    private LinkedList<City> mTopCitiesList;
    private String mAllCitiesString;
    private LinkedList<ApiRequest> mRequests = new LinkedList<>();


    public CitySearchAdapter(Context context, int resource, String searchPhrase) {
        mContext = context;
        mCellView = resource;
        mSearchPhrase = "Санкт";
        initAll();
        fillData(new LinkedList<City>());
        city();
    }


    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public City getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        mCityListView = (ListView) parent;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(mCellView, parent, false);
        }
        String item = mDataList.get(position).getName();
        if (item != null) {
            TextView textView = (TextView) view;
            if (textView != null) {
                textView.setText(item);
            }
        }
        return view;
    }

    public void setSearchPhrase(String text) {
        mSearchPhrase = text;
        city();
    }

    public void city() {
        CitiesRequest searchCitiesRequest = new CitiesRequest(mContext);
        registerRequest(searchCitiesRequest);
//        mCityListView.setVisibility(View.VISIBLE);
        searchCitiesRequest.prefix = mSearchPhrase;
        searchCitiesRequest.callback(new DataApiHandler<LinkedList<City>>() {

            @Override
            protected void success(LinkedList<City> citiesList, IApiResponse response) {
                if (citiesList.size() == 0) {
//                    mCityListView.setVisibility(View.INVISIBLE);
                } else {
                    fillData(citiesList);
                    notifyDataSetChanged();

                }
            }

            @SuppressWarnings("unchecked")
            @Override
            protected LinkedList<City> parseResponse(ApiResponse response) {
                return City.getCitiesList(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                fillData(mTopCitiesList);
                post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }).exec();
    }

    private synchronized void fillData(LinkedList<City> citiesList) {
        mDataList.clear();
        mDataList.add(City.createCity(City.ALL_CITIES, mAllCitiesString, mAllCitiesString));
        mDataList.addAll(citiesList);
        mNameList.clear();
        for (City city : mDataList)
            mNameList.add(city.full);
    }

    private void initArrays() {
        mTopCitiesList = new LinkedList<>();
        mDataList = new LinkedList<>();
        mNameList = new LinkedList<>();
    }

    private void initAllCitiesString() {
        mAllCitiesString = mContext.getResources().getString(R.string.filter_cities_all);
    }

    private void registerRequest(ApiRequest request) {
        if (!mRequests.contains(request)) {
            mRequests.add(request);
        }
    }

    private void initAll() {
        initArrays();
        initAllCitiesString();
    }

    public City getCityByPosition(int position) {
        return mDataList.get(position);
    }

    public void shortSearchPhrase() {
        fillData(mTopCitiesList);
        notifyDataSetChanged();
    }


}