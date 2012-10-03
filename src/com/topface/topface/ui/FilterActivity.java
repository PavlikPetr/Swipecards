package com.topface.topface.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Confirmation;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FilterRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

public class FilterActivity extends BasePreferenceActivity implements LocationListener {
    //---------------------------------------------------------------------------
    // class TempFilter
    //---------------------------------------------------------------------------
    class TempFilter {
        int sex; // пол пользователей
        int age_start; // возраст от
        int age_end; // возраст до
        int city_id; // город в котором ищем пользователей
        String city_name; // город в котором ищем пользователей
        boolean online; // в сети или нет
        boolean geo; // искать по координатам
    }

    //---------------------------------------------------------------------------
    // Data
    private TempFilter mTemp;
    //private CheckBoxPreference mNearby_;  // геопозиционирование отключено
    private CheckBoxPreference mAllCities;
    private CheckBoxPreference mCity;
    private LocationManager mLocationManager;
    private FilterRequest filterRequest;
    private boolean mIsChanged;
    private boolean mIsSending;
    private LockerView mSavingLocker;
    // Constants
    public static final int CITY_NEARBY = 0;
    public static final int CITY_ALL = 1;
    public static final int CITY_SELECTED = 2;
    public static final int INTENT_FILTER_ACTIVITY = 110;

    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.ac_filter);
//        setContentView(R.layout.ac_filter_container);
        Debug.log(this, "+onCreate");

        SharedPreferences preferences = getSharedPreferences(Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);

