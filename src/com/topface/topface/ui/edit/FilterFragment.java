package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.ui.CitySearchActivity;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.FormInfo;

import java.util.HashMap;

public class FilterFragment extends AbstractEditFragment implements OnClickListener {

    public static Profile mTargetUser = new User();
    public static final String INTENT_DATING_FILTER = "Topface_Dating_Filter";

    private FormInfo mFormInfo;
    private DatingFilter mInitFilter;
    private DatingFilter mFilter;
    private ViewGroup mCityFrame;
    private ViewGroup mAgeFrame;

    private ImageView mCheckGirl;
    private ImageView mCheckBoy;
    private ViewGroup mLoGirls;
    private ViewGroup mLoBoys;
    private ViewGroup mXStatusFrame;
    private ViewGroup mMarriageFrame;
    private ViewGroup mCharacterFrame;
    private ViewGroup mAlcoholFrame;
    private ViewGroup mShowOffFrame;
    private HashMap<Integer, TextView> hashTextViewByTitleId = new HashMap<Integer, TextView>();

    private EditSwitcher mSwitchOnline;
    private EditSwitcher mSwitchBeautifull;

    private boolean mExtraSavingPerformed = false;

    public static final int webAbsoluteMaxAge = 99;
    private boolean mInitFilterOnline;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTargetUser.sex = CacheProfile.dating.sex;
        mFormInfo = new FormInfo(getActivity().getApplicationContext(), mTargetUser);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_filter, container, false);

        // Navigation bar
        ActionBar actionBar = getActionBar(root);
        actionBar.setTitleText(getString(R.string.filter_filter));

        actionBar.showBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        // Preferences
        initFilter();
        initViews(root);

        return root;
    }

    private void initFilter() {
        try {
            mFilter = CacheProfile.dating.clone();
            mInitFilter = mFilter.clone();
            mInitFilterOnline = DatingFilter.getOnlineField();
        } catch (CloneNotSupportedException e) {
            Debug.error(e);
        }
    }

    private void saveFilter() {
        try {
            mInitFilter = mFilter.clone();
            mInitFilterOnline = DatingFilter.getOnlineField();
        } catch (CloneNotSupportedException e) {
            Debug.error(e);
        }
    }

    private void initViews(ViewGroup root) {
        // Girl
        mLoGirls = (ViewGroup) root.findViewById(R.id.loGirl);
        setBackground(R.drawable.edit_big_btn_top_selector, mLoGirls);
        setText(R.string.general_girls, mLoGirls);
        mCheckGirl = (ImageView) mLoGirls.findViewById(R.id.ivCheck);
        if (mFilter.sex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
        }
        mLoGirls.setOnClickListener(this);

        // Boy
        mLoBoys = (ViewGroup) root.findViewById(R.id.loBoy);
        setBackground(R.drawable.edit_big_btn_bottom_selector, mLoBoys);
        setText(R.string.general_boys, mLoBoys);
        mCheckBoy = (ImageView) mLoBoys.findViewById(R.id.ivCheck);
        if (mFilter.sex == Static.BOY) {
            mCheckBoy.setVisibility(View.VISIBLE);
        }
        mLoBoys.setOnClickListener(this);

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
        ViewGroup loSwitchOnline = (ViewGroup) root.findViewById(R.id.loOnline);
        setBackground(R.drawable.edit_big_btn_top_selector, loSwitchOnline);
        setText(R.string.filter_online, loSwitchOnline);
        mSwitchOnline = new EditSwitcher(loSwitchOnline);
        mSwitchOnline.setChecked(DatingFilter.getOnlineField());
        loSwitchOnline.setOnClickListener(this);

        // Beautiful
        ViewGroup loSwitchBeautifull = (ViewGroup) root.findViewById(R.id.loBeautiful);
        setBackground(R.drawable.edit_big_btn_bottom_selector, loSwitchBeautifull);
        setText(R.string.filter_only_beautiful, loSwitchBeautifull);
        mSwitchBeautifull = new EditSwitcher(loSwitchBeautifull);
        mSwitchBeautifull.setChecked(mFilter.beautiful);
        loSwitchBeautifull.setOnClickListener(this);

        // Extra Header
        ViewGroup frame = (ViewGroup) root.findViewById(R.id.loExtraHeader);
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(R.string.filter_extra_parameters);

        // Dating Status
        mXStatusFrame = (ViewGroup) root.findViewById(R.id.loDatingStatus);
        setBackground(R.drawable.edit_big_btn_top_selector, mXStatusFrame);
        setText(R.array.form_main_status,
                mFormInfo.getEntry(R.array.form_main_status, mFilter.xstatus), mXStatusFrame);
        mXStatusFrame.setTag(R.array.form_main_status);
        mXStatusFrame.setOnClickListener(this);

        // Marriage
        mMarriageFrame = (ViewGroup) root.findViewById(R.id.loMarriage);
        setBackground(R.drawable.edit_big_btn_middle_selector, mMarriageFrame);
        setText(R.array.form_social_marriage,
                mFormInfo.getEntry(R.array.form_social_marriage, mFilter.marriage), mMarriageFrame);
        mMarriageFrame.setTag(R.array.form_social_marriage);
        mMarriageFrame.setOnClickListener(this);

        // Character
        mCharacterFrame = (ViewGroup) root.findViewById(R.id.loCharacter);
        setBackground(R.drawable.edit_big_btn_middle_selector, mCharacterFrame);
        setText(R.array.form_main_character,
                mFormInfo.getEntry(R.array.form_main_character, mFilter.character), mCharacterFrame);
        mCharacterFrame.setTag(R.array.form_main_character);
        mCharacterFrame.setOnClickListener(this);

        // Alcohol
        mAlcoholFrame = (ViewGroup) root.findViewById(R.id.loAlcohol);
        setBackground(R.drawable.edit_big_btn_middle_selector, mAlcoholFrame);
        setText(R.array.form_habits_alcohol,
                mFormInfo.getEntry(R.array.form_habits_alcohol, mFilter.alcohol), mAlcoholFrame);
        mAlcoholFrame.setTag(R.array.form_habits_alcohol);
        mAlcoholFrame.setOnClickListener(this);

        // ShowOff
        mShowOffFrame = (ViewGroup) root.findViewById(R.id.loShowOff);
        setBackground(R.drawable.edit_big_btn_bottom_selector, mShowOffFrame);
        if (mFilter.sex == Static.GIRL) {
            setText(R.array.form_physique_breast,
                    mFormInfo.getEntry(R.array.form_physique_breast, mFilter.breast), mShowOffFrame);
            mShowOffFrame.setTag(R.array.form_physique_breast);
        } else {
            setText(R.array.form_social_finances,
                    mFormInfo.getEntry(R.array.form_social_finances, mFilter.finances), mShowOffFrame);
            mShowOffFrame.setTag(R.array.form_social_finances);
        }
        mShowOffFrame.setOnClickListener(this);

    }

    private void switchSex(int sex) {
        if (sex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
            mCheckBoy.setVisibility(View.INVISIBLE);
            setText(R.array.form_physique_breast,
                    mFormInfo.getEntry(R.array.form_physique_breast, mFilter.breast), mShowOffFrame);
            mShowOffFrame.setTag(R.array.form_physique_breast);
        } else {
            mCheckBoy.setVisibility(View.VISIBLE);
            mCheckGirl.setVisibility(View.INVISIBLE);
            setText(R.array.form_social_finances,
                    mFormInfo.getEntry(R.array.form_social_finances, mFilter.finances), mShowOffFrame);
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
        if (mFilter.city.id == City.ALL_CITIES) {
            return getResources().getString(R.string.filter_cities_all);
        } else {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(getResources().getString(R.string.general_city)).append(" ").append(mFilter.city.name);
            return strBuilder.toString();
        }
    }

    private String buildAgeString() {
        String plus = mFilter.age_end == webAbsoluteMaxAge ? "+" : "";
        int age_end = mFilter.age_end == webAbsoluteMaxAge ? EditAgeFragment.absoluteMax : mFilter.age_end;
        return getString(R.string.filter_age_string, mFilter.age_start, age_end) + plus;
    }

    @Override
    protected boolean hasChanges() {
        return !mInitFilter.equals(mFilter) || mInitFilterOnline != DatingFilter.getOnlineField();
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
            case R.id.loGirl:
                switchSex(Static.GIRL);
                break;
            case R.id.loBoy:
                switchSex(Static.BOY);
                break;
            case R.id.loAge:
                Intent ageEditIntent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
                ageEditIntent.putExtra(EditContainerActivity.INTENT_AGE_START, mFilter.age_start);
                ageEditIntent.putExtra(EditContainerActivity.INTENT_AGE_END, mFilter.age_end);
                ageEditIntent.putExtra(EditContainerActivity.FILTER_SEX, mFilter.sex);
                startActivityForResult(ageEditIntent, EditContainerActivity.INTENT_EDIT_AGE);
                break;
            case R.id.loCity:
                Intent intent = new Intent(getActivity().getApplicationContext(), CitySearchActivity.class);
                startActivityForResult(intent, CitySearchActivity.INTENT_CITY_SEARCH_FROM_FILTER_ACTIVITY);
                break;
            case R.id.loOnline:
                mSwitchOnline.doSwitch();
                DatingFilter.setOnlineField(mSwitchOnline.isChecked());
                break;
            case R.id.loBeautiful:
                mSwitchBeautifull.doSwitch();
                mFilter.beautiful = mSwitchBeautifull.isChecked();
                break;
            case R.id.loDatingStatus:
                startEditFilterFormItem(v, mFilter.xstatus);
                break;
            case R.id.loMarriage:
                startEditFilterFormItem(v, mFilter.marriage);
                break;
            case R.id.loCharacter:
                startEditFilterFormItem(v, mFilter.character);
                break;
            case R.id.loAlcohol:
                startEditFilterFormItem(v, mFilter.alcohol);
                break;
            case R.id.loShowOff:
                startEditFilterFormItem(v, mFilter.getShowOff());
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

                hashTextViewByTitleId.get(titleId).setText(mFormInfo.getEntry(titleId, selectedId));
            } else if (requestCode == CitySearchActivity.INTENT_CITY_SEARCH_FROM_FILTER_ACTIVITY) {
                int city_id = extras.getInt(CitySearchActivity.INTENT_CITY_ID);
                String city_name = extras.getString(CitySearchActivity.INTENT_CITY_NAME);

                mFilter.city = new City(city_id, city_name, city_name);

                setText(buildCityString(), mCityFrame);
            } else if (requestCode == EditContainerActivity.INTENT_EDIT_AGE) {
                int ageStart = extras.getInt(EditContainerActivity.INTENT_AGE_START);
                int ageEnd = extras.getInt(EditContainerActivity.INTENT_AGE_END);
                if (ageEnd != 0 && ageStart != 0) {
                    if (ageEnd == EditAgeFragment.absoluteMax) {
                        ageEnd = webAbsoluteMaxAge;
                    }
                    mFilter.age_end = ageEnd;
                    mFilter.age_start = ageStart;
                    setText(buildAgeString(), mAgeFrame);
                }

            }
            refreshSaveState();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void lockUi() {
//        mBackButton.setEnabled(false);
        mLoGirls.setEnabled(false);
        mLoBoys.setEnabled(false);
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
//        mBackButton.setEnabled(true);
        mLoGirls.setEnabled(true);
        mLoBoys.setEnabled(true);
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
}
