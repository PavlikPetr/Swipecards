package com.topface.topface.ui.edit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FilterRequest;
import com.topface.topface.ui.CitySearchActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

import java.util.HashMap;

public class FilterFragment extends AbstractEditFragment implements OnClickListener {

    public static Profile mTargetUser = new User();

    private FormInfo mFormInfo;
    private SharedPreferences mPreferences;
    private Filter mInitFilter;
    private Filter mFilter;
    private ViewGroup mCityFrame;
    private ViewGroup mAgeFrame;

    private ImageView mCheckGirl;
    private ImageView mCheckBoy;
    private ViewGroup mXStatusFrame;
    private ViewGroup mMarriageFrame;
    private ViewGroup mCharacterFrame;
    private ViewGroup mAlcoholFrame;
    private ViewGroup mShowOffFrame;
    private HashMap<Integer, TextView> hashTextViewByTitleId = new HashMap<Integer, TextView>();

    private EditSwitcher mSwitchOnline;
    private EditSwitcher mSwitchBeautifull;

    class Filter implements Cloneable {
        int sex; // пол пользователей
        int age_start; // возраст от
        int age_end; // возраст до
        int city_id; // город в котором ищем пользователей
        String city_name; // город в котором ищем пользователей
        boolean geo; // искать по координатам
        boolean online; // в сети или нет
        boolean beauty; // красивая или нет
        int status_id; // цель знакомства
        int marriage_id; // состоит ли в браке
        int character_id; // характер
        int alcohol_id; // отношение к алкоголю
        int showoff_id; // размер груди или материальное положение

        @Override
        public boolean equals(Object o) {
            if (o instanceof Filter) {
                Filter filter = (Filter) o;

                if (filter.sex != sex) return false;
                else if (filter.age_start != age_start) return false;
                else if (filter.age_end != age_end) return false;
                else if (filter.city_id != city_id) return false;
                else if (filter.geo != geo) return false;
                else if (filter.online != online) return false;
                else if (filter.beauty != beauty) return false;
                else if (filter.status_id != status_id) return false;
                else if (filter.marriage_id != marriage_id) return false;
                else if (filter.character_id != character_id) return false;
                else if (filter.alcohol_id != alcohol_id) return false;
                else if (filter.showoff_id != showoff_id) return false;

                return true;
            } else {
                return false;
            }
        }

        @Override
        protected Filter clone() throws CloneNotSupportedException {
            super.clone();
            Filter filter = new Filter();

            filter.sex = sex;
            filter.age_start = age_start;
            filter.age_end = age_end;
            filter.city_id = city_id;
            filter.geo = geo;
            filter.online = online;
            filter.beauty = beauty;
            filter.status_id = status_id;
            filter.marriage_id = marriage_id;
            filter.character_id = character_id;
            filter.alcohol_id = alcohol_id;
            filter.showoff_id = showoff_id;

            return filter;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTargetUser.sex = CacheProfile.dating_sex;
        mFormInfo = new FormInfo(getActivity().getApplicationContext(), mTargetUser);
        mPreferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_PROFILE,
                Context.MODE_PRIVATE);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_filter, container, false);