//        mSavingLocker = (LockerView) findViewById(R.id.llvFilterSaving);

        // подтягивание данных
        mTemp = new TempFilter();
        mTemp.city_name = CacheProfile.dating_city_name;
        mTemp.city_id = CacheProfile.dating_city_id;
        mTemp.sex = CacheProfile.dating_sex;
        mTemp.age_start = CacheProfile.dating_age_start;
        mTemp.age_end = CacheProfile.dating_age_end;
        mTemp.geo = preferences.getBoolean(getString(R.string.cache_profile_filter_geo), false);
        mTemp.online = preferences.getBoolean(getString(R.string.cache_profile_filter_online), false);

        // sex button
        Preference sex = findPreference(getString(R.string.s_dating_sex));
        if (mTemp.sex == Static.GIRL)
            sex.setSummary(getString(R.string.filter_girl));
        else
            sex.setSummary(getString(R.string.filter_boy));
        sex.setOnPreferenceClickListener(mOnSexListener);

        // age button
        Preference age = findPreference(getString(R.string.s_dating_age));
        age.setSummary(getString(R.string.filter_from) + " " + mTemp.age_start + " " + getString(R.string.filter_to) + " " + mTemp.age_end);
        age.setOnPreferenceClickListener(mOnAgeListener);

        // online button
        Preference online = findPreference(getString(R.string.cache_profile_filter_online));
        if (mTemp.online == false)
            online.setSummary(getString(R.string.filter_all));
        else
            online.setSummary(getString(R.string.filter_only_online));
        online.setOnPreferenceClickListener(mOnOnlineListener);

        // cities group    
        //mNearby = (CheckBoxPreference)findPreference(getString(R.string.s_dating_nearby));
        //mNearby.setChecked(false);

        mAllCities = (CheckBoxPreference) findPreference(getString(R.string.s_dating_cities_all));
        mAllCities.setChecked(false);

        mCity = (CheckBoxPreference) findPreference(getString(R.string.s_dating_city));
        mCity.setChecked(false);

        if (mTemp.city_id == 0) {
            mAllCities.setChecked(true);
            mCity.setTitle(CacheProfile.city_name);
        } else {
            mCity.setChecked(true);
            mCity.setTitle(mTemp.city_name);
        }

        // cities listener
        Preference.OnPreferenceChangeListener citiesChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                /* if(preference == mNearby && (Boolean)newValue == true) {
                 * mAllCities.setChecked(false);
                 * mCity.setChecked(false);
                 * mTemp.geo = true;
                 * getCoords();
                 * 
                 * } else */
                if (preference == mAllCities && (Boolean) newValue == true) {
                    //mNearby.setChecked(false);
                    mCity.setChecked(false);
                    mTemp.geo = false;
                    mTemp.city_id = 0;
                    mIsChanged = true;

                } else if (preference == mCity && (Boolean) newValue == true) {
                    //mNearby.setChecked(false);
                    mAllCities.setChecked(false);
                    if (mTemp.city_id == 0) {
                        mTemp.geo = false;
                        mTemp.city_id = CacheProfile.city_id;
                        mTemp.city_name = CacheProfile.city_name;
                        mIsChanged = true;
                    }
                }

                return true;
            }
        };

        //mNearby.setOnPreferenceChangeListener(citiesChangeListener);
        mAllCities.setOnPreferenceChangeListener(citiesChangeListener);
        mCity.setOnPreferenceChangeListener(citiesChangeListener);

        // выбор города
        Preference selectCity = findPreference(getString(R.string.s_dating_select_city));
        selectCity.setOnPreferenceClickListener(mOnSelectCityListener);

        // сервис определения координар
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    //---------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        //App.bind(getBaseContext());
    }

    //---------------------------------------------------------------------------
    @Override
    protected void onStop() {
        //App.unbind();
        super.onStop();
    }

    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    //---------------------------------------------------------------------------
    public void getCoords() {
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        //wifi
        try {
            networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (networkEnabled)
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (Exception ex) {
        }

        // gps
        if (!networkEnabled)
            try {
                gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (gpsEnabled)
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } catch (Exception ex) {
            }
    }

    //---------------------------------------------------------------------------
    public void sendFilter() {
        // сохранение данных
        CacheProfile.dating_city_name = mTemp.city_name;
        CacheProfile.dating_city_id = mTemp.city_id;
        CacheProfile.dating_sex = mTemp.sex;
        CacheProfile.dating_age_start = mTemp.age_start;
        CacheProfile.dating_age_end = mTemp.age_end;

        SharedPreferences preferences = getSharedPreferences(Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.cache_profile_filter_geo), mTemp.geo);
        editor.putBoolean(getString(R.string.cache_profile_filter_online), mTemp.online);
        editor.commit();

        filterRequest = new FilterRequest(this.getApplicationContext());
        registerRequest(filterRequest);
        filterRequest.city = mTemp.city_id; // ЧТО СТАВИМ ПРИ ЗАПРОСЕ С КООРДИНАТАМИ
        filterRequest.sex = mTemp.sex;
        filterRequest.agebegin = mTemp.age_start;
        filterRequest.ageend = mTemp.age_end;
        mIsSending = true;
        filterRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final Confirmation confirm = Confirmation.parse(response);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (!confirm.completed) {
                            Toast.makeText(FilterActivity.this, getString(R.string.general_server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mIsSending = false;
                //Toast.makeText(FilterActivity.this,"filter success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                mIsSending = false;
            }
        }).exec();
        Debug.log(this, "3.city_id:" + mTemp.city_id);
    }

    //---------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY) {
            Bundle extras = data.getExtras();
            mTemp.city_name = extras.getString(CitySearchActivity.INTENT_CITY_NAME);
            mTemp.city_id = extras.getInt(CitySearchActivity.INTENT_CITY_ID);
            Debug.log(this, "2.city_id:" + mTemp.city_id);
            mTemp.geo = false;
            mCity.setTitle(mTemp.city_name);
            mCity.setChecked(true);
            //mNearby.setChecked(false);
            mAllCities.setChecked(false);
            mIsChanged = true;
        }
    }

    //---------------------------------------------------------------------------
    // Menu
    //---------------------------------------------------------------------------
    private static final int MENU_SAVE = 0;

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        //menu.add(0,MENU_SAVE,0,getString(R.string.filter_menu_save));
        return super.onCreatePanelMenu(featureId, menu);
    }

    //---------------------------------------------------------------------------
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE:
                // if(mTemp.online)
                sendFilter();
                setResult(RESULT_OK, null);
                finish();
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    //---------------------------------------------------------------------------
    // Listeners
    //---------------------------------------------------------------------------
    // sex
    Preference.OnPreferenceClickListener mOnSexListener = new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(final Preference preference) {
            final CharSequence[] items = {getString(R.string.filter_girl), getString(R.string.filter_boy)};

            AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    preference.setSummary(items[item]);
                    mTemp.sex = item;
                    mIsChanged = true;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }
    };
    //---------------------------------------------------------------------------
    // age
    int leftPosition;
    int rightPosition;
    Preference.OnPreferenceClickListener mOnAgeListener = new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(final Preference preference) {
            // 0  1  2  3  4  5  total 6
            final int[] ages = {16, 20, 24, 28, 32, 99};
            for (int i = 0; i < ages.length - 1; i++) {
                if (mTemp.age_start >= ages[i])
                    leftPosition = i;
                if (mTemp.age_end >= ages[i])
                    rightPosition = i;
            }
            View view = LayoutInflater.from(FilterActivity.this.getApplicationContext()).inflate(R.layout.pref_age_picker, null);
            final TextView tvFrom = (TextView) view.findViewById(R.id.tvFilterFrom);
            tvFrom.setText("" + mTemp.age_start);
            final TextView tvTo = (TextView) view.findViewById(R.id.tvFilterTo);
            tvTo.setText("" + mTemp.age_end);

            Button fromUp = (Button) view.findViewById(R.id.btnFilterFromUp);
            fromUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //int n = Integer.parseInt(tvFrom.getText().toString());
                    if (leftPosition < ages.length - 1)
                        tvFrom.setText("" + (ages[++leftPosition]));
                }
            });
            Button fromDown = (Button) view.findViewById(R.id.btnFilterFromDown);
            fromDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //int n = Integer.parseInt(tvFrom.getText().toString());
                    if (leftPosition > 0)
                        tvFrom.setText("" + (ages[--leftPosition]));
                }
            });
            Button toUp = (Button) view.findViewById(R.id.btnFilterToUp);
            toUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //int n = Integer.parseInt(tvTo.getText().toString());
                    if (rightPosition < ages.length - 1)
                        tvTo.setText("" + (ages[++rightPosition]));
                }
            });
            Button toDown = (Button) view.findViewById(R.id.btnFilterToDown);
            toDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //int n = Integer.parseInt(tvTo.getText().toString());
                    if (rightPosition > 0)
                        tvTo.setText("" + (ages[--rightPosition]));
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
            builder.setTitle(getString(R.string.filter_age));
            builder.setView(view);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    mTemp.age_start = Integer.parseInt(tvFrom.getText().toString());
                    mTemp.age_end = Integer.parseInt(tvTo.getText().toString());
                    if (mTemp.age_start > mTemp.age_end) {
                        mTemp.age_start = mTemp.age_end;
                        leftPosition = rightPosition;
                    }
                    mIsChanged = true;
                    preference.setSummary(getString(R.string.filter_from) + " " + mTemp.age_start + " " + getString(R.string.filter_to) + " " + mTemp.age_end);
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }
    };
    //---------------------------------------------------------------------------
    // online
    Preference.OnPreferenceClickListener mOnOnlineListener = new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(final Preference preference) {
            final CharSequence[] items = {getString(R.string.filter_all), getString(R.string.filter_only_online)};
            AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
            //builder.setTitle(getString(R.string.filter_online));
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    preference.setSummary(items[item]);
                    mTemp.online = (item == 0 ? false : true);
                    mIsChanged = true;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }
    };
    //---------------------------------------------------------------------------
    // select city
    Preference.OnPreferenceClickListener mOnSelectCityListener = new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(final Preference preference) {
            Intent intent = new Intent(FilterActivity.this.getApplicationContext(), CitySearchActivity.class);
            startActivityForResult(intent, CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
            return true;
        }
    };

    //---------------------------------------------------------------------------
    // User Location
    //---------------------------------------------------------------------------  
    @Override
    public void onLocationChanged(final Location location) {
        SettingsRequest settingsRequest = new SettingsRequest(getApplicationContext());
        registerRequest(settingsRequest);
        settingsRequest.lat = location.getLatitude();
        settingsRequest.lng = location.getLongitude();
        mIsSending = true;
        settingsRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final Confirmation confirm = Confirmation.parse(response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!confirm.completed) {
                            Toast.makeText(FilterActivity.this, getString(R.string.general_server_error), Toast.LENGTH_SHORT).show();
                        }
                        mIsSending = false;
                    }
                });

                mLocationManager.removeUpdates(FilterActivity.this);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                mLocationManager.removeUpdates(FilterActivity.this);
                mIsSending = false;
            }
        }).exec();
        //Toast.makeText(FilterActivity.this.getApplicationContext(),"lng:"+settings.lng+ "\nlat:"+settings.lat,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    //---------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        Debug.log(this, "onBackPressed");
        if (mIsChanged) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSavingLocker.setVisibility(View.VISIBLE);
                }
            });

            sendFilter();
            setResult(RESULT_OK, null);
        }

        (new Thread() {
            @Override
            public void run() {
                long timeElapsed = 0;
                while (mIsSending) {
                    try {
                        timeElapsed += 500;
                        if (timeElapsed > 30000)
                            break;
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Debug.log("FilterActivity Thread.sleep() exception onBackPressed()");
                    }
                }

                FilterActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        FilterActivity.super.onBackPressed();
                    }
                });
            }
        }).start();
    }
    //---------------------------------------------------------------------------    
}
