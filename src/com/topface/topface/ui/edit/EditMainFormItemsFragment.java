package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.FormItem;

import java.util.HashMap;

public class EditMainFormItemsFragment extends AbstractEditFragment implements OnClickListener {

    public static final int MAX_AGE = 99;
    public static final int MIN_AGE = 16;
    private ActionBar mActionBar;

    public enum EditType {NAME, AGE, STATUS}

    public static final int MAX_STATUS_LENGTH = 200;

    private EditType[] mTypes;
    private HashMap<EditType, String> hashChangedData = new HashMap<EditMainFormItemsFragment.EditType, String>();

    private EditText mEdName;
    private EditText mEdAge;
    private EditText mEdStatus;
    private int mSex;
    private View mLoGirl;
    private View mLoBoy;

    private ImageView mCheckGirl;
    private ImageView mCheckBoy;

    public EditMainFormItemsFragment() {
        super();
    }

    public EditMainFormItemsFragment(EditType[] type) {
        this();
        mTypes = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_main_form_items, null, false);

        // Navigation bar
        mActionBar = getActionBar(root);

        mActionBar.setTitleText(getString(R.string.edit_title));
        mActionBar.showBackButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mRightPrsBar = mActionBar.getRightProgressBar();

//        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

        TextView sexTitle = (TextView) root.findViewById(R.id.tvSexTitle);
        sexTitle.setVisibility(View.GONE);

        mSex = CacheProfile.sex;
        mLoGirl = root.findViewById(R.id.loGirl);
        ((ImageView) mLoGirl.findViewById(R.id.ivEditBackground))
                .setImageResource(R.drawable.edit_big_btn_top_selector);
        ((TextView) mLoGirl.findViewById(R.id.tvTitle))
                .setText(R.string.general_girl);
        mCheckGirl = (ImageView) mLoGirl.findViewById(R.id.ivCheck);
        if (mSex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
        }
        mLoGirl.setOnClickListener(this);
        mLoGirl.setVisibility(View.GONE);

        mLoBoy = root.findViewById(R.id.loBoy);
        ((ImageView) mLoBoy.findViewById(R.id.ivEditBackground))
                .setImageResource(R.drawable.edit_big_btn_bottom_selector);
        ((TextView) mLoBoy.findViewById(R.id.tvTitle))
                .setText(R.string.general_boy);
        mCheckBoy = (ImageView) mLoBoy.findViewById(R.id.ivCheck);
        if (mSex == Static.BOY) {
            mCheckBoy.setVisibility(View.VISIBLE);
        }
        mLoBoy.setOnClickListener(this);
        mLoBoy.setVisibility(View.GONE);

