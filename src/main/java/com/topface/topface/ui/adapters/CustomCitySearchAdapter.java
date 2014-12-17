package com.topface.topface.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.views.CustomCitySearchView;

import java.util.LinkedList;

/**
 * Created by ppetr on 15.12.14.
 */

public class CustomCitySearchAdapter extends BaseAdapter implements Filterable {

    private static final int UNUSED_ID = -1;

    private City allCities;
    private City mDefaultCity;

    private final Context mContext;
    int mRequestKey;

    private LinkedList<ApiRequest> mRequests = new LinkedList<>();

    private LinkedList<City> mTopCitiesList;
    private LinkedList<City> mDataList;

    private onCitySearchProgress citySearchProgress;


    public CustomCitySearchAdapter(Context context, int requestKey) {
        mContext = context;
        mRequestKey = requestKey;
        initAll();
        update();
    }

    public CustomCitySearchAdapter(Context context, String prefix, int requestKey) {
        mContext = context;
        mRequestKey = requestKey;
        initAll();
        update();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public String getItem(int index) {
        if (mDataList.get(index).id == UNUSED_ID) {
            return mDefaultCity.getName();
        }
        return mDataList.get(index).getName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.spinner_text_layout, parent, false);
        }
        City city = mDataList.get(position);
        TextView textView = (TextView) convertView;
        if (textView != null) {
            textView.setPadding((int) mContext.getResources().getDimension(R.dimen.drop_downList_cell_padding_left),
                    (int) mContext.getResources().getDimension(R.dimen.drop_downList_cell_padding_left),
                    (int) mContext.getResources().getDimension(R.dimen.drop_downList_cell_padding_left),
                    (int) mContext.getResources().getDimension(R.dimen.drop_downList_cell_padding_left));
            textView.setText(city.getName());
        }
        return convertView;
    }

    public void setPrefix(CharSequence constraint) {
        removeAllRequests();
        if (constraint == null || constraint.length() <= 2) {
            Log.e("TOP_FACE", "prefix too short");
            showDefaultData();
        } else {
            city(constraint.toString());
        }
    }

    private void city(final String prefix) {
        Log.e("TOP_FACE", "city search prefix: " + prefix);
        CitiesRequest searchCitiesRequest = new CitiesRequest(mContext);
        registerRequest(searchCitiesRequest);
//        cityListView.setVisibility(View.VISIBLE);
        searchCitiesRequest.prefix = prefix;
        searchCitiesRequest.callback(new DataApiHandler<LinkedList<City>>() {

            @Override
            protected void success(LinkedList<City> citiesList, IApiResponse response) {
                if (citiesList.size() == 0) {
                    callOnSearchFail(true, prefix);
                } else {
                    callOnSearchFail(false, prefix);
                    fillData(citiesList);


                }
            }

            @SuppressWarnings("unchecked")
            @Override
            protected LinkedList<City> parseResponse(ApiResponse response) {
                return City.getCitiesList(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                showDefaultData();
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        notifyDataSetChanged();
//                    }
//                });
            }
        }).exec();
    }

    private synchronized void fillData(LinkedList<City> citiesList) {
        mDataList.clear();
        if (mRequestKey == CustomCitySearchView.CITY_SEARCH_FROM_FILTER_ACTIVITY)
            mDataList.add(allCities);
        mDataList.addAll(citiesList);
        Log.e("TOP_FACE", "new array has size: " + mDataList.size());
        updateList();


    }

    private void initArrays() {
        mTopCitiesList = new LinkedList<>();
        mDataList = new LinkedList<>();
    }

    private void update() {
        callInProgress(true);
        CitiesRequest citiesRequest = new CitiesRequest(mContext);
//        registerRequest(citiesRequest);
        citiesRequest.type = "top";
        citiesRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                final LinkedList<City> citiesList = City.getCitiesList(response);
                if (citiesList.size() == 0 || mTopCitiesList == null) {
                    return;
                }
                post(new Runnable() {
                    @Override
                    public void run() {
                        mTopCitiesList.addAll(citiesList);
                        showDefaultData();
                        callInProgress(false);
                    }
                });
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, R.string.general_data_error, Toast.LENGTH_SHORT).show();
                        callInProgress(false);
                    }
                });
            }
        }).exec();
    }

    private void showDefaultData() {

        Log.e("TOP_FACE", "show default data array size = " + mTopCitiesList.size());
        fillData(mTopCitiesList);
    }

    public void setOnCitySearchProgress(onCitySearchProgress progress) {
        citySearchProgress = progress;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setPrefix(constraint);
                    }
                });
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }
        };
    }

    public interface onCitySearchProgress {
        public void inProgress(boolean isOnProgress);

        public void onSearchFail(boolean state);

    }

    private void callInProgress(boolean state) {
        if (citySearchProgress != null) {
            citySearchProgress.inProgress(state);
        }
    }

    private void callOnSearchFail(boolean state, String prefix) {
        if (state) {
            mDataList.clear();
            mDataList.add(City.createCity(UNUSED_ID, mContext.getResources().getString(R.string.filter_city_fail, prefix),
                    mContext.getResources().getString(R.string.filter_city_fail, prefix)));
            updateList();
        }
        if (citySearchProgress != null) {
            citySearchProgress.onSearchFail(state);
        }
    }

    public City getCityByPosition(int position) {
        if (mDataList.get(position).id == UNUSED_ID) {
            return mDefaultCity;
        }
        return mDataList.get(position);
    }

    private void initAllCities() {
        allCities = City.createCity(City.ALL_CITIES, mContext.getResources().getString(R.string.filter_cities_all), mContext.getResources().getString(R.string.filter_cities_all));
    }

    private void initAll() {
        initArrays();
        initAllCities();
    }

    private void updateList() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void setDefaultCity(City city) {
        mDefaultCity = city;
    }

    public City getAllCitiesData() {
        return allCities;
    }

    private void removeAllRequests() {
        if (mRequests != null && mRequests.size() > 0) {
            for (ApiRequest request : mRequests) {
                cancelRequest(request);
            }
            mRequests.clear();
        }
    }

    private void registerRequest(ApiRequest request) {
        if (!mRequests.contains(request)) {
            mRequests.add(request);
        }
    }

    private void cancelRequest(ApiRequest request) {
        request.cancelFromUi();
    }
}
