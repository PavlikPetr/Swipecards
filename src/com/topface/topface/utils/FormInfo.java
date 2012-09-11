package com.topface.topface.utils;

import com.topface.topface.R;
import com.topface.topface.Static;

import android.content.Context;
import android.content.res.Resources;
import com.topface.topface.data.Profile;

/* понять и простить за эту хуйню */
public class FormInfo {	
	
    // Data
    private Resources mResources;
    private Profile mProfile;
    private int mSex;

    public FormInfo(Context context, Profile profile) {    	
        mResources = context.getResources();
        mProfile = profile;
        mSex = profile.sex;
    }

    public void setSex(int sex) {
        mSex = sex;
    }
    
    
    // =============================== Common methods ===============================
    public void fillFormItem(FormItem formItem) {
    	String title = formItem.title;
    	String data = formItem.data;
    	try {
	    	switch (formItem.type) {
			case FormItem.DATA:
				title = getFormTitle(formItem);
				data = getEntryById(getEntriesByTitleId(formItem.titleId), getIdsByTitleId(formItem.titleId), formItem.dataId);
				break;
			case FormItem.HEADER:
				title = getFormTitle(formItem);			
			default:
				break;
			}
    	} catch (Exception e) {
    		
    	} finally {
    		formItem.title = title;
    		formItem.data = data;
    	}
    }

    private String getEntryById(String[] entries, int[] ids, int targetId) {
    	if(entries.length != ids.length) {
    		Debug.error("Form entries' length don't match ids' length");
    		return null;
    	}
    	
    	for (int i = 0; i < ids.length; i++) {
			if (ids[i] == targetId) {
				return entries[i]; 
			}
		}
    	return null;
    }
    
    public String[] getEntriesByTitleId(int titleId) {    	
    	switch (titleId) {
		case R.array.form_main_character:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_character_female : R.array.profile_form_character_male);
		case R.array.form_main_communication:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_communication_female : R.array.profile_form_communication_male);
		case R.array.form_habits_alcohol:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_alcohol_female : R.array.profile_form_alcohol_male);
		case R.array.form_habits_smoking:
	        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_smoking_female : R.array.profile_form_smoking_male);
		case R.array.form_physique_eyes:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_eyes_female : R.array.profile_form_eyes_male);
		case R.array.form_physique_fitness:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_fitness_female : R.array.profile_form_fitness_male);
		case R.array.form_physique_hairs:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_hair_female : R.array.profile_form_hair_male);
		case R.array.form_social_car:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_car_female : R.array.profile_form_car_male);
		case R.array.form_social_education:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_education_female : R.array.profile_form_education_male);
		case R.array.form_social_finances:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_finances_female : R.array.profile_form_finances_male);
		case R.array.form_social_marriage:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_marriage_female : R.array.profile_form_marriage_male);
		case R.array.form_social_residence:
			return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_residence_female : R.array.profile_form_residence_male);	        
		default:
			return null;			
		}
    }
    
    public int[] getIdsByTitleId(int titleId) {
    	switch (titleId) {
		case R.array.form_main_character:
			return mResources.getIntArray(R.array.profile_form_character_ids);
		case R.array.form_main_communication:
			return mResources.getIntArray(R.array.profile_form_communication_ids);
		case R.array.form_habits_alcohol:
			return mResources.getIntArray(R.array.profile_form_alcohol_ids);
		case R.array.form_habits_smoking:
			return mResources.getIntArray(R.array.profile_form_smoking_ids);
		case R.array.form_physique_eyes:
			return mResources.getIntArray(R.array.profile_form_eyes_ids);
		case R.array.form_physique_fitness:
			return mResources.getIntArray(R.array.profile_form_fitness_ids);
		case R.array.form_physique_hairs:
			return mResources.getIntArray(R.array.profile_form_hair_ids);
		case R.array.form_social_car:
			return mResources.getIntArray(R.array.profile_form_car_ids);
		case R.array.form_social_education:
			return mResources.getIntArray(R.array.profile_form_education_ids);
		case R.array.form_social_finances:
			return mResources.getIntArray(R.array.profile_form_finances_ids);
		case R.array.form_social_marriage:
			return mResources.getIntArray(R.array.profile_form_marriage_ids);
		case R.array.form_social_residence:
			return mResources.getIntArray(R.array.profile_form_residence_ids);
		default:
			return null;
		}
    }
    
