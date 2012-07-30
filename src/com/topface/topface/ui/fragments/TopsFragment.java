package com.topface.topface.ui.fragments;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.Top;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.TopRequest;
import com.topface.topface.ui.adapters.TopsGridAdapter;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.DoubleButton;
import com.topface.topface.ui.views.ThumbView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.GalleryGridManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TopsFragment extends BaseFragment {
    // Data
    private GridView mGallery;
    private TopsGridAdapter mGridAdapter;
    private GalleryGridManager<Top> mGalleryGridManager;
    private Button mCityButton;
    private ProgressBar mProgressBar;
    private ActionData mActionData;
    private ImageView mBannerView;
    // Constats
    private static int GIRLS = 0;
    private static int BOYS = 1;

    private class ActionData {
        public int sex;
        public int city_id;
        public int city_popup_pos;
        public String city_name;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
      View view = inflater.inflate(R.layout.ac_tops, null);
        

        // Data
        Data.topsList = new LinkedList<Top>();

        // Progress
        mProgressBar = (ProgressBar)view.findViewById(R.id.prsTopsLoading);

        // Banner
        mBannerView = (ImageView)view.findViewById(R.id.ivBanner);

        // Preferences
        SharedPreferences preferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        // Action
        mActionData = new ActionData();
        mActionData.sex = preferences.getInt(Static.PREFERENCES_TOPS_SEX, GIRLS);
        mActionData.city_id = preferences.getInt(Static.PREFERENCES_TOPS_CITY_ID, CacheProfile.city_id);
        mActionData.city_name = preferences.getString(Static.PREFERENCES_TOPS_CITY_NAME, CacheProfile.city_name);
        mActionData.city_popup_pos = preferences.getInt(Static.PREFERENCES_TOPS_CITY_POS, -1);

        // Double Button
        DoubleButton btnDouble = (DoubleButton)view.findViewById(R.id.btnDoubleTops);
        btnDouble.setLeftText(getString(R.string.tops_btn_boys));
        btnDouble.setRightText(getString(R.string.tops_btn_girls));
        btnDouble.setChecked(mActionData.sex == 0 ? DoubleButton.RIGHT_BUTTON : DoubleButton.LEFT_BUTTON);
        // BOYS
        btnDouble.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionData.sex = BOYS;
                updateData();
            }
        });
        // GIRLS
        btnDouble.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionData.sex = GIRLS;
                updateData();
            }
        });

        // City Button
        mCityButton = (Button)view.findViewById(R.id.btnTopsBarCity);
        mCityButton.setText(mActionData.city_name);
        mCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choiceCity();
            }
        });

        // Gallery
        mGallery = (GridView)view.findViewById(R.id.grdTopsGallary);
        mGallery.setAnimationCacheEnabled(false);
        mGallery.setScrollingCacheEnabled(false);
        mGallery.setNumColumns(Data.GRID_COLUMN);
        mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int position,long id) {
                try {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra(ProfileActivity.INTENT_USER_ID, Data.topsList.get(position).uid);
                    startActivityForResult(intent, 0);
                } catch(Exception e) {
                    Debug.log(TopsFragment.this, "start ProfileActivity exception:" + e.toString());
                }
            }
        });

        // Control creating
        mGalleryGridManager = new GalleryGridManager<Top>(getActivity(), Data.topsList);
        mGridAdapter = new TopsGridAdapter(getActivity(), mGalleryGridManager);
        mGallery.setAdapter(mGridAdapter);
        mGallery.setOnScrollListener(mGalleryGridManager);
        return view;
    }

    @Override
    public void onDestroyView() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Static.PREFERENCES_TOPS_SEX, mActionData.sex);
        editor.putInt(Static.PREFERENCES_TOPS_CITY_ID, mActionData.city_id);
        editor.putString(Static.PREFERENCES_TOPS_CITY_NAME, mActionData.city_name);
        editor.putInt(Static.PREFERENCES_TOPS_CITY_POS, mActionData.city_popup_pos);
        editor.commit();

        ThumbView.release();

        super.onDestroy();
    }

    private void updateData() {
        mProgressBar.setVisibility(View.VISIBLE);
        mGallery.setSelection(0);

        TopRequest topRequest = new TopRequest(getActivity());
        topRequest.sex = mActionData.sex;
        topRequest.city = mActionData.city_id;
        topRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Data.topsList.clear();
                Data.topsList.addAll(Top.parse(response));
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        mGridAdapter.notifyDataSetChanged();
                        mGalleryGridManager.update();
                        mGallery.setVisibility(View.VISIBLE);
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }

    private void choiceCity() {
        if (Data.cityList != null && Data.cityList.size() > 0) {
            showCitiesDialog();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        CitiesRequest citiesRequest = new CitiesRequest(getActivity());
        citiesRequest.type = "top";
        citiesRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Data.cityList = City.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        showCitiesDialog();
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }

    private void showCitiesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.filter_select_city));
        int arraySize = Data.cityList.size();
        String[] cities = new String[arraySize];
        for (int i = 0; i < arraySize; ++i)
            cities[i] = Data.cityList.get(i).name;
        builder.setSingleChoiceItems(cities, mActionData.city_popup_pos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int position) {
                City city = Data.cityList.get(position);
                if (mActionData.city_id != city.id) {
                    mActionData.city_id = city.id;
                    mActionData.city_name = city.name;
                    mActionData.city_popup_pos = position;
                    mCityButton.setText(mActionData.city_name);
                    updateData();
                }
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGalleryGridManager != null) {
            mGalleryGridManager.release();
            mGalleryGridManager = null;
        }

        mGallery = null;
        mGridAdapter = null;
    }
    
    @Override
    public void clearLayout() {
        Debug.log(this, "TopsActivity::clearLayout");
        mGallery.setVisibility(View.INVISIBLE);
    }

    @Override
    public void fillLayout() {
        Debug.log(this, "TopsActivity::fillLayout");

        updateBanner(mBannerView, BannerRequest.TOP);
        updateData();
    }
}
