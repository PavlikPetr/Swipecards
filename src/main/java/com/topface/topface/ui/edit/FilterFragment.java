package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.ui.CitySearchActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;

import org.json.JSONException;
import org.json.JSONObject;

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
    private ViewGroup mFinanceFrame;
    private ViewGroup mBreastFrame;
    private SparseArrayCompat<TextView> hashTextViewByTitleId = new SparseArrayCompat<>();

    private EditSwitcher mSwitchOnlyOnline;
    private EditSwitcher mSwitchBeautifull;

    private boolean mExtraSavingPerformed = false;

    private boolean mInitFilterOnline;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mTargetUser.sex = CacheProfile.dating != null ? CacheProfile.dating.sex : Static.BOY;
        mFormInfo = new FormInfo(getActivity().getApplicationContext(), mTargetUser.sex, mTargetUser.getType());

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_filter, container, false);

        // Preferences
        initFilter();
        initViews(root);

        return root;
    }

    private void initFilter() {
        try {
            mFilter = CacheProfile.dating.clone();
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
        // Girl
        mLoGirls = (ViewGroup) root.findViewById(R.id.loGirl);
        setBackground(R.drawable.edit_big_btn_top_selector, mLoGirls);
        setText(R.string.general_girls, mLoGirls);
        mCheckGirl = (ImageView) mLoGirls.findViewWithTag("ivCheck");
        if (mFilter.sex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
        }
        mLoGirls.setOnClickListener(this);

        // Boy
        mLoBoys = (ViewGroup) root.findViewById(R.id.loBoy);
        setBackground(R.drawable.edit_big_btn_bottom_selector, mLoBoys);
        setText(R.string.general_boys, mLoBoys);
        mCheckBoy = (ImageView) mLoBoys.findViewWithTag("ivCheck");
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
        mSwitchOnlyOnline = new EditSwitcher(loSwitchOnline);
        mSwitchOnlyOnline.setChecked(DatingFilter.getOnlyOnlineField());
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
        ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.filter_extra_parameters);

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

        // Finance
        mFinanceFrame = (ViewGroup) root.findViewById(R.id.loFinance);
        setBackground(R.drawable.edit_big_btn_middle_selector, mFinanceFrame);
        setText(R.array.form_social_finances,
                mFormInfo.getEntry(R.array.form_social_finances, mFilter.finances), mFinanceFrame);
        mFinanceFrame.setTag(R.array.form_social_finances);
        mFinanceFrame.setOnClickListener(this);

        // ShowOff
        mBreastFrame = (ViewGroup) root.findViewById(R.id.loShowOff);
        setBackground(R.drawable.edit_big_btn_bottom_selector, mBreastFrame);
        setText(R.array.form_physique_breast,
                mFormInfo.getEntry(R.array.form_physique_breast, mFilter.breast), mBreastFrame);
        mBreastFrame.setTag(R.array.form_physique_breast);
        mBreastFrame.setOnClickListener(this);

        switchSex(mFilter.sex);
    }

    private void switchSex(int sex) {
        if (sex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
            mCheckBoy.setVisibility(View.INVISIBLE);
            mBreastFrame.setVisibility(View.VISIBLE);
            setBackground(R.drawable.edit_big_btn_middle_selector, mFinanceFrame);
        } else {
            mCheckBoy.setVisibility(View.VISIBLE);
            mCheckGirl.setVisibility(View.INVISIBLE);
            mBreastFrame.setVisibility(View.GONE);
            setBackground(R.drawable.edit_big_btn_bottom_selector, mFinanceFrame);
        }

        mTargetUser.sex = sex;
        mFilter.sex = sex;

        refreshFilterExtraCellsText();
    }

    private void refreshFilterExtraCellsText() {
        for (int i = 0; i < hashTextViewByTitleId.size(); i++) {
            int titleId = hashTextViewByTitleId.keyAt(i);
            switch (titleId) {
                case R.array.form_main_status:
                    hashTextViewByTitleId.get(titleId).setText(mFormInfo.getEntry(titleId, mFilter.xstatus));
                    break;
                case R.array.form_social_marriage:
                    hashTextViewByTitleId.get(titleId).setText(mFormInfo.getEntry(titleId, mFilter.marriage));
                    break;
                case R.array.form_main_character:
                    hashTextViewByTitleId.get(titleId).setText(mFormInfo.getEntry(titleId, mFilter.character));
                    break;
                case R.array.form_habits_alcohol:
                    hashTextViewByTitleId.get(titleId).setText(mFormInfo.getEntry(titleId, mFilter.alcohol));
                    break;
                case R.array.form_social_finances:
                    hashTextViewByTitleId.get(titleId).setText(mFormInfo.getEntry(titleId, mFilter.finances));
                    break;
                case R.array.form_physique_breast:
                    hashTextViewByTitleId.get(titleId).setText(mFormInfo.getEntry(titleId, mFilter.breast));
                    break;
            }
        }
    }

    private void setBackground(int resId, ViewGroup frame) {
        ImageView background = (ImageView) frame.findViewWithTag("ivEditBackground");
        background.setImageResource(resId);
    }

    private void setText(int titleId, String text, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvTitle")).setText(mFormInfo.getFormTitle(titleId));
        TextView textView = (TextView) frame.findViewWithTag("tvText");
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);
        hashTextViewByTitleId.put(titleId, textView);
    }

    private void setText(String title, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvTitle")).setText(title);
    }

    private void setText(int titleResId, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvTitle")).setText(titleResId);
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
            return getResources().getString(R.string.general_city) + " " + mFilter.city.name;
        }
    }

    private String buildAgeString() {
        String plus = mFilter.ageEnd == DatingFilter.webAbsoluteMaxAge ? "+" : "";
        int age_end = mFilter.ageEnd == DatingFilter.webAbsoluteMaxAge ? EditAgeFragment.absoluteMax : mFilter.ageEnd;
        return getString(R.string.filter_age_string, mFilter.ageStart, age_end) + plus;
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
            case R.id.loGirl:
                switchSex(Static.GIRL);
                break;
            case R.id.loBoy:
                switchSex(Static.BOY);
                break;
            case R.id.loAge:
                Intent ageEditIntent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
                ageEditIntent.putExtra(EditContainerActivity.INTENT_AGE_START, mFilter.ageStart);
                ageEditIntent.putExtra(EditContainerActivity.INTENT_AGE_END, mFilter.ageEnd);
                ageEditIntent.putExtra(EditContainerActivity.FILTER_SEX, mFilter.sex);
                startActivityForResult(ageEditIntent, EditContainerActivity.INTENT_EDIT_AGE);
                break;
            case R.id.loCity:
                Intent intent = new Intent(getActivity().getApplicationContext(), CitySearchActivity.class);
                try {
                    intent.putExtra(CitySearchActivity.INTENT_CITY, mFilter.city.toJson().toString());
                } catch (JSONException e) {
                    Debug.error(e);
                }
                startActivityForResult(intent, CitySearchActivity.INTENT_CITY_SEARCH_FROM_FILTER_ACTIVITY);
                break;
            case R.id.loOnline:
                mSwitchOnlyOnline.doSwitch();
                DatingFilter.setOnlyOnlineField(mSwitchOnlyOnline.isChecked());
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
            case R.id.loFinance:
                startEditFilterFormItem(v, mFilter.finances);
                break;
            case R.id.loShowOff:
                startEditFilterFormItem(v, mFilter.breast);
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
                setText(buildCityString(), mCityFrame);
            } else if (requestCode == EditContainerActivity.INTENT_EDIT_AGE) {
                int ageStart = extras.getInt(EditContainerActivity.INTENT_AGE_START);
                int ageEnd = extras.getInt(EditContainerActivity.INTENT_AGE_END);
                if (ageEnd != 0 && ageStart != 0) {
                    if (ageEnd == EditAgeFragment.absoluteMax) {
                        ageEnd = DatingFilter.webAbsoluteMaxAge;
                    }
                }
                mFilter.ageEnd = ageEnd;
                mFilter.ageStart = ageStart;
                setText(buildAgeString(), mAgeFrame);
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
        mSwitchOnlyOnline.setEnabled(false);
        mSwitchBeautifull.setEnabled(false);
        mXStatusFrame.setEnabled(false);
        mMarriageFrame.setEnabled(false);
        mCharacterFrame.setEnabled(false);
        mAlcoholFrame.setEnabled(false);
        mBreastFrame.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
//        mBackButton.setEnabled(true);
        mLoGirls.setEnabled(true);
        mLoBoys.setEnabled(true);
        mAgeFrame.setEnabled(true);
        mCityFrame.setEnabled(true);
        mSwitchOnlyOnline.setEnabled(true);
        mSwitchBeautifull.setEnabled(true);
        mXStatusFrame.setEnabled(true);
        mMarriageFrame.setEnabled(true);
        mCharacterFrame.setEnabled(true);
        mAlcoholFrame.setEnabled(true);
        mBreastFrame.setEnabled(true);
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
}