        ViewGroup loName = (ViewGroup) root.findViewById(R.id.loName);
        loName.setVisibility(View.GONE);
        ViewGroup loAge = (ViewGroup) root.findViewById(R.id.loAge);
        loAge.setVisibility(View.GONE);
        ViewGroup loStatus = (ViewGroup) root.findViewById(R.id.loStatus);
        loStatus.setVisibility(View.GONE);
        if (mTypes != null) {
            for (final EditType type : mTypes) {
                String data = getDataByEditType(type);
                switch (type) {
                    case NAME:
                        setName(sexTitle, loName, type, data);
                        break;
                    case AGE:
                        setAge(loAge, type, data);
                        break;
                    case STATUS:
                        setStatus(loStatus, type, data);
                        break;
                }

                hashChangedData.put(type, data);
            }
        }
        return root;
    }

    private void setName(TextView sexTitle, ViewGroup loName, final EditType type, String data) {
        sexTitle.setVisibility(View.VISIBLE);
        mLoBoy.setVisibility(View.VISIBLE);
        mLoGirl.setVisibility(View.VISIBLE);
        loName.setVisibility(View.VISIBLE);
        ((TextView) loName.findViewById(R.id.tvTitle)).setText(R.string.edit_name);
        mEdName = (EditText) loName.findViewById(R.id.edText);
        mEdName.setText(data);
        mEdName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mEdName.addTextChangedListener(new TextWatcher() {
            String before = Static.EMPTY;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after = s.toString();
                if (!before.equals(after)) {
                    hashChangedData.put(type, after);
                    refreshSaveState();
                }
            }
        });
    }

    private void setStatus(ViewGroup loStatus, final EditType type, String data) {
        loStatus.setVisibility(View.VISIBLE);
        ((TextView) loStatus.findViewById(R.id.tvTitle)).setText(R.string.edit_status);
        mEdStatus = (EditText) loStatus.findViewById(R.id.edText);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(MAX_STATUS_LENGTH);
        mEdStatus.setFilters(filters);
        mEdStatus.setText(data);
        mEdStatus.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mEdStatus.addTextChangedListener(new TextWatcher() {
            String before = Static.EMPTY;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after = s.toString();
                if (!before.equals(after)) {
                    hashChangedData.put(type, after);
                    refreshSaveState();
                }
            }
        });
        mActionBar.setSubTitleText(getString(R.string.edit_status));
    }

    private void setAge(ViewGroup loAge, final EditType type, String data) {
        loAge.setVisibility(View.VISIBLE);
        ((TextView) loAge.findViewById(R.id.tvTitle)).setText(R.string.edit_age);
        mEdAge = (EditText) loAge.findViewById(R.id.edText);
        mEdAge.setText(data);
        mEdAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEdAge.addTextChangedListener(new TextWatcher() {
            String before = Static.EMPTY;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after = s.toString();
                if (!before.equals(after)) {
                    hashChangedData.put(type, after);
                    refreshSaveState();
                }
            }
        });
    }

    @Override
    protected boolean hasChanges() {
        for (EditType type : hashChangedData.keySet()) {
            if (!getDataByEditType(type).equals(hashChangedData.get(type)))
                return true;
        }
        return mSex != CacheProfile.sex;
    }

    @Override
    protected void saveChanges(final Handler handler) {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mEdName != null) imm.hideSoftInputFromWindow(mEdName.getWindowToken(), 0);
        if (mEdAge != null) imm.hideSoftInputFromWindow(mEdAge.getWindowToken(), 0);
        if (mEdStatus != null) imm.hideSoftInputFromWindow(mEdStatus.getWindowToken(), 0);

        if (hasChanges()) {
            SettingsRequest request = getSettigsRequest();

            prepareRequestSend();
            registerRequest(request);
            request.callback(new ApiHandler() {

                @Override
                public void success(ApiResponse response) {
                    for (EditType type : hashChangedData.keySet()) {
                        setDataByEditType(type, hashChangedData.get(type));
                    }
                    CacheProfile.sex = mSex;
                    getActivity().setResult(Activity.RESULT_OK);
                    finishRequestSend();
                    if (handler == null) getActivity().finish();
                    else handler.sendEmptyMessage(0);
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    finishRequestSend();
                    if (handler != null) handler.sendEmptyMessage(0);
                }
            }).exec();
        } else {
            if (handler == null) getActivity().finish();
            else handler.sendEmptyMessage(0);
        }
    }

    private String getDataByEditType(EditType type) {
        switch (type) {
            case NAME:
                return CacheProfile.first_name;
            case AGE:
                return Integer.toString(CacheProfile.age);
            case STATUS:
                return CacheProfile.getStatus();
        }
        return Static.EMPTY;
    }

    private void setDataByEditType(EditType type, String data) {
        switch (type) {
            case NAME:
                if (isNameValid(data)) {
                    CacheProfile.first_name = data;
                }
                break;
            case AGE:
                if (isAgeValid(Integer.parseInt(data))) {
                    CacheProfile.age = Integer.parseInt(data);
                } else {
                    Toast.makeText(getActivity(), R.string.profile_edit_age_ranges, 1500).show();
                }
                break;
            case STATUS:
                CacheProfile.setStatus(data);
                for (FormItem item : CacheProfile.forms) {
                    if (item.type == FormItem.STATUS) {
                        item.value = CacheProfile.getStatus();
                        break;
                    }
                }
                break;
        }
    }

    private SettingsRequest getSettigsRequest() {
        SettingsRequest request = new SettingsRequest(getActivity());
        for (EditType type : hashChangedData.keySet()) {
            String changedValue = hashChangedData.get(type);
            if (!changedValue.equals(getDataByEditType(type))) {
                switch (type) {
                    case NAME:
                        if (isNameValid(changedValue)) {
                            request.name = changedValue;
                        } else {
                            Toast.makeText(getActivity(), R.string.profile_empty_name, 1500).show();
                        }
                        break;
                    case AGE:
                        try {
                            if (isAgeValid(Integer.parseInt(changedValue))) {
                                request.age = Integer.parseInt(changedValue);
                            } else {
                                Toast.makeText(getActivity(), R.string.profile_edit_age_ranges, 1500).show();
                            }
                        } catch (Exception e) {
                            Debug.error(e);
                        }
                        break;
                    case STATUS:
                        request.status = changedValue;
                        break;
                }
            }
        }
        request.sex = mSex;
        return request;
    }

    private boolean isAgeValid(int age) {
        return age <= MAX_AGE && age >= MIN_AGE;
    }

    private boolean isNameValid(String name) {
        return !name.equals("");
    }

    @Override
    protected void lockUi() {
        if (mEdName != null)
            mEdName.setEnabled(false);
        if (mEdAge != null)
            mEdAge.setEnabled(false);
        if (mEdStatus != null)
            mEdStatus.setEnabled(false);
        if (mLoBoy != null)
            mLoBoy.setEnabled(false);
        if (mLoGirl != null)
            mLoGirl.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
        if (mEdName != null)
            mEdName.setEnabled(true);
        if (mEdAge != null)
            mEdAge.setEnabled(true);
        if (mEdStatus != null)
            mEdStatus.setEnabled(true);
        if (mLoBoy != null)
            mLoBoy.setEnabled(true);
        if (mLoGirl != null)
            mLoGirl.setEnabled(true);
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
            default:
                break;
        }
    }

    private void switchSex(int sex) {
        if (sex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
            mCheckBoy.setVisibility(View.INVISIBLE);
        } else {
            mCheckBoy.setVisibility(View.VISIBLE);
            mCheckGirl.setVisibility(View.INVISIBLE);
        }
        mSex = sex;
        refreshSaveState();
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
