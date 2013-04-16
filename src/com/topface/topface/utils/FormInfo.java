package com.topface.topface.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputType;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.requests.QuestionaryRequest;

/* понять и простить за эту хуйню */
public class FormInfo {

    // Data
    private Context mContext;
    private Resources mResources;
    private Profile mProfile;
    private int mSex;

    public FormInfo(Context context, Profile profile) {
        mResources = context.getResources();
        if (profile != null) {
            mProfile = profile;
            mSex = profile.sex;
        }
        mContext = context;
    }

    // =============================== Common methods ===============================
    public void fillFormItem(FormItem formItem) {
        String title = formItem.title;
        String data = formItem.value;
        try {
            switch (formItem.type) {
                case FormItem.DATA:
                    title = getFormTitle(formItem);
                    if (formItem.dataId != FormItem.NO_RESOURCE_ID) {
                        data = getEntryById(getEntriesByTitleId(formItem.titleId), getIdsByTitleId(formItem.titleId), formItem.dataId);
                    }
                    break;
                case FormItem.HEADER:
                    title = getFormTitle(formItem);
                case FormItem.STATUS:
                    title = getFormTitle(formItem);
                    if (formItem.dataId != FormItem.NO_RESOURCE_ID) {
                        data = getEntryById(getEntriesByTitleId(formItem.titleId), getIdsByTitleId(formItem.titleId), formItem.dataId);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Debug.error(e);
            title = Static.EMPTY;
        } finally {
            formItem.title = title;
            formItem.value = data;
        }
    }

    private String getEntryById(String[] entries, int[] ids, int targetId) {
        if (entries == null || ids == null || entries.length != ids.length) {
            return null;
        }

        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == targetId) {
                return entries[i];
            }
        }
        return null;
    }

    public String getEntry(int titleId, int targetId) {
        return getEntryById(getEntriesByTitleId(titleId), getIdsByTitleId(titleId), targetId);
    }

    public String[] getEntriesByTitleId(int titleId) {
        switch (titleId) {
            case R.array.form_main_status:
                return mResources.getStringArray(mSex == Static.GIRL ? R.array.profile_form_status_female : R.array.profile_form_status_male);
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
            case R.array.form_physique_breast:
                return mResources.getStringArray(R.array.profile_form_breast_female);
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

    public String[] getEntriesByTitleId(int titleId, String[] defaultEntries) {
        String[] entries = getEntriesByTitleId(titleId);
        if (entries == null) return defaultEntries;
        else return entries;
    }

    public int[] getIdsByTitleId(int titleId) {
        switch (titleId) {
            case R.array.form_main_status:
                return mResources.getIntArray(R.array.profile_form_status_ids);
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
            case R.array.form_physique_breast:
                return mResources.getIntArray(R.array.profile_form_breast_ids);
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
                return new int[]{FormItem.NO_RESOURCE_ID};
        }
    }

    public QuestionaryRequest getFormRequest(int titleId, int selectedValueId, String selectedValue) {
        QuestionaryRequest result = new QuestionaryRequest(mContext);

        switch (titleId) {
            case R.array.form_main_status:
                result.statusid = selectedValueId;
                break;
            case R.array.form_main_about_status:
                result.status = selectedValue;
                break;
            case R.array.form_main_character:
                result.characterid = selectedValueId;
                break;
            case R.array.form_main_communication:
                result.communicationid = selectedValueId;
                break;
            case R.array.form_habits_alcohol:
                result.alcoholid = selectedValueId;
                break;
            case R.array.form_habits_smoking:
                result.smokingid = selectedValueId;
                break;
            case R.array.form_physique_eyes:
                result.eyeid = selectedValueId;
                break;
            case R.array.form_physique_fitness:
                result.fitnessid = selectedValueId;
                break;
            case R.array.form_physique_hairs:
                result.hairid = selectedValueId;
                break;
            case R.array.form_physique_breast:
                result.breastid = selectedValueId;
                break;
            case R.array.form_social_car:
                result.carid = selectedValueId;
                break;
            case R.array.form_social_education:
                result.educationid = selectedValueId;
                break;
            case R.array.form_social_finances:
                result.financesid = selectedValueId;
                break;
            case R.array.form_social_marriage:
                result.marriageid = selectedValueId;
                break;
            case R.array.form_social_residence:
                result.residenceid = selectedValueId;
                break;
            case R.array.form_main_height:
                try {
                    result.height = Integer.parseInt(selectedValue);
                } catch (Exception e) {
                    result.height = 0;
                }
                break;
            case R.array.form_main_weight:
                try {
                    result.weight = Integer.parseInt(selectedValue);
                } catch (Exception e) {
                    result.weight = 0;
                }
                break;
            case R.array.form_habits_restaurants:
                result.restaurants = selectedValue;
                break;
            case R.array.form_detail_about_dating:
                result.firstdating = selectedValue;
                break;
            case R.array.form_detail_archievements:
                result.achievements = selectedValue;
                break;
        }

        return result;
    }

    public QuestionaryRequest getFormRequest(FormItem item) {
        return getFormRequest(item.titleId, item.dataId, item.value);
    }

    public int getInputType(int titleId) {
        int result;
        switch (titleId) {
            case R.array.form_main_height:
                result = InputType.TYPE_CLASS_NUMBER;
                break;
            case R.array.form_main_weight:
                result = InputType.TYPE_CLASS_NUMBER;
                break;
            default:
                result = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
                break;
        }

        return result;
    }

    // =============================== Form Titles ===============================
    public String getFormTitle(int arrayResourceId) {
        String result = Static.EMPTY;
        String[] variants = null;
        try {
            variants = mResources.getStringArray(arrayResourceId);
        } catch (Exception ex) {
            Debug.log("No resource for ID=" + arrayResourceId);
        }

        if (variants == null)
            return result;
        if (variants.length <= 0)
            return result;
        if (variants.length == 1) {
            return variants[0] == null ? result : variants[0];
        }

        if (mProfile instanceof User) {
            switch (mSex) {
                case Static.BOY:
                    result = variants[0];
                    break;
                case Static.GIRL:
                    result = variants[1];
                    break;
            }
        } else {
            switch (mSex) {
                case Static.BOY:
                    result = variants[2];
                    break;
                case Static.GIRL:
                    result = variants[3];
                    break;
            }
        }

        if (result == null) result = Static.EMPTY;
        return result;
    }

    public String getFormTitle(FormItem formItem) {
        switch (formItem.type) {
            case FormItem.HEADER:
                return mResources.getString(formItem.titleId);
            case FormItem.DATA:
                return getFormTitle(formItem.titleId);
            case FormItem.STATUS:
                return getFormTitle(formItem.titleId);
            default:
                return Static.EMPTY;
        }
    }
}
