package com.sonetica.topface.ui.dating;

import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.R;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.FilterRequest;
import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.net.SettingsRequest;
import com.sonetica.topface.ui.CitySearchActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FilterActivity extends PreferenceActivity implements LocationListener {
  //---------------------------------------------------------------------------
  // class TempFilter
  //---------------------------------------------------------------------------
  class TempFilter {
    int sex;          // пол пользователей
    int age_start;    // возраст от
    int age_end;      // возраст до
    int city_id;      // город в котором ищем пользователей
    String city_name; // город в котором ищем пользователей
    boolean online;   // в сети или нет
    boolean geo;      // искать по координатам
  }
  //---------------------------------------------------------------------------
  // Data
  private TempFilter mTemp;
  private CheckBoxPreference mNearby;
  private CheckBoxPreference mAllCities;
  private CheckBoxPreference mCity;
  private LocationManager mLocationManager;
  // Constants
  public static final int CITY_NEARBY   = 0;
  public static final int CITY_ALL      = 1;
  public static final int CITY_SELECTED = 2;
  public static final int INTENT_FILTER_ACTIVITY = 110;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.layout.ac_filter);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // подтягивание данных
    mTemp = new TempFilter();
    mTemp.city_name = Data.s_Profile.filter_city_name;
    mTemp.city_id   = Data.s_Profile.filter_city_id;
    mTemp.sex       = Data.s_Profile.filter_sex;
    mTemp.age_start = Data.s_Profile.filter_age_start;
    mTemp.age_end   = Data.s_Profile.filter_age_end;
    mTemp.online    = Data.s_Profile.filter_online;
    mTemp.geo       = Data.s_Profile.filter_geo;
    
    // sex button
    Preference sex = findPreference(getString(R.string.s_filter_sex));
    if(mTemp.sex==Global.GIRL)
      sex.setSummary(getString(R.string.filter_girl));
    else
      sex.setSummary(getString(R.string.filter_boy));
    sex.setOnPreferenceClickListener(mOnSexListener);
    
    // age button
    Preference age = findPreference(getString(R.string.s_filter_age));
    age.setSummary(getString(R.string.filter_from)+" "+mTemp.age_start+" "+getString(R.string.filter_to)+" "+mTemp.age_end);
    age.setOnPreferenceClickListener(mOnAgeListener);
    
    // online button
    Preference online = findPreference(getString(R.string.s_filter_online));
    if(mTemp.online==false)
      online.setSummary(getString(R.string.filter_all));
    else
      online.setSummary(getString(R.string.filter_only_online));
    online.setOnPreferenceClickListener(mOnOnelineListener);
    
    // cities group    
    mNearby = (CheckBoxPreference)findPreference(getString(R.string.s_filter_nearby));
    mNearby.setChecked(false);
    
    mAllCities = (CheckBoxPreference)findPreference(getString(R.string.s_filter_cities_all));
    mAllCities.setChecked(false);
    
    mCity = (CheckBoxPreference)findPreference(getString(R.string.s_filter_city));
    mCity.setChecked(false);

    if(mTemp.city_id == 0) {
      mAllCities.setChecked(true);
      mCity.setTitle(Data.s_Profile.city_name);
    } else {
      mCity.setChecked(true);
      mCity.setTitle(mTemp.city_name);
    }
    
    // cities listener
    Preference.OnPreferenceChangeListener citiesChangeListener = new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference,Object newValue) {
        if(preference == mNearby && (Boolean)newValue == true) {
          mAllCities.setChecked(false);
          mCity.setChecked(false);
          mTemp.geo = true;
          getCoords();
          
        } else if(preference == mAllCities && (Boolean)newValue == true) {
          mNearby.setChecked(false);
          mCity.setChecked(false);
          mTemp.geo = false;
          mTemp.city_id = 0;
          
        } else if(preference == mCity && (Boolean)newValue == true) {
          mNearby.setChecked(false);
          mAllCities.setChecked(false);
          if(mTemp.city_id == 0) {
            mTemp.geo = false;
            mTemp.city_id = Data.s_Profile.city_id;
            mTemp.city_name = Data.s_Profile.city_name;
          }
        }
        
        return true;
      }
    };
    
    mNearby.setOnPreferenceChangeListener(citiesChangeListener);
    mAllCities.setOnPreferenceChangeListener(citiesChangeListener);
    mCity.setOnPreferenceChangeListener(citiesChangeListener);
    
    // выбор города
    Preference selectCity = findPreference(getString(R.string.s_filter_select_city));
    selectCity.setOnPreferenceClickListener(mOnSelectCityListener);
    
    // сервис определения координар
    mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
  }
  //---------------------------------------------------------------------------
  public void getCoords() {
    boolean gpsEnabled = false;
    boolean networkEnabled = false;

    //wifi
    try {
      networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
      if(networkEnabled)
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
    } catch (Exception ex) {}
    
    // gps
    if(!networkEnabled)
      try {
        gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(gpsEnabled)
          mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
      } catch (Exception ex) {}
  }
  //---------------------------------------------------------------------------
  public void sendFilter() {
    // сохранение данных
    Data.s_Profile.filter_city_name = mTemp.city_name;
    Data.s_Profile.filter_city_id   = mTemp.city_id;
    Data.s_Profile.filter_online    = mTemp.online;
    Data.s_Profile.filter_geo       = mTemp.geo;
    Data.s_Profile.filter_sex       = mTemp.sex;
    Data.s_Profile.filter_age_start = mTemp.age_start;
    Data.s_Profile.filter_age_end   = mTemp.age_end;
    
    FilterRequest request = new FilterRequest(this.getApplicationContext());
    request.city     = mTemp.city_id;  // ЧТО СТАВИМ ПРИ ЗАПРОСЕ С КООРДИНАТАМИ
    request.sex      = mTemp.sex;
    request.agebegin = mTemp.age_start;
    request.ageend   = mTemp.age_end; 
    request.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        //Toast.makeText(FilterActivity.this,"filter success",Toast.LENGTH_SHORT).show();
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    super.onActivityResult(requestCode,resultCode,data);
    if (resultCode == Activity.RESULT_OK && requestCode == CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY) {
      Bundle extras = data.getExtras();
      mTemp.city_name = extras.getString(CitySearchActivity.INTENT_CITY_NAME);
      mTemp.city_id   = extras.getInt(CitySearchActivity.INTENT_CITY_ID);
      mTemp.geo = false;
      mCity.setTitle(mTemp.city_name);
      mCity.setChecked(true);
      mNearby.setChecked(false);
      mAllCities.setChecked(false);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_SAVE = 0;
  @Override
  public boolean onCreatePanelMenu(int featureId,Menu menu) {
    menu.add(0,MENU_SAVE,0,getString(R.string.filter_menu_save));
    return super.onCreatePanelMenu(featureId,menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch(item.getItemId()) {
      case MENU_SAVE:
        // if(mTemp.online)
        sendFilter();
        setResult(RESULT_OK,null);
        finish();
      break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
  // Listeners
  //---------------------------------------------------------------------------
  // sex
  Preference.OnPreferenceClickListener mOnSexListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {
      final CharSequence[] items = {getString(R.string.filter_girl),getString(R.string.filter_boy)};
      
      AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
      builder.setItems(items, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          preference.setSummary(items[item]);
          mTemp.sex = item;
        }
      });
      AlertDialog alert = builder.create();
      alert.show();
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // age
  Preference.OnPreferenceClickListener mOnAgeListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {

      View view = LayoutInflater.from(FilterActivity.this.getApplicationContext()).inflate(R.layout.pref_age_picker,null);
      final TextView tvFrom = (TextView)view.findViewById(R.id.tvFilterFrom);
      tvFrom.setText(""+mTemp.age_start);
      final TextView tvTo = (TextView)view.findViewById(R.id.tvFilterTo);
      tvTo.setText(""+mTemp.age_end);
      
      Button fromUp = (Button)view.findViewById(R.id.btnFilterFromUp);
      fromUp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          int n = Integer.parseInt(tvFrom.getText().toString());
          if(n<99)
            tvFrom.setText(""+(++n));
        }
      });
      Button fromDown = (Button)view.findViewById(R.id.btnFilterFromDown);
      fromDown.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          int n = Integer.parseInt(tvFrom.getText().toString());
          if(n>12)
            tvFrom.setText(""+(--n));
        }
      });
      Button toUp = (Button)view.findViewById(R.id.btnFilterToUp);
      toUp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          int n = Integer.parseInt(tvTo.getText().toString());
          if(n<99)
            tvTo.setText(""+(++n));
        }
      });
      Button toDown = (Button)view.findViewById(R.id.btnFilterToDown);
      toDown.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          int n = Integer.parseInt(tvTo.getText().toString());
          if(n>12)
            tvTo.setText(""+(--n));
        }
      });
      
      AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
      builder.setTitle(getString(R.string.filter_age));
      builder.setView(view);
      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          mTemp.age_start = Integer.parseInt(tvFrom.getText().toString());
          mTemp.age_end   = Integer.parseInt(tvTo.getText().toString());
          if(mTemp.age_start > mTemp.age_end)
            mTemp.age_start = mTemp.age_end;

          preference.setSummary(getString(R.string.filter_from)+" "+mTemp.age_start+" "+getString(R.string.filter_to)+" "+mTemp.age_end);
        }
      });
      
      AlertDialog alert = builder.create();
      alert.show();
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // online
  Preference.OnPreferenceClickListener mOnOnelineListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {
      final CharSequence[] items = {getString(R.string.filter_all),getString(R.string.filter_only_online)};
      AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
      //builder.setTitle(getString(R.string.filter_online));
      builder.setItems(items, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          preference.setSummary(items[item]);
          mTemp.online = (item==0 ? false : true);
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
      Intent intent = new Intent(FilterActivity.this.getApplicationContext(),CitySearchActivity.class);
      startActivityForResult(intent,CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // User Location
  //---------------------------------------------------------------------------  
  @Override
  public void onLocationChanged(final Location location) {
    SettingsRequest settings = new SettingsRequest(getApplicationContext());
    settings.lat = location.getLatitude();
    settings.lng = location.getLongitude();
    settings.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        mLocationManager.removeUpdates(FilterActivity.this);
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        mLocationManager.removeUpdates(FilterActivity.this);
      }
    }).exec();
    Toast.makeText(FilterActivity.this.getApplicationContext(),"lng:"+settings.lng+ "\nlat:"+settings.lat,Toast.LENGTH_SHORT).show();
  }
  @Override 
  public void onProviderDisabled(String provider) {}
  @Override 
  public void onProviderEnabled(String provider) {}
  @Override 
  public void onStatusChanged(String provider, int status, Bundle extras) {}
  //---------------------------------------------------------------------------
}
