package com.topface.topface.ui.edit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.ui.CitySearchActivity;
import com.topface.topface.ui.adapters.FilterDialogAdapter;
import com.topface.topface.ui.adapters.SpinnerAgeAdapter;
import com.topface.topface.ui.views.CustomCitySearchView;
import com.topface.topface.ui.views.LockableScrollView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FilterFragment extends AbstractEditFragment implements OnClickListener {

    public static Profile mTargetUser = new User();
    public static final String INTENT_DATING_FILTER = "Topface_Dating_Filter";

    private static final String FRAGMENT_SEARCH_CITY_TAG = "FilterChooseCityFragment";

    private FormInfo mFormInfo;
    private DatingFilter mInitFilter;
    private DatingFilter mFilter;

    private SparseArrayCompat<TextView> hashTextViewByTitleId = new SparseArrayCompat<>();

    private Spinner mLoFilterSex;
    private Spinner mLoFilterAgeStart;
    private Spinner mLoFilterAgeEnd;

    private LockableScrollView mScroll;

    private CheckBox mLoFilterOnline;
    private CheckBox mLoFilterBeautiful;

    private ViewGroup mLoFilterDatingStatus;
    private ViewGroup mLoFilterMarriage;
    private ViewGroup mLoFilterCharacter;
    private ViewGroup mLoFilterAlcohol;
    private ViewGroup mLoFilterFinance;
    private ViewGroup mLoFilterShowOff;

    CustomCitySearchView mLoFilterChooseCity;


    private ImageView mLoFilterButtonHome;

    private FragmentManager mFragmentManager;

    private boolean mExtraSavingPerformed = false;

    private boolean mInitFilterOnline;

    private DialogRowCliCkInterface mDialogOnItemClickListener = new DialogRowCliCkInterface() {
        @Override
        public void onRowClickListener(int id, int item) {
            switch (id) {
                case R.id.loFilterDatingStatus:
                    mFilter.xstatus = item;
                    setText(mFormInfo.getEntry(R.array.form_main_status, mFilter.xstatus), mLoFilterDatingStatus);
                    break;
                case R.id.loFilterMarriage:
                    mFilter.marriage = item;
                    setText(mFormInfo.getEntry(R.array.form_social_marriage, mFilter.marriage), mLoFilterMarriage);
                    break;
                case R.id.loFilterCharacter:
                    mFilter.character = item;
                    setText(mFormInfo.getEntry(R.array.form_main_character, mFilter.character), mLoFilterCharacter);
                    break;
                case R.id.loFilterAlcohol:
                    mFilter.alcohol = item;
                    setText(mFormInfo.getEntry(R.array.form_habits_alcohol, mFilter.alcohol), mLoFilterAlcohol);
                    break;
                case R.id.loFilterFinance:
                    mFilter.finances = item;
                    setText(mFormInfo.getEntry(R.array.form_social_finances, mFilter.finances), mLoFilterFinance);
                    break;
                case R.id.loFilterShowOff:
                    mFilter.breast = item;
                    setText(mFormInfo.getEntry(R.array.form_physique_breast, mFilter.breast), mLoFilterShowOff);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mTargetUser.sex = CacheProfile.dating != null ? CacheProfile.dating.sex : Static.BOY;
        mFormInfo = new FormInfo(getActivity().getApplicationContext(), mTargetUser.sex, mTargetUser.getType());

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_filter_light_theme, container, false);

        // Preferences
        initFilter();
        initViews(root);

        return root;
    }

    private void initFilter() {
        try {
            //Странный, достаточно редкий баг, но бывает что CacheProfile.dating == null
            mFilter = (CacheProfile.dating != null) ?
                    CacheProfile.dating.clone() :
                    new DatingFilter();
            mInitFilter = mFilter.clone();
            mInitFilterOnline = DatingFilter.getOnlyOnlineField();
        } catch (CloneNotSupportedException e) {
            Debug.error(e);
        }
    }

    private void saveFilter() {
        try {
            mInitFilter = mFilter.clone();
            mInitFilterOnline = DatingFilter.getOnlyOnlineField();
        } catch (CloneNotSupportedException e) {
            Debug.error(e);
        }
    }

    private void initViews(ViewGroup root) {

        // ScrollView
        mScroll = (LockableScrollView) root.findViewById(R.id.filter_scroll);

        // Sex
        mLoFilterSex = (Spinner) root.findViewById(R.id.loFilterSex);
        mLoFilterSex.setAdapter(new SpinnerAgeAdapter(getActivity(), R.layout.spinner_text_layout, getSexArray()));
        mLoFilterSex.setSelection(mFilter.sex);
        mLoFilterSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFilter.sex = position;
                setBraSizeVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // AgeStart
        mLoFilterAgeStart = (Spinner) root.findViewById(R.id.loFilterAgeStart);
        mLoFilterAgeStart.setAdapter(new SpinnerAgeAdapter(getActivity(), R.layout.spinner_text_layout, getAgeStartArray(), getActivity().getResources().getString(R.string.filter_age_start_prefix)));
        mLoFilterAgeStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkStartAge();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // AgeEnd
        mLoFilterAgeEnd = (Spinner) root.findViewById(R.id.loFilterAgeEnd);
        mLoFilterAgeEnd.setAdapter(new SpinnerAgeAdapter(getActivity(), R.layout.spinner_text_layout, getAgeEndArray(), getActivity().getResources().getString(R.string.filter_age_end_prefix)));
        mLoFilterAgeEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkEndAge();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setCurrentAgeStartValue(mFilter.ageStart);
        setCurrentAgeEndValue(mFilter.ageEnd);
        checkStartAge();
        checkEndAge();

        // City
        mLoFilterChooseCity = (CustomCitySearchView) root.findViewById(R.id.loFilterChooseCity);
        if (!TextUtils.isEmpty(mFilter.city.getName())) {
            mLoFilterChooseCity.setDefaultCity(mFilter.city);
        }
        mLoFilterChooseCity.setOnCityClickListener(new CustomCitySearchView.onCityClickListener() {
            @Override
            public void onClick(City city) {
                mFilter.city = city;
            }
        });
        mLoFilterChooseCity.setScrollableViewToTop(mScroll);

        // Online
        mLoFilterOnline = (CheckBox) root.findViewById(R.id.loFilterOnline);
        mLoFilterOnline.setChecked(DatingFilter.getOnlyOnlineField());
        mLoFilterOnline.setOnClickListener(this);

        // Beautiful
        mLoFilterBeautiful = (CheckBox) root.findViewById(R.id.loFilterBeautiful);
        mLoFilterBeautiful.setChecked(mFilter.beautiful);
        mLoFilterBeautiful.setOnClickListener(this);

        // Dating Status
        mLoFilterDatingStatus = (ViewGroup) root.findViewById(R.id.loFilterDatingStatus);
        setText(R.array.form_main_status,
                mFormInfo.getEntry(R.array.form_main_status, mFilter.xstatus), mLoFilterDatingStatus);
        mLoFilterDatingStatus.setTag(R.array.form_main_status);
        mLoFilterDatingStatus.setOnClickListener(this);

        // Marriage
        mLoFilterMarriage = (ViewGroup) root.findViewById(R.id.loFilterMarriage);
        setText(R.array.form_social_marriage,
                mFormInfo.getEntry(R.array.form_social_marriage, mFilter.marriage), mLoFilterMarriage);
        mLoFilterMarriage.setTag(R.array.form_social_marriage);
        mLoFilterMarriage.setOnClickListener(this);

        // Character
        mLoFilterCharacter = (ViewGroup) root.findViewById(R.id.loFilterCharacter);
        setText(R.array.form_main_character,
                mFormInfo.getEntry(R.array.form_main_character, mFilter.character), mLoFilterCharacter);
        mLoFilterCharacter.setTag(R.array.form_main_character);
        mLoFilterCharacter.setOnClickListener(this);

        // Alcohol
        mLoFilterAlcohol = (ViewGroup) root.findViewById(R.id.loFilterAlcohol);
        setText(R.array.form_habits_alcohol,
                mFormInfo.getEntry(R.array.form_habits_alcohol, mFilter.alcohol), mLoFilterAlcohol);
        mLoFilterAlcohol.setTag(R.array.form_habits_alcohol);
        mLoFilterAlcohol.setOnClickListener(this);

        // Finance
        mLoFilterFinance = (ViewGroup) root.findViewById(R.id.loFilterFinance);
        setText(R.array.form_social_finances,
                mFormInfo.getEntry(R.array.form_social_finances, mFilter.finances), mLoFilterFinance);
        mLoFilterFinance.setTag(R.array.form_social_finances);
        mLoFilterFinance.setOnClickListener(this);

        // ShowOff
        mLoFilterShowOff = (ViewGroup) root.findViewById(R.id.loFilterShowOff);
        setText(R.array.form_physique_breast,
                mFormInfo.getEntry(R.array.form_physique_breast, mFilter.breast), mLoFilterShowOff);
        mLoFilterShowOff.setTag(R.array.form_physique_breast);
        mLoFilterShowOff.setOnClickListener(this);
        setBraSizeVisibility();

        // Button Home
        mLoFilterButtonHome = (ImageView) root.findViewById(R.id.loFilterButtonHome);
        mLoFilterButtonHome.setTag(R.id.loFilterButtonHome);
        mLoFilterButtonHome.setOnClickListener(this);
    }

    private void setText(int titleId, String text, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvFilterTitle")).setText(mFormInfo.getFormTitle(titleId));
        TextView textView = (TextView) frame.findViewWithTag("tvFilterText");
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);
        hashTextViewByTitleId.put(titleId, textView);
    }

    private void setText(String title, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvFilterText")).setText(title);
    }

    @Override
    protected boolean hasChanges() {
        return !mInitFilter.equals(mFilter) || mInitFilterOnline != DatingFilter.getOnlyOnlineField();
    }

    @Override
    protected void saveChanges(final Handler handler) {
        if (hasChanges()) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_DATING_FILTER, mFilter);

            getActivity().setResult(Activity.RESULT_OK, intent);

            saveFilter();
        } else {
            if (mExtraSavingPerformed) {
                getActivity().setResult(Activity.RESULT_OK);
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
            }
        }
        handler.sendEmptyMessage(0);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loFilterChooseCity:
                break;
            case R.id.loFilterOnline:
                DatingFilter.setOnlyOnlineField(mLoFilterOnline.isChecked());
                break;
            case R.id.loFilterBeautiful:
                mFilter.beautiful = mLoFilterBeautiful.isChecked();
                break;
            case R.id.loFilterDatingStatus:
                onCreateDialog(R.array.form_main_status, mFilter.xstatus, v.getId(), mDialogOnItemClickListener);
                break;
            case R.id.loFilterMarriage:
                onCreateDialog(R.array.form_social_marriage, mFilter.marriage, v.getId(), mDialogOnItemClickListener);
                break;
            case R.id.loFilterCharacter:
                onCreateDialog(R.array.form_main_character, mFilter.character, v.getId(), mDialogOnItemClickListener);
                break;
            case R.id.loFilterAlcohol:
                onCreateDialog(R.array.form_habits_alcohol, mFilter.alcohol, v.getId(), mDialogOnItemClickListener);
                break;
            case R.id.loFilterFinance:
                onCreateDialog(R.array.form_social_finances, mFilter.finances, v.getId(), mDialogOnItemClickListener);
                break;
            case R.id.loFilterShowOff:
                onCreateDialog(R.array.form_physique_breast, mFilter.breast, v.getId(), mDialogOnItemClickListener);
                break;
            case R.id.loFilterButtonHome:
                City city = null;
                try {
                    city = new City(new JSONObject(CacheProfile.city.getName()));
                } catch (JSONException e) {
                    Debug.error(e);
                }
                if (city == null) {
                    mLoFilterChooseCity.setText(CacheProfile.city.getName());
                    mFilter.city = CacheProfile.city;
                } else {
                    mLoFilterChooseCity.setText(city.getName());
                    mFilter.city = city;
                }
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
                        mFilter.xstatus = selectedId;
                        break;
                    case R.array.form_social_marriage:
                        mFilter.marriage = selectedId;
                        break;
                    case R.array.form_main_character:
                        mFilter.character = selectedId;
                        break;
                    case R.array.form_habits_alcohol:
                        mFilter.alcohol = selectedId;
                        break;
                    case R.array.form_physique_breast:
                        mFilter.breast = selectedId;
                        break;
                    case R.array.form_social_finances:
                        mFilter.finances = selectedId;
                        break;
                }

                TextView item = hashTextViewByTitleId.get(titleId);
                if (item != null) item.setText(mFormInfo.getEntry(titleId, selectedId));
            } else if (requestCode == CitySearchActivity.INTENT_CITY_SEARCH_FROM_FILTER_ACTIVITY) {
                try {
                    mFilter.city = new City(new JSONObject(extras.getString(CitySearchActivity.INTENT_CITY)));
                } catch (JSONException e) {
                    Debug.error(e);
                }
            } else if (requestCode == EditContainerActivity.INTENT_EDIT_AGE) {
                int ageStart = extras.getInt(EditContainerActivity.INTENT_AGE_START);
                int ageEnd = extras.getInt(EditContainerActivity.INTENT_AGE_END);
                if (ageEnd != 0 && ageStart != 0) {
                    if (ageEnd == EditAgeFragment.absoluteMax) {
                        ageEnd = DatingFilter.MAX_AGE;
                    }
                }
                mFilter.ageEnd = ageEnd;
                mFilter.ageStart = ageStart;
            }
            refreshSaveState();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private ArrayList<String> getSexArray() {
        ArrayList<String> array = new ArrayList<>();
        array.add(getActivity().getResources().getString(R.string.general_girls));
        array.add(getActivity().getResources().getString(R.string.general_boys));
        return array;
    }

    private ArrayList<String> getAgeStartArray() {
        ArrayList<String> array = new ArrayList<>();
        for (int i = DatingFilter.MIN_AGE; i <= DatingFilter.MAX_AGE - DatingFilter.DIFF_AGE; i++) {
            array.add(Integer.toString(i));
        }
        return array;
    }

    private int getAgeStartCurrentPosition(int currentAge) {
        return findCurrentPositionInArray(Integer.toString(currentAge), getAgeStartArray());
    }

    private ArrayList<String> getAgeEndArray() {
        ArrayList<String> array = new ArrayList<>();
        for (int i = DatingFilter.MIN_AGE + DatingFilter.DIFF_AGE; i <= DatingFilter.MAX_AGE; i++) {
            array.add(Integer.toString(i));
        }
        return array;
    }

    private int getAgeEndCurrentPosition(int currentAge) {
        return findCurrentPositionInArray(Integer.toString(currentAge), getAgeEndArray());
    }

    private int findCurrentPositionInArray(String currentAge, ArrayList<String> array) {
        int res = 0;
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(currentAge)) {
                res = i;
                break;
            }
        }
        return res;
    }

    private void checkStartAge() {
        int startAge = getSpinnerSelectedAge(mLoFilterAgeStart);
        int endAge = getSpinnerSelectedAge(mLoFilterAgeEnd);
        mFilter.ageStart = startAge;
        if (startAge + DatingFilter.DIFF_AGE > endAge) {
            setCurrentAgeEndValue(startAge + DatingFilter.DIFF_AGE);
        }
    }

    private void checkEndAge() {
        int startAge = getSpinnerSelectedAge(mLoFilterAgeStart);
        int endAge = getSpinnerSelectedAge(mLoFilterAgeEnd);
        mFilter.ageEnd = endAge;
        if (endAge - DatingFilter.DIFF_AGE < startAge) {
            setCurrentAgeStartValue(endAge - DatingFilter.DIFF_AGE);
        }
    }

    // get int spinner selected value without characters
    private int getSpinnerSelectedAge(Spinner spinner) {
        return Integer.parseInt(((String) (spinner.getAdapter().getItem(spinner.getSelectedItemPosition()))).replaceAll("[^\\d]", ""));
    }

    private void setCurrentAgeStartValue(final int value) {
        mLoFilterAgeStart.setSelection(getAgeStartCurrentPosition(value));
        mFilter.ageStart = value;

    }

    private void setCurrentAgeEndValue(final int value) {
        mLoFilterAgeEnd.setSelection(getAgeEndCurrentPosition(value));
        mFilter.ageEnd = value;
    }

    private void setBraSizeVisibility() {
        if (mFilter.sex == Static.GIRL) {
            mLoFilterShowOff.setVisibility(View.VISIBLE);
            mLoFilterFinance.setVisibility(View.GONE);
        } else {
            mLoFilterShowOff.setVisibility(View.GONE);
            mLoFilterFinance.setVisibility(View.VISIBLE);
        }
    }

    private void onCreateDialog(final int titleId, int targetId, final int viewId, final DialogRowCliCkInterface listener) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.filter_dialog_layout, null);
        ListView myList = (ListView) view.findViewWithTag("loFilterList");
        myList.setAdapter(new FilterDialogAdapter(getActivity(), R.layout.filter_edit_form_dialog_cell, mFormInfo.getEntriesByTitleId(titleId), mFormInfo.getEntry(titleId, targetId)));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        final Dialog dialog = builder.create();
        dialog.show();
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onRowClickListener(viewId, mFormInfo.getIdsByTitleId(titleId)[position]);
                }
                dialog.dismiss();
            }
        });
    }

    private interface DialogRowCliCkInterface {
        void onRowClickListener(int id, int item);
    }

    @Override
    protected void lockUi() {
        mLoFilterSex.setEnabled(false);
        mLoFilterAgeStart.setEnabled(false);
        mLoFilterAgeEnd.setEnabled(false);
        mLoFilterChooseCity.setEnabled(false);
        mLoFilterOnline.setEnabled(false);
        mLoFilterBeautiful.setEnabled(false);
        mLoFilterDatingStatus.setEnabled(false);
        mLoFilterMarriage.setEnabled(false);
        mLoFilterCharacter.setEnabled(false);
        mLoFilterAlcohol.setEnabled(false);
        mLoFilterFinance.setEnabled(false);
        mLoFilterShowOff.setEnabled(false);
        mLoFilterButtonHome.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
        mLoFilterSex.setEnabled(true);
        mLoFilterAgeStart.setEnabled(true);
        mLoFilterAgeEnd.setEnabled(true);
        mLoFilterChooseCity.setEnabled(true);
        mLoFilterOnline.setEnabled(true);
        mLoFilterBeautiful.setEnabled(true);
        mLoFilterDatingStatus.setEnabled(true);
        mLoFilterMarriage.setEnabled(true);
        mLoFilterCharacter.setEnabled(true);
        mLoFilterAlcohol.setEnabled(true);
        mLoFilterFinance.setEnabled(true);
        mLoFilterShowOff.setEnabled(true);
        mLoFilterButtonHome.setEnabled(true);
    }

    @Override
    protected void refreshSaveState() {
        super.refreshSaveState();

    }

    @Override
    protected void prepareRequestSend() {
        super.prepareRequestSend();

    }

    @Override
    protected void finishRequestSend() {
        super.finishRequestSend();

    }

    @Override
    protected String getTitle() {
        return getString(R.string.filter_filter);
    }

    @Override
    public void onPause() {
        Utils.hideSoftKeyboard(getActivity(), mLoFilterChooseCity);
        super.onPause();
    }
}
