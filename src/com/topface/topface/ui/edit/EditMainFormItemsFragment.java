package com.topface.topface.ui.edit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.*;

import java.util.HashMap;

public class EditMainFormItemsFragment extends AbstractEditFragment implements OnClickListener {

    public static final int MAX_AGE = 99;
    public static final int MIN_AGE = 16;
    public static final String INTENT_SEX_CHANGED = "SEX_CHANGED";
    private ActionBar mActionBar;
    private boolean ageIncorrect = false;
    private boolean nameIncorrect;

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
        ((ImageView) mLoGirl.findViewWithTag("ivEditBackground")).setImageResource(R.drawable.edit_big_btn_top_selector);
        ((TextView)  mLoGirl.findViewWithTag("tvTitle")).setText(R.string.general_girl);
        mCheckGirl = (ImageView) mLoGirl.findViewWithTag("ivCheck");
        if (mSex == Static.GIRL) {
            mCheckGirl.setVisibility(View.VISIBLE);
        }
        mLoGirl.setOnClickListener(this);
        mLoGirl.setVisibility(View.GONE);

        mLoBoy = root.findViewById(R.id.loBoy);
        ((ImageView) mLoBoy.findViewWithTag("ivEditBackground")).setImageResource(R.drawable.edit_big_btn_bottom_selector);
        ((TextView) mLoBoy.findViewWithTag("tvTitle")).setText(R.string.general_boy);
        mCheckBoy = (ImageView) mLoBoy.findViewWithTag("ivCheck");
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
        ((TextView) loName.findViewWithTag("tvTitle")).setText(R.string.edit_name);
        mEdName = (EditText) loName.findViewWithTag("edText");
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
        ((TextView) loStatus.findViewWithTag("tvTitle")).setText(R.string.edit_status);
        mEdStatus = (EditText) loStatus.findViewWithTag("edText");
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
        ((TextView) loAge.findViewWithTag("tvTitle")).setText(R.string.edit_age);
        mEdAge = (EditText) loAge.findViewWithTag("edText");
        int maxLength = 2;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        mEdAge.setFilters(fArray);
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
            if (ageIncorrect) {
                showAlertDialog(getString(R.string.profile_edit_age_ranges));
            } else if(nameIncorrect){
                showAlertDialog(getString(R.string.profile_empty_name));
            } else {
                prepareRequestSend();
                registerRequest(request);
                request.callback(new ApiHandler() {

                    @Override
                    public void success(ApiResponse response) {
                        for (EditType type : hashChangedData.keySet()) {
                            setDataByEditType(type, hashChangedData.get(type));
                        }
                        Intent intent = null;
                        if (CacheProfile.sex != mSex) {
                            CacheProfile.sex = mSex;
                            FormInfo formInfo = new FormInfo(getContext(), CacheProfile.getProfile());
                            formInfo.fillFormItem(CacheProfile.forms);
                            intent = new Intent();
                            intent.putExtra(INTENT_SEX_CHANGED,true);
                        }
                        if (intent != null) getActivity().setResult(Activity.RESULT_OK,intent);
                        else getActivity().setResult(Activity.RESULT_OK);
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
            }
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
                } else {
                    nameIncorrect = true;
                }
                break;
            case AGE:
                if (isAgeValid(Integer.parseInt(data))) {
                    CacheProfile.age = Integer.parseInt(data);
                } else {
                    ageIncorrect = true;
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

    private void showAlertDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.settings_error_message));
        builder.setMessage(msg);
        builder.setNegativeButton(R.string.general_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hashChangedData.clear();
                getActivity().finish();
            }
        });
        builder.setPositiveButton(R.string.general_change, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (ageIncorrect) {
                    if(mEdAge.requestFocus()) {
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                } else if (nameIncorrect) {
                    if(mEdName.requestFocus()) {
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            }
        });
        builder.create().show();
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
                            nameIncorrect = false;
                        } else {
                            nameIncorrect = true;
                        }
                        break;
                    case AGE:
                        try {
                            if (changedValue.equals("")) {
                                changedValue = "0";
                            }
                            if (isAgeValid(Integer.parseInt(changedValue))) {
                                request.age = Integer.parseInt(changedValue);
                                ageIncorrect = false;
                            } else {
                                ageIncorrect = true;
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
