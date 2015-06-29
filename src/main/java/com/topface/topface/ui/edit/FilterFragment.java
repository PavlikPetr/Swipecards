package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.ui.adapters.SpinnerAdapter;
import com.topface.topface.ui.dialogs.FilterListDialog;
import com.topface.topface.ui.views.CitySearchView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FilterFragment extends AbstractEditFragment implements OnClickListener {

    private static final String FILTER_DIALOG_SHOWN = "dialog_shown";
    public static Profile mTargetUser = new User();
    public static final String INTENT_DATING_FILTER = "Topface_Dating_Filter";
    public static String TAG = "filter_fragment_tag";

    private FormInfo mFormInfo;
    private DatingFilter mInitFilter;
    private DatingFilter mFilter;

    private SparseArrayCompat<TextView> hashTextViewByTitleId = new SparseArrayCompat<>();

    private Spinner mLoFilterSex;
    private Spinner mLoFilterAgeStart;
    private Spinner mLoFilterAgeEnd;

    private CheckBox mLoFilterOnline;
    private CheckBox mLoFilterBeautiful;

    private ViewGroup mLoFilterDatingStatus;
    private ViewGroup mLoFilterMarriage;
    private ViewGroup mLoFilterCharacter;
    private ViewGroup mLoFilterAlcohol;
    private ViewGroup mLoFilterFinance;
    private ViewGroup mLoFilterShowOff;

    private CitySearchView mLoFilterChooseCity;
    private ScrollView mScroll;

    private ImageView mLoFilterButtonHome;

    private boolean mInitFilterOnline;
    private boolean isDialogShown;

    public FilterListDialog.DialogRowCliCkInterface mDialogOnItemClickListener = new FilterListDialog.DialogRowCliCkInterface() {
        @Override
        public void onRowClickListener(int id, int item) {
            switch (id) {
                case R.id.loFilterDatingStatus:
                    mFilter.xstatus = item;
                    setText(mFormInfo.getEntry(R.array.form_main_status, mFilter.xstatus),
                            mLoFilterDatingStatus);
                    break;
                case R.id.loFilterMarriage:
                    mFilter.marriage = item;
                    setText(mFormInfo.getEntry(R.array.form_social_marriage, mFilter.marriage),
                            mLoFilterMarriage);
                    break;
                case R.id.loFilterCharacter:
                    mFilter.character = item;
                    setText(mFormInfo.getEntry(R.array.form_main_character, mFilter.character),
                            mLoFilterCharacter);
                    break;
                case R.id.loFilterAlcohol:
                    mFilter.alcohol = item;
                    setText(mFormInfo.getEntry(R.array.form_habits_alcohol, mFilter.alcohol),
                            mLoFilterAlcohol);
                    break;
                case R.id.loFilterFinance:
                    mFilter.finances = item;
                    setText(mFormInfo.getEntry(R.array.form_social_finances, mFilter.finances),
                            mLoFilterFinance);
                    break;
                case R.id.loFilterShowOff:
                    mFilter.breast = item;
                    setText(mFormInfo.getEntry(R.array.form_physique_breast, mFilter.breast),
                            mLoFilterShowOff);
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
        if (savedInstanceState != null && savedInstanceState.getBoolean(FILTER_DIALOG_SHOWN)) {
            DialogFragment dialog = (DialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(FilterListDialog.TAG);
            dialog.show(getActivity().getSupportFragmentManager(), FilterListDialog.TAG);
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean(FILTER_DIALOG_SHOWN, isDialogShown);
        }
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
        mScroll = (ScrollView) root.findViewById(R.id.filter_scroll);

        // Sex
        mLoFilterSex = (Spinner) root.findViewById(R.id.loFilterSex);
        mLoFilterSex.setAdapter(new SpinnerAdapter(getActivity(),
                R.layout.spinner_text_layout, getSexArray()));
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
        mLoFilterAgeStart.setAdapter(new SpinnerAdapter(getActivity(),
                R.layout.spinner_text_layout, getAgeStartArray(),
                getActivity().getResources().getString(R.string.filter_age_start_prefix)));
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
        mLoFilterAgeEnd.setAdapter(new SpinnerAdapter(getActivity(),
                R.layout.spinner_text_layout, getAgeEndArray(),
                getActivity().getResources().getString(R.string.filter_age_end_prefix)));
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
        mLoFilterChooseCity = (CitySearchView) root.findViewById(R.id.loFilterChooseCity);
        if (!TextUtils.isEmpty(mFilter.city.getName())) {
            mLoFilterChooseCity.setDefaultCity(mFilter.city);
        }
        mLoFilterChooseCity.setOnCityClickListener(new CitySearchView.onCityClickListener() {
            @Override
            public void onClick(City city) {
                mFilter.city = city;
            }
        });
        mLoFilterChooseCity.setScrollableViewToTop(mScroll);
        mLoFilterChooseCity.setOnRootViewListener(new CitySearchView.onRootViewListener() {
            @Override
            public int getHeight() {
                // get fragment height
                return mScroll.getMeasuredHeight();
            }
        });

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
                mFormInfo.getEntry(R.array.form_main_status, mFilter.xstatus),
                mLoFilterDatingStatus);
        mLoFilterDatingStatus.setTag(R.array.form_main_status);
        mLoFilterDatingStatus.setOnClickListener(this);

        // Marriage
        mLoFilterMarriage = (ViewGroup) root.findViewById(R.id.loFilterMarriage);
        setText(R.array.form_social_marriage,
                mFormInfo.getEntry(R.array.form_social_marriage, mFilter.marriage),
                mLoFilterMarriage);
        mLoFilterMarriage.setTag(R.array.form_social_marriage);
        mLoFilterMarriage.setOnClickListener(this);

        // Character
        mLoFilterCharacter = (ViewGroup) root.findViewById(R.id.loFilterCharacter);
        setText(R.array.form_main_character,
                mFormInfo.getEntry(R.array.form_main_character, mFilter.character),
                mLoFilterCharacter);
        mLoFilterCharacter.setTag(R.array.form_main_character);
        mLoFilterCharacter.setOnClickListener(this);

        // Alcohol
        mLoFilterAlcohol = (ViewGroup) root.findViewById(R.id.loFilterAlcohol);
        setText(R.array.form_habits_alcohol,
                mFormInfo.getEntry(R.array.form_habits_alcohol, mFilter.alcohol),
                mLoFilterAlcohol);
        mLoFilterAlcohol.setTag(R.array.form_habits_alcohol);
        mLoFilterAlcohol.setOnClickListener(this);

        // Finance
        mLoFilterFinance = (ViewGroup) root.findViewById(R.id.loFilterFinance);
        setText(R.array.form_social_finances,
                mFormInfo.getEntry(R.array.form_social_finances, mFilter.finances),
                mLoFilterFinance);
        mLoFilterFinance.setTag(R.array.form_social_finances);
        mLoFilterFinance.setOnClickListener(this);

        // ShowOff
        mLoFilterShowOff = (ViewGroup) root.findViewById(R.id.loFilterShowOff);
        setText(R.array.form_physique_breast,
                mFormInfo.getEntry(R.array.form_physique_breast, mFilter.breast),
                mLoFilterShowOff);
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
            getActivity().setResult(Activity.RESULT_CANCELED);
        }
        handler.sendEmptyMessage(0);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loFilterOnline:
                DatingFilter.setOnlyOnlineField(mLoFilterOnline.isChecked());
                break;
            case R.id.loFilterBeautiful:
                mFilter.beautiful = mLoFilterBeautiful.isChecked();
                break;
            case R.id.loFilterDatingStatus:
                createAndShowDialog(R.array.form_main_status, mFilter.xstatus, v.getId(),
                        mDialogOnItemClickListener);
                break;
            case R.id.loFilterMarriage:
                createAndShowDialog(R.array.form_social_marriage, mFilter.marriage, v.getId(),
                        mDialogOnItemClickListener);
                break;
            case R.id.loFilterCharacter:
                createAndShowDialog(R.array.form_main_character, mFilter.character, v.getId(),
                        mDialogOnItemClickListener);
                break;
            case R.id.loFilterAlcohol:
                createAndShowDialog(R.array.form_habits_alcohol, mFilter.alcohol, v.getId(),
                        mDialogOnItemClickListener);
                break;
            case R.id.loFilterFinance:
                createAndShowDialog(R.array.form_social_finances, mFilter.finances, v.getId(),
                        mDialogOnItemClickListener);
                break;
            case R.id.loFilterShowOff:
                createAndShowDialog(R.array.form_physique_breast, mFilter.breast, v.getId(),
                        mDialogOnItemClickListener);
                break;
            case R.id.loFilterButtonHome:
                City city = null;
                try {
                    city = new City(new JSONObject(CacheProfile.city.getName()));
                } catch (JSONException e) {
                    Debug.error(e);
                }
                if (city == null) {
                    mLoFilterChooseCity.setDefaultCity(CacheProfile.city);
                    mFilter.city = CacheProfile.city;
                } else {
                    mLoFilterChooseCity.setDefaultCity(city);
                    mFilter.city = city;
                }
                break;
        }
        refreshSaveState();
    }

    // create array for spinner Sex
    private ArrayList<String> getSexArray() {
        ArrayList<String> array = new ArrayList<>();
        array.add(getActivity().getResources().getString(R.string.general_girls));
        array.add(getActivity().getResources().getString(R.string.general_boys));
        return array;
    }

    // create array for sinner AgeStart
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

    // create array for spinner AgeEnd
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

    // get int spinner selected value without prefix
    private int getSpinnerSelectedAge(Spinner spinner) {
        return Integer.parseInt(((String) (spinner.getAdapter().
                getItem(spinner.getSelectedItemPosition()))).replaceAll("[^\\d]", ""));
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
        /* понять и простить за эту х...ю, так уж FormInfo реализован */
        mFormInfo = new FormInfo(App.getContext(), mFilter.sex, Profile.TYPE_OWN_PROFILE);
        setText(mFormInfo.getEntry(R.array.form_social_marriage, mFilter.marriage), mLoFilterMarriage);
    }

    // show dialog
    private void createAndShowDialog(int titleId, int targetId, int viewId,
                                     FilterListDialog.DialogRowCliCkInterface listener) {
        FilterListDialog dialog = FilterListDialog.newInstance();
        dialog.setData(titleId, targetId, viewId, listener, mFormInfo);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShown = false;
            }
        });
        dialog.show(getActivity().getSupportFragmentManager(), FilterListDialog.TAG);
        isDialogShown = true;
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
        return getString(R.string.filter_search);
    }

    @Override
    public void onPause() {
        Utils.hideSoftKeyboard(getActivity(), mLoFilterChooseCity);
        super.onPause();
    }
}
