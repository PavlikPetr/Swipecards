package com.topface.topface.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.Top;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.TopRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.TopsAdapter;
import com.topface.topface.ui.blocks.FilterBlock;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.views.DoubleButton;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.NavigationBarController;

import java.util.LinkedList;

public class TopsFragment extends BaseFragment {

    // Data cache
    private LinkedList<Top> mTopsList = new LinkedList<Top>();
    private GridView mGallery;
    private TopsAdapter mGridAdapter;
    private Button mCityButton;
    private LockerView mLoadingLocker;
    private ActionData mActionData;
    private DoubleButton mBtnDouble;
    private FloatBlock mFloatBlock;

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

        // Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup) view.findViewById(R.id.loNavigationBar));
        view.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(R.string.general_tops);

        //Инициализируем кнопку фильтров
        new FilterBlock((ViewGroup) view, R.id.loControlsGroup, R.id.btnNavigationSettingsBar, R.id.toolsBar);

        // Data
        mTopsList = new LinkedList<Top>();

        // Progress
        mLoadingLocker = (LockerView) view.findViewById(R.id.llvTopsLoading);

        // Preferences
        SharedPreferences preferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        // Action
        mActionData = new ActionData();
        mActionData.sex = preferences.getInt(Static.PREFERENCES_TOPS_SEX, GIRLS);
        mActionData.city_id = preferences.getInt(Static.PREFERENCES_TOPS_CITY_ID, CacheProfile.city_id);
        mActionData.city_name = preferences.getString(Static.PREFERENCES_TOPS_CITY_NAME, CacheProfile.city_name);
        mActionData.city_popup_pos = preferences.getInt(Static.PREFERENCES_TOPS_CITY_POS, -1);

        // Double Button
        mBtnDouble = (DoubleButton) view.findViewById(R.id.btnDoubleTops);
        mBtnDouble.setLeftText(getString(R.string.general_boys));
        mBtnDouble.setRightText(getString(R.string.general_girls));
        mBtnDouble.setChecked(mActionData.sex == 0 ? DoubleButton.RIGHT_BUTTON : DoubleButton.LEFT_BUTTON);
        // BOYS
        mBtnDouble.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionData.sex = BOYS;
                updateData();
            }
        });
        // GIRLS
        mBtnDouble.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionData.sex = GIRLS;
                updateData();
            }
        });

        // City Button
        mCityButton = (Button) view.findViewById(R.id.btnTopsBarCity);
        mCityButton.setText(mActionData.city_name);
        mCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choiceCity();
            }
        });

        // Gallery
        mGallery = (GridView) view.findViewById(R.id.grdTopsGallary);
        mGallery.setAnimationCacheEnabled(false);
        mGallery.setScrollingCacheEnabled(false);
        mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ((NavigationActivity)getActivity()).onExtraFragment(
                            ProfileNewFragment.newInstance(Data.topsList.get(position).uid, ProfileNewFragment.TYPE_USER_PROFILE));
                } catch (Exception e) {
                    Debug.log(TopsFragment.this, "start UserProfileActivity exception:" + e.toString());
                }
            }
        });

        // Control creating
        mGridAdapter = new TopsAdapter(getActivity(), mTopsList);
        mGallery.setAdapter(mGridAdapter);

        mFloatBlock = new FloatBlock(getActivity(), this, (ViewGroup) view);

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

        super.onDestroy();
    }

    private void updateData() {
        onUpdateStart(false);
        mGallery.setSelection(0);

        TopRequest topRequest = new TopRequest(getActivity());
        registerRequest(topRequest);
        topRequest.sex = mActionData.sex;
        topRequest.city = mActionData.city_id;
        topRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        mTopsList.clear();
                        mTopsList.addAll(Top.parse(response));
                        onUpdateSuccess(false);
                        if (mGridAdapter != null) {
                            mGridAdapter.notifyDataSetChanged();
                            mGallery.setVisibility(View.VISIBLE);
                        }

                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        onUpdateFail(false);
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
        onUpdateStart(false);
        CitiesRequest citiesRequest = new CitiesRequest(getActivity());
        registerRequest(citiesRequest);
        citiesRequest.type = "top";
        citiesRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Data.cityList = City.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        onUpdateSuccess(false);
                        showCitiesDialog();
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        onUpdateFail(false);
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
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
            public void onClick(DialogInterface dialog, int position) {
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

        mGallery = null;
        mGridAdapter = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTopsList.isEmpty()) {
            updateData();
        }
        mFloatBlock.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFloatBlock.onPause();
    }

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mLoadingLocker.setVisibility(View.VISIBLE);
            mBtnDouble.setClickable(false);
        }
    }

    @Override
    protected void onUpdateSuccess(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mLoadingLocker.setVisibility(View.GONE);
            mBtnDouble.setClickable(true);
        }
    }

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mLoadingLocker.setVisibility(View.GONE);
            mBtnDouble.setClickable(true);
        }
    }

}
