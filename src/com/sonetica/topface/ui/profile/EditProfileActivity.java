package com.sonetica.topface.ui.profile;

import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class EditProfileActivity extends PreferenceActivity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.layout.ac_edit_profile);
    Debug.log(this,"+onCreate");
     
    /*
     *  PROFILE
     */
    
    // name
    Preference name = findPreference(getString(R.string.s_profile_name));
    name.setSummary(Data.s_Profile.first_name);
    //name.setOnPreferenceClickListener(mOnSexListener);
    
    // sex
    Preference sex = findPreference(getString(R.string.s_profile_sex));
    if(Data.s_Profile.sex==Global.GIRL)
      sex.setSummary(getString(R.string.profile_sex_girl));
    else
      sex.setSummary(getString(R.string.profile_sex_boy));
    //sex.setOnPreferenceClickListener(mOnSexListener);

    // age
    Preference age = findPreference(getString(R.string.s_profile_age));
    age.setSummary(""+Data.s_Profile.age);
    //age.setOnPreferenceClickListener(mOnSexListener);
    
    // city
    Preference city = findPreference(getString(R.string.s_profile_city));
    city.setSummary(Data.s_Profile.city_name);
    //city.setOnPreferenceClickListener(mOnSexListener);
    
    /*
     *  FORM
     */
    
    // about
    Preference about = findPreference(getString(R.string.s_profile_about));
    about.setSummary(Data.s_Profile.questionary_status);
    //about.setOnPreferenceClickListener(mOnSexListener);
    
    // height
    Preference height = findPreference(getString(R.string.s_profile_height));
    height.setSummary(""+Data.s_Profile.questionary_height);
    //height.setOnPreferenceClickListener(mOnSexListener);
    
    // weight
    Preference weight = findPreference(getString(R.string.s_profile_weight));
    weight.setSummary(""+Data.s_Profile.questionary_weight);
    //weight.setOnPreferenceClickListener(mOnSexListener);
    
    // fitness
    Preference fitness = findPreference(getString(R.string.s_profile_fitness));
    fitness.setSummary(""+Data.s_Profile.questionary_fitness_id);
    //fitness.setOnPreferenceClickListener(mOnSexListener);
    
    // marriage
    Preference marriage = findPreference(getString(R.string.s_profile_marriage));
    marriage.setSummary(""+Data.s_Profile.questionary_marriage_id);
    //marriage.setOnPreferenceClickListener(mOnSexListener);
    
    // education
    Preference education = findPreference(getString(R.string.s_profile_education));
    education.setSummary(""+Data.s_Profile.questionary_education_id);
    //education.setOnPreferenceClickListener(mOnSexListener);
    
    // finances
    Preference finances = findPreference(getString(R.string.s_profile_finances));
    finances.setSummary(""+Data.s_Profile.questionary_finances_id);
    //finances.setOnPreferenceClickListener(mOnSexListener);
    
    // smoking
    Preference smoking = findPreference(getString(R.string.s_profile_smoking));
    smoking.setSummary(""+Data.s_Profile.questionary_smoking_id);
    //smoking.setOnPreferenceClickListener(mOnSexListener);
    
    // alcohol
    Preference alcohol = findPreference(getString(R.string.s_profile_alcohol));
    alcohol.setSummary(""+Data.s_Profile.questionary_alcohol_id);
    //alcohol.setOnPreferenceClickListener(mOnSexListener);
    
    // commutability
    Preference commutability = findPreference(getString(R.string.s_profile_commutability));
    commutability.setSummary(""+Data.s_Profile.questionary_communication_id);
    //commutability.setOnPreferenceClickListener(mOnSexListener);
    
    // character
    Preference character = findPreference(getString(R.string.s_profile_character));
    character.setSummary(""+Data.s_Profile.questionary_character_id);
    //character.setOnPreferenceClickListener(mOnSexListener);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  /*
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
        data();
        finish();
      break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
  */
}