        // Navigation bar
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle))
                .setText(R.string.filter_filter);

        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        mBackButton = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setText(R.string.navigation_back_dating);
        mBackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mSaveButton = (Button) getActivity().findViewById(R.id.btnNavigationRightWithText);
        mSaveButton.setText(getResources().getString(R.string.navigation_save));
        mSaveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveChanges(null);
            }
        });

        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

        // Preferences
        initFilter();
        initViews(root);

        return root;
    }

    private void initFilter() {
        if (mFilter == null) {
            mFilter = new Filter();
        }
        mFilter.sex = CacheProfile.dating_sex;
        mFilter.age_start = CacheProfile.dating_age_start;
        mFilter.age_end = CacheProfile.dating_age_end;
        mFilter.city_id = CacheProfile.dating_city_id;
        mFilter.city_name = CacheProfile.dating_city_name;
        mFilter.geo = mPreferences.getBoolean(getString(R.string.cache_profile_filter_geo), false);
        mFilter.online = mPreferences.getBoolean(getString(R.string.cache_profile_filter_online), false);
        mFilter.beauty = mPreferences.getBoolean(getString(R.string.cache_profile_filter_beautiful), false);
        mFilter.status_id = mPreferences.getInt(getString(R.string.cache_profile_filter_status), FormItem.NOT_SPECIFIED_ID);
        mFilter.marriage_id = mPreferences.getInt(getString(R.string.cache_profile_filter_marriage), FormItem.NOT_SPECIFIED_ID);
        mFilter.character_id = mPreferences.getInt(getString(R.string.cache_profile_filter_character), FormItem.NOT_SPECIFIED_ID);
        mFilter.alcohol_id = mPreferences.getInt(getString(R.string.cache_profile_filter_alcohol), FormItem.NOT_SPECIFIED_ID);
        mFilter.showoff_id = mPreferences.getInt(getString(R.string.cache_profile_filter_showoff), FormItem.NOT_SPECIFIED_ID);

        try {
            mInitFilter = mFilter.clone();
        } catch (CloneNotSupportedException e) {
            Debug.log("Filter clone problem: " + e.toString());
        }
    }

    private void saveFilter() {
        CacheProfile.dating_sex = mFilter.sex;
        CacheProfile.dating_age_start = mFilter.age_start;
        CacheProfile.dating_age_end = mFilter.age_end;
        CacheProfile.dating_city_id = mFilter.city_id;
        CacheProfile.dating_city_name = mFilter.city_name;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(getString(R.string.cache_profile_filter_geo), mFilter.geo);
        editor.putBoolean(getString(R.string.cache_profile_filter_online), mFilter.online);
        editor.putBoolean(getString(R.string.cache_profile_filter_beautiful), mFilter.beauty);
        editor.putInt(getString(R.string.cache_profile_filter_status), mFilter.status_id);
        editor.putInt(getString(R.string.cache_profile_filter_marriage), mFilter.marriage_id);
        editor.putInt(getString(R.string.cache_profile_filter_character), mFilter.character_id);
        editor.putInt(getString(R.string.cache_profile_filter_alcohol), mFilter.alcohol_id);
        editor.putInt(getString(R.string.cache_profile_filter_showoff), mFilter.showoff_id);
        editor.commit();

        try {
            mInitFilter = mFilter.clone();
        } catch (CloneNotSupportedException e) {
            Debug.log("Filter clone problem: " + e.toString());
        }
    }

    private void initViews(ViewGroup root) {
        ViewGroup frame;

        // Girl
        frame = (ViewGroup) root.findViewById(R.id.loGirl);
        setBackground(R.drawable.edit_big_btn_top_selector, frame);
        setText(R.string.filter_girl, frame);
        mCheckGirl = (ImageView) frame.findViewById(R.id.ivCheck);
        if (mFilter.sex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
        }
        frame.setOnClickListener(this);

        // Boy
        frame = (ViewGroup) root.findViewById(R.id.loBoy);
        setBackground(R.drawable.edit_big_btn_bottom_selector, frame);
        setText(R.string.filter_boy, frame);
        mCheckBoy = (ImageView) frame.findViewById(R.id.ivCheck);
        if (mFilter.sex == Static.BOY) {
            mCheckBoy.setVisibility(View.VISIBLE);
        }
        frame.setOnClickListener(this);

        // Age
        mAgeFrame = (ViewGroup) root.findViewById(R.id.loAge);
        setBackground(R.drawable.edit_big_btn_top_selector, mAgeFrame);
        setText(buildAgeString(), mAgeFrame);
        mAgeFrame.setOnClickListener(this);

        // City
        mCityFrame = (ViewGroup) root.findViewById(R.id.loCity);
        setBackground(R.drawable.edit_big_btn_bottom_selector, mCityFrame);
        setText(buildCityString(), mCityFrame);
        mCityFrame.setOnClickListener(this);

        // Online
        frame = (ViewGroup) root.findViewById(R.id.loOnline);
        setBackground(R.drawable.edit_big_btn_top_selector, frame);
        setText(R.string.filter_online, frame);
        mSwitchOnline = new EditSwitcher(frame);
        mSwitchOnline.setChecked(mFilter.online);
        frame.setOnClickListener(this);

        // Beautiful
        frame = (ViewGroup) root.findViewById(R.id.loBeautiful);
        setBackground(R.drawable.edit_big_btn_bottom_selector, frame);
        setText(R.string.filter_only_beautiful, frame);
        mSwitchBeautifull = new EditSwitcher(frame);
        mSwitchBeautifull.setChecked(mFilter.beauty);
        frame.setOnClickListener(this);

        // Extra Header
        frame = (ViewGroup) root.findViewById(R.id.loExtraHeader);
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(R.string.filter_extra_parameters);

        // Dating Status
        mXStatusFrame = (ViewGroup) root.findViewById(R.id.loDatingStatus);
        setBackground(R.drawable.edit_big_btn_top_selector, mXStatusFrame);
        setText(R.array.form_main_status,
                mFormInfo.getEntry(R.array.form_main_status, mFilter.status_id), mXStatusFrame);
        mXStatusFrame.setTag(R.array.form_main_status);
        mXStatusFrame.setOnClickListener(this);

        // Marriage
        mMarriageFrame = (ViewGroup) root.findViewById(R.id.loMarriage);
        setBackground(R.drawable.edit_big_btn_middle_selector, mMarriageFrame);
        setText(R.array.form_social_marriage,
                mFormInfo.getEntry(R.array.form_social_marriage, mFilter.marriage_id), mMarriageFrame);
        mMarriageFrame.setTag(R.array.form_social_marriage);
        mMarriageFrame.setOnClickListener(this);

        // Character
        mCharacterFrame = (ViewGroup) root.findViewById(R.id.loCharacter);
        setBackground(R.drawable.edit_big_btn_middle_selector, mCharacterFrame);
        setText(R.array.form_main_character,
                mFormInfo.getEntry(R.array.form_main_character, mFilter.character_id), mCharacterFrame);
        mCharacterFrame.setTag(R.array.form_main_character);
        mCharacterFrame.setOnClickListener(this);

        // Alcohol
        mAlcoholFrame = (ViewGroup) root.findViewById(R.id.loAlcohol);
        setBackground(R.drawable.edit_big_btn_middle_selector, mAlcoholFrame);
        setText(R.array.form_habits_alcohol,
                mFormInfo.getEntry(R.array.form_habits_alcohol, mFilter.alcohol_id), mAlcoholFrame);
        mAlcoholFrame.setTag(R.array.form_habits_alcohol);
        mAlcoholFrame.setOnClickListener(this);

        // ShowOff
        mShowOffFrame = (ViewGroup) root.findViewById(R.id.loShowOff);
        setBackground(R.drawable.edit_big_btn_bottom_selector, mShowOffFrame);
        if (mFilter.sex == Static.GIRL) {
            setText(R.array.form_physique_breast,
                    mFormInfo.getEntry(R.array.form_physique_breast, mFilter.showoff_id), mShowOffFrame);
            mShowOffFrame.setTag(R.array.form_physique_breast);
        } else {
            setText(R.array.form_social_finances,
                    mFormInfo.getEntry(R.array.form_social_finances, mFilter.showoff_id), mShowOffFrame);
            mShowOffFrame.setTag(R.array.form_social_finances);
        }
        mShowOffFrame.setOnClickListener(this);

    }

    private void switchSex(int sex) {
        if (sex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
            mCheckBoy.setVisibility(View.INVISIBLE);
            setText(R.array.form_physique_breast,
                    mFormInfo.getEntry(R.array.form_physique_breast, mFilter.showoff_id), mShowOffFrame);
            mShowOffFrame.setTag(R.array.form_physique_breast);
        } else {
            mCheckBoy.setVisibility(View.VISIBLE);
            mCheckGirl.setVisibility(View.INVISIBLE);
            setText(R.array.form_social_finances,
                    mFormInfo.getEntry(R.array.form_social_finances, mFilter.showoff_id), mShowOffFrame);
            mShowOffFrame.setTag(R.array.form_social_finances);
        }

        mTargetUser.sex = sex;
        mFilter.sex = sex;
    }

    private void setBackground(int resId, ViewGroup frame) {
        ImageView background = (ImageView) frame.findViewById(R.id.ivEditBackground);
        background.setImageResource(resId);
    }

    private void setText(int titleId, String text, ViewGroup frame) {
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(mFormInfo.getFormTitle(titleId));
        TextView textView = (TextView) frame.findViewById(R.id.tvText);
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);
        hashTextViewByTitleId.put(titleId, textView);
    }

    private void setText(String title, ViewGroup frame) {
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(title);
    }

    private void setText(int titleResId, ViewGroup frame) {
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(titleResId);
    }

    private void startEditFilterFormItem(View v, int targetId) {
        int titleId = (Integer) v.getTag();
        String targetValue = mFormInfo.getEntry(titleId, targetId);

        Intent intent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
        intent.putExtra(EditContainerActivity.INTENT_FORM_TITLE_ID, titleId);
        intent.putExtra(EditContainerActivity.INTENT_FORM_DATA_ID, targetId);
        intent.putExtra(EditContainerActivity.INTENT_FORM_DATA, targetValue);
        startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FILTER_FORM_CHOOSE_ITEM);
    }

    private String buildCityString() {
        if (mFilter.city_id == 0) {
            return getResources().getString(R.string.filter_cities_all);
        } else {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(getResources().getString(R.string.filter_in)).append(" ").append(mFilter.city_name);
            return strBuilder.toString();
        }
    }

    private String buildAgeString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(getResources().getString(R.string.filter_from)).append(" ")
                .append(mFilter.age_start).append(" ");
        strBuilder.append(getResources().getString(R.string.filter_to)).append(" ")
                .append(mFilter.age_end);
        return strBuilder.toString();
    }

    int leftPosition;
    int rightPosition;

    private void createNumberPickerDialog() {
        // 0  1  2  3  4  5  total 6
        final int[] ages = {16, 20, 24, 28, 32, 99};
        for (int i = 0; i < ages.length - 1; i++) {
            if (mFilter.age_start >= ages[i])
                leftPosition = i;
            if (mFilter.age_end >= ages[i])
                rightPosition = i;
        }
        View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.pref_age_picker, null);
        final TextView tvFrom = (TextView) view.findViewById(R.id.tvFilterFrom);
        tvFrom.setText("" + mFilter.age_start);
        final TextView tvTo = (TextView) view.findViewById(R.id.tvFilterTo);
        tvTo.setText("" + mFilter.age_end);

        Button fromUp = (Button) view.findViewById(R.id.btnFilterFromUp);
        fromUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftPosition < ages.length - 1)
                    tvFrom.setText("" + (ages[++leftPosition]));
            }
        });
        Button fromDown = (Button) view.findViewById(R.id.btnFilterFromDown);
        fromDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftPosition > 0)
                    tvFrom.setText("" + (ages[--leftPosition]));
            }
        });
        Button toUp = (Button) view.findViewById(R.id.btnFilterToUp);
        toUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightPosition < ages.length - 1)
                    tvTo.setText("" + (ages[++rightPosition]));
            }
        });
        Button toDown = (Button) view.findViewById(R.id.btnFilterToDown);
        toDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightPosition > 0)
                    tvTo.setText("" + (ages[--rightPosition]));
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.filter_age));
        builder.setView(view);
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                mFilter.age_start = Integer.parseInt(tvFrom.getText().toString());
                mFilter.age_end = Integer.parseInt(tvTo.getText().toString());
                if (mFilter.age_start > mFilter.age_end) {
                    mFilter.age_start = mFilter.age_end;
                    leftPosition = rightPosition;
                }
                setText(buildAgeString(), mAgeFrame);
                refreshSaveState();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected boolean hasChanges() {
        return !mInitFilter.equals(mFilter);
    }

    @Override
    protected void saveChanges(Handler handler) {
        prepareRequestSend();

        FilterRequest filterRequest = new FilterRequest(getActivity().getApplicationContext());
        registerRequest(filterRequest);
        filterRequest.beauty = mFilter.beauty;
        filterRequest.city = mFilter.city_id;
        filterRequest.sex = mFilter.sex;
        filterRequest.agebegin = mFilter.age_start;
        filterRequest.ageend = mFilter.age_end;
        filterRequest.xstatus = mFilter.status_id;
        filterRequest.character = mFilter.character_id;
        filterRequest.marriage = mFilter.marriage_id;
        //Финансовое положение и бюст - по сути одно поле, отправляем их оба, что бы не париться с опрееделением пола
        filterRequest.finances = filterRequest.breast = mFilter.showoff_id;
        prepareRequestSend();
        filterRequest.callback(new ApiHandler() {

            @Override
            public void success(ApiResponse response) throws NullPointerException {
                saveFilter();
                refreshSaveState();
                getActivity().setResult(Activity.RESULT_OK);
                finishRequestSend();
                getActivity().finish();
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                getActivity().setResult(Activity.RESULT_CANCELED);
                refreshSaveState();
                finishRequestSend();
            }
        }).exec();

        getActivity().setResult(Activity.RESULT_OK);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loGirl:
                switchSex(Static.GIRL);
                break;
            case R.id.loBoy:
                switchSex(Static.BOY);
                break;
            case R.id.loAge:
                createNumberPickerDialog();
                break;
            case R.id.loCity:
                Intent intent = new Intent(getActivity().getApplicationContext(), CitySearchActivity.class);
                startActivityForResult(intent, CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
                break;
            case R.id.loOnline:
                mSwitchOnline.doSwitch();
                mFilter.online = mSwitchOnline.isChecked();
                break;
            case R.id.loBeautiful:
                mSwitchBeautifull.doSwitch();
                mFilter.beauty = mSwitchBeautifull.isChecked();
                break;
            case R.id.loDatingStatus:
                startEditFilterFormItem(v, mFilter.status_id);
                break;
            case R.id.loMarriage:
                startEditFilterFormItem(v, mFilter.marriage_id);
                break;
            case R.id.loCharacter:
                startEditFilterFormItem(v, mFilter.character_id);
                break;
            case R.id.loAlcohol:
                startEditFilterFormItem(v, mFilter.alcohol_id);
                break;
            case R.id.loShowOff:
                startEditFilterFormItem(v, mFilter.showoff_id);
                break;
        }
        refreshSaveState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();

            if (requestCode == EditContainerActivity.INTENT_EDIT_FILTER_FORM_CHOOSE_ITEM) {
                int titleId = extras.getInt(FilterChooseFormItemFragment.INTENT_TITLE_ID);
                int selectedId = extras.getInt(FilterChooseFormItemFragment.INTENT_SELECTED_ID);

                switch (titleId) {
                    case R.array.form_main_status:
                        mFilter.status_id = selectedId;
                        break;
                    case R.array.form_social_marriage:
                        mFilter.marriage_id = selectedId;
                        break;
                    case R.array.form_main_character:
                        mFilter.character_id = selectedId;
                        break;
                    case R.array.form_habits_alcohol:
                        mFilter.alcohol_id = selectedId;
                        break;
                    case R.array.form_physique_breast:
                        mFilter.showoff_id = selectedId;
                        break;
                    case R.array.form_social_finances:
                        mFilter.showoff_id = selectedId;
                        break;
                }

                hashTextViewByTitleId.get(titleId).setText(mFormInfo.getEntry(titleId, selectedId));
            } else if (requestCode == CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY) {
                int city_id = extras.getInt(CitySearchActivity.INTENT_CITY_ID);
                String city_name = extras.getString(CitySearchActivity.INTENT_CITY_NAME);

                if (city_id == 0) {
                    mFilter.geo = false;
                } else {
                    mFilter.city_id = city_id;
                    mFilter.city_name = city_name;
                }

                setText(buildCityString(), mCityFrame);
            }
            refreshSaveState();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void lockUi() {
    	mBackButton.setEnabled(false);
    	mCheckGirl.setEnabled(false);
    	mCheckBoy.setEnabled(false);
    	mAgeFrame.setEnabled(false);
    	mCityFrame.setEnabled(false);
    	mSwitchOnline.setEnabled(false);
    	mSwitchBeautifull.setEnabled(false);
    	mXStatusFrame.setEnabled(false);
    	mMarriageFrame.setEnabled(false);
    	mCharacterFrame.setEnabled(false);
    	mAlcoholFrame.setEnabled(false);
    	mShowOffFrame.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
    	mBackButton.setEnabled(true);
    	mCheckGirl.setEnabled(true);
    	mCheckBoy.setEnabled(true);
    	mAgeFrame.setEnabled(true);
    	mCityFrame.setEnabled(true);
    	mSwitchOnline.setEnabled(true);
    	mSwitchBeautifull.setEnabled(true);
    	mXStatusFrame.setEnabled(true);
    	mMarriageFrame.setEnabled(true);
    	mCharacterFrame.setEnabled(true);
    	mAlcoholFrame.setEnabled(true);
    	mShowOffFrame.setEnabled(true);
    }
}
