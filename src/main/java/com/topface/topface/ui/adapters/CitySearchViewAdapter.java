package com.topface.topface.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
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
import com.topface.topface.ui.views.CitySearchView;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class CitySearchViewAdapter extends BaseAdapter implements Filterable {

    // minimal length of prefix for sending CitiesRequest
    private final static int MIN_LENGTH_CITY_PREFIX = 2;

    // id of fake city
    private final static int EMPTY_ID = -1;
    // delay before show loader
    private final static int MY_TIMER_VALUE = 500;

    private Timer mTimer;

    private City mDefaultCity;
    private City mUserCity;

    private final Context mContext;
    private int mRequestKey;

    private volatile CitiesRequest mFindCityByPrefRequest;
    private volatile CitiesRequest mDefaultCitiesRequest;

    private LinkedList<City> mTopCitiesList;
    private LinkedList<City> mDataList;

    private enum RequestType {DEFAULT_CITIES_REQUEST, CITIES_BY_PREFF_REQUEST}

    public CitySearchViewAdapter(Context context, int requestKey) {
        mContext = context;
        mRequestKey = requestKey;
        initAll();
        findDefaultCitiesList();
    }

    @SuppressWarnings("unused")
    public CitySearchViewAdapter(Context context, String prefix, int requestKey) {
        mContext = context;
        mRequestKey = requestKey;
        initAll();
        findDefaultCitiesList();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public String getItem(int index) {
        if (mDataList.get(index).id == EMPTY_ID) {
            return getUserCity().getFullName();
        }
        return mDataList.get(index).getFullName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CitySearchHolder holder = new CitySearchHolder();
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.city_search_drop_down_view, parent, false);

        }
        holder.cityName = (TextView) convertView.findViewById(R.id.citySearchText);
        holder.progress = (ProgressBar) convertView.findViewById(R.id.citySearchProgress);
        City city = mDataList.get(position);
        if (city.id == EMPTY_ID) {
            holder.progress.setVisibility(View.VISIBLE);
            holder.cityName.setVisibility(View.GONE);
        } else {
            holder.progress.setVisibility(View.GONE);
            holder.cityName.setVisibility(View.VISIBLE);
            holder.cityName.setText(city.getFullName());
        }
        return convertView;
    }

    public void setPrefix(CharSequence constraint) {
        removeRequest(RequestType.CITIES_BY_PREFF_REQUEST);
        if (constraint == null || constraint.length() <= MIN_LENGTH_CITY_PREFIX) {
            fillDefaultData();
        } else {
            findCityByPrefix(constraint.toString());
        }
    }

    // request to server with prefix of city
    private void findCityByPrefix(final String prefix) {
        callInProgress(true);
        registerRequest(RequestType.CITIES_BY_PREFF_REQUEST, new CitiesRequest(mContext, prefix));
        getCurrentRequest(RequestType.CITIES_BY_PREFF_REQUEST).callback(new DataApiHandler<LinkedList<City>>() {

            @Override
            protected void success(LinkedList<City> citiesList, IApiResponse response) {
                clearRequest(RequestType.CITIES_BY_PREFF_REQUEST);
                callInProgress(false);
                if (citiesList.size() == 0) {
                    callOnSearchFail(true);
                } else {
                    callOnSearchFail(false);
                    fillData(citiesList);
                }
            }

            @Override
            protected LinkedList<City> parseResponse(ApiResponse response) {
                clearRequest(RequestType.CITIES_BY_PREFF_REQUEST);
                return City.getCitiesList(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                clearRequest(RequestType.CITIES_BY_PREFF_REQUEST);
                callInProgress(false);
                fillDefaultData();
            }
        }).exec();
    }

    // set data for showing it in dropDown view
    private synchronized void fillData(LinkedList<City> citiesList) {
        mDataList.clear();
        if (getCurrentRequest(RequestType.CITIES_BY_PREFF_REQUEST) != null || getCurrentRequest(RequestType.DEFAULT_CITIES_REQUEST) != null) {
            mDataList.add(City.createCity(EMPTY_ID, "", ""));
        }
        if (mRequestKey == CitySearchView.CITY_SEARCH_FROM_FILTER_ACTIVITY) {
            mDataList.add(mDefaultCity);
            mDataList.add(getUserCity());
        }
        if (citiesList != null) {
            mDataList.addAll(citiesList);
        }
        deleteDuplicated(mDataList);
        updateList();
    }

    //delete all duplicate from list of cities
    private void deleteDuplicated(LinkedList<City> citiesList) {
        for (int i = citiesList.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (citiesList.get(i).id == citiesList.get(j).id) {
                    citiesList.remove(i);
                    break;
                }
            }
        }
    }

    private void initArrays() {
        mTopCitiesList = new LinkedList<>();
        mDataList = new LinkedList<>();
    }

    // get default cities list
    private void findDefaultCitiesList() {
        removeRequest(RequestType.DEFAULT_CITIES_REQUEST);
        registerRequest(RequestType.DEFAULT_CITIES_REQUEST, new CitiesRequest(mContext, true));
        getCurrentRequest(RequestType.DEFAULT_CITIES_REQUEST).callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                clearRequest(RequestType.DEFAULT_CITIES_REQUEST);
                final LinkedList<City> citiesList = City.getCitiesList(response);
                if (citiesList.size() == 0 || mTopCitiesList == null) {
                    return;
                }
                post(new Runnable() {
                    @Override
                    public void run() {
                        mTopCitiesList.addAll(citiesList);
                        fillDefaultData();
                    }
                });
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                clearRequest(RequestType.DEFAULT_CITIES_REQUEST);
                Toast.makeText(mContext, R.string.general_data_error, Toast.LENGTH_SHORT).show();
            }
        }).exec();
    }

    // fill default data
    public void fillDefaultData() {
        fillData(mTopCitiesList);
    }

    public void fillStartDataList() {
        fillData(null);
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

    // method, which calling when request to server start/end
    private void callInProgress(boolean state) {
        if (state) {
            initTimer();
        } else {
            stopTimer();
        }
    }

    //method, which calling when request to server return fail
    private void callOnSearchFail(boolean state) {
        if (state) {
            // show default and users cities
            fillData(null);
        }
    }

    public City getCityByPosition(int position) {
        if (mDataList.get(position).id == EMPTY_ID) {
            return getUserCity();
        }
        return mDataList.get(position);
    }

    private void initDefaultCity() {
        mDefaultCity = City.createCity(City.ALL_CITIES,
                mContext.getResources().getString(R.string.filter_cities_all),
                mContext.getResources().getString(R.string.filter_cities_all));
    }

    private void initAll() {
        initArrays();
        initDefaultCity();
    }

    private void updateList() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void setUserCity(City city) {
        mUserCity = city;
    }

    private City getUserCity() {
        if (mUserCity == null || mUserCity.isEmpty()) {
            mUserCity = mDefaultCity;
        }
        return mUserCity;
    }

    private void removeRequest(RequestType type) {
        switch (type) {
            case CITIES_BY_PREFF_REQUEST:
                if (mFindCityByPrefRequest != null) {
                    cancelRequest(mFindCityByPrefRequest);
                    clearRequest(type);
                }
                break;
            case DEFAULT_CITIES_REQUEST:
                if (mDefaultCitiesRequest != null) {
                    cancelRequest(mDefaultCitiesRequest);
                    clearRequest(type);
                }
                break;
        }
    }

    private void clearRequest(RequestType type) {
        registerRequest(type, null);
    }


    private CitiesRequest getCurrentRequest(RequestType type) {
        CitiesRequest currentRequest = null;
        switch (type) {
            case DEFAULT_CITIES_REQUEST:
                currentRequest = mDefaultCitiesRequest;
                break;
            case CITIES_BY_PREFF_REQUEST:
                currentRequest = mFindCityByPrefRequest;
                break;
        }
        return currentRequest;
    }

    private void registerRequest(RequestType type, CitiesRequest request) {
        switch (type) {
            case DEFAULT_CITIES_REQUEST:
                mDefaultCitiesRequest = request;
                break;
            case CITIES_BY_PREFF_REQUEST:
                mFindCityByPrefRequest = request;
                break;
        }
    }

    private void cancelRequest(ApiRequest request) {
        request.cancelFromUi();
        stopTimer();
    }

    protected static class CitySearchHolder {
        public TextView cityName;
        public ProgressBar progress;
    }

    private void initTimer() {
        stopTimer();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            public void run() {
                // show default and users city with progressBar
                fillData(null);
            }
        }, MY_TIMER_VALUE);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }
}
