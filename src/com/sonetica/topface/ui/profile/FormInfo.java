package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import android.content.Context;
import android.content.res.Resources;
/*
 *     понять и простить за эту хуйню
 */
public class FormInfo {
  // Data
  private Resources mResources;
  private int mSex;
  //---------------------------------------------------------------------------
  public FormInfo(Context context,int sex) {
    mResources = context.getResources();
    mSex = sex;
  }
  //---------------------------------------------------------------------------
  public String getEducation(int id){
    switch(id) {
      case 1: return mResources.getString(mSex==0?R.string.profile_form_education_female_1:R.string.profile_form_education_male_1);
      case 2: return mResources.getString(mSex==0?R.string.profile_form_education_female_2:R.string.profile_form_education_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_education_female_3:R.string.profile_form_education_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_education_female_4:R.string.profile_form_education_male_4);
      case 5: return mResources.getString(mSex==0?R.string.profile_form_education_female_5:R.string.profile_form_education_male_5);
      case 6: return mResources.getString(mSex==0?R.string.profile_form_education_female_6:R.string.profile_form_education_male_6);
      case 7: return mResources.getString(mSex==0?R.string.profile_form_education_female_7:R.string.profile_form_education_male_7);
      default: return mResources.getString(R.string.profile_form_empty);
    }
  }
  //---------------------------------------------------------------------------
  public String getCommunication(int id){
    switch(id) {
      case 1: return mResources.getString(mSex==0?R.string.profile_form_communication_female_1:R.string.profile_form_communication_male_1);
      case 2: return mResources.getString(mSex==0?R.string.profile_form_communication_female_2:R.string.profile_form_communication_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_communication_female_3:R.string.profile_form_communication_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_communication_female_4:R.string.profile_form_communication_male_4);
      default: return mResources.getString(R.string.profile_form_empty);
    }    
  }
  //---------------------------------------------------------------------------
  public String getCharacter(int id){
    switch(id) {
      case 1: return mResources.getString(mSex==0?R.string.profile_form_character_female_1:R.string.profile_form_character_male_1);
      case 2: return mResources.getString(mSex==0?R.string.profile_form_character_female_2:R.string.profile_form_character_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_character_female_3:R.string.profile_form_character_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_character_female_4:R.string.profile_form_character_male_4);
      case 5: return mResources.getString(mSex==0?R.string.profile_form_character_female_5:R.string.profile_form_character_male_5);
      case 6: return mResources.getString(mSex==0?R.string.profile_form_character_female_6:R.string.profile_form_character_male_6);
      case 7: return mResources.getString(mSex==0?R.string.profile_form_character_female_7:R.string.profile_form_character_male_7);
      default: return mResources.getString(R.string.profile_form_empty);
    }
  }
  //---------------------------------------------------------------------------
  public String getAlcohol(int id){
    switch(id) {
      case 1: return mResources.getString(mSex==0?R.string.profile_form_alcohol_female_1:R.string.profile_form_alcohol_male_1);
      case 2: return mResources.getString(mSex==0?R.string.profile_form_alcohol_female_2:R.string.profile_form_alcohol_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_alcohol_female_3:R.string.profile_form_alcohol_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_alcohol_female_4:R.string.profile_form_alcohol_male_4);
      case 5: return mResources.getString(mSex==0?R.string.profile_form_alcohol_female_5:R.string.profile_form_alcohol_male_5);
      default: return mResources.getString(R.string.profile_form_empty);
    }   
  }
  //---------------------------------------------------------------------------
  public String getFitness(int id){
    switch(id) {
      case 1: return mResources.getString(mSex==0?R.string.profile_form_fitness_female_1:R.string.profile_form_fitness_male_1);
      case 2: return mResources.getString(mSex==0?R.string.profile_form_fitness_female_2:R.string.profile_form_fitness_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_fitness_female_3:R.string.profile_form_fitness_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_fitness_female_4:R.string.profile_form_fitness_male_4);
      case 5: return mResources.getString(mSex==0?R.string.profile_form_fitness_female_5:R.string.profile_form_fitness_male_5);
      case 6: return mResources.getString(mSex==0?R.string.profile_form_fitness_female_6:R.string.profile_form_fitness_male_6);
      default: return mResources.getString(R.string.profile_form_empty);
    }    
  }
  //---------------------------------------------------------------------------
  public String getJob(int id){
    switch(id) {
      case 2: return mResources.getString(mSex==0?R.string.profile_form_job_female_2:R.string.profile_form_job_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_job_female_3:R.string.profile_form_job_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_job_female_4:R.string.profile_form_job_male_4);
      case 5: return mResources.getString(mSex==0?R.string.profile_form_job_female_5:R.string.profile_form_job_male_5);
      case 6: return mResources.getString(mSex==0?R.string.profile_form_job_female_6:R.string.profile_form_job_male_6);
      case 7: return mResources.getString(mSex==0?R.string.profile_form_job_female_7:R.string.profile_form_job_male_7);
      case 8: return mResources.getString(mSex==0?R.string.profile_form_job_female_8:R.string.profile_form_job_male_8);
      case 9: return mResources.getString(mSex==0?R.string.profile_form_job_female_9:R.string.profile_form_job_male_9);
      case 10: return mResources.getString(mSex==0?R.string.profile_form_job_female_10:R.string.profile_form_job_male_10);
      default: return mResources.getString(R.string.profile_form_empty);
    } 
  }
  //---------------------------------------------------------------------------
  public String getMarriage(int id){
    switch(id) {
      case 1: return mResources.getString(mSex==0?R.string.profile_form_marriage_female_1:R.string.profile_form_marriage_male_1);
      case 2: return mResources.getString(mSex==0?R.string.profile_form_marriage_female_2:R.string.profile_form_marriage_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_marriage_female_3:R.string.profile_form_marriage_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_marriage_female_4:R.string.profile_form_marriage_male_4);
      case 5: return mResources.getString(mSex==0?R.string.profile_form_marriage_female_5:R.string.profile_form_marriage_male_5);
      case 6: return mResources.getString(mSex==0?R.string.profile_form_marriage_female_6:R.string.profile_form_marriage_male_6);
      case 7: return mResources.getString(mSex==0?R.string.profile_form_marriage_female_7:R.string.profile_form_marriage_male_7);
      default: return mResources.getString(R.string.profile_form_empty);
    } 
  }
  //---------------------------------------------------------------------------
  public String getFinances(int id){
    switch(id) {
      case 1: return mResources.getString(mSex==0?R.string.profile_form_finances_female_1:R.string.profile_form_finances_male_1);
      case 2: return mResources.getString(mSex==0?R.string.profile_form_finances_female_2:R.string.profile_form_finances_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_finances_female_3:R.string.profile_form_finances_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_finances_female_4:R.string.profile_form_finances_male_4);
      case 5: return mResources.getString(mSex==0?R.string.profile_form_finances_female_5:R.string.profile_form_finances_male_5);
      case 6: return mResources.getString(mSex==0?R.string.profile_form_finances_female_6:R.string.profile_form_finances_male_6);
      case 7: return mResources.getString(mSex==0?R.string.profile_form_finances_female_7:R.string.profile_form_finances_male_7);
      default: return mResources.getString(R.string.profile_form_empty);
    } 
  }
  //---------------------------------------------------------------------------
  public String getSmoking(int id){
    switch(id) {
      case 1: return mResources.getString(mSex==0?R.string.profile_form_smoking_female_1:R.string.profile_form_smoking_male_1);
      case 2: return mResources.getString(mSex==0?R.string.profile_form_smoking_female_2:R.string.profile_form_smoking_male_2);
      case 3: return mResources.getString(mSex==0?R.string.profile_form_smoking_female_3:R.string.profile_form_smoking_male_3);
      case 4: return mResources.getString(mSex==0?R.string.profile_form_smoking_female_4:R.string.profile_form_smoking_male_4);
      case 5: return mResources.getString(mSex==0?R.string.profile_form_smoking_female_5:R.string.profile_form_smoking_male_5);
      case 6: return mResources.getString(mSex==0?R.string.profile_form_smoking_female_6:R.string.profile_form_smoking_male_6);
      default: return mResources.getString(R.string.profile_form_empty);
    }    
  }
  //---------------------------------------------------------------------------
}
