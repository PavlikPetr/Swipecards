package com.sonetica.topface.ui.filter;

import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.R;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.FilterRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FilterActivity extends PreferenceActivity {
  //---------------------------------------------------------------------------
  // class TempFilter
  //---------------------------------------------------------------------------
  class TempFilter {
    int sex;
    int age_start;
    int age_end;
    boolean online;
    boolean geo;
  }
  //---------------------------------------------------------------------------
  // Data
  private Preference mSex;
  private Preference mAge;
  private Preference mOnline;
  private TempFilter mTemp;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.layout.ac_filter);
    Debug.log(this,"+onCreate");
    
    // подтягивание данных
    mTemp = new TempFilter();
    mTemp.sex       = Data.s_Profile.filter_sex;
    mTemp.age_start = Data.s_Profile.filter_age_start;
    mTemp.age_end   = Data.s_Profile.filter_age_end;
    mTemp.online    = Data.s_Profile.filter_online;
    mTemp.geo       = Data.s_Profile.filter_geo;
    
    // sex button
    mSex = findPreference(getString(R.string.s_filter_sex));
    if(mTemp.sex==Global.GIRL)
      mSex.setSummary(getString(R.string.filter_girl));
    else
      mSex.setSummary(getString(R.string.filter_boy));
    mSex.setOnPreferenceClickListener(mOnSexListener);
    
    // age button
    mAge = findPreference(getString(R.string.s_filter_age));
    mAge.setSummary(getString(R.string.filter_from)+" "+mTemp.age_start+" "+getString(R.string.filter_to)+" "+mTemp.age_end);
    mAge.setOnPreferenceClickListener(mOnAgeListener);
    
    // online button
    mOnline = findPreference(getString(R.string.s_filter_online));
    if(mTemp.online==false)
      mOnline.setSummary(getString(R.string.filter_all));
    else
      mOnline.setSummary(getString(R.string.filter_online));
    mOnline.setOnPreferenceClickListener(mOnOnelineListener);
    
  }
  //---------------------------------------------------------------------------
  public void filter() {
    // сохранение данных 
    Data.s_Profile.filter_online    = mTemp.online;
    Data.s_Profile.filter_geo       = mTemp.geo;
    Data.s_Profile.filter_sex       = mTemp.sex;
    Data.s_Profile.filter_age_start = mTemp.age_start;
    Data.s_Profile.filter_age_end   = mTemp.age_end;
    FilterRequest request = new FilterRequest(this.getApplicationContext());
    request.city     = 2;
    request.sex      = mTemp.sex;
    request.agebegin = mTemp.age_start;
    request.ageend   = mTemp.age_end; 
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        //Filter filter = Filter.parse(response);
        Toast.makeText(FilterActivity.this,"filter success",Toast.LENGTH_SHORT).show();
      }
      @Override
      public void fail(int codeError) {
        Toast.makeText(FilterActivity.this,"filter fail",Toast.LENGTH_SHORT).show();
      }
    }).exec();
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
        filter();
        finish();
      break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
  // sex
  Preference.OnPreferenceClickListener mOnSexListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(Preference preference) {
      final CharSequence[] items = {getString(R.string.filter_girl),getString(R.string.filter_boy)};
      AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
      builder.setItems(items, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          mSex.setSummary(items[item]);
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
    public boolean onPreferenceClick(Preference preference) {

      View view = LayoutInflater.from(FilterActivity.this.getApplicationContext()).inflate(R.layout.age_picker,null);
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

          mAge.setSummary(getString(R.string.filter_from)+" "+mTemp.age_start+" "+getString(R.string.filter_to)+" "+mTemp.age_end);
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
    public boolean onPreferenceClick(Preference preference) {
      final CharSequence[] items = {getString(R.string.filter_all),getString(R.string.filter_online)};
      AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
      builder.setItems(items, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          mOnline.setSummary(items[item]);
          mTemp.online = (item==0 ? false : true);
        }
      });
      AlertDialog alert = builder.create();
      alert.show();      
      return true;
    }
  };
  //---------------------------------------------------------------------------
}
