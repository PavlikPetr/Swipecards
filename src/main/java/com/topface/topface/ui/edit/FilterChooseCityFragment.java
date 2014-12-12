package com.topface.topface.ui.edit;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.CacheProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class FilterChooseCityFragment extends AbstractEditFragment {

    public static final String INTENT_CITY_DATA = "city_data";
    public static final String INTENT_MARGIN_TOP = "margin_top";
    public static final String INTENT_ACTIONBAR_TITLE = "actionbar_title";

    private ArrayAdapter<String> mListAdapter;
    private LinkedList<City> mTopCitiesList;
    private LinkedList<City> mDataList;
    private LinkedList<String> mNameList;
    private ProgressBar mProgressBar;

    private String mAllCitiesString;
    private ListView cityListView;

    private String mTitle;
    private int mMarginTop;

    private City initCity;

    public static FilterChooseCityFragment newInstance(String city, int marginTop, String title) {
        FilterChooseCityFragment fragment = new FilterChooseCityFragment();

        Bundle args = new Bundle();
        args.putString(INTENT_CITY_DATA, city);
        args.putString(INTENT_ACTIONBAR_TITLE, title);
        args.putInt(INTENT_MARGIN_TOP, marginTop);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        sendUnLockScroll(false);

        ViewGroup root = (LinearLayout) inflater.inflate(R.layout.ac_filter_choose_city, container, false);

        LinearLayout mLayout = (LinearLayout) root.findViewById(R.id.loFilterCityLayout);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        lp.topMargin = mMarginTop;
        mLayout.setLayoutParams(lp);

        mAllCitiesString = getResources().getString(R.string.filter_cities_all);
        // Data
        mTopCitiesList = new LinkedList<>();
        mDataList = new LinkedList<>();
        mNameList = new LinkedList<>();
        // Progress
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsFilterCityLoading);
        // ListView
        initListView(root);
        update();
        return root;
    }

    @Override
    protected void lockUi() {
    }

    @Override
    protected void unlockUi() {
    }

    @Override
    protected boolean hasChanges() {
        return true;
    }

    @Override
    protected void saveChanges(final Handler handler) {
    }

    @Override
    protected void restoreState() {
        mMarginTop = getArguments().getInt(INTENT_MARGIN_TOP);
        try {
            initCity = new City(new JSONObject(getArguments().getString(INTENT_CITY_DATA)));
        } catch (JSONException e) {
            Debug.error(e);
            initCity = CacheProfile.city;
        }
        mTitle = getArguments().getString(INTENT_ACTIONBAR_TITLE);
    }

    @Override
    protected String getTitle() {
        return mTitle;
    }

    @Override
    public void onStop() {
        super.onStop();
        sendUnLockScroll(true);
    }

    private FilterFragment getFilterFragment() {
        FilterFragment fragment = null;
        try {
            fragment = (FilterFragment) getParentFragment();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragment;
    }

    private void sendUnLockScroll(boolean enabled) {
        if (getFilterFragment() != null) {
            getFilterFragment().setScrollingEnabled(enabled);
        }
    }

    private void initListView(ViewGroup root) {
        final LayoutInflater mInflater = LayoutInflater.from(getActivity());
        mListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mNameList) {
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
                    holder.mTitle = (TextView) convertView.findViewWithTag("tvTitle");
                    holder.mBackground = (ImageView) convertView.findViewWithTag("ivEditBackground");
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
                if (getCount() > position) {
                    holder.mTitle.setText(getItem(position));
                }
                return convertView;
            }
        };
        // ListView
        cityListView = (ListView) root.findViewById(R.id.lvFilterCityList);
        cityListView.setAdapter(mListAdapter);
        // возврат значения и выход
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                clickCityInList(mDataList.get(position));
            }
        });
    }

    private void update() {
        mProgressBar.setVisibility(View.VISIBLE);
        CitiesRequest citiesRequest = new CitiesRequest(getActivity());
        registerRequest(citiesRequest);
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
                        fillData(mTopCitiesList);
                        mListAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }


    public void city(String prefix) {
        CitiesRequest searchCitiesRequest = new CitiesRequest(getActivity());
        registerRequest(searchCitiesRequest);
        cityListView.setVisibility(View.VISIBLE);
        searchCitiesRequest.prefix = prefix;
        searchCitiesRequest.callback(new DataApiHandler<LinkedList<City>>() {

            @Override
            protected void success(LinkedList<City> citiesList, IApiResponse response) {
                if (citiesList.size() == 0) {
                    cityListView.setVisibility(View.INVISIBLE);
                } else {
                    fillData(citiesList);
                    mListAdapter.notifyDataSetChanged();

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
                        mListAdapter.notifyDataSetChanged();
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

    public void low() {
        fillData(mTopCitiesList);
        mListAdapter.notifyDataSetChanged();
    }

    private void clickCityInList(City data) {
        if (getFilterFragment() != null) {
            getFilterFragment().setSelectedCity(data);
        }
    }
}
