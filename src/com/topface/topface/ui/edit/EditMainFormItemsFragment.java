package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.utils.CacheProfile;

import java.util.HashMap;

public class EditMainFormItemsFragment extends AbstractEditFragment {

    public enum EditType {NAME, AGE, STATUS}

    ;

    private EditType[] mTypes;
    private HashMap<EditType, String> hashChangedData = new HashMap<EditMainFormItemsFragment.EditType, String>();

    private EditText mEdName;
    private EditText mEdAge;
    private EditText mEdStatus;

    public EditMainFormItemsFragment(EditType[] type) {
        mTypes = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_main_form_items, null, false);

        // Navigation bar
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle))
                .setText(R.string.edit_title);
        TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
        subTitle.setVisibility(View.VISIBLE);

        ((Button) getActivity().findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);
        Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setText(R.string.navigation_edit);
        btnBack.setOnClickListener(new OnClickListener() {
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
                saveChanges();
            }
        });

        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

        ViewGroup loName = (ViewGroup) root.findViewById(R.id.loName);
        loName.setVisibility(View.GONE);
        ViewGroup loAge = (ViewGroup) root.findViewById(R.id.loAge);
        loAge.setVisibility(View.GONE);
        ViewGroup loStatus = (ViewGroup) root.findViewById(R.id.loStatus);
        loStatus.setVisibility(View.GONE);

        for (final EditType type : mTypes) {
            String data = getDataByEditType(type);
            switch (type) {
                case NAME:
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
                    subTitle.setText(R.string.edit_name);
                    break;
                case AGE:
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
                    break;
                case STATUS:
                    loStatus.setVisibility(View.VISIBLE);
                    ((TextView) loStatus.findViewById(R.id.tvTitle)).setText(R.string.edit_status);
                    mEdStatus = (EditText) loStatus.findViewById(R.id.edText);
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
                    subTitle.setText(R.string.edit_status);
                    break;
            }

            hashChangedData.put(type, data);
        }
        return root;
    }

    @Override
    protected boolean hasChanges() {
        for (EditType type : hashChangedData.keySet()) {
            if (!getDataByEditType(type).equals(hashChangedData.get(type)))
                return true;
        }
        return false;
    }

    @Override
    protected void saveChanges() {
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
                public void success(ApiResponse response) throws NullPointerException {
                    for (EditType type : hashChangedData.keySet()) {
                        setDataByEditType(type, hashChangedData.get(type));
                    }
                    getActivity().setResult(Activity.RESULT_OK);
                    finishRequestSend();
                }

                @Override
                public void fail(int codeError, ApiResponse response)
                        throws NullPointerException {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    finishRequestSend();
                }
            }).exec();
        }
    }

    private String getDataByEditType(EditType type) {
        switch (type) {
            case NAME:
                return CacheProfile.first_name;
            case AGE:
                return Integer.toString(CacheProfile.age);
            case STATUS:
                return CacheProfile.status;
        }
        return Static.EMPTY;
    }

    private void setDataByEditType(EditType type, String data) {
        switch (type) {
            case NAME:
                CacheProfile.first_name = data;
                break;
            case AGE:
                CacheProfile.age = Integer.parseInt(data);
                break;
            case STATUS:
                CacheProfile.status = data;
                break;
        }
    }

    private SettingsRequest getSettigsRequest() {
        SettingsRequest request = new SettingsRequest(getActivity().getApplicationContext());
        for (EditType type : hashChangedData.keySet()) {
            String changedValue = hashChangedData.get(type);
            if (!changedValue.equals(getDataByEditType(type))) {
                switch (type) {
                    case NAME:
                        request.name = changedValue;
                        break;
                    case AGE:
                        request.age = Integer.parseInt(changedValue);
                        break;
                    case STATUS:
                        request.status = changedValue;
                        break;
                }
            }
        }
        return request;
    }
}
