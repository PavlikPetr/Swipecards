package com.topface.topface.banners;

import android.support.annotation.StringRes;

import com.appodeal.ads.UserSettings;
import com.topface.topface.R;

/**
 * Created by Петр on 18.03.2016.
 * формируем правила для отправки дополнительных данных о пользователе
 */
public class AppodealUserSettingsRules {

    // приводим соответствие данных о семейном положении в Appodeal тому, что есть у нас
    public enum Relation {
        SINGLE(UserSettings.Relation.SINGLE, R.string.profile_form_marriage_female_7, R.string.profile_form_marriage_male_7,
                R.string.profile_form_marriage_female_0, R.string.profile_form_marriage_male_0,
                R.string.profile_form_marriage_female_1, R.string.profile_form_marriage_male_1),
        DATING(UserSettings.Relation.DATING, R.string.profile_form_marriage_female_2, R.string.profile_form_marriage_male_2),
        MARRIED(UserSettings.Relation.MARRIED, R.string.profile_form_marriage_female_3, R.string.profile_form_marriage_male_3,
                R.string.profile_form_marriage_female_4, R.string.profile_form_marriage_male_4,
                R.string.profile_form_marriage_female_5, R.string.profile_form_marriage_male_5,
                R.string.profile_form_marriage_female_6, R.string.profile_form_marriage_male_6);

        private UserSettings.Relation mRelation;
        @StringRes
        private int[] mIdsArray;

        Relation(UserSettings.Relation relation, @StringRes int... idsArray) {
            mRelation = relation;
            mIdsArray = idsArray;
        }

        public UserSettings.Relation getRelation() {
            return mRelation;
        }

        @StringRes
        public int[] getIdsArray() {
            return mIdsArray;
        }
    }

    // приводим соответствие данных об алкоголе в Appodeal тому, что есть у нас
    public enum Alcohol {
        NEGATIVE(UserSettings.Alcohol.NEGATIVE, R.string.profile_form_alcohol_female_0, R.string.profile_form_alcohol_male_0,
                R.string.profile_form_alcohol_female_1, R.string.profile_form_alcohol_male_1),
        NEUTRAL(UserSettings.Alcohol.NEUTRAL, R.string.profile_form_alcohol_female_2, R.string.profile_form_alcohol_male_2),
        POSITIVE(UserSettings.Alcohol.POSITIVE, R.string.profile_form_alcohol_female_3, R.string.profile_form_alcohol_male_3,
                R.string.profile_form_alcohol_female_4, R.string.profile_form_alcohol_male_4);

        private UserSettings.Alcohol mAlcohol;
        @StringRes
        private int[] mIdsArray;

        Alcohol(UserSettings.Alcohol alcohol, @StringRes int... idsArray) {
            mAlcohol = alcohol;
            mIdsArray = idsArray;
        }

        public UserSettings.Alcohol getAlcohol() {
            return mAlcohol;
        }

        @StringRes
        public int[] getIdsArray() {
            return mIdsArray;
        }
    }


    // приводим соответствие данных о курении в Appodeal тому, что есть у нас
    public enum Smoking {
        NEGATIVE(UserSettings.Smoking.NEGATIVE, R.string.profile_form_smoking_female_0, R.string.profile_form_smoking_male_0,
                R.string.profile_form_smoking_female_1, R.string.profile_form_smoking_male_1),
        NEUTRAL(UserSettings.Smoking.NEUTRAL, R.string.profile_form_smoking_female_2, R.string.profile_form_smoking_male_2),
        POSITIVE(UserSettings.Smoking.POSITIVE, R.string.profile_form_smoking_female_3, R.string.profile_form_smoking_male_3,
                R.string.profile_form_smoking_female_4, R.string.profile_form_smoking_male_4,
                R.string.profile_form_smoking_female_5, R.string.profile_form_smoking_male_5);

        private UserSettings.Smoking mSmoking;
        @StringRes
        private int[] mIdsArray;

        Smoking(UserSettings.Smoking smoking, @StringRes int... idsArray) {
            mSmoking = smoking;
            mIdsArray = idsArray;
        }

        public UserSettings.Smoking getSmoking() {
            return mSmoking;
        }

        @StringRes
        public int[] getIdsArray() {
            return mIdsArray;
        }
    }
}
