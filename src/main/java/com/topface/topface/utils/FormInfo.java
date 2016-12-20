package com.topface.topface.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputType;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.QuestionaryRequest;
import com.topface.topface.requests.SettingsRequest;

/* понять и простить за эту хуйню */
public class FormInfo {

    // Data
    private Context mContext;
    private Resources mResources;
    private int mSex;
    private int mProfileType;

    public FormInfo(Context context, int sex, int profileType) {
        mResources = context.getResources();
        mSex = sex;
        mProfileType = profileType;
        mContext = context;
    }

    public int getSex() {
        return mSex;
    }

    public void fillFormItem(FormItem formItem) {
        String title = formItem.title;
        String data = formItem.value;
        String emptyValue = formItem.emptyValue;
        try {
            switch (formItem.type) {
                case FormItem.DATA:
                    title = getFormTitle(formItem);
                    if (formItem.dataId != FormItem.NO_RESOURCE_ID) {
                        if (formItem.dataId == 0) {
                            emptyValue = FormItem.EMPTY_FORM_VALUE;
                            formItem.setIsEmpty(true);
                        } else {
                            data = getEntryById(getEntriesByTitleId(formItem.titleId),
                                    getIdsByTitleId(formItem.titleId), formItem.dataId);
                        }
                    } else {
                        if (data.isEmpty()) {
                            emptyValue = FormItem.EMPTY_FORM_VALUE;
                            formItem.setIsEmpty(true);
                        }
                    }
                    break;
                case FormItem.HEADER:
                    title = getFormTitle(formItem);
                case FormItem.STATUS:
                    if (data.isEmpty()) {
                        emptyValue = FormItem.EMPTY_FORM_VALUE;
                        formItem.setIsEmpty(true);
                    }
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
            title = Utils.EMPTY;
        } finally {
            formItem.title = title;
            formItem.value = data;
            formItem.emptyValue = emptyValue;
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

    public String[] getEntriesByTitleId(int titleId) {
        switch (titleId) {
            case R.array.form_main_status:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_status_female : R.array.profile_form_status_male);
            case R.array.form_main_character:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_character_female : R.array.profile_form_character_male);
            case R.array.form_main_communication:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_communication_female : R.array.profile_form_communication_male);
            case R.array.form_habits_alcohol:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_alcohol_female : R.array.profile_form_alcohol_male);
            case R.array.form_habits_smoking:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_smoking_female : R.array.profile_form_smoking_male);
            case R.array.form_physique_eyes:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_eyes_female : R.array.profile_form_eyes_male);
            case R.array.form_physique_fitness:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_fitness_female : R.array.profile_form_fitness_male);
            case R.array.form_physique_hairs:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_hair_female : R.array.profile_form_hair_male);
            case R.array.form_physique_breast:
                return mResources.getStringArray(R.array.profile_form_breast_female);
            case R.array.form_social_car:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_car_female : R.array.profile_form_car_male);
            case R.array.form_social_education:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_education_female : R.array.profile_form_education_male);
            case R.array.form_social_finances:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_finances_female : R.array.profile_form_finances_male);
            case R.array.form_social_residence:
                return mResources.getStringArray(mSex == Profile.GIRL ? R.array.profile_form_residence_female : R.array.profile_form_residence_male);
            default:
                return null;
        }
    }

    public static int getInputType(FormItem formItem) {
        int titleId = formItem.titleId;
        switch (titleId) {
            case R.string.edit_age:
            case R.array.form_main_height:
            case R.array.form_main_weight:
                return InputType.TYPE_CLASS_NUMBER;
            default:
                return InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
        }
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
            case R.array.form_social_residence:
                return mResources.getIntArray(R.array.profile_form_residence_ids);
            default:
                return new int[]{FormItem.NO_RESOURCE_ID};
        }
    }

    private ApiRequest getFormRequest(FormItem item, int titleId, int selectedValueId, String selectedValue) {
        if (titleId == R.array.form_main_status) {
            SettingsRequest request = new SettingsRequest(mContext);
            request.xstatus = selectedValueId;
            return request;
        }

        QuestionaryRequest result = new QuestionaryRequest(mContext);

        switch (titleId) {
            case R.array.form_main_about_status:
                result.status = selectedValue;
                break;
            case R.array.form_main_character:
                result.characterId = selectedValueId;
                break;
            case R.array.form_main_communication:
                result.communicationId = selectedValueId;
                break;
            case R.array.form_habits_alcohol:
                result.alcoholId = selectedValueId;
                break;
            case R.array.form_habits_smoking:
                result.smokingId = selectedValueId;
                break;
            case R.array.form_physique_eyes:
                result.eyeId = selectedValueId;
                break;
            case R.array.form_physique_fitness:
                result.fitnessId = selectedValueId;
                break;
            case R.array.form_physique_hairs:
                result.hairId = selectedValueId;
                break;
            case R.array.form_physique_breast:
                result.breastId = selectedValueId;
                break;
            case R.array.form_social_car:
                result.carId = selectedValueId;
                break;
            case R.array.form_social_education:
                result.educationId = selectedValueId;
                break;
            case R.array.form_social_finances:
                result.financesId = selectedValueId;
                break;
            case R.array.form_social_residence:
                result.residenceId = selectedValueId;
                break;
            case R.array.form_main_height:
                try {
                    result.height = Integer.parseInt(selectedValue);
                } catch (Exception e) {
                    result.height = 0;
                }
                item.value = result.height == 0 ? Utils.EMPTY : String.valueOf(result.height);
                break;
            case R.array.form_main_weight:
                try {
                    result.weight = Integer.parseInt(selectedValue);
                } catch (Exception e) {
                    result.weight = 0;
                }
                item.value = result.weight == 0 ? Utils.EMPTY : String.valueOf(result.weight);
                break;
            case R.array.form_habits_restaurants:
                result.restaurants = selectedValue;
                break;
            case R.array.form_detail_about_dating:
                result.firstDating = selectedValue;
                break;
            case R.array.form_detail_archievements:
                result.achievements = selectedValue;
                break;
        }

        return result;
    }

    public ApiRequest getFormRequest(FormItem item) {
        return getFormRequest(item, item.titleId, item.dataId, item.value);
    }

    // =============================== Form Titles ===============================
    public String getFormTitle(int arrayResourceId) {
        String result = Utils.EMPTY;
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

        if (mProfileType == Profile.TYPE_USER_PROFILE) {
            switch (mSex) {
                case Profile.BOY:
                    result = variants[0];
                    break;
                case Profile.GIRL:
                    result = variants[1];
                    break;
            }
        } else {
            switch (mSex) {
                case Profile.BOY:
                    result = variants[2];
                    break;
                case Profile.GIRL:
                    result = variants[3];
                    break;
            }
        }

        if (result == null) result = Utils.EMPTY;
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
                return Utils.EMPTY;
        }
    }
}