//    // =============================== Education ===============================
//    public String getEducation(int id) {
//    	return getEndtryById(getEducationEntries(), getEducationIds(), id);
//    }
//
//    public String[] getEducationEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_education_female : R.array.profile_form_education_male);
//    }
//
//    public int[] getEducationIds() {
//        return mResources.getIntArray(R.array.profile_form_education_ids);
//    }
//
//    // =============================== Communication ===============================
//    public String getCommunication(int id) {
//    	return getEndtryById(getCommunicationEntries(), getCommunicationIds(), id);
//    }
//
//    public String[] getCommunicationEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_communication_female : R.array.profile_form_communication_male);
//    }
//
//    public int[] getCommunicationIds() {
//        return mResources.getIntArray(R.array.profile_form_communication_ids);
//    }
//    
//    // =============================== Character ===============================
//    public String getCharacter(int id) {
//    	return getEndtryById(getCharacterEntries(), getCharacterIds(), id);
//    }
//
//    public String[] getCharacterEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_character_female : R.array.profile_form_character_male);
//    }
//
//    public int[] getCharacterIds() {
//        return mResources.getIntArray(R.array.profile_form_character_ids);
//    }
//
//    // =============================== Alcohol ===============================
//    public String getAlcohol(int id) {
//    	return getEndtryById(getAlcoholEntries(), getAlcoholIds(), id);
//    }
//
//    public String[] getAlcoholEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_alcohol_female : R.array.profile_form_alcohol_male);
//    }
//
//    public int[] getAlcoholIds() {
//        return mResources.getIntArray(R.array.profile_form_alcohol_ids);
//    }
//
//    // =============================== Fitness ===============================
//    public String getFitness(int id) {
//    	return getEndtryById(getFitnessEntries(), getFitnessIds(), id);
//    }
//
//    public String[] getFitnessEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_fitness_female : R.array.profile_form_fitness_male);
//    }
//
//    public int[] getFitnessIds() {
//        return mResources.getIntArray(R.array.profile_form_fitness_ids);
//    }
//
//    // =============================== Job ===============================
////    public String getJob(int id) {
////        switch (id) {
////            case 2:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_2 : R.string.profile_form_job_male_2);
////            case 3:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_3 : R.string.profile_form_job_male_3);
////            case 4:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_4 : R.string.profile_form_job_male_4);
////            case 5:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_5 : R.string.profile_form_job_male_5);
////            case 6:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_6 : R.string.profile_form_job_male_6);
////            case 7:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_7 : R.string.profile_form_job_male_7);
////            case 8:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_8 : R.string.profile_form_job_male_8);
////            case 9:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_9 : R.string.profile_form_job_male_9);
////            case 10:
////                return mResources.getString(mSex == Static.GIRL ? R.string.profile_form_job_female_10 : R.string.profile_form_job_male_10);
////            default:
////                return null;//mResources.getString(R.string.profile_form_empty);
////        }
////    }
//
//    // =============================== Marriage ===============================
//    public String getMarriage(int id) {
//    	return getEndtryById(getMarriageEntries(), getMarriageIds(), id);
//    }
//
//    public String[] getMarriageEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_marriage_female : R.array.profile_form_marriage_male);
//    }
//
//    public int[] getMarriageIds() {
//        return mResources.getIntArray(R.array.profile_form_marriage_ids);
//    }
//
//    // =============================== Finances ===============================
//    public String getFinances(int id) {
//    	return getEndtryById(getFinancesEntries(), getFinancesIds(), id);
//    }
//
//    public String[] getFinancesEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_finances_female : R.array.profile_form_finances_male);
//    }
//
//    public int[] getFinancesIds() {
//        return mResources.getIntArray(R.array.profile_form_finances_ids);
//    }
//
//    // =============================== Smoking ===============================
//    public String getSmoking(int id) {
//    	return getEndtryById(getSmokingEntries(), getSmokingIds(), id);
//    }
//
//    public String[] getSmokingEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_smoking_female : R.array.profile_form_smoking_male);
//    }
//
//    public int[] getSmokingIds() {
//        return mResources.getIntArray(R.array.profile_form_smoking_ids);
//    }
//    
//    // =============================== Hair ===============================
//    public String getHair(int id) {
//    	return getEndtryById(getHairEntries(), getHairIds(), id);
//    }
//
//    public String[] getHairEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_hair_female : R.array.profile_form_hair_male);
//    }
//
//    public int[] getHairIds() {
//        return mResources.getIntArray(R.array.profile_form_hair_ids);
//    }
//    
//    // =============================== Eyes ===============================
//    public String getEyes(int id) {
//    	return getEndtryById(getEyesEntries(), getEyesIds(), id);
//    }
//
//    public String[] getEyesEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_eyes_female : R.array.profile_form_eyes_male);
//    }
//
//    public int[] getEyesIds() {
//        return mResources.getIntArray(R.array.profile_form_eyes_ids);
//    }
//
//    // =============================== Residence ===============================
//    public String getResidence(int id) {
//    	return getEndtryById(getResidenceEntries(), getResidenceIds(), id);
//    }
//
//    public String[] getResidenceEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_residence_female : R.array.profile_form_residence_male);
//    }
//
//    public int[] getResidenceIds() {
//        return mResources.getIntArray(R.array.profile_form_residence_ids);
//    }
//    
//    // =============================== Car ===============================
//    public String getCar(int id) {
//    	return getEndtryById(getCarEntries(), getCarIds(), id);
//    }
//
//    public String[] getCarEntries() {
//        return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_car_female : R.array.profile_form_car_male);
//    }
//
//    public int[] getCarIds() {
//        return mResources.getIntArray(R.array.profile_form_car_ids);
//    }
    
    // =============================== Form Titles ===============================
    public String getFormTitle(int arrayResourceId) {
    	String result = Static.EMPTY;
    	String[] variants = mResources.getStringArray(arrayResourceId);
    	if (variants == null)
    		return result;
    	if (variants.length <= 0) 
    		return result;
    	if (variants.length == 1) {
    		return variants[0];
    	}
    	
    	if (mProfile instanceof Profile) {    		
    		switch(mSex) {
    		case Static.BOY:
    			result = variants[2];
    			break;
    		case Static.GIRL:
    			result = variants[3];
    			break;
    		}
    	} else {
    		switch(mSex) {
    		case Static.BOY:
    			result = variants[0];
    			break;
    		case Static.GIRL:
    			result = variants[1];
    			break;
    		}
    	}
    	
    	return result;
    }
    
    public String getFormTitle(FormItem formItem) {
    	switch (formItem.type) {
    	case FormItem.HEADER:
    		return mResources.getString(formItem.titleId);    		
    	case FormItem.DATA:
    		return getFormTitle(formItem.titleId);
    	default:
    		return Static.EMPTY;
    	}
    }
}
