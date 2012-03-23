package com.sonetica.topface.ui.profile;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.QuestionaryRequest;
import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.net.SettingsRequest;
import com.sonetica.topface.ui.CitySearchActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditProfileActivity extends PreferenceActivity {
  // Data
  private FormInfo mFormInfo;
  private Preference mCity;
  // Constants
  // profile
  public static final int PROFILE_NAME = 0;
  public static final int PROFILE_AGE  = 1;
  public static final int PROFILE_SEX  = 2;
  public static final int PROFILE_LAT  = 3;
  public static final int PROFILE_LNG  = 4;
  public static final int PROFILE_CITYID = 5;
  public static final int PROFILE_ABOUT  = 6;
  // form
  public static final int FORM_WEIGHT    = 7;
  public static final int FORM_HEIGHT    = 8;
  public static final int FORM_JOBID     = 9;
  public static final int FORM_JOB       = 10;
  public static final int FORM_STATUSID  = 11;
  public static final int FORM_STATUS    = 12;
  public static final int FORM_EDUCATION = 13;
  public static final int FORM_MARRIAGE  = 14;
  public static final int FORM_FINANCES  = 15;
  public static final int FORM_CHARACTER = 16;
  public static final int FORM_SMOKING   = 17;
  public static final int FORM_ALCOHOL   = 18;
  public static final int FORM_FITNESS   = 19;
  public static final int FORM_COMMUNICATE = 20;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.layout.ac_edit_profile);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    mFormInfo = new FormInfo(getApplicationContext(),Data.s_Profile.sex);
     
    /*
     *  PROFILE
     */
    
    // name
    Preference name = findPreference(getString(R.string.s_profile_name));
    name.setSummary(Data.s_Profile.first_name);
    name.setOnPreferenceClickListener(mOnNameListener);
    
    // sex
    Preference sex = findPreference(getString(R.string.s_profile_sex));
    if(Data.s_Profile.sex==0)
      sex.setSummary(getString(R.string.profile_sex_girl));
    else
      sex.setSummary(getString(R.string.profile_sex_boy));
    sex.setOnPreferenceClickListener(mOnSexListener);

    // age
    Preference age = findPreference(getString(R.string.s_profile_age));
    age.setSummary(""+Data.s_Profile.age);
    age.setOnPreferenceClickListener(mOnAgeListener);
    
    // city
    mCity = findPreference(getString(R.string.s_profile_city));
    mCity.setSummary(Data.s_Profile.city_name);
    mCity.setOnPreferenceClickListener(mOnCityListener);
    
    /*
     *  FORM
     */
    
    // about
    Preference about = findPreference(getString(R.string.s_profile_about));
    about.setSummary(Data.s_Profile.status);
    about.setOnPreferenceClickListener(mOnAboutListener);
    
    // height
    Preference height = findPreference(getString(R.string.s_profile_height));
    height.setSummary(""+Data.s_Profile.questionary_height);
    height.setOnPreferenceClickListener(mOnHeightListener);
    
    // weight
    Preference weight = findPreference(getString(R.string.s_profile_weight));
    weight.setSummary(""+Data.s_Profile.questionary_weight);
    weight.setOnPreferenceClickListener(mOnWeightListener);
    
    // fitness
    ListPreference fitness = (ListPreference)findPreference(getString(R.string.s_profile_fitness));
    fitness.setSummary(mFormInfo.getFitness(Data.s_Profile.questionary_fitness_id));
    fitness.setEntries(mFormInfo.getFitnessEntries());
    fitness.setEntryValues(mFormInfo.getFitnessValues());
    fitness.setOnPreferenceChangeListener(mOnFitnessListener);
    
    // marriage
    ListPreference marriage = (ListPreference)findPreference(getString(R.string.s_profile_marriage));
    marriage.setSummary(mFormInfo.getMarriage(Data.s_Profile.questionary_marriage_id));
    marriage.setEntries(mFormInfo.getMarriageEntries());
    marriage.setEntryValues(mFormInfo.getMarriageValues());
    if(Data.s_Profile.sex==0)
      marriage.setTitle(getString(R.string.profile_marriage_female));
    marriage.setOnPreferenceChangeListener(mOnMarriageListener);
    
    // education
    ListPreference education = (ListPreference)findPreference(getString(R.string.s_profile_education));
    education.setSummary(mFormInfo.getEducation(Data.s_Profile.questionary_education_id));
    education.setEntries(mFormInfo.getEducationEntries());
    education.setEntryValues(mFormInfo.getEducationValues());
    education.setOnPreferenceChangeListener(mOnEducationListener);
    
    // finances
    ListPreference finances = (ListPreference)findPreference(getString(R.string.s_profile_finances));
    finances.setSummary(mFormInfo.getFinances(Data.s_Profile.questionary_finances_id));
    finances.setEntries(mFormInfo.getFinancesEntries());
    finances.setEntryValues(mFormInfo.getFinancesValues());
    finances.setOnPreferenceChangeListener(mOnFinancesListener);
    
    // smoking
    ListPreference smoking = (ListPreference)findPreference(getString(R.string.s_profile_smoking));
    smoking.setSummary(mFormInfo.getSmoking(Data.s_Profile.questionary_smoking_id));
    smoking.setEntries(mFormInfo.getSmokingEntries());
    smoking.setEntryValues(mFormInfo.getSmokingValues());
    smoking.setOnPreferenceChangeListener(mOnSmokingListener);
    
    // alcohol
    ListPreference alcohol = (ListPreference)findPreference(getString(R.string.s_profile_alcohol));
    alcohol.setSummary(mFormInfo.getAlcohol(Data.s_Profile.questionary_alcohol_id));
    alcohol.setEntries(mFormInfo.getAlcoholEntries());
    alcohol.setEntryValues(mFormInfo.getAlcoholValues());
    alcohol.setOnPreferenceChangeListener(mOnAlcoholListener);
    
    // commutability
    ListPreference commutability = (ListPreference)findPreference(getString(R.string.s_profile_commutability));
    commutability.setSummary(mFormInfo.getCommunication(Data.s_Profile.questionary_communication_id));
    commutability.setEntries(mFormInfo.getCommunicationEntries());
    commutability.setEntryValues(mFormInfo.getCommunicationValues());
    commutability.setOnPreferenceChangeListener(mOnCommutabilityListener);
    
    // character
    ListPreference character = (ListPreference)findPreference(getString(R.string.s_profile_character));
    character.setSummary(mFormInfo.getCharacter(Data.s_Profile.questionary_character_id));
    character.setEntries(mFormInfo.getCharacterEntries());
    character.setEntryValues(mFormInfo.getCharacterValues());
    character.setOnPreferenceChangeListener(mOnCharacterListener);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    super.onActivityResult(requestCode,resultCode,data);
    if (resultCode == Activity.RESULT_OK && requestCode == CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY) {
      Bundle extras = data.getExtras();

      Data.s_Profile.city_id   = extras.getInt(CitySearchActivity.INTENT_CITY_ID);
      Data.s_Profile.city_name = extras.getString(CitySearchActivity.INTENT_CITY_NAME);
      
      mCity.setSummary(Data.s_Profile.city_name);
      
      sendProfileData(PROFILE_CITYID,Data.s_Profile.city_id);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // data sending
  //---------------------------------------------------------------------------
  private void sendProfileData(int field,Object data) {
    SettingsRequest settings = new SettingsRequest(getApplicationContext());
    switch(field) {
      case PROFILE_NAME:
        settings.name = (String)data;
        break;
      case PROFILE_ABOUT:
        settings.status = (String)data;
        break;
      case PROFILE_AGE:
        settings.age = (Integer)data;
        break;
      case PROFILE_SEX:
        settings.sex = (Integer)data;
        break;
      case PROFILE_LAT:
        settings.lat = (Integer)data;
        break;
      case PROFILE_LNG:
        settings.lng = (Integer)data;
        break;
      case PROFILE_CITYID:
        settings.cityid = (Integer)data;
        break;
    }
    settings.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void sendFormData(int field,Object data) {
    QuestionaryRequest questionary = new QuestionaryRequest(getApplicationContext());
    switch(field) {
      case FORM_WEIGHT:
        questionary.weight = (Integer)data;
        break;
      case FORM_HEIGHT:
        questionary.height = (Integer)data;
        break;
      case FORM_JOBID:
        questionary.jobid = (Integer)data;
        break;
      case FORM_JOB:
        questionary.job = (String)data;
        break;
      case FORM_STATUSID:
        questionary.statusid = (Integer)data;
        break;
      case FORM_STATUS:
        questionary.status = (String)data;
        break;
      case FORM_EDUCATION:
        questionary.educationid = (Integer)data;
        break;
      case FORM_MARRIAGE:
        questionary.marriageid = (Integer)data;
        break;
      case FORM_FINANCES:
        questionary.financesid = (Integer)data;
        break;
      case FORM_CHARACTER:
        questionary.characterid = (Integer)data;
        break;
      case FORM_SMOKING:
        questionary.smokingid = (Integer)data;
        break;
      case FORM_ALCOHOL:
        questionary.alcoholid = (Integer)data;
        break;
      case FORM_FITNESS:
        questionary.fitnessid = (Integer)data;
        break;
      case FORM_COMMUNICATE:
        questionary.communicationid = (Integer)data;
        break;
    }
    questionary.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  // Listeners
  //---------------------------------------------------------------------------
  // name
  Preference.OnPreferenceClickListener mOnNameListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {
      View view = LayoutInflater.from(EditProfileActivity.this.getApplicationContext()).inflate(R.layout.pref_edit,null);
      final EditText editBox = (EditText)view.findViewById(R.id.etProfileInput);
      editBox.setText(preference.getSummary());
      AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
      builder.setTitle(getString(R.string.profile_form_name));
      builder.setView(view);
      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          String value = editBox.getText().toString();
          if(value.equals(""))
            return;
          preference.setSummary(editBox.getText().toString());
          Data.s_Profile.first_name = value;
          sendProfileData(PROFILE_NAME,value);
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

      View view = LayoutInflater.from(EditProfileActivity.this.getApplicationContext()).inflate(R.layout.pref_age_one_picker,null);

      final TextView age = (TextView)view.findViewById(R.id.tvProfileAgePicker);
      age.setText(preference.getSummary());
      
      Button btnAgeUp = (Button)view.findViewById(R.id.btnProfileAgeUp);
      btnAgeUp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          int n = Integer.parseInt(age.getText().toString());
          if(n<99)
            age.setText(""+(++n));
        }
      });
      Button btnAgeDown = (Button)view.findViewById(R.id.btnProfileAgeDown);
      btnAgeDown.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          int n = Integer.parseInt(age.getText().toString());
          if(n>12)
            age.setText(""+(--n));
        }
      });
      
      AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
      builder.setTitle(getString(R.string.filter_age));
      builder.setView(view);
      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          preference.setSummary(age.getText());
          int value = Integer.parseInt(age.getText().toString());
          Data.s_Profile.age = value;
          sendProfileData(PROFILE_AGE,value);
        }
      });
      
      AlertDialog alert = builder.create();
      alert.show();
      
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // sex
  Preference.OnPreferenceClickListener mOnSexListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {
      final CharSequence[] items = {getString(R.string.profile_sex_girl),getString(R.string.profile_sex_boy)};
      AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
      builder.setItems(items, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int value) {
          preference.setSummary(items[value]);
          Data.s_Profile.sex = value;
          mFormInfo.setSex(value);
          sendProfileData(PROFILE_SEX,value);
        }
      });
      AlertDialog alert = builder.create();
      alert.show();      

      return true;
    }
  };
  //---------------------------------------------------------------------------
  // city
  Preference.OnPreferenceClickListener mOnCityListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {
      Intent intent = new Intent(EditProfileActivity.this.getApplicationContext(),CitySearchActivity.class);
      startActivityForResult(intent,CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // about
  Preference.OnPreferenceClickListener mOnAboutListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {
      View view = LayoutInflater.from(EditProfileActivity.this.getApplicationContext()).inflate(R.layout.pref_edit,null);
      final EditText editBox = (EditText)view.findViewById(R.id.etProfileInput);
      editBox.setText(preference.getSummary());
      AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
      builder.setTitle(getString(R.string.profile_form_name));
      builder.setView(view);
      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          String value = editBox.getText().toString();
          preference.setSummary(value);
          Data.s_Profile.status = value;
          sendProfileData(PROFILE_ABOUT,value);
        }
      });
      AlertDialog alert = builder.create();
      alert.show();      
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // height
  Preference.OnPreferenceClickListener mOnHeightListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {
      View view = LayoutInflater.from(EditProfileActivity.this.getApplicationContext()).inflate(R.layout.pref_edit,null);
      final EditText editBox = (EditText)view.findViewById(R.id.etProfileInput);
      editBox.setInputType(InputType.TYPE_CLASS_NUMBER);
      editBox.setText(preference.getSummary());
      AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
      builder.setTitle(getString(R.string.profile_height));
      builder.setView(view);
      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          if(editBox.getText().toString().equals(""))
            return;
          Integer value = Integer.parseInt(editBox.getText().toString());
          preference.setSummary(""+value);
          Data.s_Profile.questionary_height = value;
          sendFormData(FORM_HEIGHT,value);
        }
      });
      AlertDialog alert = builder.create();
      alert.show();      
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // weight
  Preference.OnPreferenceClickListener mOnWeightListener = new Preference.OnPreferenceClickListener() {
    public boolean onPreferenceClick(final Preference preference) {
      View view = LayoutInflater.from(EditProfileActivity.this.getApplicationContext()).inflate(R.layout.pref_edit,null);
      final EditText editBox = (EditText)view.findViewById(R.id.etProfileInput);
      editBox.setInputType(InputType.TYPE_CLASS_NUMBER);
      editBox.setText(preference.getSummary());
      AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
      builder.setTitle(getString(R.string.profile_weight));
      builder.setView(view);
      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          if(editBox.getText().toString().equals(""))
            return;
          Integer value = Integer.parseInt(editBox.getText().toString());
          preference.setSummary(""+value);
          Data.s_Profile.questionary_weight = value;
          sendFormData(FORM_WEIGHT,value);
        }
      });
      AlertDialog alert = builder.create();
      alert.show();      
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // fitness
  Preference.OnPreferenceChangeListener mOnFitnessListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {      
      Integer i = Integer.parseInt(newValue.toString());
      String value = mFormInfo.getFitness(i);
      preference.setSummary(value);
      Data.s_Profile.questionary_fitness_id = i;
      sendFormData(FORM_FITNESS,i);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // marriage
  Preference.OnPreferenceChangeListener mOnMarriageListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {      
      Integer i = Integer.parseInt(newValue.toString());
      String value = mFormInfo.getMarriage(i);
      preference.setSummary(value);
      Data.s_Profile.questionary_marriage_id = i;
      sendFormData(FORM_MARRIAGE,i);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // education
  Preference.OnPreferenceChangeListener mOnEducationListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {      
      Integer i = Integer.parseInt(newValue.toString());
      String value = mFormInfo.getEducation(i);
      preference.setSummary(value);
      Data.s_Profile.questionary_education_id = i;
      sendFormData(FORM_EDUCATION,i);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // finances
  Preference.OnPreferenceChangeListener mOnFinancesListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {      
      Integer i = Integer.parseInt(newValue.toString());
      String value = mFormInfo.getFinances(i);
      preference.setSummary(value);
      Data.s_Profile.questionary_finances_id = i;
      sendFormData(FORM_FINANCES,i);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // smoking
  Preference.OnPreferenceChangeListener mOnSmokingListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {      
      Integer i = Integer.parseInt(newValue.toString());
      String value = mFormInfo.getSmoking(i);
      preference.setSummary(value);
      Data.s_Profile.questionary_smoking_id = i;
      sendFormData(FORM_SMOKING,i);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // alcohol
  Preference.OnPreferenceChangeListener mOnAlcoholListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {      
      Integer i = Integer.parseInt(newValue.toString());
      String value = mFormInfo.getAlcohol(i);
      preference.setSummary(value);
      Data.s_Profile.questionary_alcohol_id = i;
      sendFormData(FORM_ALCOHOL,i);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // commutability
  Preference.OnPreferenceChangeListener mOnCommutabilityListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {      
      Integer i = Integer.parseInt(newValue.toString());
      String value = mFormInfo.getCommunication(i);
      preference.setSummary(value);
      Data.s_Profile.questionary_communication_id = i;
      sendFormData(FORM_COMMUNICATE,i);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
  // character
  Preference.OnPreferenceChangeListener mOnCharacterListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference,Object newValue) {      
      Integer i = Integer.parseInt(newValue.toString());
      String value = mFormInfo.getCharacter(i);
      preference.setSummary(value);
      Data.s_Profile.questionary_character_id = i;
      sendFormData(FORM_CHARACTER,i);
      
      return true;
    }
  };
  //---------------------------------------------------------------------------
}
